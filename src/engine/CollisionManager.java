package engine;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.*; // NOPMD

@SuppressWarnings("PMD.LawOfDemeter")
public class CollisionManager {
  private final GameState gameState;
  private final DrawManager drawManager;
  private final Logger logger;

  public CollisionManager(final GameState gameState, final DrawManager drawManager) {
    this.gameState = gameState;
    this.drawManager = drawManager;
    this.logger = Core.getLogger();
  }

  public boolean processCollisions(
      final Set<Bullet> bullets,
      final Ship[] ships,
      final EnemyShipFormation enemyShipFormation,
      final EnemyShip enemyShipSpecial,
      final Set<Item> items,
      final Cooldown specialExplosionCooldown,
      final boolean levelFinished) {

    boolean playerHit = false;
    final Set<Bullet> recyclable = new HashSet<>();

    for (final Bullet bullet : bullets) {
      if (bullet.getSpeed() > 0) {
        // [Case 1] Enemy bullet vs Player Ships
        playerHit |= checkPlayerCollision(bullet, ships, recyclable, levelFinished);
      } else {
        // [Case 2] Player bullet vs Enemies
        checkEnemyCollision(
            bullet,
            enemyShipFormation,
            enemyShipSpecial,
            items,
            specialExplosionCooldown,
            recyclable);
      }
    }

    bullets.removeAll(recyclable);
    BulletPool.recycle(recyclable);

    return playerHit;
  }

  private boolean checkPlayerCollision(
      final Bullet bullet,
      final Ship[] ships,
      final Set<Bullet> recyclable,
      final boolean levelFinished) {

    boolean hitDetected = false;

    for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
      final Ship ship = ships[p];
      if (ship != null && !ship.isDestroyed() && checkCollision(bullet, ship) && !levelFinished) {

        recyclable.add(bullet);

        // Trigger visual effects and update state
        drawManager.triggerExplosion(
            ship.getPositionX(), ship.getPositionY(), false, gameState.getLivesRemaining() == 1);

        ship.addHit();
        ship.destroy();
        SoundManager.ingameeffect("sound/explosion.wav");

        gameState.decLife(p); // Decrement shared/team lives

        // Update UI states via DrawManager (optional here, usually done in Screen update)
        drawManager.setLastLife(gameState.getLivesRemaining() == 1);
        drawManager.setDeath(gameState.getLivesRemaining() == 0);
        if (logger.isLoggable(Level.INFO)) {
          logger.info(
              "Hit on player " + (p + 1) + ", team lives now: " + gameState.getLivesRemaining());
        }

        hitDetected = true;
        break; // One bullet hits one ship
      }
    }
    return hitDetected;
  }

  private void checkEnemyCollision( // NOPMD
      final Bullet bullet,
      final EnemyShipFormation enemyShipFormation,
      final EnemyShip enemyShipSpecial,
      final Set<Item> items,
      final Cooldown specialExplosionCooldown,
      final Set<Bullet> recyclable) {

    // Identify which player fired this bullet
    final int ownerId = bullet.getOwnerPlayerId();
    final int pIdx = (ownerId == 2) ? 1 : 0; // 0 for P1, 1 for P2

    boolean bulletAbsorbed = false;

    // 1. Check Formation Enemies
    final boolean finalShip = enemyShipFormation.lastShip();
    for (final EnemyShip enemyShip : enemyShipFormation) {
      if (!enemyShip.isDestroyed() && checkCollision(bullet, enemyShip)) {
        recyclable.add(bullet);
        bulletAbsorbed = true;
        enemyShip.hit();

        if (enemyShip.isDestroyed()) {
          handleEnemyDestruction(enemyShip, pIdx, finalShip, items);
          enemyShipFormation.destroy(enemyShip);
        }
        break;
      }
    }

    // 2. Check Special Enemy (if bullet wasn't used yet)
    if (!bulletAbsorbed
        && enemyShipSpecial != null
        && !enemyShipSpecial.isDestroyed()
        && checkCollision(bullet, enemyShipSpecial)) {

      handleSpecialEnemyDestruction(enemyShipSpecial, pIdx, specialExplosionCooldown);
      recyclable.add(bullet);
    }
  }

  private void handleEnemyDestruction(
      final EnemyShip enemyShip, final int pIdx, final boolean finalShip, final Set<Item> items) {

    final int points = enemyShip.getPointValue();
    gameState.addCoins(pIdx, enemyShip.getCoinValue());

    drawManager.triggerExplosion(
        enemyShip.getPositionX(), enemyShip.getPositionY(), true, finalShip);
    gameState.addScore(pIdx, points);
    gameState.incShipsDestroyed(pIdx);

    // Drop Item Logic
    final Item drop = Core.getItemManager().obtainDrop(enemyShip);
    if (drop != null) {
      items.add(drop);
      if (logger.isLoggable(Level.INFO)) {
        logger.info(
            "Spawned " + drop.getType() + " at " + drop.getPositionX() + "," + drop.getPositionY());
      }
    }

    SoundManager.ingameeffect("sound/invaderkilled.wav");
    logger.info("Hit on enemy ship.");
  }

  private void handleSpecialEnemyDestruction(
      final EnemyShip enemyShipSpecial, final int pIdx, final Cooldown specialExplosionCooldown) {

    final int points = enemyShipSpecial.getPointValue();
    gameState.addCoins(pIdx, enemyShipSpecial.getCoinValue());
    gameState.addScore(pIdx, points);
    gameState.incShipsDestroyed(pIdx);

    enemyShipSpecial.destroy();
    SoundManager.stop();
    SoundManager.ingameeffect("sound/explosion.wav");

    drawManager.triggerExplosion(
        enemyShipSpecial.getPositionX(), enemyShipSpecial.getPositionY(), true, true);

    specialExplosionCooldown.reset();
  }

  public void processItemPickups(final Set<Item> items, final Ship[] ships) { // NOPMD
    final Set<Item> collected = new HashSet<>();

    for (final Item item : items) {
      for (final Ship ship : ships) {
        if (ship == null) {
          continue;
        }
        // 여기서 내부의 checkCollision을 재사용합니다.
        if (checkCollision(item, ship) && !collected.contains(item)) {
          collected.add(item);
          if (logger.isLoggable(Level.INFO)) {
            logger.info("Player " + ship.getPlayerId() + " picked up item: " + item.getType());
          }
          SoundManager.ingameeffect("sound/hover.wav");

          // GameState는 이미 CollisionManager가 가지고 있습니다.
          item.applyEffect(this.gameState, ship.getPlayerId());
        }
      }
    }

    items.removeAll(collected);
    ItemPool.recycle(collected);
  }

  private boolean checkCollision(final Entity a, final Entity b) { // NOPMD - false positive
    final int centerAX = a.getPositionX() + a.getWidth() / 2;
    final int centerAY = a.getPositionY() + a.getHeight() / 2;
    final int centerBX = b.getPositionX() + b.getWidth() / 2;
    final int centerBY = b.getPositionY() + b.getHeight() / 2;
    final int maxDistanceX = a.getWidth() / 2 + b.getWidth() / 2;
    final int maxDistanceY = a.getHeight() / 2 + b.getHeight() / 2;
    final int distanceX = Math.abs(centerAX - centerBX);
    final int distanceY = Math.abs(centerAY - centerBY);
    return distanceX < maxDistanceX && distanceY < maxDistanceY;
  }
}
