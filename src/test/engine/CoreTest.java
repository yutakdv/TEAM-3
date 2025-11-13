package engine;

import org.junit.jupiter.api.*;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Core.java Focused on static utilities and configuration logic (not GUI or I/O).
 */
class CoreTest {

  /** ---------------- Logger ---------------- */
  @Test
  void testGetLogger_notNull() {
    Logger logger = Core.getLogger();
    assertNotNull(logger, "Logger should not be null");
  }

  /** ---------------- Manager accessors ---------------- */
  @Test
  void testGetDrawManager_returnsSingleton() {
    DrawManager manager1 = Core.getDrawManager();
    DrawManager manager2 = Core.getDrawManager();
    assertSame(manager1, manager2, "DrawManager should be singleton");
  }

  @Test
  void testGetInputManager_returnsSingleton() {
    InputManager input1 = Core.getInputManager();
    InputManager input2 = Core.getInputManager();
    assertSame(input1, input2, "InputManager should be singleton");
  }

  @Test
  void testGetFileManager_returnsSingleton() {
    FileManager fm1 = Core.getFileManager();
    FileManager fm2 = Core.getFileManager();
    assertSame(fm1, fm2, "FileManager should be singleton");
  }

  @Test
  void testGetAchievementManager_returnsSingleton() {
    AchievementManager am1 = Core.getAchievementManager();
    AchievementManager am2 = Core.getAchievementManager();
    assertSame(am1, am2, "AchievementManager should be singleton");
  }

  /** ---------------- Cooldown ---------------- */
  @Test
  void testGetCooldown_createsCooldown() {
    Cooldown cd = Core.getCooldown(1000);
    assertNotNull(cd);
  }

  @Test
  void testGetVariableCooldown_createsCooldownWithVariance() {
    Cooldown cd = Core.getVariableCooldown(1000, 200);
    assertNotNull(cd);
  }

  /** ---------------- Volume Logic ---------------- */
  @Test
  void testSetVolumeLevel_validRange() {
    Core.setVolumeLevel(0, 120); // 100 초과
    assertEquals(100, Core.getVolumeLevel(0));

    Core.setVolumeLevel(1, -20); // 0 미만
    assertEquals(0, Core.getVolumeLevel(1));

    Core.setVolumeLevel(0, 50);
    assertEquals(50, Core.getVolumeLevel(0));
  }

  @Test
  void testGetVolumetype_reflectsLastSetIndex() {
    Core.setVolumeLevel(1, 70);
    assertEquals(1, Core.getVolumetype());
  }

  /** ---------------- Mute Logic ---------------- */
  @Test
  void testMute_toggleState() {
    Core.setMute(0, true);
    assertTrue(Core.isMuted(0));

    Core.setMute(0, false);
    assertFalse(Core.isMuted(0));
  }

  /** ---------------- Integration-like check ---------------- */
  @Test
  void testAllManagersReturnConsistentInstances() {
    assertNotNull(Core.getDrawManager());
    assertNotNull(Core.getInputManager());
    assertNotNull(Core.getFileManager());
    assertNotNull(Core.getAchievementManager());
  }
}
