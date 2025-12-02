package screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import engine.SoundManager;

/** Implements the PlayScreen */
@SuppressWarnings({"PMD.LawOfDemeter"})
public class PlayScreen extends Screen {
  private boolean coopSelected = false; // NOPMD - redundant initializer
  private int menuIndex = 0; // NOPMD - redundant initializer | 0 = 1P, 1 = 2P, 2 = Back
  private Integer hoverIndex;

  public boolean isCoopSelected() {
    return coopSelected;
  }

  /**
   * Constructor, establishes the properties of the screen.
   *
   * @param width Screen width.
   * @param height Screen height.
   * @param fps Frames per second, frame rate at which the game is run. *
   */
  public PlayScreen(final int width, final int height, final int fps) {
    super(width, height, fps);
    SoundManager.playBGM("sound/menu_sound.wav");
    this.returnCode = 2; // default 1P
  }

  public final int run() {
    super.run();
    return this.returnCode;
  }

    @SuppressWarnings("PMD.OnlyOneReturn")
  protected final void update() {
    super.update();
    draw();

      if (handleEscape()) {
          return;
      }
      handleKeyboardNavigation();
      if (handleSpaceSelection()) {
          return;
      }
      if (handleMouseClickSelection()) {
          return;
      }
  }

    private boolean handleEscape() {
        if (inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            this.returnCode = 1;
            SoundManager.playeffect("sound/select.wav");
            this.isRunning = false;
            return true; // NOPMD - intentional early exit
        }
        return false;
    }

    private void handleKeyboardNavigation() {
        final int mx = inputManager.getMouseX();
        final int my = inputManager.getMouseY();
        final Rectangle[] modeBoxes = drawManager.menu().getPlayMenuHitboxes(this);
        final Rectangle backBox = drawManager.menu().getBackButtonHitbox(this);

        final boolean mouseHovering =
                modeBoxes[0].contains(mx, my) ||
                        modeBoxes[1].contains(mx, my) ||
                        backBox.contains(mx, my); // NOPMD - LawOfDemeter

        if (inputManager.isKeyPressed(KeyEvent.VK_UP) || inputManager.isKeyPressed(KeyEvent.VK_W)) {
            this.menuIndex = (menuIndex + 2) % 3;
            if (!mouseHovering && menuIndex != 2) {
                SoundManager.playeffect("sound/hover.wav");
            }
        }

        if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) || inputManager.isKeyPressed(KeyEvent.VK_S)) {
            this.menuIndex = (menuIndex + 1) % 3;
            if (!mouseHovering && menuIndex != 2) {
                SoundManager.playeffect("sound/hover.wav");
            }
        }
    }

    private boolean handleSpaceSelection() {
        if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
            switch (menuIndex) {
                case 0:
                    this.coopSelected = false;
                    this.returnCode = 2;
                    break;
                case 1:
                    this.coopSelected = true;
                    this.returnCode = 2;
                    break;
                case 2:
                    this.returnCode = 1;
                    break;
                default:
                    break;
            }
            SoundManager.playeffect("sound/select.wav");
            this.isRunning = false;
            return true; // NOPMD - intentional early exit
        }
        return false;
    }

    private boolean handleMouseClickSelection() {
        if (!inputManager.isMouseClicked()) {
            return false; // NOPMD - intentional early exit
        }

        final int mx = inputManager.getMouseX();
        final int my = inputManager.getMouseY();
        final Rectangle backBox = drawManager.menu().getBackButtonHitbox(this);
        final Rectangle[] modeBoxes = drawManager.menu().getPlayMenuHitboxes(this);

        final Rectangle[] allBoxes = {
                modeBoxes[0], modeBoxes[1], backBox
        };

        for (int i = 0; i < allBoxes.length; i++) {
            if (allBoxes[i].contains(mx, my)) {
                this.menuIndex = i;
                if (i == 2) {
                    this.returnCode = 1;
                } else {
                    this.coopSelected = i == 1;
                    this.returnCode = 2;
                }
                SoundManager.playeffect("sound/select.wav");
                this.isRunning = false;
                return true; // NOPMD - intentional early exit
            }
        }

        return false;
    }

  private void draw() {
    drawManager.initDrawing(this);

    // hover highlight
    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY();

    final Rectangle[] modeBoxes = drawManager.menu().getPlayMenuHitboxes(this);
    final Rectangle backBox = drawManager.menu().getBackButtonHitbox(this);
    final Rectangle[] allBoxes = {
      modeBoxes[0], // 1P
      modeBoxes[1], // 2P
      backBox // Back
    };

    final Integer prevHoverIndex = hoverIndex;
    hoverIndex = null; // NOPMD - hover cleared intentionally

    for (int i = 0; i < allBoxes.length; i++) {
      if (allBoxes[i].contains(mx, my)) {
        hoverIndex = i;
        this.menuIndex = i;
        break;
      }
    }
    if (hoverIndex != null && !hoverIndex.equals(prevHoverIndex) && hoverIndex != 2) {
      SoundManager.playeffect("sound/hover.wav");
    }

    drawManager.menu().drawPlayMenu(this, this.menuIndex == 2 ? -1 : this.menuIndex, this.menuIndex);
    drawManager.menu().drawBackButton(this, this.menuIndex == 2);
    drawManager.completeDrawing(this);
  }
}
