package engine;

import org.junit.jupiter.api.*;
import javax.sound.sampled.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for core parts of SoundManager: - Volume calculation - stop() / stopAllMusic() - Audio
 * stream handling
 */
class SoundManagerTest {
  // ---- Reflection helper to modify Core values inside SoundManagerTest ----
  private void setCoreVolume(int index, int value) throws Exception {
    var f = Core.class.getDeclaredField("volumearray");
    f.setAccessible(true);
    int[] arr = (int[]) f.get(null);
    arr[index] = value;
  }

  private void setCoreMute(int index, boolean value) throws Exception {
    var f = Core.class.getDeclaredField("Mute");
    f.setAccessible(true);
    boolean[] arr = (boolean[]) f.get(null);
    arr[index] = value;
  }

  /** ---------------- Volume calculation ---------------- */
  @Test
  void testCalculateVolumeDb_zero_returnsSilent() throws Exception {
    float db = invokeCalculateVolumeDb(0);
    assertEquals(-80.0f, db, 0.01f);
  }

  @Test
  void testCalculateVolumeDb_full_returnsZero() throws Exception {
    float db = invokeCalculateVolumeDb(100);
    assertEquals(0.0f, db, 0.01f);
  }

  @Test
  void testCalculateVolumeDb_midValue_negativeDb() throws Exception {
    float db = invokeCalculateVolumeDb(50);
    assertTrue(db < 0.0f && db > -10.0f);
  }

  /** ---------------- stop() ---------------- */
  @Test
  void testStop_setsLoopClipToNull() throws Exception {
    var loopClipField = SoundManager.class.getDeclaredField("loopClip");
    loopClipField.setAccessible(true);
    loopClipField.set(null, new DummyClip());

    SoundManager.stop();
    assertNull(loopClipField.get(null), "loopClip should be null after stop()");
  }

  /** ---------------- stopAllMusic() ---------------- */
  @Test
  void testStopAllMusic_resetsBothClips() throws Exception {
    var loopClipField = SoundManager.class.getDeclaredField("loopClip");
    var bgClipField = SoundManager.class.getDeclaredField("backgroundMusicClip");

    loopClipField.setAccessible(true);
    bgClipField.setAccessible(true);

    loopClipField.set(null, new DummyClip());
    bgClipField.set(null, new DummyClip());

    SoundManager.stopAllMusic();

    assertNull(loopClipField.get(null));
    assertNull(bgClipField.get(null));
  }

  /** ---------------- toPcmSigned() ---------------- */
  @Test
  void testToPcmSigned_convertsNonPcm() throws Exception {
    AudioFormat sourceFormat =
        new AudioFormat(AudioFormat.Encoding.ULAW, 44100, 8, 1, 1, 44100, false);

    AudioInputStream source =
        new AudioInputStream(new ByteArrayInputStream(new byte[100]), sourceFormat, 100);

    var method = SoundManager.class.getDeclaredMethod("toPcmSigned", AudioInputStream.class);
    method.setAccessible(true);

    AudioInputStream result = (AudioInputStream) method.invoke(null, source);
    AudioFormat fmt = result.getFormat();

    assertEquals(AudioFormat.Encoding.PCM_SIGNED, fmt.getEncoding());
    assertEquals(16, fmt.getSampleSizeInBits());
  }

  /** ---------------- DummyFloatControl ---------------- */
  static class DummyFloatControl extends FloatControl {
    float value = 0;

    public DummyFloatControl() {
      super(FloatControl.Type.MASTER_GAIN, -80, 6, 1, 0, 0, "");
    }

    @Override
    public void setValue(float newValue) {
      value = newValue;
    }

    @Override
    public float getValue() {
      return value;
    }
  }

  /** ---------------- updateVolume() ---------------- */
  @Test
  void testUpdateVolume_mutesCorrectly() throws Exception {
    // Prepare Dummy clip
    DummyClip clip = new DummyClip();
    DummyFloatControl ctrl = new DummyFloatControl();

    var loopClipField = SoundManager.class.getDeclaredField("loopClip");
    loopClipField.setAccessible(true);
    loopClipField.set(null, clip);

    var bgClipField = SoundManager.class.getDeclaredField("backgroundMusicClip");
    bgClipField.setAccessible(true);
    bgClipField.set(null, clip);

    // Force control support
    clip.overrideControl(ctrl);

    // Mock Core values
    setCoreVolume(0, 0);
    setCoreMute(0, true);

    SoundManager.updateVolume();

    assertEquals(-80f, ctrl.getValue(), 0.01f);
  }

