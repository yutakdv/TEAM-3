package engine;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import screen.SettingScreen;

@SuppressWarnings("PMD.LawOfDemeter")
public class SettingControl {

  private static final int MENU_VOLUME = 0;
  private static final int MENU_P1_KEYS = 1;
  private static final int MENU_P2_KEYS = 2;
  private static final int MENU_BACK = -1;

  private static final String SOUND_SELECT = "sound/select.wav";
  private static final String SOUND_HOVER = "sound/hover.wav";

  private final SettingScreen screen;
  private final InputManager inputManager;
  private final Cooldown inputCooldown;

  private final SettingVolumeHandler volumeHandler;
  private final SettingKeyHandler keyHandler;

  public SettingControl(final SettingScreen screen, final InputManager inputManager) {
    this.screen = screen;
    this.inputManager = inputManager;
    this.inputCooldown = Core.getCooldown(70);
    this.inputCooldown.reset();

    this.volumeHandler = new SettingVolumeHandler(screen, inputManager, inputCooldown);
    this.keyHandler = new SettingKeyHandler(screen, inputManager, inputCooldown);
  }

  public void update() {
    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY();
    final boolean clicked = inputManager.isMouseClicked();

    if (handleBackNavigation(mx, my, clicked)) {
      return;
    }

    handleMenuNavigation();

    screen.setEnableSoundMouseControl(screen.getSelectMenuItem() == MENU_VOLUME);

    if (screen.getSelectMenuItem() == MENU_VOLUME) {
      volumeHandler.handleVolumeSettings();
    } else if (screen.getSelectMenuItem() == MENU_P1_KEYS
        || screen.getSelectMenuItem() == MENU_P2_KEYS) {
      keyHandler.handleKeySettings();
    }

    if (handleGlobalEscape()) {
      return;
    }

    final boolean pressed = inputManager.isMousePressed();
    handleMouseInput(mx, my, pressed, clicked);
    handleReturnToTitle();
  }

  private boolean handleBackNavigation(final int mx, final int my, final boolean clicked) {
    final Rectangle backBox = screen.getBackButtonHitbox();
    if (clicked && backBox.contains(mx, my)) {
      screen.setReturnCode(1);
      SoundManager.playeffect(SOUND_SELECT);
      screen.setIsRunning(false);
      return true;
    }
    return false;
  }

  private void handleMenuNavigation() {
    if (screen.getSelectedSection() != 0) {
      return;
    }

    int menuItem = screen.getSelectMenuItem();
    final int maxItems = screen.getMenuItemCount();

    if (inputManager.isKeyPressed(KeyEvent.VK_UP)) {
      if (menuItem == MENU_BACK) {
        menuItem = maxItems - 1;
      } else if (menuItem == 0) {
        menuItem = MENU_BACK;
      } else {
        menuItem--;
      }
      SoundManager.playeffect(SOUND_HOVER);
    } else if (inputManager.isKeyPressed(KeyEvent.VK_DOWN)) {
      if (menuItem == MENU_BACK) {
        menuItem = 0;
      } else if (menuItem == maxItems - 1) {
        menuItem = MENU_BACK;
      } else {
        menuItem++;
      }
      SoundManager.playeffect(SOUND_HOVER);
    }
    screen.setSelectMenuItem(menuItem);
  }

  private boolean handleGlobalEscape() {
    if (inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)) {
      screen.setIsRunning(false);
      SoundManager.playeffect(SOUND_SELECT);
      this.inputCooldown.reset();
      return true;
    }
    return false;
  }

  private void handleMouseInput(
      final int mx, final int my, final boolean pressed, final boolean clicked) {
    if (screen.getSelectMenuItem() == MENU_VOLUME) {
      volumeHandler.handleMouseVolumeControl(mx, my, pressed);
      if (screen.isEnableSoundMouseControl()) {
        volumeHandler.handleMouseMuteControl(mx, my, clicked);
      }
    }

    for (int i = 0; i < screen.getMenuItemCount(); i++) {
      final Rectangle menuBox = screen.getSettingMenuHitbox(i);
      if (clicked && menuBox.contains(mx, my) && screen.getSelectMenuItem() != i) {
        switchMenuByMouse(i);
        break;
      }
    }
  }

  private void switchMenuByMouse(final int index) {
    if (screen.isWaitingForNewKey()) {
      final int idx = screen.getSelectedKeyIndex();
      if (idx >= 0) {
        screen.setKeySelected(idx, false);
      }
      screen.setWaitingForNewKey(false);
    }
    screen.setSelectMenuItem(index);
    screen.setSelectedSection(0);
    this.inputCooldown.reset();

    SoundManager.playeffect(SOUND_SELECT);

    if (index == MENU_P1_KEYS || index == MENU_P2_KEYS) {
      screen.setSelectedSection(1);
      screen.setSelectedKeyIndex(0);
      SoundManager.playeffect(SOUND_SELECT);
    }
  }

  private void handleReturnToTitle() {
    if (inputManager.isKeyDown(KeyEvent.VK_SPACE) && this.inputCooldown.checkFinished()) {
      if (screen.getSelectMenuItem() == MENU_BACK) {
        screen.setReturnCode(1);
        screen.setIsRunning(false);
        SoundManager.playeffect(SOUND_SELECT);
      }
      this.inputCooldown.reset();
    }
  }
}
