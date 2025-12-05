package engine;

import entity.EnemyShipFormation;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the list of achievements for a player, including loading from and saving to disk.
 * Refactored: Now handles file I/O internally.
 */
@SuppressWarnings({"PMD.LawOfDemeter"})
public class AchievementManager { // NOPMD
  private static final Logger LOGGER = Core.getLogger();
  private static final String FILENAME_ACHIEVEMENT = "achievement.csv";

  private final List<Achievement> achievements;

  private static AchievementManager instance;

  private final Queue<Toast> toastQueue = new LinkedList<>();
  private Toast activeToast;
  private static final int TOAST_DURATION_MS = 3000;

  public AchievementManager() {
    this.achievements = createDefaultAchievements();
  }

  private List<Achievement> createDefaultAchievements() { // NOPMD
    final List<Achievement> list = new ArrayList<>();
    list.add(new Achievement("First Blood", "Defeat your first enemy."));
    list.add(new Achievement("Survivor", "Clear a round without losing a life."));
    list.add(new Achievement("Clear", "Clear 5 levels."));
    list.add(new Achievement("Sharpshooter", "Record an accuracy of more than 80 percent"));
    list.add(new Achievement("50 Bullets", "Fire 50 Bullets."));
    list.add(new Achievement("Get 3000 Score", "Get more than 3,000 points"));
    list.add(new Achievement("Perfect Shooter", "Destroy all enemies with perfect accuracy."));
    return list;
  }

  // [Refactored] Now calls internal unlockAchievement instead of FileManager
  public void saveToFile(final String userName, final String mode) {
    final List<Boolean> flags = new ArrayList<>();
    for (final Achievement a : achievements) {
      flags.add(a.isUnlocked());
    }
    unlockAchievement(userName, flags, mode);
  }

  public List<Achievement> getAchievements() {
    return achievements;
  }

  public void unlock(final String name) {
    for (final Achievement a : achievements) {
      if (a.getName().equals(name) && !a.isUnlocked()) {
        a.unlock();
        SoundManager.ingameeffect("sound/achievement.wav");
        if (LOGGER.isLoggable(Level.INFO)) {
          LOGGER.info("Achievement unlocked: " + a);
        }
        toastQueue.offer(new Toast(a));
      }
    }
  }

  public void update() {
    if (activeToast == null || !activeToast.alive()) {
      activeToast = toastQueue.poll();
      if (activeToast != null) {
        activeToast.ttl.reset();
      }
    }
  }

  public List<Achievement> getActiveToasts() {
    final List<Achievement> activeList = new ArrayList<>();
    if (activeToast != null && activeToast.alive()) {
      activeList.add(activeToast.achievement);
    }
    return activeList;
  }

  public boolean hasPendingToasts() {
    return activeToast != null && activeToast.alive() || !toastQueue.isEmpty();
  }

  private static final class Toast {
    final Achievement achievement;
    final Cooldown ttl;

    Toast(final Achievement achievement) {
      this.achievement = achievement;
      this.ttl = Core.getCooldown(TOAST_DURATION_MS);
    }

    boolean alive() {
      return !ttl.checkFinished();
    }
  }

  protected static AchievementManager getInstance() {
    if (instance == null) {
      instance = new AchievementManager();
    }
    return instance;
  }

  public void checkAchievements( // NOPMD
      final GameState state,
      final EnemyShipFormation formation,
      final boolean levelFinished,
      final boolean tookDamage) {
    if (state.getShipsDestroyed() == 1) {
      unlock("First Blood");
    }
    if (state.getBulletsShot() >= 50) {
      unlock("50 Bullets");
    }
    if (state.getScore() >= 3000) {
      unlock("Get 3000 Score");
    }
    if (levelFinished && formation.isEmpty() && state.getLevel() == 5) {
      unlock("Clear");
      final float p1Acc =
          state.getBulletsShot(0) > 0
              ? (float) state.getShipsDestroyed(0) / state.getBulletsShot(0) * 100
              : 0f;
      final float p2Acc =
          state.getBulletsShot(1) > 0
              ? (float) state.getShipsDestroyed(1) / state.getBulletsShot(1) * 100
              : 0f;

      if (!tookDamage) {
        unlock("Survivor");
      }
      if (p1Acc >= 80 || p2Acc >= 80) {
        unlock("Sharpshooter");
      }
    }
  }

