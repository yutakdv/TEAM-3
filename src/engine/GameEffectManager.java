package engine;

import engine.ItemEffect.ItemEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class GameEffectManager {
    private static final java.util.logging.Logger logger = Core.getLogger();

    // 2P mode: number of players used for shared lives in co-op
    public static final int NUM_PLAYERS = 2; // adjust later if needed
    private static final String LOG_PREFIX_PLAYER = "[GameEffectManager] Player ";

    /** Each player has all effect types always initialized (inactive at start). */
    private final Map<Integer, Map<ItemEffectType, EffectState>> playerEffects = new HashMap<>();

    public static class EffectState {
        Cooldown cooldown;
        boolean active;
        Integer effectValue;

        public EffectState() {
            this.cooldown = Cooldown.EMPTY;
            this.active = false;
            this.effectValue = 0;
        }
    }

    public GameEffectManager() {
        initializeStates();
    }

    private void initializeStates() {
        for (int p = 0; p < NUM_PLAYERS; p++) {
            Map<ItemEffectType, EffectState> map = new HashMap<>();
            for (ItemEffectType type : ItemEffectType.values()) {
                map.put(type, new EffectState());
            }
            playerEffects.put(p, map);
        }
    }

    private EffectState getState(int playerIndex, ItemEffectType type) {
        Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) return null;
        return effects.get(type);
    }

    public void addEffect(
            final int playerIndex,
            final ItemEffectType type,
            final Integer effectValue,
            final int durationSeconds) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return;
        }

        final Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) {
            return;
        }

        final EffectState state = getState(playerIndex, type);
        if (state == null) {
            return;
        }

        final String valueStr = (effectValue != null) ? " (value: " + effectValue + ")" : "";

        if (state.active && state.cooldown != null) {
            // Extend existing effect
            state.cooldown.addTime(durationSeconds * 1000);

            state.effectValue = effectValue;
            if (logger.isLoggable(Level.INFO)) {
                logger.info(
                        LOG_PREFIX_PLAYER
                                + playerIndex
                                + " extended "
                                + type
                                + valueStr
                                + ") by "
                                + durationSeconds
                                + "s to "
                                + state.cooldown.getDuration());
            }
        } else {
            // Start new effect
            state.cooldown = Core.getCooldown(durationSeconds * 1000);
            state.cooldown.reset();
            state.active = true;

            state.effectValue = effectValue;
            if (logger.isLoggable(Level.INFO)) {
                logger.info(
                        LOG_PREFIX_PLAYER
                                + playerIndex
                                + " started "
                                + type
                                + valueStr
                                + ") for "
                                + durationSeconds
                                + "s");
            }
        }
    }

    public boolean hasEffect(final int playerIndex, final ItemEffectType type) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return false;
        }

        final Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) {
            return false;
        }

        final EffectState state = getState(playerIndex, type);
        if (state == null || !state.active) {
            return false;
        }

        return !state.cooldown.checkFinished();
    }

    /** Call this each frame to clean up expired effects */
    public void updateEffects() {
        for (int p = 0; p < NUM_PLAYERS; p++) {
            final Map<ItemEffectType, EffectState> effects = playerEffects.get(p);
            if (effects == null) {
                continue;
            }

            for (final Map.Entry<ItemEffectType, EffectState> entry : effects.entrySet()) {
                final EffectState state = entry.getValue();
                if (state.active && state.cooldown != null && state.cooldown.checkFinished()) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.info(LOG_PREFIX_PLAYER + p + " effect " + entry.getKey() + " expired.");
                    }
                    state.active = false;
                    state.cooldown = Cooldown.EMPTY; // Release reference
                    state.effectValue = 0;
                }
            }
        }
    }

    /**
     * Gets the effect value for a specific player and effect type
     *
     * @param playerIndex Index of the player (0 or 1)
     * @param type Type of effect to check
     * @return Effect value if active, null otherwise
     */
    public Integer getEffectValue(final int playerIndex, final ItemEffectType type) {
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return null;
        }

        final Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) {
            return null;
        }

        final EffectState state = getState(playerIndex, type);
        if (state == null || !state.active) {
            return null;
        }

        // Check if effect is still valid (not expired)
        if (state.cooldown != null && state.cooldown.checkFinished()) {
            return null;
        }

        return state.effectValue;
    }

    /** Clear all active effects for a specific player */
    public void clearEffects(final int playerIndex) {
        //
        if (playerIndex < 0 || playerIndex >= NUM_PLAYERS) {
            return;
        }

        final Map<ItemEffectType, EffectState> effects = playerEffects.get(playerIndex);
        if (effects == null) {
            return;
        }

        // for - all effect types for this player
        for (final Map.Entry<ItemEffectType, EffectState> entry : effects.entrySet()) {
            // get effect state
            final EffectState state = entry.getValue();
            // if state active then false
            if (state.active) {
                state.active = false;
                state.cooldown = Cooldown.EMPTY;
                state.effectValue = 0;
            }
        }
        if (logger.isLoggable(Level.INFO)) {
            logger.info(LOG_PREFIX_PLAYER + playerIndex + ": All effects cleared.");
        }
    }

    /** Clear all active effects for all players */
    public void clearAllEffects() {
        for (int p = 0; p < NUM_PLAYERS; p++) {


            clearEffects(p);
        }
    }
}