// This class is for ShipSelectionScreen
package engine;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;

public final class ShipSelectionInteraction {

  private static final int SHIP_COUNT = 4;
  private static final String SELECT_SOUND = "sound/select.wav";
  private static final String HOVER_SOUND = "sound/hover.wav";

  private ShipSelectionInteraction() {}

  private static final class InteractionState {
    int selectedIndex;
    boolean backSelected;
    int hoverIndex;
    boolean mouseHovering;

    InteractionState(final int selectedIndex, final boolean backSelected, final int hoverIndex) {
      this.selectedIndex = selectedIndex;
      this.backSelected = backSelected;
      this.hoverIndex = hoverIndex;
      this.mouseHovering = false;
    }
  }

  public static ShipSelectionResult handle(
      final ShipSelectionInput input,
      final int currentSelectedIndex,
      final boolean currentBackSelected,
      final int currentHoverIndex) {

    final InteractionState state =
        new InteractionState(currentSelectedIndex, currentBackSelected, currentHoverIndex);

    final ShipSelectionResult escapeResult = handleEscape(input, state);
    if (escapeResult != null) {
      return escapeResult;
    }

    updateBackSelection(input, state);

    updateHover(input, state, currentHoverIndex);

    updateArrowNavigation(input, state);

    final ShipSelectionResult spaceResult = handleSpace(input, state);
    if (spaceResult != null) {
      return spaceResult;
    }

    final ShipSelectionResult clickResult = handleMouseClick(input, state);
    if (clickResult != null) {
      return clickResult;
    }

    return ShipSelectionResult.noChange(state.selectedIndex, state.backSelected, state.hoverIndex);
  }

  private static ShipSelectionResult handleEscape(
      final ShipSelectionInput input, final InteractionState state) {
    if (!input.inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)) { // NOPMD - LawOfDemeter
      return null;
    }

    final int returnCode = (input.player == 1) ? 5 : 6;
    playSelectSound();
    return new ShipSelectionResult(
        true, returnCode, state.selectedIndex, state.backSelected, state.hoverIndex);
  }

  private static void updateBackSelection(
      final ShipSelectionInput input, final InteractionState state) {
    if (input.inputManager.isKeyPressed(KeyEvent.VK_UP) // NOPMD - LawOfDemeter
        || input.inputManager.isKeyPressed(KeyEvent.VK_W)) { // NOPMD - LawOfDemeter
      state.backSelected = true;
    }
    if (input.inputManager.isKeyPressed(KeyEvent.VK_DOWN) // NOPMD - LawOfDemeter
        || input.inputManager.isKeyPressed(KeyEvent.VK_S)) { // NOPMD - LawOfDemeter
      state.backSelected = false;
    }
  }

  private static void updateHover(
      final ShipSelectionInput input, final InteractionState state, final int previousHoverIndex) {

    state.mouseHovering =
        input.backBox.contains(input.mouseX, input.mouseY); // NOPMD - LawOfDemeter
    state.hoverIndex = previousHoverIndex;

    if (!state.mouseHovering) {
      state.hoverIndex = -1;
      for (int i = 0; i < input.shipBoxes.length; i++) {
        final Rectangle box = input.shipBoxes[i];
        if (box.contains(input.mouseX, input.mouseY)) { // NOPMD - LawOfDemeter
          state.hoverIndex = i;
          state.backSelected = false;
          state.mouseHovering = true;
          break;
        }
      }
    }

    input.unlockManager.updateToast(); // NOPMD - LawOfDemeter

    if (state.hoverIndex != -1 && state.hoverIndex != previousHoverIndex) {
      state.selectedIndex = state.hoverIndex;
      playHoverSound();
    }
  }

  private static void updateArrowNavigation(
      final ShipSelectionInput input, final InteractionState state) {
    if (state.backSelected) {
      return;
    }

    if (input.inputManager.isKeyPressed(KeyEvent.VK_LEFT) // NOPMD - LawOfDemeter
        || input.inputManager.isKeyPressed(KeyEvent.VK_A)) { // NOPMD - LawOfDemeter
      state.selectedIndex = (state.selectedIndex - 1 + SHIP_COUNT) % SHIP_COUNT;
      if (!state.mouseHovering) {
        playHoverSound();
      }
    }

    if (input.inputManager.isKeyPressed(KeyEvent.VK_RIGHT) // NOPMD - LawOfDemeter
        || input.inputManager.isKeyPressed(KeyEvent.VK_D)) { // NOPMD - LawOfDemeter
      state.selectedIndex = (state.selectedIndex + 1) % SHIP_COUNT;
      if (!state.mouseHovering) {
        playHoverSound();
      }
    }
  }

  private static ShipSelectionResult handleSpace(
      final ShipSelectionInput input, final InteractionState state) {
    if (!input.inputManager.isKeyPressed(KeyEvent.VK_SPACE)) { // NOPMD - LawOfDemeter
      return null;
    }

    if (state.backSelected) {
      final int returnCode = (input.player == 1) ? 5 : 6;
      playSelectSound();
      return new ShipSelectionResult(
          true, returnCode, state.selectedIndex, state.backSelected, state.hoverIndex);
    }

    if (!input.unlockManager.isSelectedShipUnlocked(state.selectedIndex)) { // NOPMD - LawOfDemeter
      input.unlockManager.tryUnlock(state.selectedIndex); // NOPMD - LawOfDemeter
      return ShipSelectionResult.noChange(
          state.selectedIndex, state.backSelected, state.hoverIndex);
    }

    final int returnCode = (input.player == 1) ? 6 : 2;
    playSelectSound();
    return new ShipSelectionResult(
        true, returnCode, state.selectedIndex, state.backSelected, state.hoverIndex);
  }

  private static ShipSelectionResult handleMouseClick(
      final ShipSelectionInput input, final InteractionState state) {
    if (!input.inputManager.isMouseClicked()) { // NOPMD - LawOfDemeter
      return null;
    }

    if (input.backBox.contains(input.mouseX, input.mouseY)) { // NOPMD - LawOfDemeter
      final int returnCode = (input.player == 1) ? 5 : 6;
      playSelectSound();
      return new ShipSelectionResult(
          true, returnCode, state.selectedIndex, state.backSelected, state.hoverIndex);
    }

    if (state.hoverIndex == -1) {
      return ShipSelectionResult.noChange(
          state.selectedIndex, state.backSelected, state.hoverIndex);
    }

    if (!input.unlockManager.isUnlocked(state.hoverIndex)) { // NOPMD - LawOfDemeter
      input.unlockManager.tryUnlock(state.hoverIndex); // NOPMD - LawOfDemeter
      state.selectedIndex = state.hoverIndex;
      return ShipSelectionResult.noChange(
          state.selectedIndex, state.backSelected, state.hoverIndex);
    }

    state.selectedIndex = state.hoverIndex;
    final int returnCode = (input.player == 1) ? 6 : 2;
    playSelectSound();
    return new ShipSelectionResult(
        true, returnCode, state.selectedIndex, state.backSelected, state.hoverIndex);
  }

  private static void playSelectSound() {
    SoundManager.playeffect(SELECT_SOUND); // NOPMD - LawOfDemeter: centralized static audio call
  }

  private static void playHoverSound() {
    SoundManager.playeffect(HOVER_SOUND); // NOPMD - LawOfDemeter: centralized static audio call
  }
}
