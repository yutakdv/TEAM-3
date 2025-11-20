package screen;

import engine.Core;
import engine.SoundManager;
import entity.Entity;
import entity.Ship;
import java.awt.*;
import java.awt.event.KeyEvent;

import java.io.IOException;
import entity.Ship.ShipType;
import engine.Core;

public class ShipSelectionScreen extends Screen {
  private int selectedShipIndex = 0; // 0: NORMAL, 1: BIG_SHOT, 2: DOUBLE_SHOT, 3: MOVE_FAST
  private Ship[] shipExamples = new Ship[4];

  private Integer hovershipIndex = null;
  private Integer prevHoverIndex = null;
  private int coins;

  private int player;
  private boolean backSelected = false; // If current state is on the back button, can't select ship

  private boolean[] unlockedStates = new boolean[4];

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
      var unlockMap = Core.getFileManager().loadShipUnlocks();

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
    draw();
    if (inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)) {
      this.returnCode = (player == 1) ? 5 : 6;
      SoundManager.playeffect("sound/select.wav");
      this.isRunning = false;
      return;
    }
    if (inputManager.isKeyPressed(KeyEvent.VK_UP) || inputManager.isKeyPressed(KeyEvent.VK_W)) {
      backSelected = true;
    }
    if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) || inputManager.isKeyPressed(KeyEvent.VK_S)) {
      backSelected = false;
    }

    int mx = inputManager.getMouseX();
    int my = inputManager.getMouseY();

    java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);
    java.awt.Rectangle[] shipBoxes = drawManager.getShipSelectionHitboxes(this, shipExamples);

    boolean mouseHovering = backBox.contains(mx, my);
    if (!mouseHovering) {
      for (java.awt.Rectangle r : shipBoxes) {
        if (r.contains(mx, my)) {
          mouseHovering = true;
          break;
        }
      }
    }

    if (!backSelected) {
      if (inputManager.isKeyPressed(KeyEvent.VK_LEFT) || inputManager.isKeyPressed(KeyEvent.VK_A)) {
        this.selectedShipIndex = this.selectedShipIndex - 1;
        if (this.selectedShipIndex < 0) {
          this.selectedShipIndex += 4;
        }
        this.selectedShipIndex = this.selectedShipIndex % 4;
        if (!mouseHovering) {
          SoundManager.playeffect("sound/hover.wav");
        }
      }
      if (inputManager.isKeyPressed(KeyEvent.VK_RIGHT)
          || inputManager.isKeyPressed(KeyEvent.VK_D)) {
        this.selectedShipIndex = (this.selectedShipIndex + 1) % 4;
        if (!mouseHovering) {
          SoundManager.playeffect("sound/hover.wav");
        }
      }
    }
    if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
      if (backSelected) {
        switch (player) {
          case 1 -> this.returnCode = 5;
          case 2 -> this.returnCode = 6;
        }
        SoundManager.playeffect("sound/select.wav");
        this.isRunning = false;
        return;
      }

      if (!isSelectedShipUnlocked()) {
        SoundManager.playeffect("sound/hover.wav");
        return;
      }
      switch (player) {
        case 1 -> this.returnCode = 6;
        case 2 -> this.returnCode = 2;
      }
      SoundManager.playeffect("sound/select.wav");
      this.isRunning = false;
    }

    boolean clicked = inputManager.isMouseClicked();

    prevHoverIndex = hovershipIndex;
    hovershipIndex = null;

    for (int i = 0; i < shipBoxes.length; i++) {
      if (shipBoxes[i].contains(mx, my)) {
        hovershipIndex = i;
        this.selectedShipIndex = i;
        break;
      }
    }
    if (hovershipIndex != null && !hovershipIndex.equals(prevHoverIndex)) {
      SoundManager.playeffect("sound/hover.wav");
    }

    if (clicked) {
      if (backBox.contains(mx, my)) {
        this.returnCode = (player == 1) ? 5 : 6;
        SoundManager.playeffect("sound/select.wav");
        this.isRunning = false;
        return;
      }
      if (hovershipIndex != null) {
        int clickedIndex = hovershipIndex;

        boolean unlocked = (unlockedStates == null)
                || (clickedIndex >= 0 && clickedIndex < unlockedStates.length
                && unlockedStates[clickedIndex]);
        if (!unlocked) {
          SoundManager.playeffect("sound/hover.wav");
          return;
        }
        this.selectedShipIndex = clickedIndex;

        switch (player) {
          case 1 -> this.returnCode = 6;
          case 2 -> this.returnCode = 2;
        }
        SoundManager.playeffect("sound/select.wav");
        this.isRunning = false;
      }
    }
  }

  private void draw() {
    drawManager.initDrawing(this);

    drawManager.drawShipSelectionMenu(
        this, shipExamples, this.selectedShipIndex, this.player, this.unlockedStates);
    drawManager.drawShipSelectionCoins(this, this.coins);

    // hover highlight
    int mx = inputManager.getMouseX();
    int my = inputManager.getMouseY();
    java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);
    boolean backHover = backBox.contains(mx, my);
    drawManager.drawBackButton(this, backHover || backSelected);

    drawManager.completeDrawing(this);
  }

  private boolean isSelectedShipUnlocked() {
    if (unlockedStates == null) return true;
    if (selectedShipIndex < 0 || selectedShipIndex >= unlockedStates.length) return false;
    return unlockedStates[selectedShipIndex];
  }
}
