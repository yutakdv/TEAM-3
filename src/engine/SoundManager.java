package engine;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.*;

/**
 * Minimal sound manager for short SFX.
 * Refactored to fix PMD issues: Resource leaks, Code Duplication, Generic Exceptions, Log Guards.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public final class SoundManager {//NOPMD

  private static final Logger LOGGER = Core.getLogger();
  private static Clip loopClip;
  private static Clip backgroundMusicClip;

  // PMD: Avoid duplicate literals
  private static final String ERR_MSG_PLAY_FAIL = "Unable to play sound '%s': %s";
  private static final String ERR_MSG_RES_NOT_FOUND = "Audio file not found in resources or local path: ";

  private SoundManager() {}

  /**
   * Plays a short WAV from resources folder.
   */
  public static void playeffect(final String resourcePath) {
    // Index 2 is for SFX in SoundControl
    playClip(resourcePath, 2, false);
  }

  public static void ingameeffect(final String resourcePath) {
    // Index 1 is for In-game SFX in SoundControl (ingame volume)
    playClip(resourcePath, 1, true);
  }

  /**
   * Internal helper to deduplicate play logic and reduce Complexity.
   */
  private static void playClip(final String resourcePath, final int volumeIndex, final boolean isIngame) {//NOPMD
    try (AudioInputStream rawStream = openAudioStream(resourcePath)) {
      if (rawStream == null) {
        return;
      }

      try (AudioInputStream audioStream = toPcmSigned(rawStream)) {
        final DataLine.Info info = new DataLine.Info(Clip.class, audioStream.getFormat());
        final Clip clip = (Clip) AudioSystem.getLine(info);//NOPMD
        clip.open(audioStream);

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
          final int savedLevel = isIngame
                  ? SoundControl.getIngameVolumeLevel(volumeIndex)
                  : SoundControl.getVolumeLevel(volumeIndex);

          final boolean isMuted = isIngame
                  ? SoundControl.isIngameMuted(volumeIndex) || savedLevel == 0
                  : SoundControl.isMuted(volumeIndex) || savedLevel == 0;

          if (isMuted) {
            clip.close();
            return;
          }

          final FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
          final float volumeDb = calculateVolumeDb(savedLevel);
          gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), volumeDb)));

          clip.start();
          if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Started one-shot sound: " + resourcePath);
          }
        }

        // Add listener to close clip after playback
        clip.addLineListener(event -> {
          if (event.getType() == LineEvent.Type.STOP || event.getType() == LineEvent.Type.CLOSE) {
            clip.close();
          }
        });
      }
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info(String.format(ERR_MSG_PLAY_FAIL, resourcePath, e.getMessage()));
      }
    }
  }

  public static void playBGM(final String resourcePath) {
    stop();
    stopBackgroundMusic();

    try (AudioInputStream rawStream = openAudioStream(resourcePath)) {
      if (rawStream == null) {
        return;
      }

      final AudioInputStream audioStream = toPcmSigned(rawStream);//NOPMD
      final DataLine.Info info = new DataLine.Info(Clip.class, audioStream.getFormat());

      loopClip = (Clip) AudioSystem.getLine(info);
      loopClip.open(audioStream);

      if (loopClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        final int saved = SoundControl.getVolumeLevel(1);
        final boolean muted = SoundControl.isMuted(1) || saved == 0;
        final FloatControl gain = (FloatControl) loopClip.getControl(FloatControl.Type.MASTER_GAIN);
        final float volumeDb = muted ? -80.0f : calculateVolumeDb(saved);
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), volumeDb)));
      }

      loopClip.loop(Clip.LOOP_CONTINUOUSLY);
      loopClip.start();
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Started looped sound: " + resourcePath);
      }

    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine(String.format(ERR_MSG_PLAY_FAIL, resourcePath, e.getMessage()));
      }
      if (loopClip != null) {
        loopClip.close();
        loopClip = null;//NOPMD - this null absoulty need
      }
    }
  }

  public static void stop() {
    if (loopClip != null) {
      loopClip.stop();
      loopClip.close();
      loopClip = null;//NOPMD - this null absoulty need
    }
  }

  public static void stopAllMusic() {
    stop();
    stopBackgroundMusic();
  }
  public static void ingameBGM(final String musicResourcePath) {
    stop();
    stopBackgroundMusic();

    try (AudioInputStream rawStream = openAudioStream(musicResourcePath)) {
      if (rawStream == null) {
        if (LOGGER.isLoggable(Level.FINE)) {
          LOGGER.fine("Music resource not found: " + musicResourcePath);
        }
        return;
      }

      final AudioInputStream audioStream = toPcmSigned(rawStream);//NOPMD
      final DataLine.Info info = new DataLine.Info(Clip.class, audioStream.getFormat());

      backgroundMusicClip = (Clip) AudioSystem.getLine(info);
      backgroundMusicClip.open(audioStream);
      backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);

      if (backgroundMusicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
        final int saved = SoundControl.getIngameVolumeLevel(0);
        final boolean muted = SoundControl.isIngameMuted(0) || saved == 0;

        final FloatControl gain = (FloatControl) backgroundMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
        final float volumeDb = muted ? -80.0f : calculateVolumeDb(saved);

        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), volumeDb)));
      }

      backgroundMusicClip.start();
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine("Background music started: " + musicResourcePath);
      }

    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
      if (LOGGER.isLoggable(Level.FINE)) {
        LOGGER.fine(String.format(ERR_MSG_PLAY_FAIL, musicResourcePath, e.getMessage()));
      }
      backgroundMusicClip = null;//NOPMD - this null absoulty need
    }
  }

  public static void stopBackgroundMusic() {
    if (backgroundMusicClip != null) {//NOPMD
      backgroundMusicClip.stop();
      backgroundMusicClip.close();
      backgroundMusicClip = null;//NOPMD - this null absoulty need
    }
  }

  private static AudioInputStream openAudioStream(final String resourcePath)
          throws UnsupportedAudioFileException, IOException {
    final InputStream in = SoundManager.class.getClassLoader().getResourceAsStream(resourcePath);//NOPMD
    if (in != null) {//NOPMD
      return AudioSystem.getAudioInputStream(new BufferedInputStream(in));
    }

    final File file = new File(System.getProperty("user.dir"), resourcePath);
    if (file.exists()) {//NOPMD
      return AudioSystem.getAudioInputStream(file);
    }

    if (LOGGER.isLoggable(Level.WARNING)) {
      LOGGER.warning(ERR_MSG_RES_NOT_FOUND + resourcePath);
    }
    return null;
  }

  private static AudioInputStream toPcmSigned(final AudioInputStream source)
          throws IOException {
    final AudioFormat format = source.getFormat();
    if (format.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)) {
      return source;
    }

    final AudioFormat targetFormat =
            new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false);
    return AudioSystem.getAudioInputStream(targetFormat, source);
  }

  public static void updateVolume() {
    if (loopClip != null && loopClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
      final int vol = SoundControl.getVolumeLevel(1);
      final boolean muted = SoundControl.isMuted(1) || vol == 0;
      final float volumeDb = muted ? -80.0f : calculateVolumeDb(vol);

      final FloatControl gain = (FloatControl) loopClip.getControl(FloatControl.Type.MASTER_GAIN);
      gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), volumeDb)));
    }

    if (backgroundMusicClip != null && backgroundMusicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
      final int vol = SoundControl.getIngameVolumeLevel(0);
      final boolean muted = SoundControl.isIngameMuted(0) || vol == 0;
      final float volumeDb = muted ? -80.0f : calculateVolumeDb(vol);

      final FloatControl gain = (FloatControl) backgroundMusicClip.getControl(FloatControl.Type.MASTER_GAIN);
      gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), volumeDb)));
    }
  }

  // [Fix] Restore Master Volume calculation logic
  private static float calculateVolumeDb(final int volumeLevel) {
    // 1. Check Master Mute (Index 0)
    if (SoundControl.isMuted(0)) {
      return -80.0f;
    }

    // 2. Get Master Volume
    final int masterVolume = SoundControl.getVolumeLevel(0);

    // 3. Calculate effective volume: (Channel Volume %) * (Master Volume %)
    final float effectiveRatio = (volumeLevel / 100.0f) * (masterVolume / 100.0f);

    if (effectiveRatio <= 0.0001f) {
      return -80.0f;
    }

    return (float) (20.0 * Math.log10(effectiveRatio));
  }
}