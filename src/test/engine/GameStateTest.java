package engine;

import org.junit.jupiter.api.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GameStateTest {

  /** Dummy Cooldown to replace Core.getCooldown() */
  static class DummyCooldown extends Cooldown {
    private boolean finished = false;

    public DummyCooldown() {
      super(1000);
    }

    public void forceFinish() {
      try {
        var field = Cooldown.class.getDeclaredField("lastUpdate");
        field.setAccessible(true);
        field.setLong(this, System.currentTimeMillis() - 2000); // duration 보다 훨씬 이전
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /** ---------- Constructor Tests ---------- * */
  @Test
  @DisplayName("Constructor: coop=false initializes correct values")
  void testConstructorSinglePlayer() {
    GameState gs = new GameState(1, 100, 3, 10, 5, 20);

    assertFalse(gs.isCoop());
    assertFalse(gs.isSharedLives());
    assertEquals(1, gs.getLevel());
    assertEquals(3, gs.getLivesRemaining());
    assertEquals(20, gs.getCoins());
  }

  @Test
  @DisplayName("Constructor: coop=true uses shared lives")
  void testConstructorCoopMode() {
    GameState gs = new GameState(1, 3, true, 50);

    assertTrue(gs.isCoop());
    assertTrue(gs.isSharedLives());
    assertEquals(3 * GameState.NUM_PLAYERS, gs.getTeamLives());
  }

  /** ---------- Score Tests ---------- * */
  @Test
  void testAddScoreNormal() {
    GameState gs = new GameState(1, 3, false, 10);

    gs.addScore(0, 50);
    assertEquals(50, gs.getScore(0));
  }

  @Test
  void testAddScoreWithEffectMultiplier() {
    GameState gs = new GameState(1, 3, false, 10);

    gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);
    gs.addScore(0, 10);

    assertEquals(20, gs.getScore(0)); // x2 boost
  }

  /** ---------- Bullet / Ship Counters ---------- * */
  @Test
  void testIncBulletsShot() {
    GameState gs = new GameState(1, 3, false, 10);
    gs.incBulletsShot(0);
    gs.incBulletsShot(0);

    assertEquals(2, gs.getBulletsShot(0));
    assertEquals(2, gs.getBulletsShot());
  }

  @Test
  void testIncShipsDestroyed() {
    GameState gs = new GameState(1, 3, false, 10);
    gs.incShipsDestroyed(0);

    assertEquals(1, gs.getShipsDestroyed(0));
  }

  /** ---------- Coin System ---------- * */
  @Test
  void testAddCoins() {
    GameState gs = new GameState(1, 3, false, 10);

    gs.addCoins(0, 5);
    assertEquals(15, gs.getCoins());
  }

  @Test
  void testSpendCoinsSuccess() {
    GameState gs = new GameState(1, 3, false, 20);

    assertTrue(gs.spendCoins(0, 10));
    assertEquals(10, gs.getCoins());
  }

  @Test
  void testSpendCoinsFailNotEnough() {
    GameState gs = new GameState(1, 3, false, 5);

    assertFalse(gs.spendCoins(0, 10));
    assertEquals(5, gs.getCoins());
  }

  /** ---------- Lives / Shared Lives ---------- * */
  @Test
  void testDecLifeSinglePlayer() {
    GameState gs = new GameState(1, 3, false, 10);

    gs.decLife(0);
    assertEquals(2, gs.getLivesRemaining());
  }

  @Test
  void testDecLifeSharedLives() {
    GameState gs = new GameState(1, 3, true, 10);

    int before = gs.getTeamLives();
    gs.decLife(0);

    assertEquals(before - 1, gs.getTeamLives());
  }

  @Test
  void testAddLifeSinglePlayer() {
    GameState gs = new GameState(1, 3, false, 10);

    gs.addLife(0, 2);
    assertEquals(5, gs.getLivesRemaining());
  }

  @Test
  void testAddLifeShared() {
    GameState gs = new GameState(1, 3, true, 10);

    int before = gs.getTeamLives(); // = 3 * 2 = 6

    gs.addLife(0, 2); // teamLivesCap = 6 이므로 증가 안됨

    assertEquals(before, gs.getTeamLives());
  }

  @Test
  void testTeamAlive() {
    GameState gs = new GameState(1, 1, false, 10);
    assertTrue(gs.teamAlive());

    gs.decLife(0);
    assertFalse(gs.teamAlive());
  }

  /** ---------- Level Progression ---------- * */
  @Test
  void testNextLevel() {
    GameState gs = new GameState(1, 1, false, 10);

    gs.nextLevel();
    assertEquals(2, gs.getLevel());
  }

  /** ---------- Effect System ---------- * */
  @Test
  void testAddEffectStartsEffect() {
    GameState gs = new GameState(1, 1, false, 10);

    gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);
    assertTrue(gs.hasEffect(0, ItemEffect.ItemEffectType.SCOREBOOST));
  }

  @Test
  void testGetEffectValue() {
    GameState gs = new GameState(1, 1, false, 10);

    gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 3, 1);
    assertEquals(3, gs.getEffectValue(0, ItemEffect.ItemEffectType.SCOREBOOST));
  }

  @Test
  void testEffectExpiresAfterUpdate() throws Exception {
    GameState gs = new GameState(1, 1, false, 10);

    gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);

    // 1) playerEffects private 필드 가져오기
    var field = GameState.class.getDeclaredField("playerEffects");
    field.setAccessible(true);

    @SuppressWarnings("unchecked")
    Map<Integer, Map<ItemEffect.ItemEffectType, ?>> effects =
        (Map<Integer, Map<ItemEffect.ItemEffectType, ?>>) field.get(gs);

    // 2) player 0의 effect map 가져오기
    Map<ItemEffect.ItemEffectType, ?> map = effects.get(0);

    // 3) effect 객체 가져오기
    Object effect = map.get(ItemEffect.ItemEffectType.SCOREBOOST);
    assertNotNull(effect);

    // 4) effect.cooldown 가져오기
    var cField = effect.getClass().getDeclaredField("cooldown");
    cField.setAccessible(true);
    Cooldown cd = (Cooldown) cField.get(effect);

    // 5) Cooldown.time 값을 과거로 설정 → 강제 만료
    var tField = Cooldown.class.getDeclaredField("time");
    tField.setAccessible(true);
    tField.setLong(cd, System.currentTimeMillis() - 999999L);

    // 6) updateEffects 호출 → effect 삭제 여부 확인
    gs.updateEffects();
    assertFalse(gs.hasEffect(0, ItemEffect.ItemEffectType.SCOREBOOST));
  }

  @Test
  void testClearEffects() {
    GameState gs = new GameState(1, 1, false, 10);

    gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);
    gs.clearEffects(0);

    assertFalse(gs.hasEffect(0, ItemEffect.ItemEffectType.SCOREBOOST));
  }

  @Test
  void testClearAllEffects() {
    GameState gs = new GameState(1, 1, false, 10);

    gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);
    gs.addEffect(1, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);

    gs.clearAllEffects();

    assertFalse(gs.hasEffect(0, ItemEffect.ItemEffectType.SCOREBOOST));
    assertFalse(gs.hasEffect(1, ItemEffect.ItemEffectType.SCOREBOOST));
  }
}
