package screen;

import engine.*; // NOPMD - used import
import entity.Entity;
import entity.Ship;

import java.awt.event.KeyEvent;

import java.io.IOException;
import entity.Ship.ShipType;

public class ShipSelectionScreen extends Screen {
  private int selectedShipIndex; // 0: NORMAL, 1: BIG_SHOT, 2: DOUBLE_SHOT, 3: MOVE_FAST, default 0
  private Ship[] shipExamples = new Ship[4];

  private int hovershipIndex = -1;
  private int prevHoverIndex = -1; // NOPMD - Should kept as a filed
  private int coins;

  private final int player;
  private boolean backSelected; // If current state is on the back button, can't select ship

  private boolean[] unlockedStates = new boolean[4];

  private Achievement CoinsToast;
  private Cooldown CoinsCooldown;

  private static final String SELECT_SOUND = "sound/select.wav";
  private static final String HOVER_SOUND = "sound/hover.wav";

  private boolean coinsToastActive;

  public ShipSelectionScreen(final int width, final int height, final int fps, final int player) {
    super(width, height, fps);
    this.player = player;
    SoundManager.playBGM("sound/menu_sound.wav");

    this.coins = Core.getFileManager().loadCoins();

    if (player == 1) {
      shipExamples[0] =
          new Ship(width / 2 - 100, height / 2, Entity.Team.PLAYER1, Ship.ShipType.NORMAL, null);
      shipExamples[1] =
          new Ship(width / 2 - 35, height / 2, Entity.Team.PLAYER1, Ship.ShipType.BIG_SHOT, null);
      shipExamples[2] =
          new Ship(
              width / 2 + 35, height / 2, Entity.Team.PLAYER1, Ship.ShipType.DOUBLE_SHOT, null);
      shipExamples[3] =
          new Ship(width / 2 + 100, height / 2, Entity.Team.PLAYER1, Ship.ShipType.MOVE_FAST, null);
    } else if (player == 2) {
      shipExamples[0] =
          new Ship(width / 2 - 100, height / 2, Entity.Team.PLAYER2, Ship.ShipType.NORMAL, null);
      shipExamples[1] =
          new Ship(width / 2 - 35, height / 2, Entity.Team.PLAYER2, Ship.ShipType.BIG_SHOT, null);
      shipExamples[2] =
          new Ship(
              width / 2 + 35, height / 2, Entity.Team.PLAYER2, Ship.ShipType.DOUBLE_SHOT, null);
      shipExamples[3] =
          new Ship(width / 2 + 100, height / 2, Entity.Team.PLAYER2, Ship.ShipType.MOVE_FAST, null);
    }

    try {
      final var unlockMap = Core.getFileManager().loadShipUnlocks();

      // NORMAL = Bronze
      unlockedStates[0] = unlockMap.getOrDefault(ShipType.NORMAL, true);
      // BIG_SHOT = Silver
      unlockedStates[1] = unlockMap.getOrDefault(ShipType.BIG_SHOT, false);
      // DOUBLE_SHOT = Gold
      unlockedStates[2] = unlockMap.getOrDefault(ShipType.DOUBLE_SHOT, false);
      // MOVE_FAST = Platinum
      unlockedStates[3] = unlockMap.getOrDefault(ShipType.MOVE_FAST, false);

    } catch (IOException e) {
      unlockedStates[0] = true;
      unlockedStates[1] = false;
      unlockedStates[2] = false;
      unlockedStates[3] = false;
    }
  }

  /**
   * Returns the selected ship type to Core.
   *
   * @return The selected ShipType enum.
   */
  public Ship.ShipType getSelectedShipType() {
    switch (this.selectedShipIndex) {
      case 1:
        return Ship.ShipType.BIG_SHOT;
      case 2:
        return Ship.ShipType.DOUBLE_SHOT;
      case 3:
        return Ship.ShipType.MOVE_FAST;
      case 0:
      default:
        return Ship.ShipType.NORMAL;
    }
  }

  public final int run() {
    super.run();
    return this.returnCode;
  }

  protected final void update() {
    super.update();

    if (handleEscape()) {
      return;
    }

    updateBackSelectionByKeys();

    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY();

    final java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);
    final java.awt.Rectangle[] shipBoxes = drawManager.getShipSelectionHitboxes(this, shipExamples);

