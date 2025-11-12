package screen;

import engine.Cooldown;
import engine.Core;
import engine.SoundManager;
import java.awt.event.KeyEvent;

public class SettingScreen extends Screen {
  private static final int volumeMenu = 0;
  private static final int firstplayerMenu = 1;
  private static final int secondplayerMenu = 2;
  private static final int back = -1;
  private final String[] menuItem = {"Volume", "1P Keyset", "2P Keyset"};
  private int selectMenuItem;
  private Cooldown inputCooldown;
  private int volumelevel;
  private int volumetype;
  private int selectedSection = 0;
  private int selectedKeyIndex = 0;
  private String[] keyItems = {"MOVE LEFT", "MOVE RIGHT", "ATTACK"};
  private boolean[] keySelected = {false, false, false};
  private boolean waitingForNewKey = false;
  private int[] player1Keys;
  private int[] player2Keys;

  private final String[] SLIDER_TITLES = {"BGM", "Effect Sound"};
  private final int NUM_SLIDERS = SLIDER_TITLES.length;
  private int[] volumeLevels = new int[NUM_SLIDERS];
  private int draggingIndex = -1;
  private boolean enableSoundMouseControl = false;

  /**
   * Constructor, establishes the properties of the screen.
   *
   * @param width Screen width.
   * @param height Screen height.
   * @param fps Frames per second, frame rate at which the game is run.
   */
  public SettingScreen(final int width, final int height, final int fps) {
    super(width, height, fps);

    this.returnCode = 1;
    // Import key arrangement and save it to field
    this.player1Keys = Core.getInputManager().getPlayer1Keys();
    this.player2Keys = Core.getInputManager().getPlayer2Keys();

    // Start menu music loop when the settings screen is created
    SoundManager.playBGM("sound/menu_sound.wav");
  }

  private void setVolumeFromX(java.awt.Rectangle barBox, int mouseX, int index) {
    double ratio = (double) (mouseX - barBox.x) / (double) barBox.width;
    ratio = Math.max(0.0, Math.min(1.0, ratio));
    int val = (int) Math.round(ratio * 100.0);

    volumeLevels[index] = val;

    if (index == 0) {
      this.volumelevel = val;
      Core.setVolumeLevel(index, val);
    }
    if (index == 1) {
      this.volumelevel = val;
      Core.setVolumeLevel(index, val);
    }
    SoundManager.updateVolume();
  }

  /**
   * Starts the action.
   *
   * @return Next screen code.
   */
  public final void initialize() {
    super.initialize();
    this.inputCooldown = Core.getCooldown(200);
    this.inputCooldown.reset();
    this.selectMenuItem = volumeMenu;

    volumeLevels[0] = Core.getVolumeLevel(0);
    volumeLevels[1] = Core.getVolumeLevel(1);

    this.volumetype = 0;
    this.volumelevel = volumeLevels[this.volumetype];
  }

  public final int run() {
    super.run();
    // Stop menu music when leaving the settings screen
    SoundManager.stop();

    return this.returnCode;
  }

