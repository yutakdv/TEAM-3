package entity;

import screen.Screen;

@SuppressWarnings("PMD.DataClass")
public class FormationMovement {

  private static final int INIT_POS_X = 20;
  private static final int INIT_POS_Y = 100;
  private static final int X_SPEED = 8;
  private static final int Y_SPEED = 4;
  private static final int SIDE_MARGIN = 20;
  private static final int BOTTOM_MARGIN = 80;
  private static final int DESCENT_DISTANCE = 20;
  private static final int MINIMUM_SPEED = 10;

  private int positionX;
  private int positionY;
  private final int baseSpeed;
  private int movementSpeed;
  private int movementInterval;

  private Direction currentDirection;
  private Direction previousDirection;

  public enum Direction {
    /** Movement to the right side of the screen. */
    RIGHT,
    /** Movement to the left side of the screen. */
    LEFT,
    /** Movement to the bottom of the screen. */
    DOWN
  }

  public FormationMovement(final int baseSpeed) {
    this.baseSpeed = baseSpeed;
    this.movementSpeed = baseSpeed;
    this.positionX = INIT_POS_X;
    this.positionY = INIT_POS_Y;
    this.currentDirection = Direction.RIGHT;
    this.movementInterval = 0;
  }

  public int[] update(
      final int formationWidth,
      final int formationHeight,
      final int shipCount,
      final int totalShips,
      final Screen screen) {
    movementInterval++;

    calculateMovementSpeed(shipCount, totalShips);

    if (movementInterval < this.movementSpeed) {
      return new int[] {0, 0};
    }

    movementInterval = 0;

    determineNextDirection(formationWidth, formationHeight, screen);

    return move();
  }

  private void calculateMovementSpeed(final int shipCount, final int totalShips) {
    final double remainingProportion = (double) shipCount / totalShips;
    this.movementSpeed = (int) (Math.pow(remainingProportion, 2) * this.baseSpeed);
    this.movementSpeed += MINIMUM_SPEED;
  }

  private void determineNextDirection(final int width, final int height, final Screen screen) {
    final boolean isAtBottom = positionY + height > screen.getHeight() - BOTTOM_MARGIN;
    final boolean isAtRight = positionX + width >= screen.getWidth() - SIDE_MARGIN;
    final boolean isAtLeft = positionX <= SIDE_MARGIN;

    if (currentDirection == Direction.DOWN) {
      handleDownMovement();
    } else if (currentDirection == Direction.LEFT && isAtLeft) {
      handleSideBoundary(isAtBottom, Direction.RIGHT);
    } else if (currentDirection == Direction.RIGHT && isAtRight) {
      handleSideBoundary(isAtBottom, Direction.LEFT);
    }
  }

  private void handleDownMovement() {
    if (positionY % DESCENT_DISTANCE == 0) {
      currentDirection = (previousDirection == Direction.RIGHT) ? Direction.LEFT : Direction.RIGHT;
    }
  }

  private void handleSideBoundary(final boolean isAtBottom, final Direction oppositeDirection) {
    if (isAtBottom) {
      currentDirection = oppositeDirection;
    } else {
      previousDirection = currentDirection;
      currentDirection = Direction.DOWN;
    }
  }

  private int[] move() {
    int movementX = 0;
    int movementY = 0;

    switch (currentDirection) {
      case RIGHT:
        movementX = X_SPEED;
        break;
      case LEFT:
        movementX = -X_SPEED;
        break;
      case DOWN:
        movementY = Y_SPEED;
        break;
      default:
        break;
    }

    positionX += movementX;
    positionY += movementY;

    return new int[] {movementX, movementY};
  }

  public int getPositionX() {
    return positionX;
  }

  public int getPositionY() {
    return positionY;
  }

  public void setPositionX(final int x) {
    this.positionX = x;
  }

  public void setPositionY(final int y) {
    this.positionY = y;
  }
}
