package engine;

import entity.Ship;
import screen.*; // NOPMD

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("PMD.TooManyMethods")
public class ScreenControl {

  private static final Logger LOGGER = Core.getLogger();
  private static Screen currentScreen;

  private static final int WIDTH = 448;
  private static final int HEIGHT = 520;
  private static final int FPS = 60;

  /** Lives per player (used to compute team pool in shared mode). */
  private static final int MAX_LIVES = 3;

  private static final int EXTRA_LIFE_FREQUENCY = 3;

    private final Map<Integer, ScreenAction> screenActions = new HashMap<>();

    /** Frame to draw the screen on. */
    private final Frame frame;

    private final List<GameSettings> gameSettings;

    private boolean coopSelected = false; // false = 1P, true = 2P // NOPMD
    private Ship.ShipType shipTypeP1 = Ship.ShipType.NORMAL; // P1 Ship Type
    private Ship.ShipType shipTypeP2 = Ship.ShipType.NORMAL; // P2 Ship Type

  // 1. 함수형 인터페이스 정의: 각 화면 메서드가 이 형태를 따름
  @FunctionalInterface
  private interface ScreenAction {
    int execute(AchievementManager achievementManager);
  }

  public ScreenControl(final Frame frame, final List<GameSettings> gameSettings) {
    this.frame = frame; // NOPMD
    this.gameSettings = gameSettings;
    initializeScreenMap();
  }

  private void initializeScreenMap() {
    // 람다식을 사용하여 각 번호에 맞는 메서드를 연결합니다.
    // 인자가 필요 없는 메서드는 'mgr -> method()' 형태로,
    // 인자가 필요한 메서드는 'this::method' 형태로 연결합니다.

    screenActions.put(1, mgr -> showTitleScreen());
    screenActions.put(2, this::runGameLoop); // runGameLoop은 AchievementManager를 받으므로 바로 연결 가능
    screenActions.put(3, mgr -> showAchievementScreen());
    screenActions.put(4, mgr -> showSettingScreen());
    screenActions.put(5, mgr -> showPlayScreen());
    screenActions.put(6, mgr -> showShipSelectionP1());
    screenActions.put(7, mgr -> showShipSelectionP2());
    screenActions.put(8, mgr -> showHighScoreScreen());
  }

  public int processNextScreen(final int currentReturnCode, final AchievementManager achievementManager) {
    if (screenActions.containsKey(currentReturnCode)) {
      return screenActions.get(currentReturnCode).execute(achievementManager); // NOPMD
    }
    return 0; // 매핑되지 않은 코드인 경우 종료
  }

