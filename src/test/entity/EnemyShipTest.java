package entity;

import engine.GameSettings;
import engine.DrawManager.SpriteType;
import org.junit.jupiter.api.*;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

public class EnemyShipTest {

  /** ---------- Constructor tests ---------- */
  @Test
  void testConstructor_TypeA() {
    EnemyShip ship = new EnemyShip(10, 20, SpriteType.EnemyShipA1);

    assertEquals(10, ship.getPositionX());
    assertEquals(20, ship.getPositionY());
    assertEquals(2, ship.getHealth());
    assertEquals(10, ship.getPointValue());
    assertEquals(2, ship.getCoinValue());
  }

  @Test
  void testConstructor_TypeB() {
    EnemyShip ship = new EnemyShip(10, 20, SpriteType.EnemyShipB1);

    assertEquals(1, ship.getHealth());
    assertEquals(20, ship.getPointValue());
    assertEquals(3, ship.getCoinValue());
  }

  @Test
  void testConstructor_SpecialShip() {
    EnemyShip ship = new EnemyShip();

    assertEquals(SpriteType.EnemyShipSpecial, ship.getSpriteType());
    assertEquals(100, ship.getPointValue());
    assertEquals(10, ship.getCoinValue());
    assertEquals(1, ship.getHealth());
  }

  /** ---------- changeShip() tests ---------- */
  @Test
  void testChangeShip_ModifiesStats() {
    EnemyShip ship = new EnemyShip(10, 10, SpriteType.EnemyShipA1);
    GameSettings.ChangeData data = new GameSettings.ChangeData(0, 0, 2, 2);

    ship.changeShip(data);

    assertEquals(4, ship.getHealth()); // 2 * 2
    assertEquals(20, ship.getPointValue()); // 10 * 2
    assertEquals(4, ship.getCoinValue()); // 2 * 2
  }

  /** ---------- move() tests ---------- */
  @Test
  void testMove() {
    EnemyShip ship = new EnemyShip(0, 0, SpriteType.EnemyShipA1);
    ship.move(5, 10);

    assertEquals(5, ship.getPositionX());
    assertEquals(10, ship.getPositionY());
  }

  /** ---------- update() sprite animation test ---------- */
  @Test
  void testUpdate_SpriteSwitch() throws Exception {
    EnemyShip ship = new EnemyShip(0, 0, SpriteType.EnemyShipA1);

    // cooldown 초기화 조작
    var cd = ship.getClass().getDeclaredField("animationCooldown");
    cd.setAccessible(true);
    var cooldownObj = cd.get(ship);

    var timeField = cooldownObj.getClass().getDeclaredField("time");
    timeField.setAccessible(true);

    // 강제로 cooldown 완료 상태 만들기
    timeField.setLong(cooldownObj, System.currentTimeMillis() - 99999);

    ship.update();

    // EnemyShipA1 → EnemyShipA2
    assertEquals(SpriteType.EnemyShipA2, ship.getSpriteType());
  }

  /** ---------- hit() tests ---------- */
  @Test
  void testHit_ReduceHealth() {
    EnemyShip ship = new EnemyShip(0, 0, SpriteType.EnemyShipA1);
    int h = ship.getHealth();

    ship.getDamage(1);
    assertEquals(h - 1, ship.getHealth());
    assertFalse(ship.isDestroyed());
  }

  @Test
  void testHit_Destruction() {
    EnemyShip ship = new EnemyShip(0, 0, SpriteType.EnemyShipB1); // health=1
    ship.getDamage(1);

    assertTrue(ship.isDestroyed());
    assertEquals(SpriteType.Explosion, ship.getSpriteType());
    assertEquals(255, ship.getColor().getAlpha());
  }

  /** ---------- getDamage() tests ---------- */
  @Test
  void testGetDamage_KillsEnemy() {
    EnemyShip ship = new EnemyShip(0, 0, SpriteType.EnemyShipB1); // hp=1

    int result = ship.getDamage(1);
    assertEquals(0, result);
    assertTrue(ship.isDestroyed());
    assertEquals(SpriteType.Explosion, ship.getSpriteType());
  }

  @Test
  void testGetDamage_DamagedButAlive() {
    EnemyShip ship = new EnemyShip(0, 0, SpriteType.EnemyShipA1); // hp=2

    ship.getDamage(1);
    assertEquals(1, ship.getHealth());
    assertFalse(ship.isDestroyed());
    assertEquals(178, ship.getColor().getAlpha()); // alpha logic 체크
  }

  /** ---------- destroy() tests ---------- */
  @Test
  void testDestroy() {
    EnemyShip ship = new EnemyShip(0, 0, SpriteType.EnemyShipA1);
    ship.destroy();

    assertTrue(ship.isDestroyed());
    assertEquals(SpriteType.Explosion, ship.getSpriteType());
  }

  /** ---------- getCoinValue() tests ---------- */
  @Test
  void testGetCoinValue() {
    EnemyShip ship = new EnemyShip(0, 0, SpriteType.EnemyShipC1);

    assertEquals(5, ship.getCoinValue());
  }
}
