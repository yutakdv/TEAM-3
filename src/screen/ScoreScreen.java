package screen;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import engine.*; // NOPMD - false positive, engine classes used implicitly

/**
 * Implements the score screen.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
@SuppressWarnings("PMD.OnlyOneReturn")
public class ScoreScreen extends Screen {

  /** Milliseconds between changes in user selection. */
  private static final int SELECTION_TIME = 200;

  /** Code of max high score. */
  private static final int MAX_HIGH_SCORE_NUM =
      7; // NOPMD - variable name intentionally descriptive

  /** Maximum name length. */
  private static final int MAX_NAME_LENGTH = 5;

  // Added for persist per-player breakdown
  private final GameState gameState;

  /** Current score. */
  private final int score;

  /** Player lives left. */
  private final int livesRemaining;

  /** Current coins. */
  private final int coins;

  /** Total bullets shot by the player. */
  private final int bulletsShot;

  /** Total ships destroyed by the player. */
  private final int shipsDestroyed;

  /** List of past high scores. */
  private List<Score> highScores;

  /** Checks if current score is a new high score. */
  private boolean isNewRecord;

  /** Player name for record input. */
  private StringBuilder name; // NOPMD - false positive

  /** Character of players name selected for change. */
  final int nameCharSelected;

  /** Make sure the name is less than 3 characters. */
  private boolean showNameError = false; // NOPMD - keep explicit initializer for clarity

  /** Time between changes in user selection. */
  private final Cooldown selectionCooldown;

  /** manages achievements. */
  private final AchievementManager
      achievementManager; // NOPMD - variable name intentionally descriptive

  /** Total coins earned in the game. */
  final int[] totalCoins = new int[2];

  /** check 1P/2P mode; */
  private final String mode;

  /**
   * Constructor, establishes the properties of the screen.
   *
   * @param width Screen width.
   * @param height Screen height.
   * @param fps Frames per second, frame rate at which the game is run.
   * @param gameState Current game state.
   * @param achievementManager Achievement manager instance used to track and save player
   *     achievements. 2025-10-03 add generator parameter and comment
   */
  public ScoreScreen(
      final int width,
      final int height,
      final int fps,
      final GameState gameState,
      final AchievementManager
          achievementManager) // NOPMD - variable name intentionally descriptive
      {
    super(width, height, fps);
    this.gameState = gameState; // Added

    this.score = gameState.getScore();
    this.livesRemaining = gameState.getLivesRemaining();
    this.coins = gameState.getCoins();
    this.name = new StringBuilder();
    this.bulletsShot = gameState.getBulletsShot();
    this.shipsDestroyed = gameState.getShipsDestroyed();
    this.totalCoins[0] = gameState.getCoins(); // ADD THIS LINE
    this.isNewRecord = false;
    this.name = new StringBuilder();
    this.nameCharSelected = 0;
    this.selectionCooldown = Core.getCooldown(SELECTION_TIME);
    this.selectionCooldown.reset();
    this.achievementManager = achievementManager;
    this.mode = gameState.getCoop() ? "2P" : "1P";

    try {
      this.highScores = Core.getFileManager().loadHighScores(this.mode);
      if (highScores.size() < MAX_HIGH_SCORE_NUM
          || highScores.get(highScores.size() - 1).getScore() < this.score) {
        this.isNewRecord = true;
      }

    } catch (IOException e) {
      logger.warning("Couldn't load high scores!");
    }
    // clear last key
    inputManager.clearLastKey();
  }

  /**
   * Starts the action.
   *
   * @return Next screen code.
   */
  public final int run() {
    super.run();

    return this.returnCode;
  }

  /** Updates the elements on screen and checks for events. */
  protected final void update() {
    super.update();
    draw();

    if (!this.inputDelay.checkFinished()) {
      return;
    }

    if (handleEscape()) {
      return;
    }
    if (handleSpace()) {
      return;
    }

    handleBackspace();
    handleCharInput();
  }

  private boolean handleEscape() {
    if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE)) {
      // Return to main menu.
      SoundManager.playeffect("sound/select.wav");
      this.returnCode = 1;
      this.isRunning = false;
      if (this.isNewRecord) {
        saveScore();
        saveAchievement(); // 2025-10-03 call method for save achievement released
      }
      return true;
    }
    return false;
  }

  private boolean handleSpace() {
    if (!inputManager.isKeyDown(KeyEvent.VK_SPACE)) {
      return false;
    }
    // name too short -> return
    if (this.name.length() < 3) {
      return true;
    }
    // Play again.
    SoundManager.playeffect("sound/select.wav");
    this.returnCode = 2;
    this.isRunning = false;
    if (this.isNewRecord) {
      saveScore();
      saveAchievement(); // 2025-10-03 call method for save achievement released
    }
    return true;
  }

  private void handleBackspace() {
    // Handle backspace
    if (inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)
        && this.selectionCooldown.checkFinished()
        && !this.name.isEmpty()) {

      this.name.deleteCharAt(this.name.length() - 1);
      this.selectionCooldown.reset();
    }
  }

  private void handleCharInput() {
    // Handle character input
    final char typedChar = inputManager.getLastCharTyped();
    if (typedChar != '\0') {
      // Checks the name is not short when you press the space bar
      if (typedChar == ' ') {
        if (this.name.length() < 3) {
          this.showNameError = true;
        }
      }

      // Check if it's a valid character (alphanumeric only)
      else if (Character.isLetterOrDigit(typedChar) && this.name.length() < MAX_NAME_LENGTH) {
        this.name.append(Character.toUpperCase(typedChar));
      }
    }
  }

  /**
   * Saves the score as a high score. 2025-10-18 Add ability that distinguish duplicate names and
   * save higher scores
   */
  private void saveScore() {
    final String mode =
        (gameState != null && gameState.isCoop())
            ? "2P"
            : "1P"; // NOPMD - parentheses intentional for clarity
    final String newName = this.name.toString();
    final Score newScore = new Score(newName, Objects.requireNonNull(this.gameState), mode);
    boolean foundAndReplaced = false;
    for (int i = 0; i < highScores.size(); i++) {
      final Score existingScore = highScores.get(i);
      if (existingScore.getName().equals(newName)) { // NOPMD - LoD acceptable in value comparison
        if (newScore.getScore()
            > existingScore.getScore()) { // NOPMD - LoD acceptable in value comparison
          highScores.set(i, newScore);
          foundAndReplaced = true;
        } else {
          foundAndReplaced = true;
        }
        break;
      }
    }
    if (!foundAndReplaced) {
      highScores.add(newScore);
    }
    Collections.sort(highScores);
    if (highScores.size() > MAX_HIGH_SCORE_NUM) {
      highScores.remove(highScores.size() - 1);
    }
    try {
      Core.getFileManager()
          .saveHighScores(highScores, mode); // NOPMD - LoD acceptable in value comparison
    } catch (IOException e) {
      logger.warning("Couldn't load high scores!");
    }
  }

  /** Save the achievement released. 2025-10-03 add new method */
  private void saveAchievement() {
    this.achievementManager.saveToFile(this.name.toString(), this.mode);
  }

  /** Draws the elements associated with the screen. */
  private void draw() {
    drawManager.initDrawing(this);

    drawManager.drawGameOver(this, this.inputDelay.checkFinished());

    // 2P mode: edit to include co-op + individual score/coins
    if (this.gameState != null && this.gameState.isCoop()) {
      // team summary
      drawManager.drawResults(
          this,
          this.gameState.getScore(),
          this.gameState.getCoins(), // team score
          this.gameState.getLivesRemaining(),
          this.gameState.getShipsDestroyed(),
          0f, // leaving out team accuracy
          this.isNewRecord,
          false // Draw accuracy for 2P mode
          );

      // show per-player lines when in 2P mode

      final float p1Acc =
          this.gameState.getBulletsShot(0) > 0
              ? (float) this.gameState.getShipsDestroyed(0) / this.gameState.getBulletsShot(0)
              : 0f;
      final float p2Acc =
          this.gameState.getBulletsShot(1) > 0
              ? (float) this.gameState.getShipsDestroyed(1) / this.gameState.getBulletsShot(1)
              : 0f;

      final String p1 =
          String.format("P1  %04d  |  acc %.2f%%", this.gameState.getScore(0), p1Acc * 100f);
      final String p2 =
          String.format("P2  %04d  |  acc %.2f%%", this.gameState.getScore(1), p2Acc * 100f);

      int y; // tweak these if you want
      if (this.isNewRecord) {
        y = this.getHeight() / 2 + 40; // Position if new record is True
      } else {
        y = this.getHeight() / 2 + 80; // Position if new record is False
      }
      drawManager.drawCenteredRegularString(this, p1, y);
      drawManager.drawCenteredRegularString(this, p2, y + 20); // Increase spacing

    } else {
      // 1P legacy summary with accuracy
      final float acc = this.bulletsShot > 0 ? (float) this.shipsDestroyed / this.bulletsShot : 0f;
      drawManager.drawResults(
          this,
          this.score,
          this.coins,
          this.livesRemaining,
          this.shipsDestroyed,
          acc,
          this.isNewRecord,
          true); // Draw accuracy for 1P mode
    }

    drawManager.drawNameInput(this, this.name, this.isNewRecord);
    if (showNameError) {
      drawManager.drawNameInputError(this);
    }

    drawManager.completeDrawing(this);
  }
}
