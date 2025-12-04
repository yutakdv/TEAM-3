package screen;

import engine.Achievement;
import engine.AchievementManager;
import engine.Core;
import java.awt.event.KeyEvent;
import engine.SoundManager;
import java.util.List;

@SuppressWarnings({"PMD.LawOfDemeter"})
public class AchievementScreen extends Screen {

  // Removed FileManager dependency
  AchievementManager achievementManager;
  private final List<Achievement> achievements;
  private List<String> completer;
  private int currentIdx;
  private static final String HOVER_SOUND = "sound/hover.wav";
  private static final String SELECT_SOUND = "sound/select.wav";

  public AchievementScreen(final int width, final int height, final int fps) {
    super(width, height, fps);
    achievementManager = Core.getAchievementManager();
    achievements = achievementManager.getAchievements();

    this.currentIdx = 0;
    // [Changed] Use AchievementManager to get completer list
    this.completer = achievementManager.getAchievementCompleter(achievements.get(currentIdx));
    this.returnCode = 3;

    SoundManager.playBGM("sound/menu_sound.wav");
  }

  public final int run() {
    super.run();
    SoundManager.stop();
    return this.returnCode;
  }

  protected final void update() {
    if (inputManager.isKeyPressed(KeyEvent.VK_RIGHT)) {
      currentIdx = (currentIdx + 1) % achievements.size();
      // [Changed] Use AchievementManager
      completer = achievementManager.getAchievementCompleter(achievements.get(currentIdx));
      SoundManager.playeffect(HOVER_SOUND);
    }
    if (inputManager.isKeyPressed(KeyEvent.VK_LEFT)) {
      currentIdx = (currentIdx - 1 + achievements.size()) % achievements.size();
      // [Changed] Use AchievementManager
      completer = achievementManager.getAchievementCompleter(achievements.get(currentIdx));
      SoundManager.playeffect(HOVER_SOUND);
    }

    super.update();
    draw();

    if (inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)) {
      this.returnCode = 1;
      SoundManager.playeffect(SELECT_SOUND);
      this.isRunning = false;
    }

    if (inputManager.isMouseClicked()) {
      final int mx = inputManager.getMouseX();
      final int my = inputManager.getMouseY();
      final java.awt.Rectangle backBox = drawManager.menu().getBackButtonHitbox(this);

      if (backBox.contains(mx, my)) {
        this.returnCode = 1;
        SoundManager.playeffect(SELECT_SOUND);
        this.isRunning = false;
      }

      final java.awt.Rectangle[] navBoxes = drawManager.menu().getAchievementNavHitboxes(this);

      if (navBoxes[0].contains(mx, my)) {
        currentIdx = (currentIdx - 1 + achievements.size()) % achievements.size();
        completer = achievementManager.getAchievementCompleter(achievements.get(currentIdx));
        SoundManager.playeffect(HOVER_SOUND);
      }

      if (navBoxes[1].contains(mx, my)) {
        currentIdx = (currentIdx + 1) % achievements.size();
        completer = achievementManager.getAchievementCompleter(achievements.get(currentIdx));
        SoundManager.playeffect(HOVER_SOUND);
      }
    }
  }

  private void draw() {
    drawManager.initDrawing(this);
    drawManager.menu().drawAchievementMenu(this, achievements.get(currentIdx), completer);
    handleBackButtonHover();
    drawManager.completeDrawing(this);
  }
}