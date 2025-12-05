package engine;

import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements core game logic.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
@SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
public final class Core {

  private static final int WIDTH = 448;
  private static final int HEIGHT = 520;

  private static final Logger LOGGER = Logger.getLogger(Core.class.getSimpleName());
  private static Handler fileHandler;

  /**
   * Test implementation.
   *
   * @param args Program args, ignored.
   */
  @Generated
  @SuppressWarnings("PMD.CyclomaticComplexity")
  public static void main(final String[] args) {
    initializeLogger();
    CoinManager.load();

    /* Frame to draw the screen on. */
    final Frame frame = new Frame(WIDTH, HEIGHT);
    final InputManager input = getInputManager();
    frame.addKeyListener(
        input); // Register an instance to allow the window to receive keyboard event information
    getDrawManager().setFrame(frame); // NOPMD - LawOfDemeter

    final List<GameSettings> gameSettings = GameSettings.getGameSettings();

    final ScreenControl screencontrol = new ScreenControl(frame, gameSettings);

    int returnCode = 1;

    do {
      // Game & score.
      final AchievementManager achievementManager =
          new AchievementManager(); // add 1P/2P achievement manager

      returnCode = screencontrol.processNextScreen(returnCode, achievementManager);

    } while (returnCode != 0);

    fileHandler.flush();
    fileHandler.close();
    System.exit(0);
  }

  private static void initializeLogger() {
    try {
      LOGGER.setUseParentHandlers(false);
      fileHandler = new FileHandler("log");
      fileHandler.setFormatter(new MinimalFormatter());
      final ConsoleHandler consoleHandler = new ConsoleHandler();
      consoleHandler.setFormatter(new MinimalFormatter());
      LOGGER.addHandler(fileHandler);
      LOGGER.addHandler(consoleHandler);
      LOGGER.setLevel(Level.ALL);
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to initialize logging system.", e);
    }
  }

  /** Constructor, not called. */
  private Core() {}

  /**
   * Controls access to the logger.
   *
   * @return Application logger.
   */
  @SuppressWarnings("MS_EXPOSE_REP")
  public static Logger getLogger() {
    return Logger.getLogger(Core.class.getName());
  }

  /**
   * Controls access to the drawing manager.
   *
   * @return Application draw manager.
   */
  public static DrawManager getDrawManager() {
    return DrawManager.getInstance();
  }

  /**
   * Controls access to the input manager.
   *
   * @return Application input manager.
   */
  public static InputManager getInputManager() {
    return InputManager.getInstance();
  }

  /**
   * Controls access to the file manager.
   *
   * @return Application file manager.
   */
  public static FileManager getFileManager() {
    return FileManager.getInstance();
  }

  /**
   * Controls creation of new cooldowns.
   *
   * @param milliseconds Duration of the cooldown.
   * @return A new cooldown.
   */
  public static Cooldown getCooldown(final int milliseconds) {
    return new Cooldown(milliseconds);
  }

  /**
   * Controls access to the achievement manager.
   *
   * @return Application achievement manager. [2025-10-09] Added in commit: feat: complete
   *     drawAchievementMenu method in DrawManager
   */
  public static AchievementManager getAchievementManager() {
    return AchievementManager.getInstance();
  }

  /**
   * Controls creation of new cooldowns with variance.
   *
   * @param milliseconds Duration of the cooldown.
   * @param variance Variation in the cooldown duration.
   * @return A new cooldown with variance.
   */
  public static Cooldown getVariableCooldown(final int milliseconds, final int variance) {
    return new Cooldown(milliseconds, variance);
  }

  public static ItemManager getItemManager() {
    return ItemManager.getInstance();
  }
}
