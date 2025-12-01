// This class is for ShipSelectionScreen

package engine;

public class ShipSelectionResult {
  public final boolean exitScreen;
  public final int returnCode;
  public final int selectedShipIndex;
  public final boolean backSelected;
  public final int hoverShipIndex;

  public ShipSelectionResult(
      final boolean exitScreen,
      final int returnCode,
      final int selectedShipIndex,
      final boolean backSelected,
      final int hoverShipIndex) {
    this.exitScreen = exitScreen;
    this.returnCode = returnCode;
    this.selectedShipIndex = selectedShipIndex;
    this.backSelected = backSelected;
    this.hoverShipIndex = hoverShipIndex;
  }

  public static ShipSelectionResult noChange(
      final int selectedShipIndex, final boolean backSelected, final int hoverShipIndex) {
    return new ShipSelectionResult(false, -1, selectedShipIndex, backSelected, hoverShipIndex);
  }
}
