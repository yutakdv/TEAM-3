package engine;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import screen.SettingScreen;

/**
 * Handles the business logic and input processing for the SettingScreen.
 * Renamed to 'SettingControl' to match project conventions (SoundControl, ScreenControl).
 */
@SuppressWarnings("PMD.LawOfDemeter")
public class SettingControl {//NOPMD

    private static final Logger LOGGER = Core.getLogger();

    private static final int MENU_VOLUME = 0;
    private static final int MENU_P1_KEYS = 1;
    private static final int MENU_P2_KEYS = 2;
    private static final int MENU_BACK = -1;

    private static final String SOUND_SELECT = "sound/select.wav";
    private static final String SOUND_HOVER = "sound/hover.wav";

    private final SettingScreen screen;
    private final InputManager inputManager;
    private final Cooldown inputCooldown;

    public SettingControl(final SettingScreen screen, final InputManager inputManager) {
        this.screen = screen;
        this.inputManager = inputManager;
        this.inputCooldown = Core.getCooldown(70);
        this.inputCooldown.reset();
    }

    public void update() {
        final int mx = inputManager.getMouseX();
        final int my = inputManager.getMouseY();
        final boolean clicked = inputManager.isMouseClicked();

        if (handleBackNavigation(mx, my, clicked)) {
            return;
        }
        final boolean pressed = inputManager.isMousePressed();//NOPMD

        handleMenuNavigation();

        screen.setEnableSoundMouseControl(screen.getSelectMenuItem() == MENU_VOLUME);

        if (screen.getSelectMenuItem() == MENU_VOLUME) {
            handleVolumeSettings();
        } else if (screen.getSelectMenuItem() == MENU_P1_KEYS || screen.getSelectMenuItem() == MENU_P2_KEYS) {
            handleKeySettings();
        }

        if (handleGlobalEscape()) {
            return;
        }

        handleMouseInput(mx, my, pressed, clicked);
        handleReturnToTitle();
    }

    private boolean handleBackNavigation(final int mx, final int my, final boolean clicked) {
        final Rectangle backBox = screen.getBackButtonHitbox();
        if (clicked && backBox.contains(mx, my)) {
            screen.setReturnCode(1);
            SoundManager.playeffect(SOUND_SELECT);
            screen.setIsRunning(false);
            return true;
        }
        return false;
    }

    private void handleMenuNavigation() {
        if (screen.getSelectedSection() != 0) {
            return;
        }

        int menuItem = screen.getSelectMenuItem();
        final int maxItems = screen.getMenuItemCount();

        if (inputManager.isKeyPressed(KeyEvent.VK_UP)) {
            if (menuItem == MENU_BACK) {
                menuItem = maxItems - 1;
            } else if (menuItem == 0) {
                menuItem = MENU_BACK;
            } else {
                menuItem--;
            }
            SoundManager.playeffect(SOUND_HOVER);
        } else if (inputManager.isKeyPressed(KeyEvent.VK_DOWN)) {
            if (menuItem == MENU_BACK) {
                menuItem = 0;
            } else if (menuItem == maxItems - 1) {
                menuItem = MENU_BACK;
            } else {
                menuItem++;
            }
            SoundManager.playeffect(SOUND_HOVER);
        }
        screen.setSelectMenuItem(menuItem);
    }

    private void handleVolumeSettings() {
        if (!this.inputCooldown.checkFinished()) {
            return;
        }

        if (screen.getSelectedSection() == 0 && inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
            screen.setSelectedSection(1);
            screen.setVolumetype(0);
            SoundManager.playeffect(SOUND_SELECT);
            this.inputCooldown.reset();
            return;
        }

        if (screen.getSelectedSection() == 1) {
            if (inputManager.isKeyPressed(KeyEvent.VK_BACK_SPACE)) {
                screen.setSelectedSection(0);
                SoundManager.playeffect(SOUND_SELECT);
            }
            handleVolumeAdjustment();
        }
    }

