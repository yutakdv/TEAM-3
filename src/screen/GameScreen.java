package screen;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.GameSettings;
import engine.GameState;
import engine.*; // NOPMD
import engine.SoundManager;
import entity.Bullet;
import entity.BulletPool;
import entity.EnemyShip;
import entity.EnemyShipFormation;
import entity.Entity;
import entity.Ship;
import java.awt.*; // NOPMD

// NEW Item code
import entity.Item;
import entity.ItemPool;

/**
 * Implements the game screen, where the action happens.(supports co-op with shared team lives)
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public class GameScreen extends Screen { // NOPMD
  /** Milliseconds until the screen accepts user input. */
  private static final int INPUT_DELAY = 6000;

  /** Used when there is no message */
  private static final int INPUT_DELAY_NO_MESSAGE = 4000;

  /** Bonus score for each life remaining at the end of the level. */
  private static final int LIFE_SCORE = 100;

  /** Minimum time between bonus ship's appearances. */
  private static final int BONUS_SHIP_INTERVAL = 20000; // NOPMD

  /** Maximum variance in the time between bonus ship's appearances. */
  private static final int BONUS_SHIP_VARIANCE = 10000; // NOPMD

  /** Time until bonus ship explosion disappears. */
  private static final int BONUS_SHIP_EXPLOSION = 500;

  /** Time from finishing the level to screen change. */
  private static final int SCREEN_CHANGE_INTERVAL = 1500;

  /** Height of the interface separation line. */
  private static final int PAUSE_COOLDOWN = 300;

  private static final int RETURN_MENU_COOLDOWN = 300;
  private static final int SEPARATION_LINE_HEIGHT = 68;
  private static final int HIGH_SCORE_NOTICE_DURATION = 2000;
  private static boolean sessionHighScoreNotified = false; // NOPMD

  /** For Check Achievement 2015-10-02 add new */
  private AchievementManager achievementManager; // NOPMD

  private final CollisionManager collisionManager;
  private final PauseMenuHandler pauseMenuHandler;

  /** Current game difficulty settings. */
  private final GameSettings gameSettings;

  /** Formation of enemy ships. */
  private EnemyShipFormation enemyShipFormation;

  private EnemyShip enemyShipSpecial;

  /** Formation of player ships. */
  private final Ship[] ships = new Ship[GameState.NUM_PLAYERS];

  /** Minimum time between bonus ship appearances. */
  private Cooldown enemyShipSpecialCooldown;

  /** Time until bonus ship explosion disappears. */
  private Cooldown enemyShipSpecialExplosionCooldown;

  /** Time from finishing the level to screen change. */
  private Cooldown screenFinishedCooldown;

  /** Set of all bullets fired by on screen ships. */
  private Set<Bullet> bullets;

  /** Set of all items spawned. */
  private Set<Item> items;

  private long gameStartTime;

  /** Checks if the level is finished. */
  private boolean levelFinished;

  /** Checks if a bonus life is received. */
  private final boolean bonusLife;

  private int topScore;
  private boolean highScoreNotified;
  private long highScoreNoticeStartTime;

  private boolean isPaused;
  private Cooldown pauseCooldown;
  private Cooldown returnMenuCooldown;
  int lives;

  private boolean hasCountdownMessage;

  /** checks if player took damage 2025-10-02 add new variable */
  private boolean tookDamageThisLevel;

  private boolean countdownSoundPlayed = false; // NOPMD

  private final GameState state;

  private final Ship.ShipType shipTypeP1;
  private final Ship.ShipType shipTypeP2;
  private final InputHandler inputHandler;

  /**
   * Constructor, establishes the properties of the screen.
   *
   * @param gameState Current game state.
   * @param gameSettings Current game settings.
   * @param bonusLife Checks if a bonus life is awarded this level.
   * @param width Screen width.
   * @param height Screen height.
   * @param fps Frames per second, frame rate at which the game is run.
   * @param shipTypeP1 Player 1's ship type.
   * @param shipTypeP2 Player 2's ship type.
   * @param achievementManager Achievement manager instance used to track and save player
   *     achievements. 2025-10-03 add generator parameter and comment
   */
  public GameScreen(
      final GameState gameState,
      final GameSettings gameSettings,
      final boolean bonusLife,
      final int width,
      final int height,
      final int fps,
      final Ship.ShipType shipTypeP1,
      final Ship.ShipType shipTypeP2,
      final AchievementManager achievementManager) {
    super(width, height, fps);

    this.state = gameState;
    this.gameSettings = gameSettings;
    this.bonusLife = bonusLife;
    this.shipTypeP1 = shipTypeP1;
    this.shipTypeP2 = shipTypeP2;

    this.lives = gameState.getLivesRemaining();
    if (this.bonusLife) {
      this.lives++;
    }

    // for check Achievement 2025-10-02 add
    this.achievementManager = achievementManager;
    this.collisionManager = new CollisionManager(this.state, this.drawManager);
    this.pauseMenuHandler = new PauseMenuHandler();
    this.inputHandler = new InputHandler(this.inputManager);

    this.tookDamageThisLevel = false;

    this.highScoreNotified = false;
    this.highScoreNoticeStartTime = 0;

    // 2P: bonus life adds to team pool + singleplayer mode
    if (this.bonusLife) {
      if (state.isSharedLives()) {
        state.addTeamLife(1); // two player
      } else {
        // 1P legacy: grant to P1
        state.addLife(0, 1); // singleplayer
      }
    }
    // [ADD] ensure achievementManager is not null for popup system
    if (this.achievementManager == null) {
      this.achievementManager = new AchievementManager();
    }
  }

  /** Initializes basic screen properties, and adds necessary elements. */
  public final void initialize() {
    super.initialize();

    // Start background music for gameplay
    SoundManager.ingameBGM("sound/SpaceInvader-GameTheme.wav");

    enemyShipFormation = new EnemyShipFormation(this.gameSettings);
    enemyShipFormation.attach(this);

    // 2P mode: create both ships, tagged to their respective teams
    this.ships[0] =
        new Ship(
            this.width / 2 - 60,
            this.height - 30,
            Entity.Team.PLAYER1,
            shipTypeP1,
            this.state); // P1
    this.ships[0].setPlayerId(1);

    // only allowing second ship to spawn when 2P mode is chosen
    if (state.isCoop()) {
      this.ships[1] =
          new Ship(
              this.width / 2 + 60,
              this.height - 30,
              Entity.Team.PLAYER2,
              shipTypeP2,
              this.state); // P2

      this.ships[1].setPlayerId(2);
    } else {
      this.ships[1] = null; // NOPMD | ensuring there's no P2 ship in 1P mode
    }

    this.enemyShipSpecialCooldown =
        Core.getVariableCooldown(BONUS_SHIP_INTERVAL, BONUS_SHIP_VARIANCE);
    this.enemyShipSpecialCooldown.reset();
    this.enemyShipSpecialExplosionCooldown = Core.getCooldown(BONUS_SHIP_EXPLOSION);
    this.screenFinishedCooldown = Core.getCooldown(SCREEN_CHANGE_INTERVAL);
    this.bullets = new HashSet<>();

    // New Item Code
    this.items = new HashSet<>();

    final String message =
        DrawManager.getCountdownMessage(this.state.getLevel(), 5, this.bonusLife);

    this.hasCountdownMessage = message != null;

    // Special input delay / countdown.
    this.gameStartTime = System.currentTimeMillis();
    int delay;
    if (this.hasCountdownMessage) {
      delay = INPUT_DELAY;
    } else {
      delay = INPUT_DELAY_NO_MESSAGE;
    }
    this.inputDelay = Core.getCooldown(delay);
    this.inputDelay.reset();
    drawManager.setDeath(false);

    this.isPaused = false;
    this.pauseCooldown = Core.getCooldown(PAUSE_COOLDOWN);
    this.returnMenuCooldown = Core.getCooldown(RETURN_MENU_COOLDOWN);
  }

  /**
   * Starts the action.
   *
   * @return Next screen code.
   */
  public final int run() {
    super.run();

    // 2P mode: award bonus score for remaining TEAM lives
    state.addScore(0, LIFE_SCORE * state.getLivesRemaining());

    // Stop all music on exiting this screen
    SoundManager.stopAllMusic();

    this.logger.info("Screen cleared with a score of " + state.getScore());
    return this.returnCode;
  }

  /** Updates the elements on screen and checks for events. */
  protected final void update() { // NOPMD
    super.update();

    // Countdown beep once during pre-start
    if (!this.inputDelay.checkFinished() && !countdownSoundPlayed) {
      final long elapsed = System.currentTimeMillis() - this.gameStartTime;
      if (this.hasCountdownMessage) {
        if (elapsed > 1750) {
          SoundManager.ingameeffect("sound/CountDownSound.wav");
          countdownSoundPlayed = true;
        }
      } else {
        if (elapsed > 1) {
          SoundManager.ingameeffect("sound/CountDownSound.wav");
          countdownSoundPlayed = true;
        }
      }
    }

    this.achievementManager.checkAchievements(
        state, enemyShipFormation, levelFinished, tookDamageThisLevel);
    if (this.inputDelay.checkFinished()
        && inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)
        && this.pauseCooldown.checkFinished()) {
      this.isPaused = !this.isPaused;
      SoundControl.setIngameVolumetype(0);
      this.pauseCooldown.reset();

      if (this.isPaused) {
        // Pause game music when pausing - no sound during pause
        SoundManager.stopBackgroundMusic();
      } else {
        // Resume game music when unpausing
        SoundManager.ingameBGM("sound/SpaceInvader-GameTheme.wav");
      }
    }
    if (this.isPaused
        && inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)
        && this.returnMenuCooldown.checkFinished()) {
      SoundManager.ingameeffect("sound/select.wav");
      SoundManager.stopAllMusic(); // Stop all music before returning to menu
      returnCode = 1;
      this.isRunning = false;
      Core.getFileManager().saveCoins(state.getCoins()); // NOPMD
    }

    if (this.isPaused) {
      this.inputHandler.handlePauseInput(this.pauseMenuHandler, this.drawManager, this);
    } else {
      updateGameLogic();
    }

    draw();
  }

  private void updateGameLogic() {
    handlePlayerActions();
    handleCollisionsLogic();
    cleanBullets();
    cleanItems();
    handleItemPickups();
    updateUILastLifeFlag();
    updateHighScoreNotice();
    checkLevelEndCondition();
    checkScreenExitCondition();
    updateAchievements();
  }

  private void handlePlayerActions() {
    if (this.inputDelay.checkFinished() && !this.levelFinished) {
      this.inputHandler.handleInput(this.ships, this.bullets, this.state, this.width);

      for (final Ship s : this.ships) {
        if (s != null) {
          s.update();
        }
      }

      updateEnemies();
    }
  }

  private void handleCollisionsLogic() {
    if (this.collisionManager.processCollisions(
        this.bullets,
        this.ships,
        this.enemyShipFormation,
        this.enemyShipSpecial,
        this.items,
        this.enemyShipSpecialExplosionCooldown,
        this.levelFinished)) {
      this.tookDamageThisLevel = true;
    }
  }

  private void handleItemPickups() {
    this.collisionManager.processItemPickups(this.items, this.ships);
  }

  private void updateUILastLifeFlag() {
    drawManager.setLastLife(state.getLivesRemaining() == 1);
  }

  private void updateHighScoreNotice() {
    if (!sessionHighScoreNotified && this.state.getScore() > this.topScore) {
      sessionHighScoreNotified = true;
      this.highScoreNotified = true;
      this.highScoreNoticeStartTime = System.currentTimeMillis();
    }
  }

  private void checkScreenExitCondition() {
    if (this.levelFinished
        && this.screenFinishedCooldown.checkFinished()
        && !achievementManager.hasPendingToasts()) {
      this.isRunning = false;
    }
  }

  private void updateAchievements() {
    if (this.achievementManager != null) {
      this.achievementManager.update();
    }
  }

  private void updateEnemies() {
    // Special ship lifecycle
    if (this.enemyShipSpecial != null) {
      if (this.enemyShipSpecial.isDestroyed()) {
        if (this.enemyShipSpecialExplosionCooldown.checkFinished()) {
          this.enemyShipSpecial = null; // NOPMD
        }
      } else {
        this.enemyShipSpecial.move(2, 0);
      }
    }
    if (this.enemyShipSpecial == null && this.enemyShipSpecialCooldown.checkFinished()) {
      this.enemyShipSpecial = new EnemyShip();
      this.enemyShipSpecialCooldown.reset();
      SoundManager.ingameeffect("sound/special_ship_sound.wav");
      this.logger.info("A special ship appears");
    }
    if (this.enemyShipSpecial != null && this.enemyShipSpecial.getPositionX() > this.width) {
      this.enemyShipSpecial = null; // NOPMD
      SoundManager.stop();
      this.logger.info("The special ship has escaped");
    }

    this.enemyShipFormation.update();
    final int bulletsBefore = this.bullets.size();
    this.enemyShipFormation.shoot(this.bullets);
    if (this.bullets.size() > bulletsBefore) {
      SoundManager.ingameeffect("sound/shoot_enemies.wav");
    }
  }

  private void checkLevelEndCondition() { // NOPMD
    if ((this.enemyShipFormation.isEmpty() || !state.teamAlive()) && !this.levelFinished) {
      BulletPool.recycle(this.bullets);
      this.bullets.clear();
      ItemPool.recycle(items);
      this.items.clear();

      this.levelFinished = true;
      this.screenFinishedCooldown.reset();

      if (enemyShipFormation.getShipCount() == 0
          && state.getBulletsShot() > 0
          && state.getBulletsShot() == state.getShipsDestroyed()) {
        achievementManager.unlock("Perfect Shooter");
      }
      if (enemyShipFormation.getShipCount() == 0 && !this.tookDamageThisLevel) {
        achievementManager.unlock("Survivor");
      }
      if (enemyShipFormation.getShipCount() == 0 && state.getLevel() == 5) {
        achievementManager.unlock("Clear");
      }
      this.achievementManager.checkAchievements(
          state, enemyShipFormation, levelFinished, tookDamageThisLevel);
    }
  }

  private void draw() {
    prepareFrame();
    drawShips();
    drawSpecialShip();
    drawEnemies();
    drawBullets();
    drawItems();
    drawHUD();
    drawCountdownIfNeeded();
    drawHighScoreNoticeIfNeeded();
    drawAchievementToastsSection();
    drawPauseMenuIfNeeded();
    finishFrame();
  }

  private void prepareFrame() {
    drawManager.initDrawing(this);
    drawManager.drawExplosions();
    drawManager.updateGameSpace();
  }

  private void drawShips() {
    for (final Ship s : this.ships) {
      if (s != null) {
        drawManager.drawEntity(s, s.getPositionX(), s.getPositionY());
      }
    }
  }

  private void drawSpecialShip() {
    if (this.enemyShipSpecial != null) {
      drawManager.drawEntity(
          this.enemyShipSpecial,
          this.enemyShipSpecial.getPositionX(),
          this.enemyShipSpecial.getPositionY());
    }
  }

  private void drawEnemies() {
    enemyShipFormation.draw();
  }

  private void drawBullets() {
    for (final Bullet bullet : this.bullets) {
      drawManager.drawEntity(bullet, bullet.getPositionX(), bullet.getPositionY());
    }
  }

  private void drawItems() {
    for (final Item item : this.items) {
      drawManager.drawEntity(item, item.getPositionX(), item.getPositionY());
    }
  }

  private void drawHUD() {
    drawManager.drawScore(this, state.getScore());
    drawManager.drawLives(this, state.getLivesRemaining(), state.isCoop());
    drawManager.drawCoins(this, state.getCoins());
    drawManager.drawLevel(this, this.state.getLevel());
    drawManager.drawHorizontalLine(this, SEPARATION_LINE_HEIGHT - 1);
    drawManager.drawShipCount(this, enemyShipFormation.getShipCount());
  }

  private void drawCountdownIfNeeded() {
    if (!this.inputDelay.checkFinished()) {
      final int countdown =
          this.hasCountdownMessage
              ? (int) ((INPUT_DELAY - (System.currentTimeMillis() - this.gameStartTime)) / 1000)
              : (int)
                  ((INPUT_DELAY_NO_MESSAGE - (System.currentTimeMillis() - this.gameStartTime))
                      / 1000);
      drawManager.drawCountDown(this, this.state.getLevel(), countdown, this.bonusLife);
      drawManager.drawHorizontalLine(this, this.height / 2 - this.height / 12);
      drawManager.drawHorizontalLine(this, this.height / 2 + this.height / 12);
    }
  }

  private void drawHighScoreNoticeIfNeeded() {
    if (this.highScoreNotified
        && System.currentTimeMillis() - this.highScoreNoticeStartTime
            < HIGH_SCORE_NOTICE_DURATION) {
      drawManager.drawNewHighScoreNotice(this);
    }
  }

  private void drawAchievementToastsSection() {
    drawManager.drawAchievementToasts(
        this,
        (this.achievementManager != null)
            ? this.achievementManager.getActiveToasts()
            : java.util.Collections.emptyList()); // NOPMD
  }

  private void drawPauseMenuIfNeeded() {
    if (this.isPaused) {
      this.pauseMenuHandler.draw(this.drawManager, this);
    }
  }

  private void finishFrame() {
    drawManager.completeDrawing(this);
  }

  private void cleanBullets() {
    final Set<Bullet> recyclable = new HashSet<>();
    for (final Bullet bullet : this.bullets) {
      bullet.update();
      if (bullet.getPositionY() < SEPARATION_LINE_HEIGHT || bullet.getPositionY() > this.height) {
        recyclable.add(bullet);
      }
    }
    this.bullets.removeAll(recyclable);
    BulletPool.recycle(recyclable);
  }

  private void cleanItems() {
    final Set<Item> recyclableItems = new HashSet<>();
    for (final Item item : this.items) {
      item.update();
      if (item.getPositionY() > this.height) {
        recyclableItems.add(item);
      }
    }
    this.items.removeAll(recyclableItems);
    ItemPool.recycle(recyclableItems);
  }

  public final GameState getGameState() {
    return this.state;
  }
}
