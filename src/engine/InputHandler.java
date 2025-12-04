package engine;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Set;
import entity.Bullet;
import entity.Ship;
import screen.GameScreen;

/**
 * Handles player input (movement and shooting). Extracted from GameScreen to reduce WMC and God
 * Class issues.
 */
public class InputHandler { // NOPMD

  private final InputManager inputManager;
  private final Cooldown menuCooldown;

  public InputHandler(final InputManager inputManager) {
    this.inputManager = inputManager;
    this.menuCooldown = Core.getCooldown(70);
    this.menuCooldown.reset();
  }

  /**
   * Updates ships based on player input.
   *
   * @param ships Array of player ships.
   * @param bullets Set of bullets to add new shots to.
   * @param state GameState to update shot counters.
   * @param screenWidth Width of the screen for boundary checks.
   */
  public void handleInput(
      final Ship[] ships, final Set<Bullet> bullets, final GameState state, final int screenWidth) {
    for (int p = 0; p < GameState.NUM_PLAYERS; p++) {
      final Ship ship = ships[p];
      if (ship == null || ship.isDestroyed()) { // NOPMD
        continue;
      }

      handleMovement(ship, p, screenWidth);
      handleShooting(ship, p, bullets, state);
    }
  }

  private void handleMovement(final Ship ship, final int playerIndex, final int screenWidth) {
    // 1P vs 2P Controls
    final boolean moveRight =
        (playerIndex == 0) ? inputManager.isP1RightPressed() : inputManager.isP2RightPressed();
    final boolean moveLeft =
        (playerIndex == 0) ? inputManager.isP1LeftPressed() : inputManager.isP2LeftPressed();

    // Boundary Checks
    final boolean isRightBorder =
        ship.getPositionX() + ship.getWidth() + ship.getSpeed() > screenWidth - 1;
    final boolean isLeftBorder = ship.getPositionX() - ship.getSpeed() < 1;

    if (moveRight && !isRightBorder) {
      ship.moveRight();
    }
    if (moveLeft && !isLeftBorder) {
      ship.moveLeft();
    }
  }

  private void handleShooting(
      final Ship ship, final int playerIndex, final Set<Bullet> bullets, final GameState state) {
    final boolean fire =
        (playerIndex == 0) ? inputManager.isP1ShootPressed() : inputManager.isP2ShootPressed();

    if (fire && ship.shoot(bullets)) {
      SoundManager.ingameeffect("sound/shoot.wav");
      state.incBulletsShot(playerIndex);
    }
  }

  public void handlePauseInput(
      final PauseMenuHandler handler, final DrawManager drawManager, final GameScreen screen) {
    handlePauseKeyboard(handler);
    handlePauseMouse(handler, drawManager, screen);
  }

  private void handlePauseKeyboard(PauseMenuHandler handler) { // NOPMD
    if (!menuCooldown.checkFinished()) {
      return;
    }

    if (handler.getSelectedSection() == 1) {
      handlePauseKeyboardNavigation(handler);
      handlePauseKeyboardVolumeActions(handler);
    }
  }

  private void handlePauseKeyboardNavigation(final PauseMenuHandler handler) {
    final int currentType = handler.getVolumeType();
    final int maxIndex = handler.getNumPauseSliders() - 1;

    if (inputManager.isKeyDown(KeyEvent.VK_UP) && currentType > 0) {
      handler.setVolumeType(currentType - 1);
      triggerMenuSound(handler);
    } else if (inputManager.isKeyDown(KeyEvent.VK_DOWN) && currentType < maxIndex) {
      handler.setVolumeType(currentType + 1);
      triggerMenuSound(handler);
    }
  }

