package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

import entity.Ship.ShipType;
import engine.DrawManager.SpriteType;

/**
 * Manages files used in the application.
 * Refactored to fix PMD issues: God class, Complexity, Try-with-resources.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public final class FileManager {
  private static FileManager instance;
  private static final Logger LOGGER = Core.getLogger();
  private static String testDirectory = null;

  // PMD: Avoid duplicate literals
  private static final String FILENAME_ACHIEVEMENT = "achievement.csv";
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
        LOGGER.fine("Sprite " + sprite.getKey() + " loaded.");
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
    final String[] pair = line.split(",");
    return new Score(pair[0], Integer.parseInt(pair[1]), pair[2]);
  }

  public List<Score> loadHighScores(final String mode) throws IOException {
    final List<Score> highScores = new ArrayList<>();
    final File file = new File(getSaveDirectory() + mode + "scores.csv");

    if (!file.exists()) {
      LOGGER.info("Loading default high scores.");
      return loadDefaultHighScores();
    }

    try (FileInputStream fis = new FileInputStream(file);
         InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
         BufferedReader bufferedReader = new BufferedReader(isr)) {

      LOGGER.info("Loading user high scores.");
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

      LOGGER.info("Saving user high scores.");
      bufferedWriter.write("player,score");
      bufferedWriter.newLine();

      for (final Score score : highScores) {
        bufferedWriter.write(score.getName() + "," + score.getScore());
        bufferedWriter.newLine();
      }
    }
  }

  public void unlockAchievement(final String userName, final List<Boolean> unlockedAchievement, final String mode) {
    // Extract only numeric part (e.g., "1P" -> "1")
    final String numericMode = mode.replaceAll("[^0-9]", "");
    final List<String[]> records = new ArrayList<>();
    final File achFile = new File(getSaveDirectory() + FILENAME_ACHIEVEMENT);
    boolean found = false;

    // 1. Read existing records
    if (achFile.exists()) {
      try (BufferedReader bReader = new BufferedReader(
              new InputStreamReader(new FileInputStream(achFile), StandardCharsets.UTF_8))) {

        String line;
        while ((line = bReader.readLine()) != null) {
          final String[] row = line.split(",");
          if (row.length < 3) {
            records.add(row);
            continue;
          }

          // Match both username and mode
          if (row[1].trim().equals(userName) && row[0].trim().equals(numericMode)) {
            found = true;
            updateAchievementRow(row, unlockedAchievement);
          }
          records.add(row);
        }
      } catch (IOException e) {
        LOGGER.warning("Error reading achievements: " + e.getMessage());
      }
    }

    // 2. Add new record if not found
    if (!found) {
      LOGGER.info("User not found, creating new record.");
      records.add(createNewAchievementRow(numericMode, userName, unlockedAchievement));
    }

    // 3. Save back to file
    writeCSV(achFile, records);
  }

  private void updateAchievementRow(final String[] row, final List<Boolean> unlocked) {
    LOGGER.info("Achievement has been updated.");
    for (int i = 2; i < row.length && (i - 2) < unlocked.size(); i++) {
      if ("0".equals(row[i]) && unlocked.get(i - 2)) {
        row[i] = "1";
      }
    }
  }

  private String[] createNewAchievementRow(final String mode, final String name, final List<Boolean> unlocked) {
    final String[] newRecord = new String[unlocked.size() + 2];
    newRecord[0] = mode;
    newRecord[1] = name;
    for (int i = 0; i < unlocked.size(); i++) {
      newRecord[i + 2] = unlocked.get(i) ? "1" : "0";
    }
    return newRecord;
  }

  private void writeCSV(final File file, final List<String[]> rows) {
    try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
      for (final String[] row : rows) {
        writer.write(String.join(",", row));
        writer.newLine();
      }
    } catch (IOException e) {
      LOGGER.warning("Error writing CSV: " + e.getMessage());
    }
  }

  public List<String> getAchievementCompleter(final Achievement achievement) {
    final List<String> completer = new ArrayList<>();
    final File achFile = new File(getSaveDirectory() + FILENAME_ACHIEVEMENT);

    if (!achFile.exists()) {
      return completer;
    }

    try (BufferedReader bReader = new BufferedReader(
            new InputStreamReader(new FileInputStream(achFile), StandardCharsets.UTF_8))) {

      final String headerLine = bReader.readLine();
      if (headerLine == null) {
        return completer;
      }

      final String[] header = headerLine.split(",");
      int idx = -1;

      for (int i = 2; i < header.length; i++) {
        if (header[i].trim().equalsIgnoreCase(achievement.getName().trim())) {
          idx = i;
          break;
        }
      }

      if (idx == -1) {
        LOGGER.warning("Achievement column not found: " + achievement.getName());
        return completer;
      }

      String line;
      while ((line = bReader.readLine()) != null) {
        final String[] tokens = line.split(",");
        if (tokens.length > idx && "1".equals(tokens[idx].trim())) {
          completer.add(tokens[0].trim() + ":" + tokens[1].trim());
        }
      }

    } catch (IOException e) {
      LOGGER.warning("Error reading achievement file.");
    }

    return completer;
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
            LOGGER.warning("Unknown ship type in ships.csv");
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
      LOGGER.warning("Error reading coins file: " + e.getMessage());
    }
    return 0;
  }

  public void saveCoins(final int coins) {
    final File coinsFile = new File(getSaveDirectory() + FILENAME_COINS);

    // Ensure parent directory exists
    final File parent = coinsFile.getParentFile();
    if (parent != null && !parent.exists() && !parent.mkdirs()) {
      LOGGER.warning("Failed to create directory: " + parent.getAbsolutePath());
    }

    try (BufferedWriter writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(coinsFile), StandardCharsets.UTF_8))) {
      writer.write(Integer.toString(coins));
      writer.newLine();
      LOGGER.info("User coins saved.");
    } catch (IOException e) {
      LOGGER.warning("Failed to save coins: " + e.getMessage());
    }
  }

  private boolean isRunningFromJarOrExe() {
    final String protocol = FileManager.class.getResource("").getProtocol();
    return !"file".equals(protocol);
  }

  private String getSaveDirectory() {
    if (testDirectory != null) {
      return testDirectory;
    }

    final String dirPath = isRunningFromJarOrExe()
            ? System.getProperty("user.home") + File.separator + "TEAM3" + File.separator
            : System.getProperty("user.dir") + File.separator + DIR_RES + File.separator;

    final File d = new File(dirPath);
    if (!d.exists() && !d.mkdirs()) {
      LOGGER.warning("Failed to create directory: " + d.getAbsolutePath());
    }

    return dirPath;
  }

  public static void setTestDirectory(final String path) {
    testDirectory = path;
  }
}