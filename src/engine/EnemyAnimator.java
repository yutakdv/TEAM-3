package engine;

import engine.DrawManager.SpriteType;

public class EnemyAnimator {
  @SuppressWarnings("PMD")
  public EnemyAnimator() {
    // utility class
  }

  /** Returns the next animation frame for the given sprite. */
  public SpriteType nextFrame(final SpriteType spriteType) {
    return switch (spriteType) {
      case EnemyShipA1 -> SpriteType.EnemyShipA2;
      case EnemyShipA2 -> SpriteType.EnemyShipA1;
      case EnemyShipB1 -> SpriteType.EnemyShipB2;
      case EnemyShipB2 -> SpriteType.EnemyShipB1;
      case EnemyShipC1 -> SpriteType.EnemyShipC2;
      case EnemyShipC2 -> SpriteType.EnemyShipC1;
      default -> spriteType;
    };
  }
}
