package engine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

/**
 * Handles loading and managing item data from the CSV file. (item_db.csv)
 *
 * <p>Supports both legacy CSV format (5 columns) and new format with cost (6 columns):
 * type,spriteType,dropTier,effectValue,effectDuration[,cost]
 */
public class ItemDB {
  /** Path to the item database CSV file. */
  private static final String FILE_PATH = "res/item_db.csv";

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
    Logger logger = Core.getLogger();
    BufferedReader br = null;

    try {
      InputStream in = ItemDB.class.getClassLoader().getResourceAsStream("item_db.csv");

      // Try classpath (.jar / .exe)
      if (in != null) {
        br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        logger.info("[ItemDB] Loaded item_db.csv from classpath.");
      }

      // local /res/folder
      if (br == null) {
        File local = new File("res/item_db.csv");
        if (local.exists()) {
            br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(local), StandardCharsets.UTF_8));
          logger.info("[ItemDB] Loaded item_db.csv from local res/folder.");
        }
      }

      // 3) File not found
      if (br == null) {
        logger.warning("[ItemDB] item_db.csv NOT FOUND in classpath or res folder.");
        return;
      }

      String line;
      boolean header = true;

      while ((line = br.readLine()) != null) {
        if (header) {
          header = false;
          continue;
        }

        String[] tokens = line.split(",");
        if (tokens.length < 5) {
          logger.warning("[ItemDB] Skipping malformed line: " + line);
          continue;
        }

        String type = tokens[0].trim();
        String spriteType = tokens[1].trim();
        String dropTier = tokens[2].trim();

        int effectValue = parseSafe(tokens[3]);

        itemMap.put(type, new ItemData(type, spriteType, dropTier, effectValue));
      }
    } catch (IOException e) {
      Logger l = Core.getLogger();
      l.warning("Failed to load item database from " + FILE_PATH + ": " + e.getMessage());
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException ignore) {

        }
      }
    }
  }

  private int parseSafe(String s) {
    try {
      return Integer.parseInt(s.trim());
    } catch (Exception e) {
      return 0;
    }
  }

  /**
   * Return the ItemData object for the given item type.
   *
   * @param type type of the item.
   * @return ItemData object, or null if not found.
   */
  public ItemData getItemData(String type) {
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
