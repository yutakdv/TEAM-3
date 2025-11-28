package engine;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import entity.EnemyShip;
import entity.Item;
import entity.ItemPool;

/**
 * Responsible for item drop decisions and applying item effects. Implemented as a singleton for
 * easy access.
 */
public final class ItemManager {

  private static ItemManager instance;

  /** Debug logger init */
  private static final Logger LOGGER = Core.getLogger();

  /** Random Roll for item */
  private final Random itemRoll = new Random();

  /** Counter for pity system, increases when no item is dropped. */
  private int pityCounter;

  /** Item database loaded from CSV. */
  private final ItemDB itemDB = new ItemDB();

  /** -------------------------- ITEM DATA -------------------------- * */

  /** ITEM WEIGHT * */
  public enum DropTier {
    // DEBUG    (500.0),
    NONE(70.0),
    COMMON(20.0),
    UNCOMMON(9.0),
    RARE(1.0);

    public final double tierWeight;

    DropTier(final double tierWeight) {
      this.tierWeight = Math.max(0.0, tierWeight);
    }
  }

  /** -------------------------- INIT -------------------------- * */

  /** Total weight of all item tiers except NONE. */
  private static final double ITEM_WEIGHT;

  static {
    double sum = 0.0;
    for (final DropTier t : DropTier.values()) {
      if (t != DropTier.NONE) {
        sum += t.tierWeight;
      }
    }
    ITEM_WEIGHT = sum;
  }

  private ItemManager() {
    // singleton
  }

  public static ItemManager getInstance() {
    if (instance == null) {
      instance = new ItemManager();
    }
    return instance;
  }

  /** -------------------------- MAIN -------------------------- * */

  /**
   * Determines and returns the item dropped by the given enemy.
   *
   * @param enemy enemy ship that was defeated.
   * @return dropped Item, or null if no item is dropped.
   */
  public Item obtainDrop(final EnemyShip enemy) {
    if (enemy == null) {
      return null;
    }

    // Pity Boost
    final double pityBoost = Math.min(pityCounter * 0.05, 0.5);
    final double boostedNoneWeight = DropTier.NONE.tierWeight * (1.0 - pityBoost);

    // Roll Item
    final double dropRoll = itemRoll.nextDouble() * (ITEM_WEIGHT + boostedNoneWeight);
    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info(String.format("[ItemManager]: DropRoll %.1f", dropRoll));
    }

    DropTier chosenTier = DropTier.NONE;
    double acc = 0.0;

    for (final DropTier tier : DropTier.values()) {
      double weight = tier.tierWeight;

      if (tier == DropTier.NONE) {
        weight = boostedNoneWeight;
      }

      acc += weight;

      if (dropRoll < acc) {
        chosenTier = tier;
        break;
      }
    }

    // Calculate Pity
    if (chosenTier == DropTier.NONE) {
      pityCounter++;
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info(String.format("[ItemManager]: Tier=NONE (pity=%d)", pityCounter));
      }
      return null;
    }

    pityCounter = 0;

    // Load item list from CSV by DropTier
    final java.util.List<ItemData> candidates = new java.util.ArrayList<>();
    final java.util.Collection<ItemData> allItems = itemDB.getAllItems();
    for (final ItemData data : allItems) {
      if (data.getDropTier().equalsIgnoreCase(chosenTier.name())) {
        candidates.add(data);
      }
    }

    if (candidates.isEmpty()) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.warning("[ItemManager]: No items defined for tier " + chosenTier);
      }
      return null;
    }

    final ItemData chosenData = candidates.get(itemRoll.nextInt(candidates.size()));

    // get spawn position / enemy death position
    final int centerX = enemy.getPositionX() + enemy.getWidth() / 2;
    final int centerY = enemy.getPositionY() + enemy.getHeight() / 2;

    // Pass ItemData directly to ItemPool
    final int itemSpeed = 2;
    final Item drop = ItemPool.getItem(chosenData, centerX, centerY, itemSpeed);

    if (drop == null) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.warning("[ItemManager]: Failed to create item: " + chosenData.getType());
      }
      return null;
    }

    if (LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info(
          "[ItemManager]: created item "
              + drop.getType()
              + " at ("
              + centerX
              + ", "
              + centerY
              + ")");
    }

    return drop;
  }
}