    final boolean mouseHovering = updateMouseHoverAndToast(mx, my, backBox, shipBoxes);

    handleArrowNavigation(mouseHovering);

    if (handleSpaceSelection()) {
      return;
    }

    updateHoverIndex(mx, my, shipBoxes);
    handleMouseClick(mx, my, backBox);

    draw();
  }

  private void draw() {

    drawManager.initDrawing(this);

    drawManager.drawShipSelectionMenu(
        this, shipExamples, this.selectedShipIndex, this.player, this.unlockedStates);
    drawManager.drawShipSelectionCoins(this, this.coins);

    // hover highlight
    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY();
    final java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);
    final boolean backHover = backBox.contains(mx, my); // NOPMD - LawofDemeter
    drawManager.drawBackButton(this, backHover || backSelected);

    if (coinsToastActive && CoinsToast != null) {
      final java.util.List<Achievement> list = new java.util.ArrayList<>();
      list.add(CoinsToast);
      drawManager.drawAchievementToasts(this, list);
    }

    drawManager.completeDrawing(this);
  }

  private boolean isSelectedShipUnlocked() {
    if (unlockedStates == null) {
      return true;
    }
    if (selectedShipIndex < 0 || selectedShipIndex >= unlockedStates.length) {
      return false;
    }
    return unlockedStates[selectedShipIndex];
  }

  private int getShipUnlockCost(final int index) {
    switch (index) {
      case 0: // Bronze (NORMAL)
        return 0;
      case 1: // Silver (BIG_SHOT)
        return 2000;
      case 2: // Gold (DOUBLE_SHOT)
        return 3500;
      case 3: // Platinum (MOVE_FAST)
        return 5000;
      default:
        return 0;
    }
  }

  private ShipType getShipTypeByIndex(final int index) {
    switch (index) {
      case 0:
        return ShipType.NORMAL;
      case 1:
        return ShipType.BIG_SHOT;
      case 2:
        return ShipType.DOUBLE_SHOT;
      case 3:
        return ShipType.MOVE_FAST;
      default:
        return ShipType.NORMAL;
    }
  }

  private boolean tryUnlockShip(final int index) {
    if (isUnlocked(index)) {
      return true;
    }
    final int cost = getShipUnlockCost(index);

    if (coins < cost) {
      return handleCoins();
    }
    coins -= cost;
    unlockedStates[index] = true;

    saveUnlockState(index);

    SoundManager.playeffect(SELECT_SOUND);
    return true;
  }

  private boolean isUnlocked(final int index) {
    return unlockedStates != null
        && index >= 0
        && index < unlockedStates.length
        && unlockedStates[index];
  }

  private boolean handleCoins() {
    SoundManager.playeffect(HOVER_SOUND);
    CoinsToast = new Achievement("NOT ENOUGH COINS", "");
    CoinsCooldown = Core.getCooldown(3000);
    CoinsCooldown.reset();
    coinsToastActive = true;
    return false;
  }

  private void saveUnlockState(final int index) {
    try {
      final var fm = Core.getFileManager();
      fm.saveCoins(coins); // NOPMD - LawofDemeter(why???)

      final var unlockMap = fm.loadShipUnlocks(); // NOPMD - LawofDemeter
      unlockMap.put(getShipTypeByIndex(index), true); // NOPMD - LawofDemeter
      fm.saveShipUnlocks(unlockMap); // NOPMD - LawofDemeter

    } catch (IOException e) {
      Core.getLogger() // NOPMD - LawofDemeter
          .warning("Failed to save ship unlock state: " + e.getMessage()); // NOPMD - LawofDemeter
    }
  }

  private boolean handleEscape() {
    if (inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)) {
      this.returnCode = (player == 1) ? 5 : 6;
      SoundManager.playeffect(SELECT_SOUND);
      this.isRunning = false;
      return true;
    }
    return false;
  }

  private void updateBackSelectionByKeys() {
    if (inputManager.isKeyPressed(KeyEvent.VK_UP) || inputManager.isKeyPressed(KeyEvent.VK_W)) {
      backSelected = true;
    }
    if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) || inputManager.isKeyPressed(KeyEvent.VK_S)) {
      backSelected = false;
    }
  }

  private boolean updateMouseHoverAndToast(
      final int mx,
      final int my,
      final java.awt.Rectangle backBox,
      final java.awt.Rectangle[] shipBoxes) { // NOPMD - Do not need varargs

    boolean mouseHovering = backBox.contains(mx, my); // NOPMD - LawofDemeter
    if (!mouseHovering) {
      for (final java.awt.Rectangle r : shipBoxes) {
        if (r.contains(mx, my)) {
          mouseHovering = true;
          backSelected = false;
          break;
        }
      }
      if (CoinsToast != null && CoinsCooldown != null && CoinsCooldown.checkFinished()) {
        coinsToastActive = false;
      }
    }
    return mouseHovering;
  }

  private void handleArrowNavigation(final boolean mouseHovering) {
    if (backSelected) {
      return;
    }

    if (inputManager.isKeyPressed(KeyEvent.VK_LEFT) || inputManager.isKeyPressed(KeyEvent.VK_A)) {
      this.selectedShipIndex = this.selectedShipIndex - 1;
      if (this.selectedShipIndex < 0) {
        this.selectedShipIndex += 4;
      }
      this.selectedShipIndex = this.selectedShipIndex % 4;
      if (!mouseHovering) {
        SoundManager.playeffect(HOVER_SOUND);
      }
    }

    if (inputManager.isKeyPressed(KeyEvent.VK_RIGHT) || inputManager.isKeyPressed(KeyEvent.VK_D)) {
      this.selectedShipIndex = (this.selectedShipIndex + 1) % 4;
      if (!mouseHovering) {
        SoundManager.playeffect(HOVER_SOUND);
      }
    }
  }

  private boolean handleSpaceSelection() {
    if (!inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
      return false;
    }

    if (backSelected) {
      if (player == 1) {
        this.returnCode = 5;
      } else if (player == 2) {
        this.returnCode = 6;
      }
      SoundManager.playeffect(SELECT_SOUND);
      this.isRunning = false;
      return true;
    }

    if (!isSelectedShipUnlocked()) {
      tryUnlockShip(selectedShipIndex);
      return true;
    }

    if (player == 1) {
      this.returnCode = 6;
    } else if (player == 2) {
      this.returnCode = 2;
    }
    SoundManager.playeffect(SELECT_SOUND);
    this.isRunning = false;
    return true;
  }

  private void updateHoverIndex(final int mx, final int my, final java.awt.Rectangle[] shipBoxes) { //NOPMD - DO not need varargs
    prevHoverIndex = hovershipIndex;
    hovershipIndex = -1;

    for (int i = 0; i < shipBoxes.length; i++) {
      if (shipBoxes[i].contains(mx, my)) {
        hovershipIndex = i;
        this.selectedShipIndex = i;
        break;
      }
    }

    if (hovershipIndex != -1 && hovershipIndex != prevHoverIndex) {
      SoundManager.playeffect(HOVER_SOUND);
    }
  }

  private void handleMouseClick(final int mx, final int my, final java.awt.Rectangle backBox) {

    final boolean clicked = inputManager.isMouseClicked();
    if (!clicked) {
      return;
    }

    if (backBox.contains(mx, my)) {
      this.returnCode = (player == 1) ? 5 : 6;
      SoundManager.playeffect(SELECT_SOUND);
      this.isRunning = false;
      return;
    }

    if (hovershipIndex == -1) {
      return;
    }

    final int clickedIndex = hovershipIndex;

    final boolean unlocked =
        unlockedStates == null
            || clickedIndex >= 0
                && clickedIndex < unlockedStates.length
                && unlockedStates[clickedIndex];

    if (!unlocked) {
      tryUnlockShip(clickedIndex);
      this.selectedShipIndex = clickedIndex;
      return;
    }

    this.selectedShipIndex = clickedIndex;

    if (player == 1) {
      this.returnCode = 6;
    } else if (player == 2) {
      this.returnCode = 2;
    }
    SoundManager.playeffect(SELECT_SOUND);
    this.isRunning = false;
  }
}
