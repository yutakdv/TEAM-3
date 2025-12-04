package engine;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import screen.SettingScreen;

public class SettingVolumeHandler {

    private final SettingScreen screen;
    private final InputManager inputManager;
    private final Cooldown inputCooldown;

    public SettingVolumeHandler(final SettingScreen screen, final InputManager inputManager, final Cooldown inputCooldown) {
        this.screen = screen;
        this.inputManager = inputManager;
        this.inputCooldown = inputCooldown;
    }

    public void handleVolumeSettings() {
        if (!this.inputCooldown.checkFinished()) {
            return;
        }

        if (screen.getSelectedSection() == 0) {
            handleMenuSelection();
        } else if (screen.getSelectedSection() == 1) {
            handleVolumeControlSection();
        }
    }

    private void handleMenuSelection() {
        if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
            screen.setSelectedSection(1);
            screen.setVolumetype(0);
            SoundManager.playeffect("sound/select.wav");
            this.inputCooldown.reset();
        }
    }

    private void handleVolumeControlSection() {
        if (inputManager.isKeyPressed(KeyEvent.VK_BACK_SPACE)) {
            screen.setSelectedSection(0);
            SoundManager.playeffect("sound/select.wav");
        }
        handleVolumeAdjustment();
    }

    private void handleVolumeAdjustment() {
        updateVolumeTypeSelection();
        updateVolumeLevelChange();
        updateMuteToggle();
    }

    private void updateVolumeTypeSelection() {
        int type = screen.getVolumetype();
        final int maxType = screen.getSliderTitlesCount() - 1;

        if (inputManager.isKeyPressed(KeyEvent.VK_UP) && type > 0) {
            type--;
            SoundManager.playeffect("sound/hover.wav");
        } else if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) && type < maxType) {
            type++;
            SoundManager.playeffect("sound/hover.wav");
        }
        screen.setVolumetype(type);
        screen.setVolumelevel(screen.getVolumeLevel(type));
    }

    private void updateVolumeLevelChange() {
        int level = screen.getVolumelevel();
        final int type = screen.getVolumetype();

        if (inputManager.isKeyDown(KeyEvent.VK_LEFT) && level > 0) {
            level--;
            applyVolumeChange(type, level);
        } else if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) && level < 100) {
            level++;
            applyVolumeChange(type, level);
        }
    }

    private void updateMuteToggle() {
        if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
            final int type = screen.getVolumetype();
            SoundControl.setMute(type, !SoundControl.isMuted(type));
            SoundManager.updateVolume();
            this.inputCooldown.reset();
        }
    }

    private void applyVolumeChange(final int type, final int level) {
        SoundControl.setVolumeLevel(type, level);
        SoundControl.setMute(type, false);
        SoundManager.updateVolume();
        screen.setVolumeLevel(type, level);
        screen.setVolumelevel(level);
        this.inputCooldown.reset();
    }

    public void handleMouseVolumeControl(final int mx, final int my, final boolean pressed) {
        final int draggingIndex = screen.getDraggingIndex();

        if (draggingIndex == -1) {
            checkMouseHoverOnSliders(mx, my, pressed);
        } else {
            updateSliderDrag(mx, draggingIndex, pressed);
        }
    }

    private void checkMouseHoverOnSliders(final int mx, final int my, final boolean pressed) {
        if (!pressed) return;

        for (int i = 0; i < screen.getSliderTitlesCount(); i++) {
            final Rectangle box = screen.getVolumeBarHitbox(i);
            if (box.contains(mx, my)) {
                screen.setVolumetype(i);
                screen.setDraggingIndex(i);
                setVolumeFromX(box, mx, i);
                break;
            }
        }
    }

    private void updateSliderDrag(final int mx, final int draggingIndex, final boolean pressed) {
        if (pressed) {
            final Rectangle box = screen.getVolumeBarHitbox(draggingIndex);
            setVolumeFromX(box, mx, draggingIndex);
            SoundControl.setMute(screen.getVolumetype(), false);
        } else {
            screen.setDraggingIndex(-1);
        }
    }

    public void handleMouseMuteControl(final int mx, final int my, final boolean clicked) {
        if (!clicked) return;

        for (int i = 0; i < screen.getSliderTitlesCount(); i++) {
            final Rectangle iconBox = screen.getSpeakerHitbox(i);
            if (iconBox.contains(mx, my)) {
                SoundControl.setMute(i, !SoundControl.isMuted(i));
                SoundManager.updateVolume();
                this.inputCooldown.reset();
                break;
            }
        }
    }

    private void setVolumeFromX(final Rectangle barBox, final int mouseX, final int index) {
        double ratio = (double) (mouseX - barBox.x) / (double) barBox.width;
        ratio = Math.max(0.0, Math.min(1.0, ratio));
        final int val = (int) Math.round(ratio * 100.0);

        screen.setVolumeLevel(index, val);
        screen.setVolumelevel(val);

        SoundControl.setVolumeLevel(index, val);
        SoundManager.updateVolume();
    }
}