package entity;

import java.awt.Color;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.Core;
import engine.DrawManager.SpriteType;

import engine.GameState;
import engine.ItemDB;
import engine.ItemData;
import engine.ItemEffect;

/** Implements Item that moves vertically down. */
public class Item extends Entity {

  /** Logger instance for logging purposes. */
  private static final Logger LOGGER = Core.getLogger();

  /** Shared ItemDB instance for all items. */
  private static final ItemDB ITEM_DB = new ItemDB();

  /** Type of Item. */
  private String type;

  /** Item Movement Speed. */
  private int itemSpeed;

  /**
   * Constructor, establishes the Item's properties.
   *
   * @param itemType Type of Item being spawned
   * @param positionX Initial position of the Item in the X axis.
   * @param positionY Initial position of the Item in the Y axis.
   * @param speed Speed of the Item, positive or negative depending on direction - positive is down.
   */
  public Item(final String itemType, final int positionX, final int positionY, final int speed) {

    super(positionX, positionY, 3 * 2, 5 * 2, Color.WHITE);

    this.type = itemType;
    this.itemSpeed = speed;

    setSprite();
  }

  /** Safely fetches ItemData for the current type (logs if missing). */
  private ItemData getItemData() {
    final ItemData data = ITEM_DB.getItemData(this.type);

    if (data == null && LOGGER.isLoggable(Level.WARNING)) {
      LOGGER.warning("[Item]: No ItemData found for type " + this.type);
    }

    return data;
  }

  /** Setter for the sprite of the Item using data from ItemDB. */
  public final void setSprite() {
    final ItemData data = getItemData();

    if (data != null) {
      try {
        this.spriteType = SpriteType.valueOf(data.getSpriteType());
      } catch (IllegalArgumentException e) {
        this.spriteType = SpriteType.ItemScore; // fallback
        if (LOGGER.isLoggable(Level.WARNING)) {
          LOGGER.warning(
              "[Item]: Unknown sprite type in ItemDB: "
                  + data.getSpriteType()
                  + ", using default.");
        }
      }
    } else {
      this.spriteType = SpriteType.ItemScore;
    }

    applyColorByType();
  }

  /** Applies a color to the item based on its type for better visibility. */
  private void applyColorByType() {
    switch (this.type) {
      case "COIN" -> this.changeColor(Color.YELLOW);
      case "SCORE", "SCOREBOOST" -> this.changeColor(Color.GREEN);
      case "HEAL" -> this.changeColor(Color.PINK);
      case "TRIPLESHOT", "BULLETSPEEDUP" -> this.changeColor(Color.BLUE);
      default -> {
        this.changeColor(Color.BLACK);
        if (LOGGER.isLoggable(Level.WARNING)) {
          LOGGER.warning(
              "[Item]: Unknown item type in applyColorByType(): "
                  + this.type
                  + " (fallback color BLACK)");
        }
      }
    }
  }

  /** Updates the Item's position. */
  public final void update() {
    this.positionY += this.itemSpeed;
  }

  /**
   * Applies the effect of the Item to the player.
   *
   * @param gameState current game state instance.
   * @param playerId ID of the player to apply the effect to.
   */
  public void applyEffect(final GameState gameState, final int playerId) {
    final ItemData data = getItemData();
    if (data == null) {
      return;
    }
    applyEffect(gameState, playerId, data);
  }

  private void applyEffect(final GameState gameState, final int playerId, final ItemData data) {

    final int value = data.getEffectValue();

    final boolean applied =
        switch (this.type) {
          case "COIN" -> {
            ItemEffect.applyCoinItem(gameState, playerId, value);
            yield true;
          }
          case "HEAL" -> {
            ItemEffect.applyHealItem(gameState, playerId, value);
            yield true;
          }
          case "SCORE" -> {
            ItemEffect.applyScoreItem(gameState, playerId, value);
            yield true;
          }
          default -> {
            if (LOGGER.isLoggable(Level.WARNING)) {
              LOGGER.warning("[Item]: No ItemEffect for type " + this.type);
            }
            yield false;
          }
        };

    if (!applied && LOGGER.isLoggable(Level.INFO)) {
      LOGGER.info("[Item]: Player " + playerId + " couldn't afford or use " + this.type);
    }
  }

  /**
   * Setter of the speed of the Item.
   *
   * @param itemSpeed New speed of the Item.
   */
  public final void setItemSpeed(final int itemSpeed) {
    this.itemSpeed = itemSpeed;
  }

  /**
   * Reset the Item. Set the item type and sprite to newType, and the speed to 0.
   *
   * @param newType new type of the Item.
   */
  public final void reset(final String newType) {
    this.type = newType;
    this.itemSpeed = 0;
    setSprite(); // change to your enum if different
  }

  /**
   * Getter for the speed of the Item.
   *
   * @return type of the Item.
   */
  public final String getType() {
    return this.type;
  }
}
