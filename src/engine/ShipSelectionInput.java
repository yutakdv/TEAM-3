// This class is for ShipSelectionScreen input context
package engine;

import java.awt.Rectangle;

public class ShipSelectionInput {

  final InputManager inputManager;
  final ShipUnlockManager unlockManager;
  final int player;
  final Rectangle backBox;
  final Rectangle[] shipBoxes;
  final int mouseX;
  final int mouseY;

  public ShipSelectionInput(
      final InputManager inputManager,
      final ShipUnlockManager unlockManager,
      final int player,
      final Rectangle backBox,
      final Rectangle[] shipBoxes,
      final int mouseX,
      final int mouseY) {
    this.inputManager = inputManager;
    this.unlockManager = unlockManager;
    this.player = player;
    this.backBox = backBox;
    this.shipBoxes = shipBoxes == null ? new Rectangle[0] : shipBoxes.clone();
    this.mouseX = mouseX;
    this.mouseY = mouseY;
  }
}
