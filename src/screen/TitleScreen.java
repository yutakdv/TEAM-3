package screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import engine.SoundManager;

/**
 * Implements the title screen.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
@SuppressWarnings("PMD.OnlyOneReturn")
public class TitleScreen extends Screen {

  /** Time between changes in user selection. */
  //	private Cooldown selectionCooldown;

  // menu index added for user mode selection
  private int menuIndex;

  /** Added variable to store which menu option is currently hovered */
  private Integer hoverOption;

  /**
   * Constructor, establishes the properties of the screen.
   *
   * @param width Screen width.
   * @param height Screen height.
   * @param fps Frames per second, frame rate at which the game is run.
   */
  public TitleScreen(final int width, final int height, final int fps) {
    super(width, height, fps);

    // Defaults to play.
    this.returnCode = 1; // 2P mode: changed to default selection as 1P

    // Start menu music loop when the title screen is created
    SoundManager.playBGM("sound/menu_sound.wav");
  }

  /**
   * Starts the action.
   *
   * @return Next screen code.
   */
  public final int run() {
    super.run();
    // Stop menu music when leaving the title screen
    SoundManager.stop();
    return this.returnCode;
  }

  /** Updates the elements on screen and checks for events. */
  protected final void update() {
    super.update();
    draw();

    handleKeyboardNavigation();
    if (handleSpaceSelect()) {
      return;
    }
    if (handleMouseClick()) {
      return; // NOPMD - intentional early exit
    }
  }

  private void handleKeyboardNavigation() {
    if (this.hoverOption == null) {
      if (inputManager.isKeyPressed(KeyEvent.VK_UP) || inputManager.isKeyPressed(KeyEvent.VK_W)) {
        SoundManager.playeffect("sound/hover.wav");
        previousMenuItem();
        this.hoverOption = null; // NOPMD - intentional null to represent no hover
      }
      if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) || inputManager.isKeyPressed(KeyEvent.VK_S)) {
        SoundManager.playeffect("sound/hover.wav");
        nextMenuItem();
        this.hoverOption = null; // NOPMD - intentional null to represent no hover
      }
    }
  }

  private boolean handleSpaceSelect() {
    // Play : Adjust the case so that 1p and 2p can be determined within the play.
    if (!inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
      return false;
    }

    SoundManager.playeffect("sound/select.wav");
    switch (this.menuIndex) {
      case 0: // "Play"
        this.returnCode = 5; // go to PlayScreen
        break;

      case 1: // "Achievements"
        this.returnCode = 3;
        break;

      case 2: // "High scores"
        this.returnCode = 8;
        break;

      case 3: // "Settings"
        this.returnCode = 4;
        break;

      case 4: // "Quit"
        this.returnCode = 0;
        break;

      default:
        break;
    }
    this.isRunning = false;
    return true;
  }

  private boolean handleMouseClick() {
    if (!inputManager.isMouseClicked()) {
      return false;
    }

    final int tempX = inputManager.getMouseX();
    final int tempY = inputManager.getMouseY();

    final Rectangle[] boxes = drawManager.getMenuHitboxes(this);
    final int[] pos = {5, 3, 8, 4, 0};

    for (int i = 0; i < boxes.length; i++) {
      if (boxes[i].contains(tempX, tempY)) {
        this.returnCode = pos[i];
        SoundManager.playeffect("sound/select.wav");
        this.isRunning = false;
        return true;
      }
    }
    return false;
  }

  /** Shifts the focus to the next menu item. - modified for 2P mode selection */
  private void nextMenuItem() {
    this.menuIndex = (this.menuIndex + 1) % 5;
    drawManager.menuHover(this.menuIndex);
  }

  /** Shifts the focus to the previous menu item. */
  private void previousMenuItem() {
    this.menuIndex =
        (this.menuIndex + 4)
            % 5; // Fix : an issue where only the down arrow keys on the keyboard are entered and
    // not up
    drawManager.menuHover(this.menuIndex);
  }

  /** Check hover based on mouse position and menu hitbox. */
  private void draw() {
    drawManager.initDrawing(this);

    // Main menu space animation
    drawManager.updateMenuSpace();

    final int mx = inputManager.getMouseX();
    final int my = inputManager.getMouseY();
    final Rectangle[] boxesForHover = drawManager.getMenuHitboxes(this);

    Integer newHover = null;
    if (boxesForHover[0].contains(mx, my)) {
      newHover = 0;
      drawManager.menuHover(0);
    }
    if (boxesForHover[1].contains(mx, my)) {
      newHover = 1;
      drawManager.menuHover(1);
    }
    if (boxesForHover[2].contains(mx, my)) {
      newHover = 2;
      drawManager.menuHover(2);
    }
    if (boxesForHover[3].contains(mx, my)) {
      newHover = 3;
      drawManager.menuHover(3);
    }
    if (boxesForHover[4].contains(mx, my)) {
      newHover = 4;
      drawManager.menuHover(4);
    }

    // Modify : Update after hover calculation
    if (newHover != null) {
      // Hover Update + Promote to Select Index when mouse is raised (to keep mouse away)
      if (!newHover.equals(this.hoverOption)) {
        this.hoverOption = newHover;
        SoundManager.playeffect("sound/hover.wav");
      }
    } else {
      // If we had a hover and the mouse left, promote last hover to selection for persistance
      if (this.hoverOption != null) {
        this.menuIndex = this.hoverOption; // persist last hovered as selection
        this.hoverOption = null; // NOPMD - intentional null state (no hover) | clear hover state
      }
    }

    // pass hoverOption for menu highlights respond to mouse hover
    drawManager.drawTitle(this);
    drawManager.drawMenu(
        this,
        this.menuIndex,
        hoverOption,
        this.menuIndex); // 2P mode: using menu index for highlighting

    drawManager.completeDrawing(this);
  }
}
