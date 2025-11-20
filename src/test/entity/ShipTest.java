package entity;

import engine.Core;
import engine.Cooldown;
import engine.GameState;
import engine.DrawManager.SpriteType;

import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.Set;

import static engine.ItemEffect.ItemEffectType.*;
import static org.junit.jupiter.api.Assertions.*;

class ShipTest {

  /** Fake GameState for item effects */
  static class FakeGameState extends GameState {
    public FakeGameState() {
      super(1, 3, false);
    }

    public boolean triple = false;
    public int bulletSpeed = 1;
    public int tripleOffset = 5;

    @Override
    public boolean hasEffect(int idx, engine.ItemEffect.ItemEffectType type) {
      return triple && type == TRIPLESHOT;
    }

    @Override
    public Integer getEffectValue(int idx, engine.ItemEffect.ItemEffectType type) {
      if (type == BULLETSPEEDUP) return bulletSpeed;
      if (type == TRIPLESHOT) return tripleOffset;
      return null;
    }
  }

  /** ---------------- Movement tests ---------------- */
  @Test
  void testMoveRight() {
    Ship ship = new Ship(100, 200, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, null);

    int before = ship.getPositionX();
    ship.moveRight();

    assertEquals(before + ship.getSpeed(), ship.getPositionX());
  }

  @Test
  void testMoveLeft() {
    Ship ship = new Ship(100, 200, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, null);

    int before = ship.getPositionX();
    ship.moveLeft();

    assertEquals(before - ship.getSpeed(), ship.getPositionX());
  }

  /** ---------------- Speed / Type tests ---------------- */
  @Test
  void testShipTypeNormal() {
    Ship s = new Ship(0, 0, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, null);
    assertEquals(2, s.getSpeed());
    assertEquals(SpriteType.Ship1, s.getSpriteType());
  }

  @Test
  void testShipTypeBigShot() {
    Ship s = new Ship(0, 0, Entity.Team.PLAYER1, Ship.ShipType.BIG_SHOT, null);
    assertEquals(1, s.getSpeed()); // BASE_SPEED-1
    assertEquals(SpriteType.Ship2, s.getSpriteType());
  }

  @Test
  void testShipTypeDoubleShot() {
    Ship s = new Ship(0, 0, Entity.Team.PLAYER1, Ship.ShipType.DOUBLE_SHOT, null);
    assertEquals(1, s.getSpeed());
    assertEquals(SpriteType.Ship3, s.getSpriteType());
  }

  @Test
  void testShipTypeMoveFast() {
    Ship s = new Ship(0, 0, Entity.Team.PLAYER1, Ship.ShipType.MOVE_FAST, null);
    assertEquals(3, s.getSpeed()); // BASE_SPEED+1
    assertEquals(SpriteType.Ship4, s.getSpriteType());
  }

  /** ---------------- Player ID tests ---------------- */
  @Test
  void testPlayerIdSetter() {
    Ship s = new Ship(0, 0, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, null);

    s.setPlayerId(2);
    assertEquals(2, s.getPlayerId());
  }

  /** ---------------- addHit tests ---------------- */
  @Test
  void testAddHit() throws Exception {
    Ship s = new Ship(0, 0, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, null);

    s.addHit();
    s.addHit();

    var hitsField = Ship.class.getDeclaredField("hits");
    hitsField.setAccessible(true);
    assertEquals(2, hitsField.getInt(s));
  }

  /** ---------------- destroy / isDestroyed / update tests ---------------- */
  @Test
  void testDestroyMakesIsDestroyedTrue() {
    Ship s = new Ship(0, 0, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, null);

    s.destroy();
    assertTrue(s.isDestroyed());
  }

  @Test
  void testUpdateChangesSpriteWhenDestroyed() {
    Ship s = new Ship(0, 0, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, null);

    s.destroy();
    s.update();

    assertEquals(SpriteType.ShipDestroyed1, s.getSpriteType());
  }

  /** ---------------- Shooting tests ---------------- */
  @Test
  void testShootDefault() {
    Ship s = new Ship(50, 200, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, null);
    Set<Bullet> bullets = new HashSet<>();

    boolean result = s.shoot(bullets);

    assertTrue(result);
    assertEquals(1, bullets.size());
  }

  @Test
  void testShootDoubleShot() {
    Ship s = new Ship(50, 200, Entity.Team.PLAYER1, Ship.ShipType.DOUBLE_SHOT, null);
    Set<Bullet> bullets = new HashSet<>();

    s.shoot(bullets);

    assertEquals(2, bullets.size());
  }

  @Test
  void testShootTripleShot() {
    FakeGameState gs = new FakeGameState();
    gs.triple = true;
    gs.tripleOffset = 5;

    Ship s = new Ship(50, 200, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, gs);
    Set<Bullet> bullets = new HashSet<>();

    s.shoot(bullets);

    assertEquals(3, bullets.size());
  }

  @Test
  void testBulletSpeedMultiplier() {
    FakeGameState gs = new FakeGameState();
    gs.bulletSpeed = 3;

    Ship s = new Ship(50, 200, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, gs);
    Set<Bullet> bullets = new HashSet<>();

    s.shoot(bullets);

    int speed = bullets.iterator().next().getSpeed();
    assertEquals(-6 * 3, speed);
  }
}
