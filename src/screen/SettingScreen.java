package screen;

import java.awt.Rectangle;
import engine.Core;
import engine.SoundControl;
import engine.SoundManager;
import engine.SettingControl;

@SuppressWarnings("PMD.LawOfDemeter")
public class SettingScreen extends Screen {//NOPMD
  private static final int MENU_VOLUME = 0;
  private static final int MENU_P1_KEYS = 1;
  private static final int MENU_P2_KEYS = 2;
  private static final int MENU_BACK = -1;

  private final String[] menuItem = {"Volume", "1P Keyset", "2P Keyset"};
  private final String[] sliderTitles = {"Master", "BGM", "Effect Sound"};
  private final String[] keyItems = {"MOVE LEFT", "MOVE RIGHT", "ATTACK"};

  private int selectMenuItem;
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
  private SettingControl settingControl;

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

    this.settingControl = new SettingControl(this, this.inputManager);

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
    if (settingControl != null) {
      settingControl.update();
    }
    draw();
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

  public void setIsRunning(final boolean isRunning) { this.isRunning = isRunning; }
  public void setReturnCode(final int returnCode) { this.returnCode = returnCode; }

  public int getSelectMenuItem() { return selectMenuItem; }
  public void setSelectMenuItem(final int item) { this.selectMenuItem = item; }

  public int getSelectedSection() { return selectedSection; }
  public void setSelectedSection(final int section) { this.selectedSection = section; }

  public int getSelectedKeyIndex() { return selectedKeyIndex; }
  public void setSelectedKeyIndex(final int index) { this.selectedKeyIndex = index; }

  public int getVolumetype() { return volumetype; }
  public void setVolumetype(final int type) { this.volumetype = type; }

  public int getVolumelevel() { return volumelevel; }
  public void setVolumelevel(final int level) { this.volumelevel = level; }

  public boolean isWaitingForNewKey() { return waitingForNewKey; }
  public void setWaitingForNewKey(final boolean waiting) { this.waitingForNewKey = waiting; }

  public boolean isEnableSoundMouseControl() { return enableSoundMouseControl; }
  public void setEnableSoundMouseControl(final boolean enable) { this.enableSoundMouseControl = enable; }

  public int getDraggingIndex() { return draggingIndex; }
  public void setDraggingIndex(final int index) { this.draggingIndex = index; }

  public int[] getPlayer1Keys() { return player1Keys; }
  public int[] getPlayer2Keys() { return player2Keys; }

  public int getVolumeLevel(final int index) { return volumeLevels[index]; }
  public void setVolumeLevel(final int index, final int val) { volumeLevels[index] = val; }

  public void setKeySelected(final int index, final boolean selected) {
    if (index >= 0 && index < keySelected.length) {
      keySelected[index] = selected;
    }
  }

  public int getMenuItemCount() { return menuItem.length; }
  public int getSliderTitlesCount() { return sliderTitles.length; }
  public int getKeyItemsCount() { return keyItems.length; }

  public Rectangle getBackButtonHitbox() {
    return drawManager.menu().getBackButtonHitbox(this);
  }
  public Rectangle getSettingMenuHitbox(final int index) {
    return drawManager.settings().getSettingMenuHitbox(this, index);
  }
  public Rectangle getVolumeBarHitbox(final int index) {
    return drawManager.settings().getVolumeBarHitbox(this, index);
  }
  public Rectangle getSpeakerHitbox(final int index) {
    return drawManager.settings().getSpeakerHitbox(this, index);
  }
}