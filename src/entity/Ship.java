package entity;

import java.awt.Color;
import java.util.Set;

import engine.Cooldown;
import engine.Core;
import engine.GameState;
import engine.DrawManager.SpriteType;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implements a ship, to be controlled by the player.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public class Ship extends Entity {

  private static final Logger LOGGER = Core.getLogger();

  /** Bullet Variables * */
  // default bullet variables
  private static final int BASE_BULLET_SPEED = -6;

  private static final int BASE_SHOOTING_INTERVAL = 750;
  private static final int BASE_BULLET_WIDTH = 6; // 3 * 2
  private static final int BASE_BULLET_HEIGHT = 10;
  // special bullet variables
  private static final int DOUBLE_SHOT_OFFSET = 6;

  /** Ship Variables * */
  private static final int BASE_SPEED = 2;

  private static final int SHIP_WIDTH = 26; // 13 * 2
  private static final int SHIP_HEIGHT = 16;
  private static final int DESTRUCTION_COOLDOWN = 1000;

  /*
   * Initializes ship properties based on ship type.
   *
   * <p>NORMAL = Bronze BIG_SHOT = Silver DOUBLE_SHOT = Gold MOVE_FAST = Platinum
   */

  /** Types of ships. */
  public enum ShipType {
    NORMAL(BASE_SPEED, BASE_SHOOTING_INTERVAL, SpriteType.Ship1) {
      void shoot(final Ship ship, final Set<Bullet> bullets, final int centerX, final int bulletY) {
        ship.addBullet(bullets, centerX, bulletY);
      }
    },
    BIG_SHOT(3, 700, SpriteType.Ship2) {
      void shoot(final Ship ship, final Set<Bullet> bullets, final int centerX, final int bulletY) {
        ship.addBullet(bullets, centerX, bulletY);
      }
    },
    DOUBLE_SHOT(4, 700, SpriteType.Ship3) {
      void shoot(final Ship ship, final Set<Bullet> bullets, final int centerX, final int bulletY) {
        ship.addBullet(bullets, centerX - DOUBLE_SHOT_OFFSET, bulletY);
        ship.addBullet(bullets, centerX + DOUBLE_SHOT_OFFSET, bulletY);
      }
    },
    MOVE_FAST(5, 500, SpriteType.Ship4) {
      void shoot(final Ship ship, final Set<Bullet> bullets, final int centerX, final int bulletY) {
        ship.addBullet(bullets, centerX - DOUBLE_SHOT_OFFSET, bulletY);
        ship.addBullet(bullets, centerX + DOUBLE_SHOT_OFFSET, bulletY);
      }
    };

    private final int moveSpeed;
    private final int shootingInterval;
    private final SpriteType spriteType;

    ShipType(final int moveSpeed, final int shootingInterval, final SpriteType spriteType) {
      this.moveSpeed = moveSpeed;
      this.shootingInterval = shootingInterval;
      this.spriteType = spriteType;
    }

    void applyStats(final Ship ship) {
      ship.moveSpeed = this.moveSpeed;
      ship.shootingInterval = this.shootingInterval;
      ship.spriteType = this.spriteType;
      ship.bulletSpeed = BASE_BULLET_SPEED;
      ship.bulletWidth = BASE_BULLET_WIDTH;
      ship.bulletHeight = BASE_BULLET_HEIGHT;
    }

    abstract void shoot(Ship ship, Set<Bullet> bullets, int centerX, int bulletY);
  }

  /** Game state and Ship type * */
  final GameState gameState; // NOPMD

  private final ShipType type;

  // Ship properties (vary by type)
  private int moveSpeed = BASE_SPEED;
  private int bulletSpeed = BASE_BULLET_SPEED;
  private int shootingInterval = BASE_SHOOTING_INTERVAL;
  private int bulletWidth = BASE_BULLET_WIDTH;
  private int bulletHeight = BASE_BULLET_HEIGHT;

  /** Cooldowns */
  private final Cooldown shootingCooldown;

  private final Cooldown destructionCooldown;

  // Identify player in index: 0 = P1, 1 = P2
  private int playerIndex;

  private int hits; // NOPMD

  /**
   * Constructor, establishes the ship's properties.
   *
   * @param positionX Initial position of the ship in the X axis.
   * @param positionY Initial position of the ship in the Y axis.
   * @param team Player team (null defaults to PLAYER1)
   * @param type Ship type (null defaults to NORMAL)
   * @param gameState Game state reference (can be null)
   */
  public Ship(
      final int positionX,
      final int positionY,
      final Team team,
      final ShipType type,
      final GameState gameState) {
    super(positionX, positionY, SHIP_WIDTH, SHIP_HEIGHT, Color.GREEN);

    this.gameState = gameState;
    this.type = (type != null) ? type : ShipType.NORMAL;
    this.spriteType = SpriteType.Ship1;

    this.type.applyStats(this);

    this.shootingCooldown = Core.getCooldown(this.shootingInterval);
    this.destructionCooldown = Core.getCooldown(DESTRUCTION_COOLDOWN);

    // apply entity
    final Team playerID = (team != null) ? team : Team.PLAYER1;
    this.setTeam(playerID);
    this.playerIndex = (playerID == Team.PLAYER1) ? 0 : (playerID == Team.PLAYER2) ? 1 : 0;

    this.hits = 0;
  }

  /** Moves the ship speed uni ts right, or until the right screen border is reached. */
  public final void moveRight() {
    this.positionX += this.moveSpeed;
  }

  /** Moves the ship speed units left, or until the left screen border is reached. */
  public final void moveLeft() {
    this.positionX -= this.moveSpeed;
  }

  /**
   * Shoots a bullet based on ship type and active effects.
   *
   * @param bullets List of bullets on screen, to add the new bullet.
   * @return True if shooting was successful, false if on cooldown
   */
  public final boolean shoot(final Set<Bullet> bullets) {

    if (!this.shootingCooldown.checkFinished()) {
      return false;
    }

    this.shootingCooldown.reset();
    if (LOGGER.isLoggable(Level.FINE)) {
      LOGGER.fine("[Ship] Shooting :" + this.type);
    }
    final int bulletX = positionX + this.width / 2;
    final int bulletY = this.positionY - this.bulletHeight;

    // Default shooting based on ship type
    this.type.shoot(this, bullets, bulletX, bulletY);
    return true;
  }

  /** Updates status of the ship. */
  public final void update() {
    if (this.destructionCooldown.checkFinished()) {
      resetToNormalSprite();
    } else {
      setDestroyedSprite();
    }
  }

  private void setDestroyedSprite() {
    switch (this.spriteType) {
      case Ship1 -> this.spriteType = SpriteType.ShipDestroyed1;
      case Ship2 -> this.spriteType = SpriteType.ShipDestroyed2;
      case Ship3 -> this.spriteType = SpriteType.ShipDestroyed3;
      case Ship4 -> this.spriteType = SpriteType.ShipDestroyed4;
      default -> {}
    }
  }

  private void resetToNormalSprite() {
    switch (this.spriteType) {
      case ShipDestroyed1 -> this.spriteType = SpriteType.Ship1;
      case ShipDestroyed2 -> this.spriteType = SpriteType.Ship2;
      case ShipDestroyed3 -> this.spriteType = SpriteType.Ship3;
      case ShipDestroyed4 -> this.spriteType = SpriteType.Ship4;
      default -> {}
    }
  }

  /** Switches the ship to its destroyed state. */
  public final void destroy() {
    this.destructionCooldown.reset();
  }

  /**
   * Checks if the ship is destroyed.
   *
   * @return True if the ship is currently destroyed.
   */
  public final boolean isDestroyed() {
    return !this.destructionCooldown.checkFinished();
  }

  /**
   * Getter for the ship's speed.
   *
   * @return Speed of the ship.
   */
  public final int getSpeed() {
    return this.moveSpeed;
  }

  // 2P mode: adding playerIndex getter and setter
  public final int getPlayerId() {
    return this.playerIndex + 1;
  }

  public void setPlayerId(final int id) {
    this.playerIndex = id - 1;
  }

  /** Creates and adds a bullet to the game. */
  void addBullet(final Set<Bullet> bullets, final int x, final int y) {
    final Bullet bullet =
        BulletPool.getBullet(
            x, y, this.bulletSpeed, this.bulletWidth, this.bulletHeight, this.getTeam());
    bullet.setOwnerPlayerId(this.getPlayerId()); // NOPMD
    bullets.add(bullet);
  }

  public void addHit() {
    this.hits++;
  }
}
