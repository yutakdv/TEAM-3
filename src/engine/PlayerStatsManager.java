package engine;

public class PlayerStatsManager {
  // 2P mode: number of players used for shared lives in co-op
  public static final int NUM_PLAYERS = 2; // adjust later if needed

  // 2P mode: per-player tallies (used for stats/scoring; lives[] unused in shared
  // mode).
  private final int[] score = new int[NUM_PLAYERS];
  private final int[] bulletsShot = new int[NUM_PLAYERS];
  private final int[] shipsDestroyed = new int[NUM_PLAYERS];

  public PlayerStatsManager() { // NOPMD
    // no-op
  }

  /* ====================== UTIL ====================== */

  private boolean isValidPlayer(final int p) {
    return p >= 0 && p < NUM_PLAYERS;
  }

  /* ====================== SCORE ====================== */

  public void addRawScore(final int p, final int delta) {
    score[p] += delta;
  }

  /* ------- 2P mode: aggregate totals used by Core/ScoreScreen/UI------- */
  public int getTotalScore() {
    int t = 0;
    for (int p = 0; p < NUM_PLAYERS; p++) {
      t += score[p];
    }
    return t;
  }

  /* ----- Per-player getters (needed by Score.java) ----- */
  public int getScore(final int p) {
    return isValidPlayer(p) ? score[p] : 0;
  }

  /* ====================== BULLETS ====================== */

  public int getTotalBulletsShot() {
    int t = 0;
    for (int p = 0; p < NUM_PLAYERS; p++) {
      t += bulletsShot[p];
    }
    return t;
  }

  public int getBulletsShot(final int p) {
    return isValidPlayer(p) ? bulletsShot[p] : 0;
  }

  public void incBulletsShot(final int p) {
    bulletsShot[p]++;
  }

  /* ====================== SHIPS DESTROYED ====================== */

  public void incShipsDestroyed(final int p) {
    shipsDestroyed[p]++;
  }

  public int getTotalShipsDestroyed() {
    int t = 0;
    for (int p = 0; p < NUM_PLAYERS; p++) {
      t += shipsDestroyed[p];
    }
    return t;
  }

  public int getShipsDestroyed(final int p) {
    return isValidPlayer(p) ? shipsDestroyed[p] : 0;
  }
}
