package entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import engine.DrawManager.SpriteType;

/**
 * Manages the grid structure of enemy ships. Handles storage, dimensions, and cleaning up of empty
 * columns.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public class EnemyShipGrid implements Iterable<EnemyShip> {

  private static final int SEPARATION_DISTANCE = 40;
  private static final double PROPORTION_C = 0.2;
  private static final double PROPORTION_B = 0.4;

  private final List<List<EnemyShip>> enemyShips;
  private final int nShipsHigh;

  // 차원 및 상태 데이터
  private int width;
  private int height;
  private int shipWidth;
  private int shipHeight;
  private int shipCount;

  public EnemyShipGrid(
      final int nShipsWide, final int nShipsHigh, final int startX, final int startY) {
    this.nShipsHigh = nShipsHigh;
    this.enemyShips = new ArrayList<>();
    this.shipCount = 0;
    initializeShips(nShipsWide, startX, startY);
  }

  private void initializeShips(final int nShipsWide, final int startX, final int startY) {
    SpriteType spriteType;
    for (int i = 0; i < nShipsWide; i++) {
      this.enemyShips.add(new ArrayList<>()); // NOPMD
    }

    for (int col = 0; col < nShipsWide; col++) {
      final List<EnemyShip> column = this.enemyShips.get(col);
      for (int row = 0; row < this.nShipsHigh; row++) {
        if (row / (float) this.nShipsHigh < PROPORTION_C) {
          spriteType = SpriteType.EnemyShipC1;
        } else if (row / (float) this.nShipsHigh < PROPORTION_B + PROPORTION_C) {
          spriteType = SpriteType.EnemyShipB1;
        } else {
          spriteType = SpriteType.EnemyShipA1;
        }

        column.add(
            new EnemyShip(
                SEPARATION_DISTANCE * col + startX,
                SEPARATION_DISTANCE * row + startY,
                spriteType));
        this.shipCount++;
      }
    }

    // 초기 치수 설정
    if (!enemyShips.isEmpty() && !enemyShips.getFirst().isEmpty()) {
      final EnemyShip firstShip = enemyShips.getFirst().getFirst();
      this.shipWidth = firstShip.getWidth();
      this.shipHeight = firstShip.getHeight();
      updateDimensions(nShipsWide);
    }
  }

  /**
   * Cleans up empty columns and recalculates dimensions. The new top-left [x, y] position if the
   * grid shifted, otherwise logic handles it.
   */
  public void cleanUp() {
    // 1. 빈 컬럼 제거
    this.enemyShips.removeIf(List::isEmpty);

    // 2. 남은 배가 없으면 리턴
    if (enemyShips.isEmpty()) {
      this.width = 0;
      this.height = 0;
      return;
    }

    // 3. 높이 및 너비 재계산
    int maxColumnHeight = 0;
    final int leftMost = enemyShips.getFirst().getFirst().getPositionX();
    final int rightMost = enemyShips.getLast().getFirst().getPositionX();

    for (final List<EnemyShip> column : this.enemyShips) {
      final int colHeight =
          column.getLast().getPositionY() - column.getFirst().getPositionY() + this.shipHeight;
      maxColumnHeight = Math.max(maxColumnHeight, colHeight);
    }

    this.width = (rightMost - leftMost) + this.shipWidth;
    this.height = maxColumnHeight;
  }

  public void move(final int deltaX, final int deltaY) {
    for (final List<EnemyShip> column : this.enemyShips) {
      for (final EnemyShip ship : column) {
        ship.move(deltaX, deltaY);
        ship.update();
      }
    }
  }

  public boolean removeShip(final EnemyShip ship) {
    for (final List<EnemyShip> column : this.enemyShips) {
      if (column.remove(ship)) {
        this.shipCount--;
        return true;
      }
    }
    return false;
  }

  public EnemyShip getShip(final int col, final int row) {
    if (col >= 0 && col < enemyShips.size()) {
      final List<EnemyShip> column = enemyShips.get(col);
      if (row >= 0 && row < column.size()) {
        return column.get(row);
      }
    }
    return null;
  }

  private void updateDimensions(final int nShipsWide) {
    this.width = (nShipsWide - 1) * SEPARATION_DISTANCE + this.shipWidth;
    this.height = (this.nShipsHigh - 1) * SEPARATION_DISTANCE + this.shipHeight;
  }

  // Getters
  public List<List<EnemyShip>> getColumns() {
    return enemyShips;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getShipCount() {
    return shipCount;
  }

  public int getMinX() {
    if (enemyShips.isEmpty() || enemyShips.getFirst().isEmpty()) {
      return 0;
    }
    return enemyShips.getFirst().getFirst().getPositionX();
  }

  public int getMinY() {
    if (enemyShips.isEmpty() || enemyShips.getFirst().isEmpty()) {
      return 0;
    }
    return enemyShips.getFirst().getFirst().getPositionY();
  }

  @Override
  public Iterator<EnemyShip> iterator() {
    final List<EnemyShip> allShips = new ArrayList<>();
    for (final List<EnemyShip> col : enemyShips) {
      allShips.addAll(col);
    }
    return allShips.iterator();
  }
}
