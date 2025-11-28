package entity;

import engine.GameState;
import engine.DrawManager.SpriteType;

import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ShipTest {

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
    assertEquals(3, s.getSpeed()); // BASE_SPEED-1
    assertEquals(SpriteType.Ship2, s.getSpriteType());
  }

  @Test
  void testShipTypeDoubleShot() {
    Ship s = new Ship(0, 0, Entity.Team.PLAYER1, Ship.ShipType.DOUBLE_SHOT, null);
    assertEquals(4, s.getSpeed());
    assertEquals(SpriteType.Ship3, s.getSpriteType());
  }

  @Test
  void testShipTypeMoveFast() {
    Ship s = new Ship(0, 0, Entity.Team.PLAYER1, Ship.ShipType.MOVE_FAST, null);
    assertEquals(5, s.getSpeed()); // BASE_SPEED+1
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
}
