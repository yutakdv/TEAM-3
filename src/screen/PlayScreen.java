package screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import engine.Cooldown;
import engine.Core;
import engine.SoundManager;

/**
 *
 * Implements the PlayScreen
 *
 */

public class PlayScreen extends Screen {
    private boolean coopSelected = false;
    public boolean isCoopSelected() { return coopSelected; }
    private int menuIndex = 0; // 0 = 1P, 1 = 2P, 2 = Back

    private Integer prevHoverIndex = null;
    private Integer hoverIndex = null;

/**
 * Constructor, establishes the properties of the screen.
 *
 * @param width  Screen width.
 * @param height Screen height.
 * @param fps    Frames per second, frame rate at which the game is run.
 *  **/

    public PlayScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        SoundManager.playBGM("sound/menu_sound.wav");
        this.returnCode = 2; // default 1P
    }

    public final int run() {
        super.run();
        return this.returnCode;
    }

    protected final void update() {
        super.update();
        draw();

        if(inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            this.returnCode = 1;
            SoundManager.playeffect("sound/select.wav");
            this.isRunning = false;
            return;
        }

        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        java.awt.Rectangle[] modeBoxesForKey = drawManager.getPlayMenuHitboxes(this);
        java.awt.Rectangle backBoxForKey = drawManager.getBackButtonHitbox(this);
        boolean mouseHovering = modeBoxesForKey[0].contains(mx, my)
                || modeBoxesForKey[1].contains(mx, my)
                || backBoxForKey.contains(mx, my);

        if (inputManager.isKeyPressed(KeyEvent.VK_UP) || inputManager.isKeyPressed(KeyEvent.VK_W)) {
            this.menuIndex = (this.menuIndex + 2) % 3;
            if(!mouseHovering && this.menuIndex != 2) {
                SoundManager.playeffect("sound/hover.wav");// UP
            }
        }
        if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) || inputManager.isKeyPressed(KeyEvent.VK_S)) {
            this.menuIndex = (this.menuIndex + 1) % 3;
            if(!mouseHovering && this.menuIndex != 2) {
                SoundManager.playeffect("sound/hover.wav");// DOWN
            }
        }

        // back button click event & 1P, 2P button click event
        if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
            switch (this.menuIndex) {
                case 0: // "1 Player"
                    this.coopSelected = false;
                    this.returnCode = 2; // go to GameScreen
                    break;

                case 1: // "2 Players"
                    this.coopSelected = true;
                    this.returnCode = 2; // go to GameScreen
                    break;

                case 2: // "Back"
                    this.returnCode = 1; // go back to TitleScreen
                    break;
            }
            SoundManager.playeffect("sound/select.wav");
            this.isRunning = false;
        }
        if (inputManager.isMouseClicked()) {
            java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);
            java.awt.Rectangle[] modeBoxes = drawManager.getPlayMenuHitboxes(this);
            java.awt.Rectangle[] allBoxes = {
                    modeBoxes[0], // 1P
                    modeBoxes[1],  // 2P
                    backBox      // Back
            };

            for  (int i = 0; i < allBoxes.length; i++) {
                if (allBoxes[i].contains(mx, my)) {
                    this.menuIndex = i;
                    if (i == 2) this.returnCode = 1; // Back
                    else {
                        this.coopSelected = (i == 1); // Mode Select
                        this.returnCode = 2;
                    }
                    SoundManager.playeffect("sound/select.wav");
                    this.isRunning = false;
                    return;
                }
            }

        }
    }

    private void draw() {
        drawManager.initDrawing(this);

        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();

        java.awt.Rectangle[] modeBoxes = drawManager.getPlayMenuHitboxes(this);
        java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);
        java.awt.Rectangle[] allBoxes = {
                modeBoxes[0], // 1P
                modeBoxes[1], // 2P
                backBox       // Back
        };

        prevHoverIndex = hoverIndex;
        hoverIndex = null;

        for (int i = 0; i < allBoxes.length; i++) {
            if (allBoxes[i].contains(mx, my)) {
                hoverIndex = i;
                this.menuIndex = i;
                break;
            }
        }
        if(hoverIndex != null && !hoverIndex.equals(prevHoverIndex) && hoverIndex != 2) {
            SoundManager.playeffect("sound/hover.wav");
        }

        drawManager.drawPlayMenu(this, this.menuIndex==2 ? -1 : this.menuIndex, this.menuIndex);
        drawManager.drawBackButton(this, this.menuIndex==2);
        drawManager.completeDrawing(this);
    }

}


