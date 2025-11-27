package engine;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** GameState 단위 테스트 — 최신 구조(LifeManager, CoinManager, StatsManager, EffectManager) 반영 */
public class GameStateTest {

    /** ---------------------------------------------
     * Helper: coinManager.coins 반영 (reflection)
     * --------------------------------------------- */
    private void setCoins(GameState gs, int value) throws Exception {
        var coinField = GameState.class.getDeclaredField("coinManager");
        coinField.setAccessible(true);
        Object coinManager = coinField.get(gs);

        var coinsField = coinManager.getClass().getDeclaredField("coins");
        coinsField.setAccessible(true);
        coinsField.setInt(coinManager, value);
    }

    /** ---------------------------------------------
     * Helper: effectManager.playerEffects 접근
     * --------------------------------------------- */
    @SuppressWarnings("unchecked")
    private Map<Integer, Map<ItemEffect.ItemEffectType, ?>> getEffects(GameState gs) throws Exception {
        var effField = GameState.class.getDeclaredField("effectManager");
        effField.setAccessible(true);
        Object effMgr = effField.get(gs);

        var peField = effMgr.getClass().getDeclaredField("playerEffects");
        peField.setAccessible(true);

        return (Map<Integer, Map<ItemEffect.ItemEffectType, ?>>) peField.get(effMgr);
    }

    /** ---------------------------------------------
     * Helper: 효과 만료 강제 처리
     * --------------------------------------------- */
    private void forceExpire(Object effectState) throws Exception {
        var cdField = effectState.getClass().getDeclaredField("cooldown");
        cdField.setAccessible(true);
        Cooldown cd = (Cooldown) cdField.get(effectState);

        var timeField = Cooldown.class.getDeclaredField("time");
        timeField.setAccessible(true);
        timeField.setLong(cd, System.currentTimeMillis() - 999_999L);
    }

    /** ======================================================
     *  Constructor Tests
     * ====================================================== */

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

    /** ======================================================
     *  Score / Stats
     * ====================================================== */

    @Test
    void testAddScoreNormal() {
        GameState gs = new GameState(1, 3, false);

        gs.addScore(0, 50);
        assertEquals(50, gs.getScore(0));
    }

    @Test
    void testAddScoreWithEffectMultiplier() {
        GameState gs = new GameState(1, 3, false);

        gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);
        gs.addScore(0, 10);

        assertEquals(20, gs.getScore(0));
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

    /** ======================================================
     *  Coin System
     * ====================================================== */

    @Test
    void testAddCoins() throws Exception {
        GameState gs = new GameState(1, 3, false);
        setCoins(gs, 10);

        gs.addCoins(0, 5);
        assertEquals(15, gs.getCoins());
    }

    @Test
    void testSpendCoinsSuccess() throws Exception {
        GameState gs = new GameState(1, 3, false);
        setCoins(gs, 20);

        assertTrue(gs.spendCoins(1, 15));
        assertEquals(5, gs.getCoins());
    }

    @Test
    void testSpendCoinsFail() throws Exception {
        GameState gs = new GameState(1, 3, false);
        setCoins(gs, 5);

        assertFalse(gs.spendCoins(1, 10));
        assertEquals(5, gs.getCoins());
    }

    /** ======================================================
     *  Life System
     * ====================================================== */

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

    /** ======================================================
     *  Level
     * ====================================================== */
    @Test
    void testNextLevel() {
        GameState gs = new GameState(1, 1, false);
        gs.nextLevel();

        assertEquals(2, gs.getLevel());
    }

    /** ======================================================
     *  Effect System
     * ====================================================== */

    @Test
    void testAddEffectStartsEffect() {
        GameState gs = new GameState(1, 1, false);

        gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);
        assertTrue(gs.hasEffect(0, ItemEffect.ItemEffectType.SCOREBOOST));
    }

    @Test
    void testGetEffectValue() {
        GameState gs = new GameState(1, 1, false);
        gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 3, 1);

        assertEquals(3, gs.getEffectValue(0, ItemEffect.ItemEffectType.SCOREBOOST));
    }

    @Test
    void testEffectExpiresAfterUpdate() throws Exception {
        GameState gs = new GameState(1, 1, false);

        gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);
        Map<Integer, Map<ItemEffect.ItemEffectType, ?>> effects = getEffects(gs);

        Object effect = effects.get(0).get(ItemEffect.ItemEffectType.SCOREBOOST);
        assertNotNull(effect);

        forceExpire(effect);
        gs.updateEffects();

        assertFalse(gs.hasEffect(0, ItemEffect.ItemEffectType.SCOREBOOST));
    }

    @Test
    void testClearEffects() {
        GameState gs = new GameState(1, 1, false);

        gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);
        gs.clearEffects(0);

        assertFalse(gs.hasEffect(0, ItemEffect.ItemEffectType.SCOREBOOST));
    }

    @Test
    void testClearAllEffects() {
        GameState gs = new GameState(1, 1, false);

        gs.addEffect(0, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);
        gs.addEffect(1, ItemEffect.ItemEffectType.SCOREBOOST, 2, 1);

        gs.clearAllEffects();

        assertFalse(gs.hasEffect(0, ItemEffect.ItemEffectType.SCOREBOOST));
        assertFalse(gs.hasEffect(1, ItemEffect.ItemEffectType.SCOREBOOST));
    }
}