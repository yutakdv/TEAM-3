package engine;

import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import screen.SettingScreen;

@SuppressWarnings("PMD.LawOfDemeter")
public class SettingKeyHandler {//NOPMD

    private static final Logger LOGGER = Core.getLogger();
    private static final int MENU_P1_KEYS = 1;
    private static final String SOUND_SELECT = "sound/select.wav";
    private static final String SOUND_HOVER = "sound/hover.wav";

    private final SettingScreen screen;
    private final InputManager inputManager;
    private final Cooldown inputCooldown;

    public SettingKeyHandler(final SettingScreen screen, final InputManager inputManager, final Cooldown inputCooldown) {
        this.screen = screen;
        this.inputManager = inputManager;
        this.inputCooldown = inputCooldown;
    }

    public void handleKeySettings() {
        if (!this.inputCooldown.checkFinished()) {
            return;
        }

        if (screen.isWaitingForNewKey()) {
            processNewKeyInput();
            return;
        }

        if (screen.getSelectedSection() == 0) {
            handleMenuSelection();
        } else if (screen.getSelectedSection() == 1) {
            handleKeyNavigation();
        }
    }

    private void handleMenuSelection() {
        if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
            screen.setSelectedSection(1);
            screen.setSelectedKeyIndex(0);
            SoundManager.playeffect(SOUND_SELECT);
            this.inputCooldown.reset();
        }
    }

    private void handleKeyNavigation() {
        if (inputManager.isKeyPressed(KeyEvent.VK_BACK_SPACE)) {
            screen.setSelectedSection(0);
            SoundManager.playeffect(SOUND_SELECT);
            this.inputCooldown.reset();
            return;
        }

        updateKeyIndexSelection();
        checkKeyModificationStart();
    }

    private void updateKeyIndexSelection() {
        int selectedKeyIndex = screen.getSelectedKeyIndex();

        if (inputManager.isKeyPressed(KeyEvent.VK_UP) && selectedKeyIndex > 0) {
            selectedKeyIndex--;
            SoundManager.playeffect(SOUND_HOVER);
        } else if (inputManager.isKeyPressed(KeyEvent.VK_DOWN) && selectedKeyIndex < screen.getKeyItemsCount() - 1) {
            selectedKeyIndex++;
            SoundManager.playeffect(SOUND_HOVER);
        }
        screen.setSelectedKeyIndex(selectedKeyIndex);
    }

    private void checkKeyModificationStart() {
        if (inputManager.isKeyPressed(KeyEvent.VK_SPACE)) {
            screen.setKeySelected(screen.getSelectedKeyIndex(), true);
            screen.setWaitingForNewKey(true);
            SoundManager.playeffect(SOUND_SELECT);
            this.inputCooldown.reset();
            inputManager.getLastPressedKey();
        }
    }

    private void processNewKeyInput() {
        final int newKey = inputManager.getLastPressedKey();
        if (newKey == -1 || !this.inputCooldown.checkFinished()) {
            return;
        }

        if (isCancelKey(newKey)) {
            cancelKeyChange(newKey);
            return;
        }

        if (isKeyDuplicate(newKey)) {
            cancelKeyChange(newKey);
            return;
        }

        saveNewKey(newKey);
    }

    private boolean isCancelKey(final int key) {
        return key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_BACK_SPACE;
    }

    private boolean isKeyDuplicate(final int newKey) {
        final int[] targetKeys = (screen.getSelectMenuItem() == MENU_P1_KEYS) ? screen.getPlayer1Keys() : screen.getPlayer2Keys();
        final int[] otherKeys = (screen.getSelectMenuItem() == MENU_P1_KEYS) ? screen.getPlayer2Keys() : screen.getPlayer1Keys();
        final int selectedKeyIndex = screen.getSelectedKeyIndex();

        for (int i = 0; i < targetKeys.length; i++) {
            if (checkDuplicate(i, selectedKeyIndex, targetKeys[i], otherKeys[i], newKey)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDuplicate(final int i, final int selectedIndex, final int targetKey, final int otherKey, final int newKey) {
        if (i != selectedIndex && targetKey == newKey || otherKey == newKey) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Key already in use: " + KeyEvent.getKeyText(newKey));
            }
            return true;
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
        updatePlayerKeys(selectedKeyIndex, newKey);

        screen.setKeySelected(selectedKeyIndex, false);
        screen.setWaitingForNewKey(false);
        Core.getInputManager().saveKeyConfig();

        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("New key saved -> " + KeyEvent.getKeyText(newKey));
        }
        SoundManager.playeffect(SOUND_SELECT);
        this.inputCooldown.reset();
    }

    private void updatePlayerKeys(final int index, final int newKey) {
        if (screen.getSelectMenuItem() == MENU_P1_KEYS) {
            final int[] keys = screen.getPlayer1Keys();
            keys[index] = newKey;
            Core.getInputManager().setPlayer1Keys(keys);
        } else {
            final int[] keys = screen.getPlayer2Keys();
            keys[index] = newKey;
            Core.getInputManager().setPlayer2Keys(keys);
        }
    }
}