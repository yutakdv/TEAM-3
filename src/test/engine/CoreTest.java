package engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CoreTest {

    @Test
    void testLoggerNotNull() {
        assertNotNull(Core.getLogger(), "Logger should not be null");
    }

    @Test
    void testDrawManagerInstance() {
        assertSame(DrawManager.getInstance(), Core.getDrawManager());
    }

    @Test
    void testInputManagerInstance() {
        assertSame(InputManager.getInstance(), Core.getInputManager());
    }

    @Test
    void testVolumeClamp() {
        Core.setVolumeLevel(0, 150);
        assertEquals(100, Core.getVolumeLevel(0), "Volume should be clamped to 100");
        Core.setVolumeLevel(0, -50);
        assertEquals(0, Core.getVolumeLevel(0), "Volume should be clamped to 0");
    }

    @Test
    void testMuteFunction() {
        Core.setMute(0, true);
        assertTrue(Core.isMuted(0));
        Core.setMute(0, false);
        assertFalse(Core.isMuted(0));
    }

    @Test
    void testCooldownCreation() {
        assertNotNull(Core.getCooldown(1000), "Cooldown object should be created");
    }
}