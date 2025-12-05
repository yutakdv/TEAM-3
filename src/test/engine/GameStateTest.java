package engine;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** GameState 단위 테스트 — 최신 구조(LifeManager, CoinManager, StatsManager, EffectManager) 반영 */
public class GameStateTest {

  /**
   * --------------------------------------------- Helper: coinManager.coins 반영 (reflection)
   * ---------------------------------------------
   */
  private void setCoins(int value) throws Exception {
    var coinsField = CoinManager.class.getDeclaredField("coins");
    coinsField.setAccessible(true);
    coinsField.setInt(null, value);
  }

  /**
   * ====================================================== Constructor Tests
   * ======================================================
   */
  @Test
  void testConstructorSinglePlayer() throws Exception {
    GameState gs = new GameState(1, 100, 3, 10, 5);

    assertFalse(gs.isCoop());
    assertEquals(1, gs.getLevel());
    assertEquals(3, gs.getLivesRemaining());
  }

  @Test
  void testConstructorCoopMode() {
    GameState gs = new GameState(1, 3, true);

    assertTrue(gs.isCoop());
    assertTrue(gs.isSharedLives());
    assertEquals(3 * GameState.NUM_PLAYERS, gs.getTeamLives());
  }

  /**
   * ====================================================== Score / Stats
   * ======================================================
   */
  @Test
  void testAddScoreNormal() {
    GameState gs = new GameState(1, 3, false);

    gs.addScore(0, 50);
    assertEquals(50, gs.getScore(0));
  }

  @Test
  void testIncBulletsShot() {
    GameState gs = new GameState(1, 3, false);

    gs.incBulletsShot(0);
    gs.incBulletsShot(0);

    assertEquals(2, gs.getBulletsShot(0));
  }

  @Test
  void testIncShipsDestroyed() {
    GameState gs = new GameState(1, 3, false);
    gs.incShipsDestroyed(0);

    assertEquals(1, gs.getShipsDestroyed(0));
  }

  /**
   * ====================================================== Coin System
   * ======================================================
   */
  @Test
  void testAddCoins() throws Exception {
    GameState gs = new GameState(1, 3, false);
    setCoins(10);

    gs.addCoins(0, 5);
    assertEquals(15, gs.getCoins());
  }

  @Test
  void testSpendCoinsSuccess() throws Exception {
    GameState gs = new GameState(1, 3, false);
    setCoins(20);

    assertTrue(gs.spendCoins(1, 15));
    assertEquals(5, gs.getCoins());
  }

  @Test
  void testSpendCoinsFail() throws Exception {
    GameState gs = new GameState(1, 3, false);
    setCoins(5);

    assertFalse(gs.spendCoins(1, 10));
    assertEquals(5, gs.getCoins());
  }

  /**
   * ====================================================== Life System
   * ======================================================
   */
  @Test
  void testDecLifeSinglePlayer() {
    GameState gs = new GameState(1, 3, false);
    gs.decLife(0);

    assertEquals(2, gs.getLivesRemaining());
  }

  @Test
  void testDecLifeSharedLives() {
    GameState gs = new GameState(1, 3, true);
    int before = gs.getTeamLives();

    gs.decLife(0);

    assertEquals(before - 1, gs.getTeamLives());
  }

  @Test
  void testAddLifeSinglePlayer() {
    GameState gs = new GameState(1, 3, false);

    gs.addLife(0, 2);
    assertEquals(5, gs.getLivesRemaining());
  }

  /**
   * ====================================================== Level
   * ======================================================
   */
  @Test
  void testNextLevel() {
    GameState gs = new GameState(1, 1, false);
    gs.nextLevel();

    assertEquals(2, gs.getLevel());
  }
}