  /** ---------------- playeffect() ---------------- */
  @Test
  void testPlayEffect_muted_closesImmediately() throws Exception {
    // Mock Core values directly via reflection
    setCoreVolume(1, 0); // effect volume = 0
    setCoreMute(1, true); // effect muted = true

    var method = SoundManager.class.getDeclaredMethod("playeffect", String.class);
    method.setAccessible(true);

    // openAudioStream returns null → nothing should break
    method.invoke(null, "sound/nonexistent.wav");
  }

  /** ---------------- playBackgroundMusic() ---------------- */
  @Test
  void testStopBackgroundMusic_setsBackgroundToNull() throws Exception {
    var bgField = SoundManager.class.getDeclaredField("backgroundMusicClip");
    bgField.setAccessible(true);
    bgField.set(null, new DummyClip());

    SoundManager.stopBackgroundMusic();

    assertNull(bgField.get(null));
  }

  /** ---------------- playBGM() ---------------- */
  @Test
  void testPlayBGM_callsStop() throws Exception {
    var loopClipField = SoundManager.class.getDeclaredField("loopClip");
    loopClipField.setAccessible(true);

    DummyClip loop = new DummyClip();
    loopClipField.set(null, loop);

    // Force openAudioStream to return null
    var open = SoundManager.class.getDeclaredMethod("openAudioStream", String.class);
    open.setAccessible(true);

    SoundManager.playBGM("nonexistent.wav");

    assertNull(loopClipField.get(null)); // stop() was called
  }

  /** ---------------- openAudioStream() ---------------- */
  @Test
  void testOpenAudioStream_invalidPath_returnsNull() throws Exception {
    var method = SoundManager.class.getDeclaredMethod("openAudioStream", String.class);
    method.setAccessible(true);
    Object result = method.invoke(null, "nonexistent/file.wav");
    assertNull(result, "Invalid audio path should return null");
  }

  // --- Utility Reflection Method ---
  private float invokeCalculateVolumeDb(int value) throws Exception {
    var m = SoundManager.class.getDeclaredMethod("calculateVolumeDb", int.class);
    m.setAccessible(true);
    return (float) m.invoke(null, value);
  }

  // --- DummyClip to simulate Clip safely ---
  static class DummyClip implements Clip {
    boolean closed = false;

    @Override
    public void close() {
      closed = true;
    }

    @Override
    public void open(AudioInputStream stream) {}

    @Override
    public void open(AudioFormat format, byte[] data, int offset, int bufferSize) {}

    @Override
    public int getFrameLength() {
      return 0;
    }

    @Override
    public long getMicrosecondLength() {
      return 0;
    }

    @Override
    public void setFramePosition(int frames) {}

    @Override
    public void setMicrosecondPosition(long microseconds) {}

    @Override
    public void setLoopPoints(int start, int end) {}

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public boolean isRunning() {
      return false;
    }

    @Override
    public boolean isActive() {
      return false;
    }

    @Override
    public void loop(int count) {}

    @Override
    public void drain() {}

    @Override
    public void flush() {}

    @Override
    public Line.Info getLineInfo() {
      return new Line.Info(DummyClip.class);
    }

    @Override
    public void addLineListener(LineListener listener) {}

    @Override
    public void removeLineListener(LineListener listener) {}

    @Override
    public void open() {}

    //        @Override public Control getControl(Control.Type control) { return null; }
    //        @Override public boolean isControlSupported(Control.Type control) { return false; }
    @Override
    public Control[] getControls() {
      return new Control[0];
    }

    @Override
    public AudioFormat getFormat() {
      return null;
    }

    @Override
    public int getBufferSize() {
      return 0;
    }

    // ---- 추가 구현 (DataLine, Line 상속분) ----
    @Override
    public int available() {
      return 0;
    }

    @Override
    public int getFramePosition() {
      return 0;
    }

    @Override
    public long getLongFramePosition() {
      return 0;
    }

    @Override
    public float getLevel() {
      return 0.0f;
    }

    @Override
    public long getMicrosecondPosition() {
      return 0;
    }

    @Override
    public boolean isOpen() {
      return true;
    }

    public void overrideControl(FloatControl ctrl) {
      this.override = ctrl;
    }

    private Control override = null;

    @Override
    public Control getControl(Control.Type control) {
      if (override != null && control == FloatControl.Type.MASTER_GAIN) {
        return override;
      }
      return null;
    }

    @Override
    public boolean isControlSupported(Control.Type control) {
      return override != null && control == FloatControl.Type.MASTER_GAIN;
    }
  }
}