  private int launchScreen(final Screen screen) { // NOPMD
    currentScreen = screen;
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info(
          "Starting "
              + screen.getWidth()
              + "x"
              + screen.getHeight()
              + " screen at "
              + FPS
              + " fps.");
    }
    final int returnCode = frame.setScreen(currentScreen);
    LOGGER.info("Closing screen.");
    return returnCode;
  }

  private int showTitleScreen() {
    int returnCode = launchScreen(new TitleScreen(frame.getWidth(), frame.getHeight(), FPS));

    // 2P mode: reading the mode which user chose from TitleScreen
    // (edit) TitleScreen to PlayScreen
    if (returnCode == 2) {
      currentScreen = new PlayScreen(frame.getWidth(), frame.getHeight(), FPS);
      returnCode = frame.setScreen(currentScreen);

      coopSelected = ((PlayScreen) currentScreen).isCoopSelected();
    }

    return returnCode;
  }

  private int runGameLoop(final AchievementManager achievementManager) {
    // 2P mode: building gameState now using user choice
    GameState gameState = new GameState(1, MAX_LIVES, coopSelected);
    int returnCode;

    do {
      returnCode = playSingleLevel(gameState, achievementManager);

      if (returnCode == 1) {
        break;
      }

      gameState = ((GameScreen) currentScreen).getGameState();

      if (gameState.teamAlive()) {
        gameState.nextLevel();
        if (gameState.getLevel() >= GameState.INFINITE_LEVEL) {
          break;
        }
      }
    } while (gameState.teamAlive()); // NOPMD
    if (returnCode == 1) {
      return 1;
    }

    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info(
          "Starting "
              + WIDTH
              + "x"
              + HEIGHT
              + " score screen at "
              + FPS
              + " fps, with a score of "
              + gameState.getScore()
              + ", "
              + gameState.getLivesRemaining()
              + " lives remaining, "
              + gameState.getBulletsShot()
              + " bullets shot and "
              + gameState.getShipsDestroyed()
              + " ships destroyed.");
    }
    currentScreen =
        new ScoreScreen(frame.getWidth(), frame.getHeight(), FPS, gameState, achievementManager);
    returnCode = frame.setScreen(currentScreen);
    LOGGER.info("Closing score screen.");

    return returnCode;
  }

  private int playSingleLevel(
      final GameState gameState, final AchievementManager achievementManager) {
    // Extra life this level? Give it if team pool is below cap.
    final int teamCap =
        gameState.isCoop() ? (MAX_LIVES * GameState.NUM_PLAYERS) : MAX_LIVES; // NOPMD
    final boolean bonusLife =
        gameState.getLevel() % EXTRA_LIFE_FREQUENCY == 0 && gameState.getLivesRemaining() < teamCap;

    currentScreen =
        new GameScreen(
            gameState,
            gameSettings.get(gameState.getLevel() - 1),
            bonusLife,
            frame.getWidth(),
            frame.getHeight(),
            FPS,
            shipTypeP1,
            shipTypeP2,
            achievementManager);

    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("Starting " + WIDTH + "x" + HEIGHT + " game screen at " + FPS + " fps.");
    }
    final int returnCode = frame.setScreen(currentScreen);
    LOGGER.info("Closing game screen.");
    Core.getFileManager().saveCoins(gameState.getCoins()); // NOPMD

    return returnCode;
  }

  private int showAchievementScreen() {
    return launchScreen(new AchievementScreen(frame.getWidth(), frame.getHeight(), FPS));
  }

  private int showSettingScreen() {
    final int returnCode = launchScreen(new SettingScreen(frame.getWidth(), frame.getHeight(), FPS));

    frame.removeKeyListener(InputManager.getInstance());
    frame.addKeyListener(
        InputManager.getInstance()); // Remove and re-register the input manager, forcing the key

    return returnCode;
  }

  private int showPlayScreen() {
    int returnCode = launchScreen(new PlayScreen(frame.getWidth(), frame.getHeight(), FPS));
    coopSelected = ((PlayScreen) currentScreen).isCoopSelected();

    // playscreen -> shipselectionscreen
    if (returnCode == 2) {
      returnCode = 6;
    }
    LOGGER.info("Closing play screen.");

    return returnCode;
  }

  private int showShipSelectionP1() {
    // Ship selection for Player 1.
    final int returnCode =
        launchScreen(new ShipSelectionScreen(frame.getWidth(), frame.getHeight(), FPS, 1));
    shipTypeP1 = ((ShipSelectionScreen) currentScreen).getSelectedShipType();

    // If clicked back button, go back to the screen 1P screen -> Player select screen
    if (returnCode == 5) {
      return 5;
    }

    if (coopSelected) {
      return 7; // Go to Player 2 selection.
    } else {
      return 2; // Start game.
    }
  }

  private int showShipSelectionP2() {
    final int returnCode =
        launchScreen(new ShipSelectionScreen(frame.getWidth(), frame.getHeight(), FPS, 2));
    shipTypeP2 = ((ShipSelectionScreen) currentScreen).getSelectedShipType();

    // If clicked back button, go back to the screen 2P screen -> 1P screen
    if (returnCode == 6) {
      return 6;
    }
    return 2; // Start game.
  }

  private int showHighScoreScreen() {
    return launchScreen(new HighScoreScreen(frame.getWidth(), frame.getHeight(), FPS));
  }
}
