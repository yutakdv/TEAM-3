package engine;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import java.util.HashMap;
import entity.Ship.ShipType;

import engine.DrawManager.SpriteType;

/**
 * Manages files used in the application.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public final class FileManager {
  /** Singleton instance of the class. */
  private static FileManager instance;

  /** Application logger. */
  private static Logger logger;

  /** private constructor. */
  private FileManager() {
    logger = Core.getLogger();
  }

  // Test only override directory
  private static String testDirectory = null;

  /**
   * Returns shared instance of FileManager.
   *
   * @return Shared instance of FileManager.
   */
  protected static FileManager getInstance() {
    if (instance == null) instance = new FileManager();
    return instance;
  }

  /**
   * Loads sprites from disk.
   *
   * @param spriteMap Mapping of sprite type and empty boolean matrix that will contain the image.
   * @throws IOException In case of loading problems.
   */
  public void loadSprite(final Map<SpriteType, boolean[][]> spriteMap) throws IOException {
    InputStream inputStream = null;

    try {
      inputStream = DrawManager.class.getClassLoader().getResourceAsStream("graphics");
      char c;

      // Sprite loading.
      for (Map.Entry<SpriteType, boolean[][]> sprite : spriteMap.entrySet()) {
        for (int i = 0; i < sprite.getValue().length; i++)
          for (int j = 0; j < sprite.getValue()[i].length; j++) {
            do c = (char) inputStream.read();
            while (c != '0' && c != '1');

            if (c == '1') sprite.getValue()[i][j] = true;
            else sprite.getValue()[i][j] = false;
          }
        logger.fine("Sprite " + sprite.getKey() + " loaded.");
      }
      if (inputStream != null) inputStream.close();
    } finally {
      if (inputStream != null) inputStream.close();
    }
  }

  /**
   * Loads a font of a given size.
   *
   * @param size Point size of the font.
   * @return New font.
   * @throws IOException In case of loading problems.
   * @throws FontFormatException In case of incorrect font format.
   */
  public Font loadFont(final float size) throws IOException, FontFormatException {
    InputStream inputStream = null;
    Font font;

    try {
      // Font loading.
      inputStream = FileManager.class.getClassLoader().getResourceAsStream("font.ttf");
      font = Font.createFont(Font.TRUETYPE_FONT, inputStream).deriveFont(size);
    } finally {
      if (inputStream != null) inputStream.close();
    }

    return font;
  }

  /**
   * Returns the filepath
   *
   * @param fileName file to get path
   * @return full file path
   * @throws IOException In case of loading problems
   */
  /* yutak - getFilepath can do both local file path and .jar file path*/
  //  private static InputStream getFileStream(String fileName) throws IOException {
  //    InputStream inputStream =
  //        FileManager.class.getClassLoader().getResourceAsStream("res/" + fileName);
  //
  //    if (inputStream != null) {
  //      return inputStream;
  //    }
  //
  //    File localFile =
  //        new File(
  //            System.getProperty("user.dir") + File.separator + "res" + File.separator +
  // fileName);
  //    if (localFile.exists()) {
  //      return new FileInputStream(localFile);
  //    }
  //
  //    throw new FileNotFoundException("Resource not found: res/" + fileName);
  //  }

  /**
   * Returns the application default scores if there is no user high scores file.
   *
   * @return Default high scores.
   * @throws IOException In case of loading problems.
   */
  private List<Score> loadDefaultHighScores() throws IOException {
    List<Score> highScores = new ArrayList<>();
    InputStream inputStream = null;
    BufferedReader reader;

    try {
      inputStream = FileManager.class.getClassLoader().getResourceAsStream("1Pscores.csv");
      reader = new BufferedReader(new InputStreamReader(inputStream));

      // except first line
      reader.readLine();
      String input;
      while ((input = reader.readLine()) != null) {
        String[] pair = input.split(",");
        String name = pair[0], score = pair[1];
        String mode = pair[2];
        Score highScore = new Score(name, Integer.parseInt(score), mode);
        highScores.add(highScore);
      }
    } finally {
      if (inputStream != null) inputStream.close();
    }

    return highScores;
  }

  /**
   * Loads high scores from file, and returns a sorted list of pairs score - value.
   *
   * @param mode get game mode 1P/2P.
   * @return Sorted list of scores - players.
   * @throws IOException In case of loading problems.
   */
  public List<Score> loadHighScores(String mode) throws IOException {
    List<Score> highScores = new ArrayList<>();
    InputStream inputStream = null;
    BufferedReader bufferedReader = null;

    try {
      File file = new File(getSaveDirectory() + mode + "scores.csv");

      bufferedReader =
          new BufferedReader(
              new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
      logger.info("Loading user high scores.");
      // except first line
      bufferedReader.readLine();
      String input;
      while ((input = bufferedReader.readLine()) != null) {
        String[] pair = input.split(",");
        String name = pair[0], score = pair[1];
        Score highScore = new Score(name, Integer.parseInt(score), mode);
        highScores.add(highScore);
      }
    } catch (FileNotFoundException e) {
      // loads default if there's no user scores.
      logger.info("Loading default high scores.");
      highScores = loadDefaultHighScores();
    } finally {
      if (bufferedReader != null) bufferedReader.close();
    }

    Collections.sort(highScores);
    return highScores;
  }

  /**
   * Saves user high scores to disk.
   *
   * @param highScores High scores to save.
   * @param mode get game mode 1P/2P.
   * @throws IOException In case of loading problems.
   */
  public void saveHighScores(final List<Score> highScores, String mode) throws IOException {
    OutputStream outputStream = null;
    BufferedWriter bufferedWriter = null;

    try {
      File scoresFile = new File(getSaveDirectory() + mode + "scores.csv");

      if (!scoresFile.exists()) scoresFile.createNewFile();

      outputStream = new FileOutputStream(scoresFile);
      bufferedWriter =
          new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

      logger.info("Saving user high scores.");
      bufferedWriter.write("player,score");
      bufferedWriter.newLine();

      for (Score score : highScores) {
        bufferedWriter.write(score.getName() + "," + score.getScore());
        bufferedWriter.newLine();
      }

    } finally {
      if (bufferedWriter != null) bufferedWriter.close();
    }
  }

  /**
   * Search Achievement list of user
   *
   * @param userName user's name to search.
   * @throws IOException In case of loading problems.
   */
  public List<Boolean> searchAchievementsByName(String userName) throws IOException {
    List<Boolean> achievementList = new ArrayList<>();

    try {
      File achFile = new File(getSaveDirectory() + "achievement.csv");

      try (BufferedReader bReader =
          new BufferedReader(
              new InputStreamReader(new FileInputStream(achFile), StandardCharsets.UTF_8))) {

        bReader.readLine(); // Skip header
        String line;
        boolean found = false;

        while ((line = bReader.readLine()) != null) {
          String[] playRecord = line.split(",");
          if (playRecord.length < 3)
            continue; // Minimum fields: mode, userName, at least 1 achievement

          String mode = playRecord[0].trim(); // Mode: "1" or "2"
          String name = playRecord[1].trim();

          if (name.equals(userName)) {
            found = true;
            logger.info("Loading user achievements.");
            // Achievements start from index 2
            for (int i = 2; i < playRecord.length; i++) {
              achievementList.add(playRecord[i].equals("1"));
            }
            break;
          }
        }

        if (!found) {
          logger.info("Loading default achievements.");
          for (int i = 0; i < 5; i++) { // Default to 5 achievements, all set to false
            achievementList.add(false);
          }
        }
      }

    } catch (FileNotFoundException e) {
      logger.info("Achievement file not found, loading default achievements.");
      for (int i = 0; i < 5; i++) {
        achievementList.add(false);
      }
    }

    return achievementList;
  }

  /**
   * Unlocks achievements for a specific user.
   *
   * @param userName The name of the user.
   * @param unlockedAchievement A list of booleans representing which achievements have been
   *     unlocked.
   */
  public void unlockAchievement(String userName, List<Boolean> unlockedAchievement, String mode) {
    List<String[]> records = new ArrayList<>();

    // Extract only numeric part from mode string (e.g., "1P" → "1", "2P" → "2")
    String numericMode = mode.replaceAll("[^0-9]", "");

    try {
      File achFile = new File(getSaveDirectory() + "achievement.csv");
      try (BufferedReader bReader =
          new BufferedReader(
              new InputStreamReader(new FileInputStream(achFile), StandardCharsets.UTF_8))) {

        String line;
        boolean found = false;

        while ((line = bReader.readLine()) != null) {
          String[] playRecord = line.split(",");

          // Skip invalid or incomplete lines
          if (playRecord.length < 3) {
            records.add(playRecord);
            continue;
          }

          String currentMode = playRecord[0].trim();
          String name = playRecord[1].trim();

          // ✅ Match both username and mode to consider it the same record
          if (name.equals(userName) && currentMode.equals(numericMode)) {
            found = true;
            Logger.getLogger(getClass().getName()).info("Achievement has been updated.");
            for (int i = 2; i < playRecord.length; i++) {
              if (playRecord[i].equals("0") && unlockedAchievement.get(i - 2)) {
                playRecord[i] = "1";
              }
            }
          }

          records.add(playRecord);
        }

        // If no existing record found, create a new one
        if (!found) {
          Logger.getLogger(getClass().getName()).info("User not found, creating new record.");
          String[] newRecord = new String[unlockedAchievement.size() + 2];
          newRecord[0] = numericMode; // Store numeric mode only
          newRecord[1] = userName;
          for (int i = 0; i < unlockedAchievement.size(); i++) {
            newRecord[i + 2] = unlockedAchievement.get(i) ? "1" : "0";
          }
          records.add(newRecord);
        }
      }

      File achievementFile = new File(getSaveDirectory() + "achievement.csv");

      // Write the updated records back to the CSV file
      try (BufferedWriter bWriter =
          new BufferedWriter(
              new OutputStreamWriter(
                  new FileOutputStream(achievementFile), StandardCharsets.UTF_8))) {
        for (String[] record : records) {
          bWriter.write(String.join(",", record));
          bWriter.newLine();
        }
      }

    } catch (IOException e) {
      Logger.getLogger(getClass().getName()).info("No achievements to save or error occurred.");
    }
  }

  /**
   * Returns a list of users who have completed a specific achievement.
   *
   * @param achievement The achievement to check.
   * @return A list of strings in the format "mode:username" for those who have completed the
   *     achievement.
   *     <p>[2025-10-09] Added in commit: feat: add method to retrieve achievement completer
   */
  public List<String> getAchievementCompleter(Achievement achievement) {
    List<String> completer = new ArrayList<>();
    try {
      File achFile = new File(getSaveDirectory() + "achievement.csv");

      try (BufferedReader bReader =
          new BufferedReader(
              new InputStreamReader(new FileInputStream(achFile), StandardCharsets.UTF_8))) {

        String line;
        String[] header = bReader.readLine().split(",");
        int idx = -1;

        // Find the column index of the given achievement name
        for (int i = 2; i < header.length; i++) { // Achievements start from column index 2
          if (header[i].trim().equalsIgnoreCase(achievement.getName().trim())) {
            idx = i;
            break;
          }
        }

        if (idx == -1) {
          logger.warning("Achievement not found: " + achievement.getName());
          return completer;
        }

        // Parse each line in the file
        while ((line = bReader.readLine()) != null) {
          String[] tokens = line.split(",");
          if (tokens.length <= idx) continue;

          String mode = tokens[0].trim();
          String playerName = tokens[1].trim();
          String value = tokens[idx].trim();

          if (value.equals("1")) {
            completer.add(mode + ":" + playerName);
          }
        }
      }

    } catch (IOException e) {
      logger.warning("Error reading achievement file. Returning default users...");
    }

    return completer;
  }

  public void saveShipUnlocks(Map<ShipType, Boolean> unlockMap) throws IOException {
    File file = new File(getSaveDirectory() + "ships.csv");

    try (BufferedWriter writer =
        new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

      writer.write("shipType,unlocked");
      writer.newLine();

      for (ShipType type : ShipType.values()) {
        boolean unlocked = unlockMap.getOrDefault(type, false);
        writer.write(type.name() + "," + unlocked);
        writer.newLine();
      }
    }
  }

  public Map<ShipType, Boolean> loadShipUnlocks() throws IOException {
    Map<ShipType, Boolean> unlockMap = new HashMap<>();

    File file = new File(getSaveDirectory() + "ships.csv");

    if (!file.exists()) {
      unlockMap.put(ShipType.NORMAL, true);
      unlockMap.put(ShipType.BIG_SHOT, false);
      unlockMap.put(ShipType.DOUBLE_SHOT, false);
      unlockMap.put(ShipType.MOVE_FAST, false);

      saveShipUnlocks(unlockMap);
      return unlockMap;
    }

    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

      String line = reader.readLine();

      while ((line = reader.readLine()) != null) {
        String[] tokens = line.split(",");
        if (tokens.length < 2) continue;

        String shipName = tokens[0].trim();
        String unlockedStr = tokens[1].trim();

        try {
          ShipType type = ShipType.valueOf(shipName);
          boolean unlocked = Boolean.parseBoolean(unlockedStr);
          unlockMap.put(type, unlocked);
        } catch (IllegalArgumentException e) {

          logger.warning("Unknown ship type in ships.csv: " + shipName);
        }
      }
    }
    return unlockMap;
  }

  public int loadCoins() {
    BufferedReader bufferedReader = null;

    try {
      File file = new File(getSaveDirectory() + "coins.csv");

      bufferedReader =
          new BufferedReader(
              new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));

      String line = bufferedReader.readLine();
      if (line != null) {
        return Integer.parseInt(line.trim());
      }
    } catch (FileNotFoundException e) {
      logger.info("Coins file not found, defaulting to 0.");
    } catch (IOException e) {
      logger.warning("Error reading coins file: " + e.getMessage());
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException ignored) {
        }
      }
    }
    saveCoins(0);
    return 0;
  }

  public void saveCoins(int coins) {
    BufferedWriter bufferedWriter = null;

    try {
      File coinsFile = new File(getSaveDirectory() + "coins.csv");

      if (!coinsFile.exists()) {
        boolean created = coinsFile.createNewFile();
        if (!created) {
          logger.warning("Failed to create coins file: " + coinsFile.getAbsolutePath());
        }
      }

      bufferedWriter =
          new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream(coinsFile), StandardCharsets.UTF_8));

      bufferedWriter.write(Integer.toString(coins));
      bufferedWriter.newLine();

      logger.info("User coins saved.");

    } catch (IOException e) {
      logger.warning("Failed to save coins: " + e.getMessage());
    } finally {
      if (bufferedWriter != null) {
        try {
          bufferedWriter.close();
        } catch (IOException ignored) {
        }
      }
    }
  }

  private boolean isRunningFromJarOrExe() {
    String protocol = FileManager.class.getResource("").getProtocol();
    return !protocol.equals("file");
  }

  private String getSaveDirectory() {
    if (testDirectory != null) return testDirectory;

    String dir;
    if (isRunningFromJarOrExe()) {
      dir = System.getProperty("user.home") + File.separator + "TEAM3" + File.separator;
    } else {
      dir = System.getProperty("user.dir") + File.separator + "res" + File.separator;
    }

    File d = new File(dir);
    if (!d.exists()) {
      if (!d.mkdirs()) {
        logger.warning("Failed to create directory: " + d.getAbsolutePath());
      }
    }

    File achFile = new File(d, "achievement.csv");
    if (!achFile.exists()) {
      try (BufferedWriter w =
          new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream(achFile), StandardCharsets.UTF_8))) {
        w.write("mode,player,First Blood,Survivor,Clear,Sharpshooter,50 Bullets,Get 3000 Score");
        w.newLine();
      } catch (IOException e) {
        logger.warning("Failed to create achievement.csv: " + achFile.getAbsolutePath());
      }
    }

    File coinsFile = new File(d, "coins.csv");
    if (!coinsFile.exists()) {
      try (BufferedWriter w =
          new BufferedWriter(
              new OutputStreamWriter(new FileOutputStream(coinsFile), StandardCharsets.UTF_8))) {
        w.write("0");
        w.newLine();
      } catch (IOException e) {
        logger.warning("Failed to create coins.csv: " + coinsFile.getAbsolutePath());
      }
    }

    return dir;
  }

  /** JUnit test code can force FileManager to use a specific directory */
  public static void setTestDirectory(String path) {
    testDirectory = path;
  }
}
