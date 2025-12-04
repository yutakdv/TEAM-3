package screen;

import engine.*; // NOPMD - used import
import entity.Entity;
import entity.Ship;

@SuppressWarnings("PMD.LawOfDemeter")
public class ShipSelectionScreen extends Screen {
  private int selectedShipIndex; // 0: NORMAL, 1: BIG_SHOT, 2: DOUBLE_SHOT, 3: MOVE_FAST, default 0
  private final Ship[] shipExamples = new Ship[4];

  private int hovershipIndex = -1;

  private final int player;
  private boolean backSelected; // If current state is on the back button, can't select ship

  private final ShipUnlockManager unlockManager;

  public ShipSelectionScreen(final int width, final int height, final int fps, final int player) {
    super(width, height, fps);
    this.player = player;
    SoundManager.playBGM("sound/menu_sound.wav");

    this.unlockManager = new ShipUnlockManager();

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
  }

  /**
   * Returns the selected ship type to Core.
   *
   * @return The selected ShipType enum.
   */
  public Ship.ShipType getSelectedShipType() {
    return switch (this.selectedShipIndex) {
      case 1 -> Ship.ShipType.BIG_SHOT;
      case 2 -> Ship.ShipType.DOUBLE_SHOT;
      case 3 -> Ship.ShipType.MOVE_FAST;
      default -> Ship.ShipType.NORMAL;
    };
  }

  public final int run() {
    super.run();
    return this.returnCode;
  }

  protected final void update() {
    super.update();

    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY();

    final java.awt.Rectangle backBox = drawManager.menu().getBackButtonHitbox();
    final java.awt.Rectangle[] shipBoxes =
        drawManager.menu().getShipSelectionHitboxes(shipExamples);

    final ShipSelectionInput input =
        new ShipSelectionInput(
            inputManager, unlockManager, this.player, backBox, shipBoxes, mx, my);

    final ShipSelectionResult result =
        ShipSelectionInteraction.handle(
            input, this.selectedShipIndex, this.backSelected, this.hovershipIndex);

    this.selectedShipIndex = result.selectedShipIndex;
    this.backSelected = result.backSelected;
    this.hovershipIndex = result.hoverShipIndex;

    if (result.exitScreen) {
      this.returnCode = result.returnCode;
      this.isRunning = false;
    }

    draw();
  }

  private void draw() {

    drawManager.initDrawing(this);

    drawManager
        .menu()
        .drawShipSelectionMenu(
            this,
            shipExamples,
            this.selectedShipIndex,
            this.player,
            unlockManager.getUnlockedStates());
    drawManager.hud().drawShipSelectionCoins(this, unlockManager.getCoins());

    // hover highlight
    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY();
    final java.awt.Rectangle backBox = drawManager.menu().getBackButtonHitbox();
    final boolean backHover = backBox.contains(mx, my);
    drawManager.menu().drawBackButton(backHover || backSelected);

    if (unlockManager.isToastActive()) {
      final java.util.List<Achievement> list = new java.util.ArrayList<>();
      list.add(unlockManager.getCoinsToast());
      drawManager.hud().drawAchievementToasts(this, list);
    }

    drawManager.completeDrawing();
  }
}