    private void handleVolumeAdjustment() {//NOPMD
        int type = screen.getVolumetype();
        int level = screen.getVolumelevel();
        final int maxType = screen.getSliderTitlesCount() - 1;

        if (inputManager.isKeyPressed(KeyEvent.VK_UP) && type > 0) {
            type--;
            level = screen.getVolumeLevel(type);
            SoundManager.playeffect(SOUND_HOVER);
        } else if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) && type < maxType) {
            type++;
            level = screen.getVolumeLevel(type);
            SoundManager.playeffect(SOUND_HOVER);
        }

        if (inputManager.isKeyDown(KeyEvent.VK_LEFT) && level > 0) {
            level--;
            applyVolumeChange(type, level);
        } else if (inputManager.isKeyDown(KeyEvent.VK_RIGHT) && level < 100) {
            level++;
            applyVolumeChange(type, level);
        }

        screen.setVolumetype(type);
        screen.setVolumelevel(level);

        if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
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
        this.inputCooldown.reset();
    }

    private void handleKeySettings() {//NOPMD
        if (!this.inputCooldown.checkFinished()) {
            return;
        }

        if (screen.isWaitingForNewKey()) {
            processNewKeyInput();
            return;
        }

        final int selectedSection = screen.getSelectedSection();
        int selectedKeyIndex = screen.getSelectedKeyIndex();

        if (selectedSection == 0 && inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
            screen.setSelectedSection(1);
            screen.setSelectedKeyIndex(0);
            SoundManager.playeffect(SOUND_SELECT);
            this.inputCooldown.reset();
        } else if (selectedSection == 1) {
            if (inputManager.isKeyPressed(KeyEvent.VK_BACK_SPACE)) {
                screen.setSelectedSection(0);
                SoundManager.playeffect(SOUND_SELECT);
                this.inputCooldown.reset();
            } else if (inputManager.isKeyPressed(KeyEvent.VK_UP) && selectedKeyIndex > 0) {
                selectedKeyIndex--;
                SoundManager.playeffect(SOUND_HOVER);
            } else if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) && selectedKeyIndex < screen.getKeyItemsCount() - 1) {
                selectedKeyIndex++;
                SoundManager.playeffect(SOUND_HOVER);
            } else if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
                screen.setKeySelected(selectedKeyIndex, true);
                screen.setWaitingForNewKey(true);
                SoundManager.playeffect(SOUND_SELECT);
                this.inputCooldown.reset();
                inputManager.getLastPressedKey();
            }
            screen.setSelectedKeyIndex(selectedKeyIndex);
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
        final int[] targetKeys = (screen.getSelectMenuItem() == MENU_P1_KEYS) ? screen.getPlayer1Keys() : screen.getPlayer2Keys();
        final int[] otherKeys = (screen.getSelectMenuItem() == MENU_P1_KEYS) ? screen.getPlayer2Keys() : screen.getPlayer1Keys();
        final int selectedKeyIndex = screen.getSelectedKeyIndex();

        for (int i = 0; i < targetKeys.length; i++) {
            if (i != selectedKeyIndex && targetKeys[i] == newKey || otherKeys[i] == newKey) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("Key already in use: " + KeyEvent.getKeyText(newKey));
                }
                return true;
            }
        }
        return false;
    }

    private void cancelKeyChange(final int key) {
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Key setting change cancelled: " + KeyEvent.getKeyText(key) + " input");
        }
        screen.setKeySelected(screen.getSelectedKeyIndex(), false);
        screen.setWaitingForNewKey(false);
        this.inputCooldown.reset();
    }

    private void saveNewKey(final int newKey) {
        final int selectedKeyIndex = screen.getSelectedKeyIndex();

        if (screen.getSelectMenuItem() == MENU_P1_KEYS) {
            final int[] keys = screen.getPlayer1Keys();
            keys[selectedKeyIndex] = newKey;
            Core.getInputManager().setPlayer1Keys(keys);
        } else {
            final int[] keys = screen.getPlayer2Keys();
            keys[selectedKeyIndex] = newKey;
            Core.getInputManager().setPlayer2Keys(keys);
        }

        screen.setKeySelected(selectedKeyIndex, false);
        screen.setWaitingForNewKey(false);
        Core.getInputManager().saveKeyConfig();

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("New key saved -> " + KeyEvent.getKeyText(newKey));
        }
        SoundManager.playeffect(SOUND_SELECT);
        this.inputCooldown.reset();
    }

    private boolean handleGlobalEscape() {
        if (inputManager.isKeyPressed(KeyEvent.VK_ESCAPE)) {
            screen.setIsRunning(false);
            SoundManager.playeffect(SOUND_SELECT);
            this.inputCooldown.reset();
            return true;
        }
        return false;
    }

    private void handleMouseInput(final int mx, final int my, final boolean pressed, final boolean clicked) {
        if (screen.getSelectMenuItem() == MENU_VOLUME) {
            handleMouseVolumeControl(mx, my, pressed);
            if (screen.isEnableSoundMouseControl()) {
                handleMouseMuteControl(mx, my, clicked);
            }
        }

        for (int i = 0; i < screen.getMenuItemCount(); i++) {
            final Rectangle menuBox = screen.getSettingMenuHitbox(i);
            if (clicked && menuBox.contains(mx, my) && screen.getSelectMenuItem() != i) {
                switchMenuByMouse(i);
                break;
            }
        }
    }

    private void handleMouseVolumeControl(final int mx, final int my, final boolean pressed) {
        final int draggingIndex = screen.getDraggingIndex();

        if (draggingIndex == -1 && pressed) {
            for (int i = 0; i < screen.getSliderTitlesCount(); i++) {
                final Rectangle box = screen.getVolumeBarHitbox(i);
                if (box.contains(mx, my)) {
                    screen.setVolumetype(i);
                    screen.setDraggingIndex(i);
                    setVolumeFromX(box, mx, i);
                    break;
                }
            }
        } else if (draggingIndex != -1) {
            if (pressed) {
                final Rectangle box = screen.getVolumeBarHitbox(draggingIndex);
                setVolumeFromX(box, mx, draggingIndex);
                SoundControl.setMute(screen.getVolumetype(), false);
            } else {
                screen.setDraggingIndex(-1);
            }
        }
    }

    private void handleMouseMuteControl(final int mx, final int my, final boolean clicked) {
        for (int i = 0; i < screen.getSliderTitlesCount(); i++) {
            final Rectangle iconBox = screen.getSpeakerHitbox(i);
            if (clicked && iconBox.contains(mx, my)) {
                SoundControl.setMute(i, !SoundControl.isMuted(i));
                SoundManager.updateVolume();
                this.inputCooldown.reset();
                break;
            }
        }
    }

    private void switchMenuByMouse(final int index) {
        if (screen.isWaitingForNewKey()) {
            final int idx = screen.getSelectedKeyIndex();
            if (idx >= 0) {
                screen.setKeySelected(idx, false);
            }
            screen.setWaitingForNewKey(false);
        }
        screen.setSelectMenuItem(index);
        screen.setSelectedSection(0);
        this.inputCooldown.reset();

        SoundManager.playeffect(SOUND_SELECT);

        if (index == MENU_P1_KEYS || index == MENU_P2_KEYS) {
            screen.setSelectedSection(1);
            screen.setSelectedKeyIndex(0);
            SoundManager.playeffect(SOUND_SELECT);
        }
    }

    private void handleReturnToTitle() {
        if (inputManager.isKeyDown(KeyEvent.VK_SPACE) && this.inputCooldown.checkFinished()) {
            if (screen.getSelectMenuItem() == MENU_BACK) {
                screen.setReturnCode(1);
                screen.setIsRunning(false);
                SoundManager.playeffect(SOUND_SELECT);
            }
            this.inputCooldown.reset();
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