  private void handlePauseKeyboardVolumeActions(final PauseMenuHandler handler) {
    final int currentType = handler.getVolumeType();

    if (inputManager.isKeyDown(KeyEvent.VK_LEFT)) {
      SoundControl.changeIngameVolume(currentType, -1);
      applyMenuAction();
    } else if (inputManager.isKeyDown(KeyEvent.VK_RIGHT)) {
      SoundControl.changeIngameVolume(currentType, 1);
      applyMenuAction();
    } else if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
      SoundControl.toggleIngameMute(currentType);
      applyMenuAction();
    }
  }

  private void handlePauseMouse(
      final PauseMenuHandler handler, final DrawManager drawManager, final GameScreen screen) {
    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY(); // NOPMD
    final boolean pressed = inputManager.isMousePressed();
    final boolean clicked = inputManager.isMouseClicked(); // NOPMD
    final int draggingIdx = handler.getDraggingIndex();
    final int numSliders = handler.getNumPauseSliders(); // NOPMD

    if (draggingIdx != -1) {
      handleMouseDrag(handler, drawManager, screen, mx, pressed, draggingIdx);
      return;
    }

    handleMouseSectionSelection(handler, drawManager, screen, mx, my, numSliders);
    if (handler.getSelectedSection() != 1) {
      return;
    }

    handleMouseInteractions(handler, drawManager, screen, mx, my, pressed, clicked, numSliders);
  }

  private void handleMouseDrag( // NOPMD
      final PauseMenuHandler handler,
      final DrawManager drawManager,
      final GameScreen screen,
      final int mx,
      final boolean pressed,
      final int draggingIdx) {

    if (pressed) {
      final Rectangle sliderBox = drawManager.settings().getpauseVolumeBarHitbox(screen, draggingIdx); // NOPMD
      setVolumeFromX(sliderBox, mx, draggingIdx);

      if (handler.getVolumeType() != draggingIdx) {
        handler.setVolumeType(draggingIdx);
        SoundControl.setIngameVolumetype(draggingIdx);
      }
    } else {
      handler.setDraggingIndex(-1);
    }
  }

  private void handleMouseSectionSelection(
      final PauseMenuHandler handler,
      final DrawManager drawManager,
      final GameScreen screen,
      final int mx,
      final int my,
      final int numSliders) {

    if (handler.getSelectedSection() != 1) {
      for (int i = 0; i < numSliders; i++) {
        if (drawManager.settings().getpauseVolumeBarHitbox(screen, i).contains(mx, my)) { // NOPMD
          handler.setSelectedSection(1);
          break;
        }
      }
    }
  }
    @SuppressWarnings("PMD.LawOfDemeter")
  private void handleMouseInteractions( // NOPMD
      final PauseMenuHandler handler,
      final DrawManager drawManager,
      final GameScreen screen,
      final int mx,
      final int my,
      final boolean pressed,
      final boolean clicked,
      final int numSliders) {

    for (int i = 0; i < numSliders; i++) {
      final Rectangle iconBox = drawManager.settings().getPauseSpeakerHitbox(screen, i);
      final Rectangle sliderBox = drawManager.settings().getpauseVolumeBarHitbox(screen, i); // NOPMD

      if (clicked && iconBox.contains(mx, my)) {
        handler.setVolumeType(i);
        SoundControl.setIngameVolumetype(i);
        SoundControl.toggleIngameMute(i);
        applyMenuAction();
        return;
      }

      if (pressed && sliderBox.contains(mx, my)) {
        handler.setVolumeType(i);
        handler.setDraggingIndex(i);
        SoundControl.setIngameVolumetype(i);
        setVolumeFromX(sliderBox, mx, i);
        return;
      }

      if (!pressed && (iconBox.contains(mx, my) || sliderBox.contains(mx, my))) {
        if (handler.getVolumeType() != i) {
          handler.setVolumeType(i);
          SoundControl.setIngameVolumetype(i);
          SoundManager.ingameeffect("sound/hover.wav");
        }
        break;
      }
    }
  }

  private void triggerMenuSound(final PauseMenuHandler handler) {
    SoundControl.setIngameVolumetype(handler.getVolumeType());
    SoundManager.ingameeffect("sound/hover.wav");
    menuCooldown.reset();
  }

  private void applyMenuAction() {
    SoundManager.updateVolume();
    menuCooldown.reset();
  }

  private void setVolumeFromX(final Rectangle barBox, final int mouseX, final int index) {
    double ratio = (double) (mouseX - barBox.x) / (double) barBox.width;
    ratio = Math.max(0.0, Math.min(1.0, ratio));
    final int val = (int) Math.round(ratio * 100.0);

    SoundControl.setIngameVolumeLevel(index, val);
    SoundControl.setIngameMute(index, false);
    SoundManager.updateVolume();
  }
}
