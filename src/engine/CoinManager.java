package engine;

import java.util.logging.Level;

public final class CoinManager {
  private static final java.util.logging.Logger logger = Core.getLogger();

  // 2P mode: number of players used for shared lives in co-op
  public static final int NUM_PLAYERS = 2; // adjust later if needed

  /** Current coin count. */
  // ADD THIS LINE
  private static int coins; // ADD THIS LINE - edited for 2P mode

  private CoinManager() {}

  public static void load() {
    coins = Core.getFileManager().loadCoins(); // NOPMD
    if (logger.isLoggable(Level.INFO)) {
      logger.info("[CoinManager] Loaded coins from file: " + coins);
    }
  }

  // 2P mode: per-player coin tracking
  public static int getCoins() {
    return coins;
  } // legacy total for ScoreScreen

  public static void addCoins(final int p, final int delta) {
    if (p >= 0 && p < NUM_PLAYERS && delta > 0) {
      coins = Math.max(0, coins + delta);
    }
  }

  public static boolean spendCoins(final int p, final int amount) {
    if (p < 0 || p >= NUM_PLAYERS || amount < 0) {
      return false;
    }
    if (coins < amount) {
      return false;
    }
    coins -= amount;
    return true;
  }
}