  /** Unlocks achievements for a specific user and saves to file. */
  public void unlockAchievement( // NOPMD
      final String userName, final List<Boolean> unlockedAchievement, final String mode) { // NOPMD
    final String numericMode = mode.replaceAll("[^0-9]", "");
    final List<String[]> records = new ArrayList<>();
    final String path = FileManager.getInstance().getSaveDirectory() + FILENAME_ACHIEVEMENT;
    final File achFile = new File(path);
    boolean found = false;

    if (achFile.exists()) {
      try (BufferedReader bReader =
          new BufferedReader(
              new InputStreamReader(
                  new FileInputStream(achFile), StandardCharsets.UTF_8))) { // NOPMD
        String line = bReader.readLine();
        while (line != null) {
          final String[] row = line.split(",");
          if (row.length < 3) {
            records.add(row);
            line = bReader.readLine();
            continue;
          }
          if (row[1].trim().equals(userName) && row[0].trim().equals(numericMode)) {
            found = true;
            updateAchievementRow(row, unlockedAchievement);
          }
          records.add(row);
          line = bReader.readLine();
        }
      } catch (IOException e) {
        if (LOGGER.isLoggable(Level.WARNING)) {
          LOGGER.warning("Error reading achievements: " + e.getMessage());
        }
      }
    }

    // Create new if not found
    if (!found) {
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("User not found, creating new record.");
      }
      records.add(createNewAchievementRow(numericMode, userName, unlockedAchievement));
    }

    // Write back
    writeCSV(achFile, records);
  }

  private void updateAchievementRow(final String[] row, final List<Boolean> unlocked) {
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Achievement has been updated.");
    }
    for (int i = 2; i < row.length && (i - 2) < unlocked.size(); i++) {
      if ("0".equals(row[i]) && unlocked.get(i - 2)) {
        row[i] = "1";
      }
    }
  }

  private String[] createNewAchievementRow(
      final String mode, final String name, final List<Boolean> unlocked) {
    final String[] newRecord = new String[unlocked.size() + 2];
    newRecord[0] = mode;
    newRecord[1] = name;
    for (int i = 0; i < unlocked.size(); i++) {
      newRecord[i + 2] = unlocked.get(i) ? "1" : "0";
    }
    return newRecord;
  }

  private void writeCSV(final File file, final List<String[]> rows) {
    try (BufferedWriter writer =
        new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) { // NOPMD
      for (final String[] row : rows) {
        writer.write(String.join(",", row));
        writer.newLine();
      }
    } catch (IOException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.warning("Error writing CSV: " + e.getMessage());
      }
    }
  }

  /** Returns a list of users who have completed a specific achievement. */
  public List<String> getAchievementCompleter(final Achievement achievement) { // NOPMD
    final List<String> completer = new ArrayList<>();
    final String path = FileManager.getInstance().getSaveDirectory() + FILENAME_ACHIEVEMENT;
    final File achFile = new File(path);

    if (!achFile.exists()) {
      return completer;
    }
    try (BufferedReader bReader =
        new BufferedReader(
            new InputStreamReader(new FileInputStream(achFile), StandardCharsets.UTF_8))) { // NOPMD

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
        if (LOGGER.isLoggable(Level.WARNING)) {
          LOGGER.warning("Achievement column not found: " + achievement.getName());
        }
        return completer;
      }

      String line = bReader.readLine();
      while (line != null) {
        final String[] tokens = line.split(",");
        if (tokens.length > idx && "1".equals(tokens[idx].trim())) {
          completer.add(tokens[0].trim() + ":" + tokens[1].trim());
        }
        line = bReader.readLine();
      }
    } catch (IOException e) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.warning("Error reading achievement file.");
      }
    }
    return completer;
  }
}