  /** Updates the elements on screen and checks for events. */
  protected final void update() {
    super.update();

    if (inputManager.isKeyDown(KeyEvent.VK_UP)
        && this.inputCooldown.checkFinished()
        && this.selectedSection == 0) {
      if (this.selectMenuItem == back) {
        this.selectMenuItem = menuItem.length - 1;
      } else if (this.selectMenuItem == 0) {
        this.selectMenuItem = back;
      } else {
        this.selectMenuItem--;
      }
      SoundManager.playeffect("sound/hover.wav");
      this.inputCooldown.reset();
    }

    if (inputManager.isKeyDown(KeyEvent.VK_DOWN)
        && this.inputCooldown.checkFinished()
        && this.selectedSection == 0) {
      if (this.selectMenuItem == back) {
        this.selectMenuItem = 0;
      } else if (this.selectMenuItem == menuItem.length - 1) {
        this.selectMenuItem = back;
      } else {
        this.selectMenuItem++;
      }
      SoundManager.playeffect("sound/hover.wav");
      this.inputCooldown.reset();
    }

    /*
       2025-11-09 "Choi yutak"
       Turn the mouse-based sound control on only
       when the currently selected menu item is the Volume menu.
    */
    this.enableSoundMouseControl = (this.selectMenuItem == volumeMenu);

    if (this.selectMenuItem == volumeMenu) {
      if (this.inputCooldown.checkFinished()) {
        if (inputManager.isKeyDown(KeyEvent.VK_SPACE) && selectedSection == 0) {
          this.selectedSection = 1;
          this.volumetype = 0;
          SoundManager.playeffect("sound/select.wav");
          this.inputCooldown.reset();
        }
      }
      if (inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE) && selectedSection == 1) {
        this.selectedSection = 0;
        SoundManager.playeffect("sound/select.wav");
        this.inputCooldown.reset();
      }
      if (this.selectedSection == 1
          && inputManager.isKeyDown(KeyEvent.VK_UP)
          && this.inputCooldown.checkFinished()
          && volumetype > 0
          && selectedSection == 1) {
        this.volumetype--;
        this.volumelevel = volumeLevels[this.volumetype];
        SoundManager.playeffect("sound/hover.wav");
        this.inputCooldown.reset();
      }
      if (this.selectedSection == 1
          && inputManager.isKeyDown(KeyEvent.VK_DOWN)
          && this.inputCooldown.checkFinished()
          && volumetype < SLIDER_TITLES.length - 1
          && selectedSection == 1) {
        this.volumetype++;
        this.volumelevel = volumeLevels[this.volumetype];
        SoundManager.playeffect("sound/hover.wav");
        this.inputCooldown.reset();
      }
      if (inputManager.isKeyDown(KeyEvent.VK_LEFT)
          && this.inputCooldown.checkFinished()
          && volumelevel > 0
          && selectedSection == 1) {
        this.volumelevel--;
        Core.setVolumeLevel(this.volumetype, this.volumelevel);
        Core.setMute(this.volumetype, false);
        SoundManager.updateVolume();
        volumeLevels[this.volumetype] = this.volumelevel;
        this.inputCooldown.reset();
      }
      if (inputManager.isKeyDown(KeyEvent.VK_RIGHT)
          && this.inputCooldown.checkFinished()
          && volumelevel < 100
          && selectedSection == 1) {
        this.volumelevel++;
        Core.setVolumeLevel(this.volumetype, this.volumelevel);
        Core.setMute(this.volumetype, false);
        SoundManager.updateVolume();
        volumeLevels[this.volumetype] = this.volumelevel;
        this.inputCooldown.reset();
      }
      if (inputManager.isKeyDown(KeyEvent.VK_SPACE)
          && selectedSection == 1
          && this.inputCooldown.checkFinished()) {
        boolean newMuted = !Core.isMuted(this.volumetype);
        Core.setMute(this.volumetype, newMuted);
        SoundManager.updateVolume();
        this.inputCooldown.reset();
      }
    }
    /** Change key settings */
    else if (this.selectMenuItem == firstplayerMenu || this.selectMenuItem == secondplayerMenu) {
      if (inputManager.isKeyDown(KeyEvent.VK_SPACE)
          && this.inputCooldown.checkFinished()
          && waitingForNewKey == false
          && selectedSection == 0) {
        this.selectedSection = 1;
        this.selectedKeyIndex = 0;
        SoundManager.playeffect("sound/select.wav");
        this.inputCooldown.reset();
      }
      if (this.selectedSection == 1
          && inputManager.isKeyDown(KeyEvent.VK_BACK_SPACE)
          && this.inputCooldown.checkFinished()
          && waitingForNewKey == false) {
        selectedSection = 0;
        SoundManager.playeffect("sound/select.wav");
        this.inputCooldown.reset();
      }
      if (this.selectedSection == 1
          && inputManager.isKeyDown(KeyEvent.VK_UP)
          && this.inputCooldown.checkFinished()
          && selectedKeyIndex > 0
          && waitingForNewKey == false) {
        selectedKeyIndex--;
        SoundManager.playeffect("sound/hover.wav");
        this.inputCooldown.reset();
      }
      if (this.selectedSection == 1
          && inputManager.isKeyDown(KeyEvent.VK_DOWN)
          && this.inputCooldown.checkFinished()
          && selectedKeyIndex < keyItems.length - 1
          && waitingForNewKey == false) {
        selectedKeyIndex++;
        SoundManager.playeffect("sound/hover.wav");
        this.inputCooldown.reset();
      }
      // Start waiting for new keystrokes
      if (this.selectedSection == 1
          && inputManager.isKeyDown(KeyEvent.VK_SPACE)
          && this.inputCooldown.checkFinished()
          && waitingForNewKey == false) {
        keySelected[selectedKeyIndex] = !keySelected[selectedKeyIndex];

        if (keySelected[selectedKeyIndex]) {
          waitingForNewKey = true;
        } else {
          waitingForNewKey = false;
        }
        SoundManager.playeffect("sound/select.wav");
        this.inputCooldown.reset();
      }
      /** check duplicate and exception when new key is pressed, and save as new key if valid */
      if (waitingForNewKey) {
        int newKey = inputManager.getLastPressedKey();
        if (newKey != -1 && this.inputCooldown.checkFinished()) {
          // exception of esc key and backspace key
          if (newKey == KeyEvent.VK_ESCAPE || newKey == KeyEvent.VK_BACK_SPACE) {
            System.out.println(
                "Key setting change cancelled : " + KeyEvent.getKeyText(newKey) + " input");
            keySelected[selectedKeyIndex] = false;
            waitingForNewKey = false;
            this.inputCooldown.reset();
            return;
          }
          // Check duplicate keys
          int[] targetKeys = (this.selectMenuItem == firstplayerMenu) ? player1Keys : player2Keys;
          int[] otherKeys = (this.selectMenuItem == firstplayerMenu) ? player2Keys : player1Keys;

          boolean duplicate = false;

          for (int i = 0; i < targetKeys.length; i++) {
            if (i != selectedKeyIndex && targetKeys[i] == newKey) {
              duplicate = true;
              System.out.println("Key already in use:" + KeyEvent.getKeyText(newKey));
              break;
            }

            if (otherKeys[i] == newKey) {
              duplicate = true;
              System.out.println("Key already in use:" + KeyEvent.getKeyText(newKey));
              break;
            }
          }

          if (duplicate) {
            keySelected[selectedKeyIndex] = false;
            waitingForNewKey = false;
            this.inputCooldown.reset();
            return;
          }
          // key assignment entered and save to keyconfig
          if (this.selectMenuItem == firstplayerMenu) {
            player1Keys[selectedKeyIndex] = newKey;
            Core.getInputManager().setPlayer1Keys(player1Keys);
          } else {
            player2Keys[selectedKeyIndex] = newKey;
            Core.getInputManager().setPlayer2Keys(player2Keys);
          }

          keySelected[selectedKeyIndex] = false;
          waitingForNewKey = false;
          Core.getInputManager().saveKeyConfig();
          System.out.println("New key saved → " + KeyEvent.getKeyText(newKey));
          SoundManager.playeffect("sound/select.wav");
          this.inputCooldown.reset();
        }
      }
    }

