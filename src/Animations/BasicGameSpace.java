package Animations; // NOPMD - PackageCase

import java.util.concurrent.ThreadLocalRandom;

/*
 * The basic background stars effect during the game
 * */
public class BasicGameSpace {

  private final Star[] stars;
  private final int[][] positions;
  private int speed;
  private final int numStars;

  public BasicGameSpace(final int numStars) {

    this.numStars = numStars;
    this.stars = new Star[this.numStars];
    this.positions = new int[this.numStars][3];

    for (int i = 0; i < this.numStars; i++) {

      stars[i] =
          new Star(
              ThreadLocalRandom.current().nextInt(10, 448),
              ThreadLocalRandom.current().nextInt(-500, 5),
              randomSpeed() ? 2 : 1);
      positions[i][0] = stars[i].x;
      positions[i][1] = stars[i].y;
      positions[i][2] = stars[i].speed;
    }
  }

  // Update star locations
  public void update() {
    int i = 0;
    for (Star star : stars) {
      if (this.speed == 3) {
        star.y += 3;
      } else {
        star.y += star.speed;
      }
      positions[i][1] = star.y;

      if (star.y >= 525) {
        star.y = 0;
        positions[i][1] = 0;
      }
      i++;
    }
  }

  public void setLastLife(final boolean status) {
    if (status) {
      this.speed = 3;
    } else {
      this.speed = 1;
    }
  }

  public boolean isLastLife() {
    return this.speed == 3;
  }

  public int[][] getStarLocations() {
    return this.positions;
  }

  public int getNumStars() {
    return this.numStars;
  }

  public final boolean randomSpeed() {
    final double r = Math.random();

    return r < 0.85;
  }

  // Star format
  private static class Star {
    final int x;
    int y;
    final int speed;

    Star(final int x, final int y, final int speed) {
      this.x = x;
      this.y = y;
      this.speed = speed;
    }
  }
}
