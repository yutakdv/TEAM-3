package engine;

public class LifeManager {
  // 2P mode: number of players used for shared lives in co-op
  public static final int NUM_PLAYERS = 2; // adjust later if needed

  // 2P mode: if true, lives are shared in a team pool; else per-player lives
  private final boolean sharedLives;

  // 2P mode: per-player tallies (used for stats/scoring; lives[] unused in shared
  // mode).
  private final int[] lives = new int[NUM_PLAYERS];

  // team life pool and cap (used when sharedLives == true).
  private int teamLives;
  private final int teamLivesCap;

  public LifeManager(final int livesEach, final boolean coopMode) {
    this.sharedLives = coopMode;

    if (coopMode) {
      this.teamLives = Math.max(0, livesEach * NUM_PLAYERS);
      this.teamLivesCap = this.teamLives;
    } else {
      this.teamLives = 0;
      this.teamLivesCap = 0;
      lives[0] = Math.max(0, livesEach);
    }
  }

  // ---- Shared lives vs Player lives ---- //
  public boolean isShared() {
    return sharedLives;
  }

  public int getLivesRemaining() {
    return sharedLives ? teamLives : (lives[0] + lives[1]); // NOPMD
  }

  public int getTeamLives() {
    return teamLives;
  }

  public int getTeamLivesCap() {
    return teamLivesCap;
  }

  public int getPlayerLives(final int p) {
    return (p >= 0 && p < NUM_PLAYERS) ? lives[p] : 0; // NOPMD
  }

  // ---- Modify life values ---- //

  // 2P mode: decrement life (shared pool if enabled; otherwise per player). */
  public void addTeamLife(final int n) {
    if (!sharedLives) {
      return; // only for shared mode
    }
    teamLives = Math.min(teamLivesCap, teamLives + Math.max(0, n));
  }

  public void decLife(final int p) {
    if (sharedLives) {
      teamLives = Math.max(0, teamLives - 1);
    } else if (p >= 0 && p < NUM_PLAYERS && lives[p] > 0) {
      lives[p]--;
    }
  }

  public void addLife(final int p, final int n) {
    if (sharedLives) {
      teamLives = Math.min(teamLivesCap, teamLives + Math.max(0, n));
    } else if (p >= 0 && p < NUM_PLAYERS) {
      lives[p] = Math.max(0, lives[p] + Math.max(0, n));
    }
  }

  // ---- Alive check ----

  public boolean teamAlive() {
    return sharedLives ? (teamLives > 0) : (lives[0] > 0 || lives[1] > 0); // NOPMD
  }
}