    // change space to escape
    if (inputManager.isKeyDown(KeyEvent.VK_ESCAPE) && this.inputCooldown.checkFinished()) {
      this.isRunning = false;
      SoundManager.playeffect("sound/select.wav");
      this.inputCooldown.reset();
    }

    // make mouse work on volume bar
    int mx = inputManager.getMouseX();
    int my = inputManager.getMouseY();
    boolean pressed = inputManager.isMousePressed();
    boolean clicked = inputManager.isMouseClicked();

    java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);

    if (clicked && backBox.contains(mx, my)) {
      this.returnCode = 1;
      SoundManager.playeffect("sound/select.wav");
      this.isRunning = false;
      return;
    }

    if (this.selectMenuItem == volumeMenu) {
      if (draggingIndex == -1 && pressed) {
        for (int i = 0; i < SLIDER_TITLES.length; i++) {
          java.awt.Rectangle box = drawManager.getVolumeBarHitbox(this, i);
          if (box.contains(mx, my)) {
            volumetype = i;
            draggingIndex = i;
            setVolumeFromX(box, mx, i);
            break;
          }
        }
      }

      if (draggingIndex != -1 && pressed) {
        java.awt.Rectangle box = drawManager.getVolumeBarHitbox(this, draggingIndex);
        setVolumeFromX(box, mx, draggingIndex);
        Core.setMute(this.volumetype, false);
      }

      if (!pressed) {
        draggingIndex = -1;
      }
    }
    if (inputManager.isKeyDown(KeyEvent.VK_SPACE) && this.inputCooldown.checkFinished()) {
      if (this.selectMenuItem == back) {
        this.returnCode = 1;
        this.isRunning = false;
        SoundManager.playeffect("sound/select.wav");
        return;
      }
      this.inputCooldown.reset();
    }

    /*
       2025-11-09
       Choi Yutak
       - Checks that the Volume menu is active.
       - Detects mouse clicks on each speark icon.
       - Toggles the mute state for that sound category.
    */
    if (this.selectMenuItem == volumeMenu && this.enableSoundMouseControl) {
      for (int i = 0; i < SLIDER_TITLES.length; i++) {
        java.awt.Rectangle iconBox = drawManager.getSpeakerHitbox(this, i);
        if (clicked && iconBox.contains(mx, my)) {
          boolean newMuted = !Core.isMuted(i);
          Core.setMute(i, newMuted);
          SoundManager.updateVolume();
          this.inputCooldown.reset();
          break;
        }
      }
    }

    for (int i = 0; i < menuItem.length; i++) {
      java.awt.Rectangle menuBox = drawManager.getSettingMenuHitbox(this, i);
      if (clicked && menuBox.contains(mx, my) && selectMenuItem != i) {
        this.selectMenuItem = i;
        this.selectedSection = 0;
        this.inputCooldown.reset();

        this.enableSoundMouseControl = (i == volumeMenu);
        SoundManager.playeffect("sound/select.wav");

        if (i == firstplayerMenu || i == secondplayerMenu) {
          this.selectedSection = 1;
          this.selectedKeyIndex = 0;
          SoundManager.playeffect("sound/select.wav");
        }
        break;
      }
    }

    draw();
  }

  /** Draws the elements associated with the screen. */
  private void draw() {
    drawManager.initDrawing(this);
    drawManager.drawSettingMenu(this);
    drawManager.drawSettingLayout(this, menuItem, this.selectMenuItem);

    switch (this.selectMenuItem) {
      case volumeMenu:
        for (int i = 0; i < NUM_SLIDERS; i++) {
          boolean dragging = (draggingIndex == i);
          drawManager.drawVolumeBar(
              this,
              volumeLevels[i],
              dragging,
              i,
              SLIDER_TITLES[i],
              this.selectedSection,
              this.volumetype);
        }
        break;
      case firstplayerMenu:
        drawManager.drawKeysettings(
            this,
            1,
            this.selectedSection,
            this.selectedKeyIndex,
            this.keySelected,
            this.player1Keys);
        break;
      case secondplayerMenu:
        drawManager.drawKeysettings(
            this,
            2,
            this.selectedSection,
            this.selectedKeyIndex,
            this.keySelected,
            this.player2Keys);
        break;
    }

    // hover highlight
    int mx = inputManager.getMouseX();
    int my = inputManager.getMouseY();
    java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);

    boolean backHover = backBox.contains(mx, my);
    boolean backSelected = (this.selectMenuItem == back);
    drawManager.drawBackButton(this, backHover || backSelected);

    drawManager.completeDrawing(this);
  }
}
