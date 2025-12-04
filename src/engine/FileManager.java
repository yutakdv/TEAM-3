package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.Ship.ShipType;
import engine.DrawManager.SpriteType;

/**
 * Manages files used in the application.
 * Refactored: Achievement logic moved to AchievementManager.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public final class FileManager { //NOPMD
  private static FileManager instance;
  private static final Logger LOGGER = Core.getLogger();

  private static String testDirectory;

  private static final String FILENAME_COINS = "coins.csv";
  private static final String FILENAME_SHIPS = "ships.csv";
  private static final String DIR_RES = "res";

  private FileManager() {
  }

  static FileManager getInstance() {
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

      for (final Map.Entry<SpriteType, boolean[][]> sprite : spriteMap.entrySet()) {
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
    c = is.read();
    while (c != '0' && c != '1' && c != -1) {
      c = is.read();
    }
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

      String input = reader.readLine();
      while (input != null) {
        highScores.add(parseScoreLine(input));
        input = reader.readLine();
      }
    }
    return highScores;
  }

  private Score parseScoreLine(final String line) {
    final String[] data = line.split(",");

    if (data.length < 2) {
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
    final List<Score> highScores = new ArrayList<>();//NOPMD
    final File file = new File(getSaveDirectory() + mode + "scores.csv");

    if (!file.exists()) {
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("Loading default high scores.");
      }
      return loadDefaultHighScores();
    }

    try (BufferedReader bufferedReader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {

      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("Loading user high scores.");
      }
      bufferedReader.readLine(); // skip header

      String input = bufferedReader.readLine();
      while (input != null) {
        highScores.add(parseScoreLine(input));
        input = bufferedReader.readLine();
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

    try (BufferedWriter bufferedWriter = Files.newBufferedWriter(scoresFile.toPath(), StandardCharsets.UTF_8)) {

      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("Saving user high scores.");
      }
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

    try (BufferedWriter writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {

      writer.write("shipType,unlocked");
      writer.newLine();

      for (final ShipType type : ShipType.values()) {
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

    try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {

      reader.readLine();

      String line = reader.readLine();
      while (line != null) {
        final String[] tokens = line.split(",");
        if (tokens.length >= 2) {
          try {
            final ShipType type = ShipType.valueOf(tokens[0].trim());
            final boolean unlocked = Boolean.parseBoolean(tokens[1].trim());
            unlockMap.put(type, unlocked);
          } catch (IllegalArgumentException e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
              LOGGER.warning("Unknown ship type in ships.csv");
            }
          }
        }
        line = reader.readLine();
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

    try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
      final String line = reader.readLine();
      if (line != null) {
        return Integer.parseInt(line.trim());
      }
    } catch (IOException | NumberFormatException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.warning("Error reading coins file: " + e.getMessage());
      }
    }
    return 0;
  }

  public void saveCoins(final int coins) {
    final File coinsFile = new File(getSaveDirectory() + FILENAME_COINS);

    final File parent = coinsFile.getParentFile();
    if (parent != null && !parent.exists() && !parent.mkdirs()) {
      if (LOGGER.isLoggable(Level.WARNING)) {//NOPMD
        LOGGER.warning("Failed to create directory: " + parent.getAbsolutePath());
      }
    }

    try (BufferedWriter writer = Files.newBufferedWriter(coinsFile.toPath(), StandardCharsets.UTF_8)) {
      writer.write(Integer.toString(coins));
      writer.newLine();

      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("User coins saved.");
      }
    } catch (IOException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.warning("Failed to save coins: " + e.getMessage());
      }
    }
  }

  private boolean isRunningFromJarOrExe() {
    final String protocol = FileManager.class.getResource("").getProtocol();
    return !"file".equals(protocol);
  }

  String getSaveDirectory() {
    if (testDirectory != null) {
      return testDirectory;
    }

    final String dirPath = isRunningFromJarOrExe()
            ? System.getProperty("user.home") + File.separator + "TEAM3" + File.separator
            : System.getProperty("user.dir") + File.separator + DIR_RES + File.separator;

    final File d = new File(dirPath);
    if (!d.exists() && !d.mkdirs()) {
      if (LOGGER.isLoggable(Level.WARNING)) {//NOPMD
        LOGGER.warning("Failed to create directory: " + d.getAbsolutePath());
      }
    }

    return dirPath;
  }

  public static void setTestDirectory(final String path) {
    testDirectory = path;
  }
}