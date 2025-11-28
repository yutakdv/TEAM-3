package entity;

import engine.*; // NOPMD
import java.util.*;
import engine.DrawManager.SpriteType;

@SuppressWarnings("PMD.LawOfDemeter")
public class FormationShooting {
  private static final int BULLET_SPEED = 4;
  private static final double SHOOTING_VARIANCE = .2;

  private Cooldown shootingCooldown;
  private final List<EnemyShip> shooters;
  private final int shootingInterval;
  private final int shootingVariance;

  public FormationShooting(final int shootingInterval) {
    this.shootingInterval = shootingInterval;
    this.shootingVariance = (int) (shootingInterval * SHOOTING_VARIANCE);
    this.shooters = new ArrayList<>();
  }

  public void addShooter(final EnemyShip ship) {
    this.shooters.add(ship);
  }

  public void removeShooter(final EnemyShip ship) {
    this.shooters.remove(ship);
  }

  public void updateShooter(final int index, final EnemyShip newShooter) {
    if (index >= 0 && index < shooters.size()) {
      this.shooters.set(index, newShooter);
    }
  }

  public List<EnemyShip> getShooters() {
    return shooters;
  }

  public void shoot(final Set<Bullet> bullets) {
    if (this.shootingCooldown == null) {
      this.shootingCooldown = Core.getVariableCooldown(shootingInterval, shootingVariance);
      this.shootingCooldown.reset();
    }

    if (shooters.isEmpty() || !this.shootingCooldown.checkFinished()) {
      return;
    }

    this.shootingCooldown.reset();

    // Logic to pick a random shooter and fire
    final int index = (int) (Math.random() * shooters.size());
    final EnemyShip shooter = shooters.get(index);

    int bulletSpeed = BULLET_SPEED;
    final int bulletWidth = 6;
    final int bulletHeight = 10;
    final int spawnY = shooter.getPositionY() + shooter.getHeight();

    if (shooter.getSpriteType() == SpriteType.EnemyShipB1
        || shooter.getSpriteType() == SpriteType.EnemyShipB2) {
      bulletSpeed = BULLET_SPEED * 2;
    }

    if (shooter.getSpriteType() == SpriteType.EnemyShipC1
        || shooter.getSpriteType() == SpriteType.EnemyShipC2) {
      createSplitBullets(bullets, shooter, spawnY, bulletSpeed, bulletWidth, bulletHeight);
    } else {
      final Bullet b =
          BulletPool.getBullet(
              shooter.getPositionX() + shooter.getWidth() / 2,
              spawnY,
              bulletSpeed,
              bulletWidth,
              bulletHeight,
              Entity.Team.ENEMY);
      bullets.add(b);
    }
  }

  private void createSplitBullets(
      final Set<Bullet> bullets,
      final EnemyShip shooter,
      final int spawnY,
      final int speed,
      final int w,
      final int h) {
    final int offset = 6;
    bullets.add(
        BulletPool.getBullet(
            shooter.getPositionX() + shooter.getWidth() / 2 - offset,
            spawnY,
            speed,
            w,
            h,
            Entity.Team.ENEMY));
    bullets.add(
        BulletPool.getBullet(
            shooter.getPositionX() + shooter.getWidth() / 2 + offset,
            spawnY,
            speed,
            w,
            h,
            Entity.Team.ENEMY));
  }
}
