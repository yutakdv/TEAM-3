package engine;

import java.io.IOException;
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
  public void saveToFile(final String userName, final String mode) throws IOException {
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
        toastQueue.offer(new Toast(a, TOAST_DURATION_MS));
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

    Toast(final Achievement achievement, final int ms) {
      this.achievement = achievement;
      this.ttl = Core.getCooldown(ms);
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
}
