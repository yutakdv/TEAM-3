package entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import engine.*; // NOPMD
import screen.Screen;

/**
 * Groups enemy ships into a formation that moves together.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public class EnemyShipFormation implements Iterable<EnemyShip> {

  /** DrawManager instance. */
  private final DrawManager drawManager;

  /** Screen to draw ships on. */
  private Screen screen;

  private final FormationMovement movement;
  private final FormationShooting shooting;
  private final int initialShipCount;
  private final EnemyShipGrid grid;

  /**
   * Constructor, sets the initial conditions.
   *
   * @param gameSettings Current game settings.
   */
  public EnemyShipFormation(final GameSettings gameSettings) {
    this.drawManager = Core.getDrawManager();
    /** Application logger. */
    final Logger logger = Core.getLogger();

    // 1. Movement & Shooting 초기화
    this.movement = new FormationMovement(gameSettings.getBaseSpeed());
    this.shooting = new FormationShooting(gameSettings.getShootingFrequency());

    // 2. Grid 초기화 (배 생성 및 배치)
    this.grid =
        new EnemyShipGrid(
            gameSettings.getFormationWidth(),
            gameSettings.getFormationHeight(),
            movement.getPositionX(),
            movement.getPositionY());

    this.initialShipCount = grid.getShipCount();

    initializeShooters();
    processInitialChanges(gameSettings);

    logger.info("Initializing ship formation.");
  }

  private void initializeShooters() {
    // Grid에서 컬럼 정보를 가져와 사수 설정
    for (final List<EnemyShip> column : this.grid.getColumns()) {
      if (!column.isEmpty()) {
        this.shooting.addShooter(column.get(column.size() - 1));
      }
    }
  }

  private void processInitialChanges(final GameSettings gameSettings) {
    List<EnemyShip> shipsToDestroy = new ArrayList<>();
    for (final GameSettings.ChangeData changeData : gameSettings.getChangeDataList()) {
      // Grid에게 특정 위치의 배를 달라고 요청
      final EnemyShip ship = this.grid.getShip(changeData.x, changeData.y);

      if (ship != null) {
        if (changeData.hp == 0) {
          shipsToDestroy.add(ship);
        } else {
          ship.changeShip(changeData); // NOPMD
        }
      }
    }

    for (EnemyShip ship : shipsToDestroy) {
      destroy(ship);
    }
  }

  /**
   * Associates the formation to a given screen.
   *
   * @param newScreen Screen to attach.
   */
  public final void attach(final Screen newScreen) {
    screen = newScreen;
  }

  /** Draws every individual component of the formation. */
  public final void draw() {
    // Grid가 Iterable을 구현했으므로 바로 루프 가능
    for (final EnemyShip enemyShip : this.grid) {
      drawManager.drawEntity(enemyShip, enemyShip.getPositionX(), enemyShip.getPositionY());
    }
  }

  /** Updates the position of the ships. */
  public final void update() {
    // 1. Grid 정리 (빈 컬럼 삭제 등)
    grid.cleanUp();

    // Grid가 비었다면 업데이트 중단
    if (grid.getShipCount() == 0) {
      return;
    }

    // 2. Movement 업데이트 (Grid의 현재 상태 전달)
    // Movement 로직이 계산을 위해 현재 Grid의 가장 왼쪽/위쪽 좌표를 알아야 할 수 있음
    movement.setPositionX(grid.getMinX());
    movement.setPositionY(grid.getMinY());

    final int[] delta =
        movement.update(
            grid.getWidth(),
            grid.getHeight(),
            grid.getShipCount(),
            initialShipCount, // Movement가 속도 계산에 필요하다면 유지
            screen);

    // 3. 계산된 이동값(Delta)을 Grid에 적용
    if (delta != null && (delta[0] != 0 || delta[1] != 0)) {
      grid.move(delta[0], delta[1]);
    }
  }

  /**
   * Shoots a bullet downwards. Fires bullets from C-type and B-type enemies in the formation.
   * C-type fires double bullets, B-type fires faster bullets.
   *
   * @param bullets Bullets set to add the bullet being shot.
   */
  public final void shoot(final Set<Bullet> bullets) {
    shooting.shoot(bullets);
  }

  /**
   * Destroys a ship.
   *
   * @param destroyedShip Ship to be destroyed.
   */
  public final void destroy(final EnemyShip destroyedShip) {
    // 1. Grid에서 배 제거
    final boolean removed = grid.removeShip(destroyedShip);
    if (!removed) {
      return;
    }

    destroyedShip.destroy();

    // 2. 사수(Shooter) 목록 갱신
    final List<EnemyShip> shooters = shooting.getShooters();
    if (shooters.contains(destroyedShip)) { // NOPMD
      final int destroyedIndex = shooters.indexOf(destroyedShip);
      final EnemyShip nextShooter = getNextShooter(destroyedShip);

      if (nextShooter != null) {
        shooting.updateShooter(destroyedIndex, nextShooter);
      } else {
        shooting.removeShooter(destroyedShip);
      }
    }
  }

  /**
   * Gets the ship on a given column that will be in charge of shooting.
   *
   * @return New shooter ship.
   */
  private EnemyShip getNextShooter(final EnemyShip destroyedShip) {
    for (final List<EnemyShip> col : grid.getColumns()) {
      // 파괴된 배와 X좌표가 거의 같은(같은 컬럼인) 리스트를 찾음
      if (!col.isEmpty()
          && Math.abs(col.get(0).getPositionX() - destroyedShip.getPositionX()) < 5) { // NOPMD
        final EnemyShip next = col.get(col.size() - 1);
        if (next.isDestroyed()) { // NOPMD
          return null;
        }
        return next;
      }
    }
    return null;
  }

  /**
   * Returns an iterator over the ships in the formation.
   *
   * @return Iterator over the enemy ships.
   */
  @Override
  public final Iterator<EnemyShip> iterator() {
    return grid.iterator();
  }

  public boolean lastShip() {
    return this.grid.getShipCount() == 1;
  }

  public final boolean isEmpty() {
    return this.grid.getShipCount() <= 0;
  }

  public int getShipCount() {
    return this.grid.getShipCount();
  }
}
