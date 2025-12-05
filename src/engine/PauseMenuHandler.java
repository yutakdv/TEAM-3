package engine;

import screen.Screen;

/** Manages the Pause Menu logic (Volume control, Mute). Extracted from GameScreen to reduce WMC. */
public class PauseMenuHandler {

  private final String[] PAUSE_SLIDER_TITLES = {"Game BGM", "Game Effect"};
  private final int numPauseSliders = PAUSE_SLIDER_TITLES.length;

  private int pauseVolumetype;
  private int pauseSelectedSection = 1;
  private int pauseDraggingIndex = -1;

  public PauseMenuHandler() {
    this.pauseVolumetype = SoundControl.getIngameVolumetype();
  }

  public void draw(final DrawManager drawManager, final Screen screen) {
    drawManager.hud().drawPauseOverlay(screen); // NOPMD
    for (int i = 0; i < numPauseSliders; i++) {
      drawManager // NOPMD
          .settings()
          .drawpauseVolumeBar( // NOPMD
              screen,
              SoundControl.getIngameVolumeLevel(i),
              pauseDraggingIndex == i,
              i,
              PAUSE_SLIDER_TITLES[i],
              pauseSelectedSection,
              this.pauseVolumetype);
    }
  }

  public int getNumPauseSliders() {
    return numPauseSliders;
  }

  public int getVolumeType() {
    return pauseVolumetype;
  }

  public void setVolumeType(final int type) {
    this.pauseVolumetype = type;
  }

  public int getSelectedSection() {
    return pauseSelectedSection;
  }

  public void setSelectedSection(final int section) {
    this.pauseSelectedSection = section;
  }

  public int getDraggingIndex() {
    return pauseDraggingIndex;
  }

  public void setDraggingIndex(final int index) {
    this.pauseDraggingIndex = index;
  }
}
