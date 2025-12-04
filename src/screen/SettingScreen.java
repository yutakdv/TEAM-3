package screen;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.logging.Level;

import engine.Cooldown;
import engine.Core;
import engine.SoundControl;
import engine.SoundManager;

/**
 * Implements the setting screen.
 * Refactored to drastically reduce Complexity (OCavg) by extracting logic and fix PMD logger issues.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public class SettingScreen extends Screen {
  private static final int MENU_VOLUME = 0;
  private static final int MENU_P1_KEYS = 1;
  private static final int MENU_P2_KEYS = 2;
  private static final int MENU_BACK = -1;

  private static final String SOUND_SELECT = "sound/select.wav";
  private static final String SOUND_HOVER = "sound/hover.wav";

  private final String[] menuItem = {"Volume", "1P Keyset", "2P Keyset"};
  private final String[] sliderTitles = {"Master", "BGM", "Effect Sound"};
  private final String[] keyItems = {"MOVE LEFT", "MOVE RIGHT", "ATTACK"};

  private int selectMenuItem;
  private Cooldown inputCooldown;
  private int volumelevel;
  private int volumetype;
  private int selectedSection;
  private int selectedKeyIndex;

  private final boolean[] keySelected = {false, false, false};
  private boolean waitingForNewKey;

  private final int[] player1Keys;
  private final int[] player2Keys;

  private final int[] volumeLevels = new int[3];
  private int draggingIndex = -1;
  private boolean enableSoundMouseControl;

  public SettingScreen(final int width, final int height, final int fps) {
    super(width, height, fps);
    this.returnCode = 1;
    this.player1Keys = Core.getInputManager().getPlayer1Keys();
    this.player2Keys = Core.getInputManager().getPlayer2Keys();
    SoundManager.playBGM("sound/menu_sound.wav");
  }

  @Override
  public final void initialize() {
    super.initialize();
    this.inputCooldown = Core.getCooldown(70);
    this.inputCooldown.reset();
    this.selectMenuItem = MENU_VOLUME;

    volumeLevels[0] = SoundControl.getVolumeLevel(0);
    volumeLevels[1] = SoundControl.getVolumeLevel(1);
    volumeLevels[2] = SoundControl.getVolumeLevel(2);

    this.volumetype = 0;
    this.volumelevel = volumeLevels[this.volumetype];
  }

  @Override
  public final int run() {
    super.run();
    SoundManager.stop();
    return this.returnCode;
  }

  @Override
  protected final void update() {
    super.update();

    if (handleBackNavigation()) {
      return;
    }

    handleMenuNavigation();

    this.enableSoundMouseControl = this.selectMenuItem == MENU_VOLUME;

    if (this.selectMenuItem == MENU_VOLUME) {
      handleVolumeSettings();
    } else if (this.selectMenuItem == MENU_P1_KEYS || this.selectMenuItem == MENU_P2_KEYS) {
      handleKeySettings();
    }

    if (handleGlobalEscape()) {
      return;
    }

    handleMouseInput();
    handleReturnToTitle();

    draw();
  }

  // --- Extracted Logic to Reduce Complexity ---

  private void handleMenuNavigation() {
    if (this.selectedSection != 0) {
      return;
    }

    if (inputManager.isKeyPressed(KeyEvent.VK_UP)) {
      if (this.selectMenuItem == MENU_BACK) {
        this.selectMenuItem = menuItem.length - 1;
      } else if (this.selectMenuItem == 0) {
        this.selectMenuItem = MENU_BACK;
      } else {
        this.selectMenuItem--;
      }
      SoundManager.playeffect(SOUND_HOVER);
    } else if (inputManager.isKeyPressed(KeyEvent.VK_DOWN)) {
      if (this.selectMenuItem == MENU_BACK) {
        this.selectMenuItem = 0;
      } else if (this.selectMenuItem == menuItem.length - 1) {
        this.selectMenuItem = MENU_BACK;
      } else {
        this.selectMenuItem++;
      }
      SoundManager.playeffect(SOUND_HOVER);
    }
  }

  private void handleVolumeSettings() {
    if (!this.inputCooldown.checkFinished()) {
      return;
    }

    if (selectedSection == 0 && inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
      this.selectedSection = 1;
      this.volumetype = 0;
      SoundManager.playeffect(SOUND_SELECT);
      this.inputCooldown.reset();
      return;
    }

    if (selectedSection == 1) {
      if (inputManager.isKeyPressed(KeyEvent.VK_BACK_SPACE)) {
        this.selectedSection = 0;
        SoundManager.playeffect(SOUND_SELECT);
      }

      handleVolumeAdjustment();
    }
  }

  private void handleVolumeAdjustment() {
    // Navigation
    if (inputManager.isKeyPressed(KeyEvent.VK_UP) && volumetype > 0) {
      this.volumetype--;
      this.volumelevel = volumeLevels[this.volumetype];
      SoundManager.playeffect(SOUND_HOVER);
    } else if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) && volumetype < sliderTitles.length - 1) {
      this.volumetype++;
      this.volumelevel = volumeLevels[this.volumetype];
      SoundManager.playeffect(SOUND_HOVER);
    }

    // Adjustment
    if (inputManager.isKeyDown(KeyEvent.VK_LEFT) && volumelevel > 0) {
      this.volumelevel--;
      applyVolumeChange();
    } else if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) && volumelevel < 100) {
      this.volumelevel++;
      applyVolumeChange();
    }

    // Mute
    if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
      SoundControl.setMute(this.volumetype, !SoundControl.isMuted(this.volumetype));
      SoundManager.updateVolume();
      this.inputCooldown.reset();
    }
  }

  private void applyVolumeChange() {
    SoundControl.setVolumeLevel(this.volumetype, this.volumelevel);
    SoundControl.setMute(this.volumetype, false);
    SoundManager.updateVolume();
    volumeLevels[this.volumetype] = this.volumelevel;
    this.inputCooldown.reset();
  }

  private void handleKeySettings() {
    if (!this.inputCooldown.checkFinished()) {
      return;
    }

    if (waitingForNewKey) {
      processNewKeyInput();
      return;
    }

    if (selectedSection == 0 && inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
      this.selectedSection = 1;
      this.selectedKeyIndex = 0;
      SoundManager.playeffect(SOUND_SELECT);
      this.inputCooldown.reset();
    } else if (selectedSection == 1) {
      if (inputManager.isKeyPressed(KeyEvent.VK_BACK_SPACE)) {
        selectedSection = 0;
        SoundManager.playeffect(SOUND_SELECT);
        this.inputCooldown.reset();
      } else if (inputManager.isKeyPressed(KeyEvent.VK_UP) && selectedKeyIndex > 0) {
        selectedKeyIndex--;
        SoundManager.playeffect(SOUND_HOVER);
      } else if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) && selectedKeyIndex < keyItems.length - 1) {
        selectedKeyIndex++;
        SoundManager.playeffect(SOUND_HOVER);
      } else if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
        keySelected[selectedKeyIndex] = true;
        waitingForNewKey = true;
        SoundManager.playeffect(SOUND_SELECT);
        this.inputCooldown.reset();
      }
    }
  }

  private void processNewKeyInput() {
    final int newKey = inputManager.getLastPressedKey();
    if (newKey == -1 || !this.inputCooldown.checkFinished()) {
      return;
    }

    if (newKey == KeyEvent.VK_ESCAPE || newKey == KeyEvent.VK_BACK_SPACE) {
      cancelKeyChange(newKey);
      return;
    }

    if (isKeyDuplicate(newKey)) {
      cancelKeyChange(newKey);
      return;
    }

    saveNewKey(newKey);
  }

  private boolean isKeyDuplicate(final int newKey) {
    final int[] targetKeys = (this.selectMenuItem == MENU_P1_KEYS) ? player1Keys : player2Keys;
    final int[] otherKeys = (this.selectMenuItem == MENU_P1_KEYS) ? player2Keys : player1Keys;

    for (int i = 0; i < targetKeys.length; i++) {
      if (i != selectedKeyIndex && targetKeys[i] == newKey || otherKeys[i] == newKey) {
        if (logger.isLoggable(Level.INFO)) {
          logger.info("Key already in use: " + KeyEvent.getKeyText(newKey));
        }
        return true;
      }
    }
    return false;
  }

  private void cancelKeyChange(final int key) {
    if (logger.isLoggable(Level.INFO)) {
      logger.info("Key setting change cancelled: " + KeyEvent.getKeyText(key) + " input");
    }
    keySelected[selectedKeyIndex] = false;
    waitingForNewKey = false;
    this.inputCooldown.reset();
  }

  private void saveNewKey(final int newKey) {
    if (this.selectMenuItem == MENU_P1_KEYS) {
      player1Keys[selectedKeyIndex] = newKey;
      Core.getInputManager().setPlayer1Keys(player1Keys);
    } else {
      player2Keys[selectedKeyIndex] = newKey;
      Core.getInputManager().setPlayer2Keys(player2Keys);
    }

    keySelected[selectedKeyIndex] = false;
    waitingForNewKey = false;
    Core.getInputManager().saveKeyConfig();
    if (logger.isLoggable(Level.INFO)) {
      logger.info("New key saved -> " + KeyEvent.getKeyText(newKey));
    }
    SoundManager.playeffect(SOUND_SELECT);
    this.inputCooldown.reset();
  }

  private boolean handleGlobalEscape() {
    if (inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)) {
      this.isRunning = false;
      SoundManager.playeffect(SOUND_SELECT);
      this.inputCooldown.reset();
      return true;
    }
    return false;
  }

  private boolean handleBackNavigation() {
    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY();
    final boolean clicked = inputManager.isMouseClicked();

    final Rectangle backBox = drawManager.menu().getBackButtonHitbox(this);
    if (clicked && backBox.contains(mx, my)) {
      this.returnCode = 1;
      SoundManager.playeffect(SOUND_SELECT);
      this.isRunning = false;
      return true;
    }
    return false;
  }

  private void handleMouseInput() {
    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY();
    final boolean pressed = inputManager.isMousePressed();
    final boolean clicked = inputManager.isMouseClicked();

    if (this.selectMenuItem == MENU_VOLUME) {
      handleMouseVolumeControl(mx, my, pressed);
      if (this.enableSoundMouseControl) {
        handleMouseMuteControl(mx, my, clicked);
      }
    }

    // Menu selection via mouse
    for (int i = 0; i < menuItem.length; i++) {
      final Rectangle menuBox = drawManager.settings().getSettingMenuHitbox(this, i);
      if (clicked && menuBox.contains(mx, my) && selectMenuItem != i) {
        switchMenuByMouse(i);
        break;
      }
    }
  }

  private void handleMouseVolumeControl(final int mx, final int my, final boolean pressed) {
    if (draggingIndex == -1 && pressed) {
      for (int i = 0; i < sliderTitles.length; i++) {
        final Rectangle box = drawManager.settings().getVolumeBarHitbox(this, i);
        if (box.contains(mx, my)) {
          volumetype = i;
          draggingIndex = i;
          setVolumeFromX(box, mx, i);
          break;
        }
      }
    } else if (draggingIndex != -1) {
      if (pressed) {
        final Rectangle box = drawManager.settings().getVolumeBarHitbox(this, draggingIndex);
        setVolumeFromX(box, mx, draggingIndex);
        SoundControl.setMute(this.volumetype, false);
      } else {
        draggingIndex = -1;
      }
    }
  }

  private void handleMouseMuteControl(final int mx, final int my, final boolean clicked) {
    for (int i = 0; i < sliderTitles.length; i++) {
      final Rectangle iconBox = drawManager.settings().getSpeakerHitbox(this, i);
      if (clicked && iconBox.contains(mx, my)) {
        SoundControl.setMute(i, !SoundControl.isMuted(i));
        SoundManager.updateVolume();
        this.inputCooldown.reset();
        break;
      }
    }
  }

  private void switchMenuByMouse(final int index) {
    if (waitingForNewKey) {
      if (selectedKeyIndex >= 0 && selectedKeyIndex < keySelected.length) {
        keySelected[selectedKeyIndex] = false;
      }
      waitingForNewKey = false;
    }
    this.selectMenuItem = index;
    this.selectedSection = 0;
    this.inputCooldown.reset();
    this.enableSoundMouseControl = index == MENU_VOLUME;
    SoundManager.playeffect(SOUND_SELECT);

    if (index == MENU_P1_KEYS || index == MENU_P2_KEYS) {
      this.selectedSection = 1;
      this.selectedKeyIndex = 0;
      SoundManager.playeffect(SOUND_SELECT);
    }
  }

  private void handleReturnToTitle() {
    if (inputManager.isKeyDown(KeyEvent.VK_SPACE) && this.inputCooldown.checkFinished()) {
      if (this.selectMenuItem == MENU_BACK) {
        this.returnCode = 1;
        this.isRunning = false;
        SoundManager.playeffect(SOUND_SELECT);
      }
      this.inputCooldown.reset();
    }
  }

  private void setVolumeFromX(final Rectangle barBox, final int mouseX, final int index) {
    double ratio = (double) (mouseX - barBox.x) / (double) barBox.width;
    ratio = Math.max(0.0, Math.min(1.0, ratio));
    final int val = (int) Math.round(ratio * 100.0);

    volumeLevels[index] = val;
    this.volumelevel = val;
    SoundControl.setVolumeLevel(index, val);
    SoundManager.updateVolume();
  }

  private void draw() {
    drawManager.initDrawing(this);
    drawManager.settings().drawSettingMenu(this);
    drawManager.settings().drawSettingLayout(this, menuItem, this.selectMenuItem);

    switch (this.selectMenuItem) {
      case MENU_VOLUME:
        for (int i = 0; i < sliderTitles.length; i++) {
          drawManager.settings().drawVolumeBar(
                  this,
                  volumeLevels[i],
                  draggingIndex == i,
                  i,
                  sliderTitles[i],
                  this.selectedSection,
                  this.volumetype);
        }
        break;
      case MENU_P1_KEYS:
        drawManager.settings().drawKeysettings(
                this, 1, this.selectedSection, this.selectedKeyIndex, this.keySelected, this.player1Keys);
        break;
      case MENU_P2_KEYS:
        drawManager.settings().drawKeysettings(
                this, 2, this.selectedSection, this.selectedKeyIndex, this.keySelected, this.player2Keys);
        break;
      default:
        break;
    }

    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY();
    final Rectangle backBox = drawManager.menu().getBackButtonHitbox(this);
    final boolean backHover = backBox.contains(mx, my);
    final boolean backSelected = this.selectMenuItem == MENU_BACK;

    drawManager.menu().drawBackButton(this, backHover || backSelected);
    drawManager.completeDrawing(this);
  }
}