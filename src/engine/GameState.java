// engine/GameState.java
package engine;


/**
 * Implements an object that stores the state of the game between levels - supports 2-player co-op
 * with shared lives.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.TooManyMethods"})
public class GameState {

  private static final java.util.logging.Logger logger = Core.getLogger();

  // 2P mode: number of players used for shared lives in co-op
  public static final int NUM_PLAYERS = 2; // adjust later if needed

  // 2P mode: true if in co-op mode
  private final boolean coop;

  /** Current game level. */
  private int level;

  public static final int FINITE_LEVEL = 5;

  public static final int INFINITE_LEVEL = 100;
  // 2P mode: if true, lives are shared in a team pool; else per-player lives

  // team life pool and cap (used when sharedLives == true).

  // 2P mode: per-player tallies (used for stats/scoring; lives[] unused in shared
  // mode).

  private final PlayerStatsManager statsManager = new PlayerStatsManager();
  private final LifeManager lifeManager;

  // 2P mode: co-op aware constructor used by the updated Core loop - livesEach
  // applies per-player; co-op uses shared pool.
  public GameState(final int level, final int livesEach, final boolean coop) {
    this.level = level;
    this.coop = coop;
    this.lifeManager = new LifeManager(livesEach, coop);
  }

  /* ---------- Constructors ---------- */

  /**
   * Constructor.
   *
   * @param level Current game level.
   * @param score Current score.
   * @param livesRemaining Lives currently remaining.
   * @param bulletsShot Bullets shot until now.
   * @param shipsDestroyed Ships destroyed until now.
   */
  public GameState(
      final int level,
      final int score,
      final int livesRemaining,
      final int bulletsShot,
      final int shipsDestroyed) {
    this.level = level;
    this.coop = false; // 2P: single-player mode
    this.lifeManager = new LifeManager(livesRemaining, false);

    statsManager.addRawScore(0, score);
    for (int i = 0; i < bulletsShot; i++) {
      statsManager.incBulletsShot(0);
    }
    for (int i = 0; i < shipsDestroyed; i++) {
      statsManager.incShipsDestroyed(0);
    }
  }

  /* ------- 2P mode: aggregate totals used by Core/ScoreScreen/UI------- */
  public int getScore() {
    return statsManager.getTotalScore();
  }

  public int getLivesRemaining() {
    return lifeManager.getLivesRemaining();
  }

  public int getBulletsShot() {
    return statsManager.getTotalBulletsShot();
  }

  public int getShipsDestroyed() {
    return statsManager.getTotalShipsDestroyed();
  }

  /* ----- Per-player getters (needed by Score.java) ----- */
  public int getScore(final int p) {
    return statsManager.getScore(p);
  }

  public int getBulletsShot(final int p) {
    return statsManager.getBulletsShot(p);
  }

  public int getShipsDestroyed(final int p) {
    return statsManager.getShipsDestroyed(p);
  }

  public void addScore(final int p, final int delta) {
    int realDelta = delta;
    statsManager.addRawScore(p, realDelta);
  }

  public void incBulletsShot(final int p) {
    statsManager.incBulletsShot(p);
  }

  public void incShipsDestroyed(final int p) {
    statsManager.incShipsDestroyed(p);
  }

  // 2P mode: per-player coin tracking
  public int getCoins() {
    return CoinManager.getCoins();
  } // legacy total for ScoreScreen

  public void addCoins(final int p, final int delta) {
    CoinManager.addCoins(p, delta);
  }

  public boolean spendCoins(final int p, final int amount) {
    return CoinManager  .spendCoins(p, amount);
  }

  // ===== Mode / life-pool helpers expected elsewhere =====
  public boolean isCoop() {
    return coop;
  }

  public boolean isSharedLives() {
    return lifeManager.isShared();
  }

  public int getTeamLives() {
    return lifeManager.getTeamLives();
  }

  public void addTeamLife(final int n) {
    lifeManager.addTeamLife(n);
  }

  // 2P mode: decrement life (shared pool if enabled; otherwise per player). */
  public void decLife(final int p) {
    lifeManager.decLife(p);
  }

  // for bonusLife, balance out decLife (+/- life)
  public void addLife(final int p, final int n) {
    lifeManager.addLife(p, n);
  }

  public int getLevel() {
    return level;
  }

  public void nextLevel() {
    level++;
  }

  // Team alive if pool > 0 (shared) or any player has lives (separate).
  public boolean teamAlive() {
    return lifeManager.teamAlive();
  }

  // for ItemEffect.java
  public int getTeamLivesCap() {
    return lifeManager.getTeamLivesCap();
  }

  // for ItemEffect.java
  public int get1PlayerLives() {
    return lifeManager.getPlayerLives(0);
  }


}
