package engine;

import org.junit.jupiter.api.*;
import javax.sound.sampled.*;
import java.io.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for core parts of SoundManager:
 *  - Volume calculation
 *  - stop() / stopAllMusic()
 *  - Audio stream handling
 */
class SoundManagerTest {

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

        @Override public void close() { closed = true; }
        @Override public void open(AudioInputStream stream) {}
        @Override public void open(AudioFormat format, byte[] data, int offset, int bufferSize) {}
        @Override public int getFrameLength() { return 0; }
        @Override public long getMicrosecondLength() { return 0; }
        @Override public void setFramePosition(int frames) {}
        @Override public void setMicrosecondPosition(long microseconds) {}
        @Override public void setLoopPoints(int start, int end) {}
        @Override public void start() {}
        @Override public void stop() {}
        @Override public boolean isRunning() { return false; }
        @Override public boolean isActive() { return false; }
        @Override public void loop(int count) {}
        @Override public void drain() {}
        @Override public void flush() {}
        @Override public Line.Info getLineInfo() { return new Line.Info(DummyClip.class); }
        @Override public void addLineListener(LineListener listener) {}
        @Override public void removeLineListener(LineListener listener) {}
        @Override public void open() {}
        @Override public Control getControl(Control.Type control) { return null; }
        @Override public boolean isControlSupported(Control.Type control) { return false; }
        @Override public Control[] getControls() { return new Control[0]; }
        @Override public AudioFormat getFormat() { return null; }
        @Override public int getBufferSize() { return 0; }

        // ---- 추가 구현 (DataLine, Line 상속분) ----
        @Override public int available() { return 0; }
        @Override public int getFramePosition() { return 0; }
        @Override public long getLongFramePosition() { return 0; }
        @Override public float getLevel() { return 0.0f; }
        @Override public long getMicrosecondPosition() { return 0; }

        @Override public boolean isOpen() { return true; }
    }
}