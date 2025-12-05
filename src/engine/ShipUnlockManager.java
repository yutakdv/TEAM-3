// This class is for ShipSelectionScreen
package engine;

import entity.Ship.ShipType;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

public class ShipUnlockManager {

  private static final int SHIP_COUNT = 4;
  private static final String SELECT_SOUND = "sound/select.wav";
  private static final String HOVER_SOUND = "sound/hover.wav";

  private final FileManager fileManager;

  private final Logger logger;

  private int coins;
  private final boolean[] unlockedStates = new boolean[SHIP_COUNT];

  private Achievement coinsToast;
  private Cooldown coinsCooldown;
  private boolean coinsToastActive;

  public ShipUnlockManager() {
    this(Core.getFileManager(), Core.getLogger());
  }

  public ShipUnlockManager(final FileManager fileManager, final Logger logger) {
    this.fileManager = fileManager;
    this.logger = logger;
    this.coins = this.fileManager.loadCoins();
    initUnlockStates();
  }

  private void initUnlockStates() {
    try {
      final Map<ShipType, Boolean> unlockMap = this.fileManager.loadShipUnlocks();
      initUnlockStatesFromMap(unlockMap);
    } catch (IOException e) {
      setDefaultLockedStates();
    }
  }

  private void initUnlockStatesFromMap(final Map<ShipType, Boolean> unlockMap) {
    unlockedStates[0] = unlockMap.getOrDefault(ShipType.NORMAL, true);
    unlockedStates[1] = unlockMap.getOrDefault(ShipType.BIG_SHOT, false);
    unlockedStates[2] = unlockMap.getOrDefault(ShipType.DOUBLE_SHOT, false);
    unlockedStates[3] = unlockMap.getOrDefault(ShipType.MOVE_FAST, false);
  }

  private void setDefaultLockedStates() {
    unlockedStates[0] = true;
    unlockedStates[1] = false;
    unlockedStates[2] = false;
    unlockedStates[3] = false;
  }

  public int getCoins() {
    return coins;
  }

  public boolean[] getUnlockedStates() {
    return unlockedStates;
  }

  public boolean isSelectedShipUnlocked(final int selectedIndex) {
    if (selectedIndex < 0 || selectedIndex >= unlockedStates.length) {
      return false;
    }
    return unlockedStates[selectedIndex];
  }

  public boolean isUnlocked(final int index) {
    return index >= 0 && index < unlockedStates.length && unlockedStates[index];
  }

  public void tryUnlock(final int index) {
    if (isUnlocked(index)) {
      return;
    }

    final int cost = getShipUnlockCost(index);
    if (coins < cost) {
      showNotEnoughCoinsToast();
      return;
    }

    coins -= cost;
    unlockedStates[index] = true;
    saveUnlockState(index);
    SoundManager.playeffect(SELECT_SOUND);
  }

  public void updateToast() {
    if (coinsToast != null && coinsCooldown != null && coinsCooldown.checkFinished()) {
      coinsToastActive = false;
    }
  }

  public boolean isToastActive() {
    return coinsToastActive && coinsToast != null;
  }

  public Achievement getCoinsToast() {
    return coinsToast;
  }

  private int getShipUnlockCost(final int index) {
    return switch (index) {
      case 0 -> // NORMAL
          0;
      case 1 -> // BIG_SHOT
          2000;
      case 2 -> // DOUBLE_SHOT
          3500;
      case 3 -> // MOVE_FAST
          5000;
      default -> 0;
    };
  }

  private ShipType getShipTypeByIndex(final int index) {
    return switch (index) {
      case 1 -> ShipType.BIG_SHOT;
      case 2 -> ShipType.DOUBLE_SHOT;
      case 3 -> ShipType.MOVE_FAST;
      default -> ShipType.NORMAL;
    };
  }

  private void showNotEnoughCoinsToast() {
    SoundManager.playeffect(HOVER_SOUND);
    coinsToast = new Achievement("NOT ENOUGH COINS", "");
    coinsCooldown = Core.getCooldown(3000);
    coinsCooldown.reset();
    coinsToastActive = true;
  }

  private void saveUnlockState(final int index) {
    try {
      final Map<ShipType, Boolean> unlockMap = this.fileManager.loadShipUnlocks();
      updateUnlockMap(unlockMap, index);
      this.fileManager.saveShipUnlocks(unlockMap);
      this.fileManager.saveCoins(this.coins);
    } catch (IOException e) {
      this.logger.warning("Failed to save ship unlock state: " + e.getMessage());
    }
  }

  private void updateUnlockMap(final Map<ShipType, Boolean> unlockMap, final int index) {
    unlockMap.put(getShipTypeByIndex(index), true);
  }
}
