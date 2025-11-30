package engine;

import java.util.*;

/**
 * Handles loading and managing item data from the CSV file. (item_db.csv)
 *
 * <p>Supports both legacy CSV format (5 columns) and new format with cost (6 columns):
 * type,spriteType,dropTier,effectValue,effectDuration[,cost]
 */
public class ItemDB {

  /** Map of item type name to its corresponding ItemData. */
  private final Map<String, ItemData> itemMap = new HashMap<>();

  /** Constructor. Automatically loads the CSV file into memory. */
  public ItemDB() {
    loadItemDB();
  }

  /**
   * Loads all item data from the CSV file into the itemMap. The CSV format is expected as: type,
   * spriteType, dropTier, effectValue, effectDuration, cost
   */
  private void loadItemDB() {
    itemMap.put("SCORE", new ItemData("SCORE", "ItemScore", "COMMON", 10));

    itemMap.put("COIN", new ItemData("COIN", "ItemCoin", "UNCOMMON", 20));

    itemMap.put("HEAL", new ItemData("HEAL", "ItemHeal", "RARE", 1));
  }

  /**
   * Return the ItemData object for the given item type.
   *
   * @param type type of the item.
   * @return ItemData object, or null if not found.
   */
  public ItemData getItemData(final String type) {
    return itemMap.get(type);
  }

  /**
   * Return a collection of all ItemData objects.
   *
   * @return Collection of all items in the database.
   */
  public Collection<ItemData> getAllItems() {
    return itemMap.values();
  }
}
