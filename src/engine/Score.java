package engine;

import java.util.Arrays;

/**
 * Implements a high score record.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public class Score implements Comparable<Score> {

  /** Player's name. */
  private final String name;

  /** Score points. */
  private final int score; // NOPMD - Field name matches class name

  /** per-player breakdown */
  private int[] playerScores;

  int[] playerBullets;
  int[] playerKills;

  /** 1P/2P mode */
  final String mode;

  /**
   * Constructor.
   *
   * @param name Player name, three letters.
   * @param score Player score.
   */
  public Score(final String name, final int score, final String mode) {
    this.name = name;
    this.score = score;
    this.mode = mode; // add 1P/2P mode
  }

  /** NEW Constructor: (team co-op) */
  public Score(final String name, final GameState gs, final String mode) {
    this.name = name;
    this.score = gs.getScore();
    this.mode = mode; // add 1P/2P mode

    final int n = GameState.NUM_PLAYERS;
    this.playerScores = new int[n];
    this.playerBullets = new int[n];
    this.playerKills = new int[n];

    for (int i = 0; i < n; i++) {
      this.playerScores[i] = gs.getScore(i);
      this.playerBullets[i] = gs.getBulletsShot(i);
      this.playerKills[i] = gs.getShipsDestroyed(i);
    }
  }

  /**
   * Getter for the player's name.
   *
   * @return Name of the player.
   */
  public final String getName() {
    return this.name;
  }

  /**
   * Getter for the player's score.
   *
   * @return High score.
   */
  public final int getScore() {
    return this.score;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Score)) {
      return false;
    }
    final Score other = (Score) obj;
    return this.score == other.score;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(this.score);
  }

  /**
   * Orders the scores descending by score.
   *
   * @param other Score to compare the current one with.
   * @return Comparison between the two scores. Positive if the current one is smaller, positive if
   *     its bigger, zero if it's the same.
   */
  @Override
  public final int compareTo(final Score other) {
    return Integer.compare(other.getScore(), this.score); // descending
  }

  @Override
  public String toString() {
    return "Score{name='"
        + name
        + "', score="
        + score
        + ", perPlayer="
        + Arrays.toString(playerScores)
        + "}";
  }
}
