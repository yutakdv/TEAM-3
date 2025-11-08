package screen;

import engine.Achievement;
import engine.AchievementManager;
import engine.Core;
import engine.FileManager;

import java.awt.event.KeyEvent;
import engine.SoundManager;
import java.util.List;

public class AchievementScreen extends Screen {

    private FileManager fileManager;
    private AchievementManager achievementManager;
    private List<Achievement> achievements;
    private List<String> completer;
    private int currentIdx = 0;

    public AchievementScreen(final int width, final int height, final int fps) {
        super(width, height, fps);
        achievementManager = Core.getAchievementManager();
        achievements = achievementManager.getAchievements();
        fileManager = Core.getFileManager();
        this.completer = Core.getFileManager().getAchievementCompleter(achievements.get(currentIdx));
        this.returnCode = 3;

        // Start menu music loop when the achievement screen is created
        SoundManager.playLoop("sound/menu_sound.wav");
    }

    public final int run() {
        super.run();
        // Stop menu music when leaving the achievement screen
        SoundManager.stop();

        return this.returnCode;
    }

    protected final void update() {

        // [2025-10-17] feat: Added key input logic to navigate achievements
        // When the right or left arrow key is pressed, update the current achievement index
        // and reload the completer list for the newly selected achievement.
        if (inputManager.isKeyPressed(KeyEvent.VK_RIGHT)) {
            currentIdx = (currentIdx + 1) % achievements.size();
            completer = fileManager.getAchievementCompleter(achievements.get(currentIdx));
        }
        if (inputManager.isKeyPressed(KeyEvent.VK_LEFT)) {
            currentIdx = (currentIdx - 1 + achievements.size()) % achievements.size();
            completer = fileManager.getAchievementCompleter(achievements.get(currentIdx));
        }

        super.update();
        draw();

        if (inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            this.returnCode = 1;
            this.isRunning = false;
        }

        // back button click event
        if (inputManager.isMouseClicked()) {
            int mx = inputManager.getMouseX();
            int my = inputManager.getMouseY();
            java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);

            if (backBox.contains(mx, my)) {
                this.returnCode = 1;
                this.isRunning = false;
            }

            java.awt.Rectangle[] navBoxes = drawManager.getAchievementNavHitboxes(this);

            if (navBoxes[0].contains(mx, my)) {
                currentIdx = (currentIdx - 1 + achievements.size()) % achievements.size();
                completer = fileManager.getAchievementCompleter(achievements.get(currentIdx));
            }

            if (navBoxes[1].contains(mx, my)) {
                currentIdx = (currentIdx + 1) % achievements.size();
                completer = fileManager.getAchievementCompleter(achievements.get(currentIdx));
            }
        }
    }

    private void draw() {
        drawManager.initDrawing(this);
        drawManager.drawAchievementMenu(this, achievements.get(currentIdx), completer);

        // hover highlight
        int mx = inputManager.getMouseX();
        int my = inputManager.getMouseY();
        java.awt.Rectangle backBox = drawManager.getBackButtonHitbox(this);

        if (backBox.contains(mx, my)) {
            drawManager.drawBackButton(this, true);
        }

        drawManager.completeDrawing(this);
    }
}
