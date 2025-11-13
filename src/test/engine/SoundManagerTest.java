package engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SoundManager's volume calculation logic.
 *
 * <p>This test does NOT load any actual audio resources and does NOT create any Clips. It only
 * validates SoundManager.calculateVolumeDb() behavior by manipulating Core volume settings.
 */
public class SoundManagerTest {

  @BeforeEach
  void resetCoreState() {
    // Core index 0 = BGM, index 1 = Menu BGM, index 2 = SFX (팀 코드 구조 기준)
    Core.setMute(0, false);
    Core.setMute(1, false);
//    Core.setMute(2, false);

    Core.setVolumeLevel(0, 100); // For BGM
    Core.setVolumeLevel(1, 100); // For menu BGM loop
//    Core.setVolumeLevel(2, 100); // For SFX

//    Core.setIngameVolumeLevel(0, 100);
//    Core.setIngameVolumeLevel(1, 100);
  }

  // -------------------------------------------------------------
  // 1) Full volume should be 0 dB
  // -------------------------------------------------------------
  @Test
  void testCalculateVolumeDb_full_returnsZero() throws Exception {
    float db = invokeCalculate(100);
    assertEquals(0.0f, db, 0.01f, "Volume 100 should be 0 dB");
  }

  // -------------------------------------------------------------
  // 2) Middle volume should return negative dB
  // -------------------------------------------------------------
  @Test
  void testCalculateVolumeDb_midValue_negativeDb() throws Exception {
    float db = invokeCalculate(50);
    assertTrue(db < 0.0f, "Volume 50 should return negative dB");
  }

  // -------------------------------------------------------------
  // 3) Volume 0 should be -80 dB (silent)
  // -------------------------------------------------------------
  @Test
  void testCalculateVolumeDb_zeroVolume_isSilent() throws Exception {
    float db = invokeCalculate(0);
    assertEquals(-80.0f, db, 0.01f, "Volume 0 should be silent (−80 dB)");
  }

  // -------------------------------------------------------------
  // 4) Muted should force -80 dB regardless of level
  // -------------------------------------------------------------
//  @Test
//  void testCalculateVolumeDb_muted_isSilent() throws Exception {
//    Core.setMute(0, true);
//
//    float db = invokeCalculate(80);
//    assertEquals(-80.0f, db, 0.01f, "Muted should return −80 dB");
//  }
//
//  // -------------------------------------------------------------
//  // 5) When Core volume baseline < 100, result changes
//  // -------------------------------------------------------------
//  @Test
//  void testCalculateVolumeDb_respectsCoreBaseline() throws Exception {
//    Core.setVolumeLevel(0, 50); // baseline volume = 50%
//
//    float db = invokeCalculate(100);
//
//    // 50% baseline → result must be < 0dB
//    assertTrue(db < -2.0f, "Baseline 50% should reduce calculated dB");
//  }

  // -------------------------------------------------------------
  // Utility: reflectively call private calculateVolumeDb()
  // -------------------------------------------------------------
  private float invokeCalculate(int level) throws Exception {
    var method = SoundManager.class.getDeclaredMethod("calculateVolumeDb", int.class);
    method.setAccessible(true);
    return (float) method.invoke(null, level);
  }
}
