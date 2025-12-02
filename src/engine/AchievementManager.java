package engine;

import entity.EnemyShipFormation;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Manages the list of achievements for a player, including loading from and saving to the
 * FileManager.
 */
public class AchievementManager {
  private static final java.util.logging.Logger logger = Core.getLogger();

  private final List<Achievement> achievements;

  private static AchievementManager instance;

  private final Queue<Toast> toastQueue = new LinkedList<>();
  private Toast activeToast;
  private static final int TOAST_DURATION_MS = 3000;

  public AchievementManager() {
    this.achievements = createDefaultAchievements();
  }

  /** Defines the default achievements available in the game. */
  private List<Achievement> createDefaultAchievements() {
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

  /** Converts the achievements into a boolean list and saves them using FileManager. */
  public void saveToFile(final String userName, final String mode) {
    final List<Boolean> flags = new ArrayList<>();
    for (final Achievement a : achievements) {
      flags.add(a.isUnlocked());
    }
    // mode 추가
    final FileManager fm = FileManager.getInstance();
    fm.unlockAchievement(userName, flags, mode); // NOPMD - LawOfDemeter
  }

  /** Returns the current achievement list. */
  public List<Achievement> getAchievements() {
    return achievements;
  }

  /** Unlocks the achievement by name. */
  public void unlock(final String name) {
    for (final Achievement a : achievements) {
      if (a.getName().equals(name) && !a.isUnlocked()) { // NOPMD - LawOfDemeter
        a.unlock();
        SoundManager.ingameeffect("sound/achievement.wav");
        if (logger.isLoggable(java.util.logging.Level.INFO)) {
          logger.info("Achievement unlocked: " + a);
        }
        toastQueue.offer(new Toast(a));
      }
    }
  }

  public void update() {
    if (activeToast == null || !activeToast.alive()) {
      activeToast = toastQueue.poll();
      if (activeToast != null) {
        activeToast.ttl.reset(); // NOPMD - LawOfDemeter
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

  /**
   * Make sure you have a pop-up on the screen, or you have a pop-up left in the queue.
   *
   * @return If there is at least one pop-up left, true, or false
   */
  public boolean hasPendingToasts() {
    return (activeToast != null && activeToast.alive()) // NOPMD - UselessParentheses
        || !toastQueue.isEmpty();
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

  /**
   * Returns the shared instance of AchievementManager. [2025-10-17] Added in commit feat: complete
   * drawAchievementMenu method in DrawManager.
   */
  protected static AchievementManager getInstance() {
    if (instance == null) {
      instance = new AchievementManager();
    }
    return instance;
  }

  public void checkAchievements(
      GameState state, EnemyShipFormation formation, boolean levelFinished, boolean tookDamage) {
    if (state.getShipsDestroyed() == 1) unlock("First Blood");
    if (state.getBulletsShot() >= 50) unlock("50 Bullets");
    if (state.getScore() >= 3000) unlock("Get 3000 Score");

    if (levelFinished && formation.isEmpty() && state.getLevel() == 5) {
      unlock("Clear");
      float p1Acc =
          state.getBulletsShot(0) > 0
              ? (float) state.getShipsDestroyed(0) / state.getBulletsShot(0) * 100
              : 0f;
      float p2Acc =
          state.getBulletsShot(1) > 0
              ? (float) state.getShipsDestroyed(1) / state.getBulletsShot(1) * 100
              : 0f;

      if (!tookDamage) unlock("Survivor");
      if (p1Acc >= 80 || p2Acc >= 80) unlock("Sharpshooter");
    }
  }
}
