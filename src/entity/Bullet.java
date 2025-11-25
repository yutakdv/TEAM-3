package entity;

import java.awt.Color;

import engine.DrawManager.SpriteType;

/**
 * Implements a bullet that moves vertically up or down.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public class Bullet extends Entity { // NOPMD - DataClass

  /** Speed of the bullet, positive or negative depending on direction - positive is down. */
  private int speed;

  /** 2P mode: id number to specifying who fired the bullet - 0 = enemy, 1 = P1, 2 = P2 */
  private int ownerPlayerId;

  // standardised for DrawManager scaling
  int playerId;

  /**
   * Constructor, establishes the bullet's properties.
   *
   * @param positionX Initial position of the bullet in the X axis.
   * @param positionY Initial position of the bullet in the Y axis.
   * @param speed Speed of the bullet, positive or negative depending on direction - positive is
   *     down.
   */
  // change the constructor to receive width and height
  public Bullet(final int positionX, final int positionY, final int speed) {
    super(positionX, positionY, 0, 0, Color.WHITE);
    this.speed = speed;
  }

  public void initialize(
      final int positionX,
      final int positionY,
      final int speed,
      final int width,
      final int height,
      final Team team) {
    this.setPositionX(positionX - width / 2);
    this.setPositionY(positionY);
    this.setSpeed(speed);
    this.setSize(width, height);
    this.setTeam(team);
    this.setSprite();
  }

  // reset the size when recycling bullets
  public final void setSize(final int width, final int height) {
    this.width = width;
    this.height = height;
  }

  /** Sets correct sprite for the bullet, based on speed. */
  public final void setSprite() {
    if (this.speed < 0) {
      this.spriteType = SpriteType.Bullet; // player bullet fired, team remains NEUTRAL
    } else {
      this.spriteType = SpriteType.EnemyBullet; // enemy fired bullet
    }
  }

  /** Updates the bullet's position. */
  public final void update() {
    this.positionY += this.speed;
  }

  /**
   * Setter of the speed of the bullet.
   *
   * @param speed New speed of the bullet.
   */
  public final void setSpeed(final int speed) {
    this.speed = speed;
  }

  /**
   * Getter for the speed of the bullet.
   *
   * @return Speed of the bullet.
   */
  public final int getSpeed() {
    return this.speed;
  }

  // 2P mode: adding owner API, standardised player API
  public final int getOwnerPlayerId() {
    return ownerPlayerId;
  }

  public final void setOwnerPlayerId(final int ownerPlayerId) {
    this.ownerPlayerId = ownerPlayerId;
  }

  public int getPlayerId() {
    return this.playerId;
  }
}
