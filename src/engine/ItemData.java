package engine;

/**
 * Represents the data for an item, including its type, sprite, tier, effect value, and duration.
 */
public class ItemData { //NOPMD
  /** Unique identifier for the item (e.g. "COIN", "HEAL", "SCORE"). */
  private final String type;

  /** sprite type (e.g. "ItemScore", "ItemHeal"). */
  private final String spriteType;

  /** rarity tier (e.g. "COMMON", "UNCOMMON", "RARE"). */
  private final String dropTier;

  /** numerical value of the item effect (e.g. heal amount, score amount). */
  private final int effectValue;

  /* duration that the effect remains active. */

  /**
   * Constructs an ItemData object.
   *
   * @param type Unique identifier for the item.
   * @param spriteType sprite type.
   * @param dropTier rarity tier.
   * @param effectValue numerical value of the item's effect.duration the effect remains active.
   */
  public ItemData(final String type,
                  final String spriteType,
                  final String dropTier,
                  final int effectValue) {
    // Unique identifier for the item (e.g "COIN", "HEAL", "SCORE").
    this.type = type;
    // The sprite type (e.g "ItemScore, ItemHeal", etc).
    this.spriteType = spriteType;
    // The rarity tier (e.g "COMMON", "UNCOMMON", "RARE").
    this.dropTier = dropTier;
    // The numerical value of the item's effect (e.g. heal amount, score amount).
    this.effectValue = effectValue;
    // The duration (in seconds or frames) that the effect remains active.
  }

  /**
   * Getter for item type.
   *
   * @return item type.
   */
  public String getType() {
    return type;
  }

  /**
   * Getter for sprite type of the item.
   *
   * @return sprite type.
   */
  public String getSpriteType() {
    return spriteType;
  }

  /**
   * Getter for drop tier.
   *
   * @return drop tier.
   */
  public String getDropTier() {
    return dropTier;
  }

  /**
   * Getter for the numerical value of the item effect.
   *
   * @return effect value.
   */
  public int getEffectValue() {
    return effectValue;
  }
}
