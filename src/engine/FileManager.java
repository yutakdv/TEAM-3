package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import entity.Ship.ShipType;
import engine.DrawManager.SpriteType;

/**
 * Manages files used in the application.
 * Refactored: Achievement logic moved to AchievementManager.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public final class FileManager {
  private static FileManager instance;
  private static final Logger LOGGER = Core.getLogger();
  private static String testDirectory = null;

  private static final String FILENAME_COINS = "coins.csv";
  private static final String FILENAME_SHIPS = "ships.csv";
  private static final String DIR_RES = "res";

  private FileManager() {
    // Singleton
  }

  protected static FileManager getInstance() {
    if (instance == null) {
      instance = new FileManager();
    }
    return instance;
  }

  public void loadSprite(final Map<SpriteType, boolean[][]> spriteMap) throws IOException {
    try (InputStream inputStream = DrawManager.class.getClassLoader().getResourceAsStream("graphics")) {
      if (inputStream == null) {
        throw new IOException("Graphics resource not found.");
      }

      for (Map.Entry<SpriteType, boolean[][]> sprite : spriteMap.entrySet()) {
        final boolean[][] image = sprite.getValue();
        for (int i = 0; i < image.length; i++) {
          for (int j = 0; j < image[i].length; j++) {
            image[i][j] = readBooleanChar(inputStream);
          }
        }
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("Sprite " + sprite.getKey() + " loaded.");
        }
      }
    }
  }

  private boolean readBooleanChar(final InputStream is) throws IOException {
    int c;
    do {
      c = is.read();
    } while (c != '0' && c != '1' && c != -1);
    return c == '1';
  }

  public Font loadFont(final float size) throws IOException, FontFormatException {
    try (InputStream inputStream = FileManager.class.getClassLoader().getResourceAsStream("font.ttf")) {
      if (inputStream == null) {
        throw new IOException("Font resource not found.");
      }
      return Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(size);
    }
  }

  private List<Score> loadDefaultHighScores() throws IOException {
    final List<Score> highScores = new ArrayList<>();
    try (InputStream inputStream = FileManager.class.getClassLoader().getResourceAsStream("1Pscores.csv");
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

      reader.readLine(); // skip header
      String input;
      while ((input = reader.readLine()) != null) {
        highScores.add(parseScoreLine(input));
      }
    }
    return highScores;
  }

  private Score parseScoreLine(final String line) {
    final String[] data = line.split(",");

    if (data.length < 2) {
      System.err.println("잘못된 데이터 형식 무시함: " + line);
      return new Score("Error", 0, "N/A");
    }
    String date = "N/A";
    if (data.length >= 3) {
      date = data[2];
    }

    try {
      return new Score(data[0], Integer.parseInt(data[1].trim()), date);
    } catch (NumberFormatException e) {
      return new Score("Error", 0, "N/A");
    }
  }

  public List<Score> loadHighScores(final String mode) throws IOException {
    final List<Score> highScores = new ArrayList<>();
    final File file = new File(getSaveDirectory() + mode + "scores.csv");

    if (!file.exists()) {
      if (LOGGER.isLoggable(Level.INFO)) LOGGER.info("Loading default high scores.");
      return loadDefaultHighScores();
    }

    try (FileInputStream fis = new FileInputStream(file);
         InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
         BufferedReader bufferedReader = new BufferedReader(isr)) {

      if (LOGGER.isLoggable(Level.INFO)) LOGGER.info("Loading user high scores.");
      bufferedReader.readLine(); // skip header

      String input;
      while ((input = bufferedReader.readLine()) != null) {
        highScores.add(parseScoreLine(input));
      }
    }

    Collections.sort(highScores);
    return highScores;
  }

  public void saveHighScores(final List<Score> highScores, final String mode) throws IOException {
    final File scoresFile = new File(getSaveDirectory() + mode + "scores.csv");
    if (!scoresFile.exists()) {
      scoresFile.createNewFile();
    }

    try (FileOutputStream fos = new FileOutputStream(scoresFile);
         OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
         BufferedWriter bufferedWriter = new BufferedWriter(osw)) {

      if (LOGGER.isLoggable(Level.INFO)) LOGGER.info("Saving user high scores.");
      bufferedWriter.write("player,score");
      bufferedWriter.newLine();

      for (final Score score : highScores) {
        bufferedWriter.write(score.getName() + "," + score.getScore());
        bufferedWriter.newLine();
      }
    }
  }

  public void saveShipUnlocks(final Map<ShipType, Boolean> unlockMap) throws IOException {
    final File file = new File(getSaveDirectory() + FILENAME_SHIPS);
    try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

      writer.write("shipType,unlocked");
      writer.newLine();

      for (ShipType type : ShipType.values()) {
        final boolean unlocked = unlockMap.getOrDefault(type, false);
        writer.write(type.name() + "," + unlocked);
        writer.newLine();
      }
    }
  }

  public Map<ShipType, Boolean> loadShipUnlocks() throws IOException {
    final Map<ShipType, Boolean> unlockMap = new HashMap<>();
    final File file = new File(getSaveDirectory() + FILENAME_SHIPS);

    if (!file.exists()) {
      unlockMap.put(ShipType.NORMAL, true);
      saveShipUnlocks(unlockMap);
      return unlockMap;
    }

    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

      reader.readLine(); // skip header
      String line;
      while ((line = reader.readLine()) != null) {
        final String[] tokens = line.split(",");
        if (tokens.length >= 2) {
          try {
            final ShipType type = ShipType.valueOf(tokens[0].trim());
            final boolean unlocked = Boolean.parseBoolean(tokens[1].trim());
            unlockMap.put(type, unlocked);
          } catch (IllegalArgumentException e) {
            if (LOGGER.isLoggable(Level.WARNING)) LOGGER.warning("Unknown ship type in ships.csv");
          }
        }
      }
    }
    return unlockMap;
  }

  public int loadCoins() {
    final File file = new File(getSaveDirectory() + FILENAME_COINS);
    if (!file.exists()) {
      saveCoins(0);
      return 0;
    }

    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
      final String line = reader.readLine();
      if (line != null) {
        return Integer.parseInt(line.trim());
      }
    } catch (IOException | NumberFormatException e) {
      if (LOGGER.isLoggable(Level.WARNING)) LOGGER.warning("Error reading coins file: " + e.getMessage());
    }
    return 0;
  }

  public void saveCoins(final int coins) {
    final File coinsFile = new File(getSaveDirectory() + FILENAME_COINS);

    final File parent = coinsFile.getParentFile();
    if (parent != null && !parent.exists() && !parent.mkdirs()) {
      if (LOGGER.isLoggable(Level.WARNING)) LOGGER.warning("Failed to create directory: " + parent.getAbsolutePath());
    }

    try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(coinsFile), StandardCharsets.UTF_8))) {
      writer.write(Integer.toString(coins));
      writer.newLine();
      if (LOGGER.isLoggable(Level.INFO)) LOGGER.info("User coins saved.");
    } catch (IOException e) {
      if (LOGGER.isLoggable(Level.WARNING)) LOGGER.warning("Failed to save coins: " + e.getMessage());
    }
  }

  private boolean isRunningFromJarOrExe() {
    final String protocol = FileManager.class.getResource("").getProtocol();
    return !"file".equals(protocol);
  }

  // [Changed] Visibility changed to package-private to allow access from AchievementManager
  String getSaveDirectory() {
    if (testDirectory != null) {
      return testDirectory;
    }

    final String dirPath = isRunningFromJarOrExe()
            ? System.getProperty("user.home") + File.separator + "TEAM3" + File.separator
            : System.getProperty("user.dir") + File.separator + DIR_RES + File.separator;

    final File d = new File(dirPath);
    if (!d.exists() && !d.mkdirs()) {
      if (LOGGER.isLoggable(Level.WARNING)) LOGGER.warning("Failed to create directory: " + d.getAbsolutePath());
    }

    return dirPath;
  }

  public static void setTestDirectory(final String path) {
    testDirectory = path;
  }
}