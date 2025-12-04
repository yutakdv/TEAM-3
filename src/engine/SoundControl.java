package engine;

/**
 * Controls the sound volume and mute settings. Refactored to address PMD issues and encapsulate
 * static state.
 */
@SuppressWarnings({"PMD.LawOfDemeter"})
public final class SoundControl {

  // PMD: Avoid non-final non-private static fields -> Made private
  private static final int[] IN_GAME_VOLUME = {50, 50};
  private static final boolean[] IN_GAME_MUTE = {false, false};
  private static int ingameVolumetype;

  private static final int[] PREVIOUS_IN_GAME_VOLUME = {50, 50};

  private static final int[] VOLUME_ARRAY = {50, 50, 50};
  private static final boolean[] MUTE_ARRAY = {false, false, false};
  private static int volumetype;

  private SoundControl() {
    // Private constructor to prevent instantiation
  }

  public static int getVolumeLevel(final int w) {
    return VOLUME_ARRAY[w];
  }

  public static int getVolumetype() {
    return volumetype;
  }

  public static void setVolumeLevel(final int w, final int v) {
    VOLUME_ARRAY[w] = Math.max(0, Math.min(100, v));
    volumetype = w;
  }

  public static boolean isMuted(final int index) {
    return MUTE_ARRAY[index];
  }

  public static void setMute(final int index, final boolean m) {
    MUTE_ARRAY[index] = m;
  }

  public static int getIngameVolumeLevel(final int idx) {
    return IN_GAME_VOLUME[idx];
  }

  public static int getIngameVolumetype() {
    return ingameVolumetype;
  }

  public static void setIngameVolumeLevel(final int idx, final int v) {
    final int clamped = Math.max(0, Math.min(100, v));
    IN_GAME_VOLUME[idx] = clamped;
    ingameVolumetype = idx;

    // Backup user-set value for unmute restoration
    if (clamped > 0) {
      PREVIOUS_IN_GAME_VOLUME[idx] = clamped;
    }
  }

  public static boolean isIngameMuted(final int idx) {
    return IN_GAME_MUTE[idx];
  }

  public static void setIngameMute(final int idx, final boolean m) {
    IN_GAME_MUTE[idx] = m;
  }

  public static void setIngameVolumetype(final int idx) {
    ingameVolumetype = idx;
  }

  public static void changeIngameVolume(final int idx, final int delta) {
    final int current = IN_GAME_VOLUME[idx];
    // PMD: Simplify boolean expressions logic
    final boolean canDecrease = delta < 0 && current > 0;
    final boolean canIncrease = delta > 0 && current < 100;

    if (canDecrease || canIncrease) {
      setIngameVolumeLevel(idx, current + delta);
      setIngameMute(idx, false); // Unmute if volume is changed manually
    }
  }

  public static void toggleIngameMute(final int idx) {
    int restore = PREVIOUS_IN_GAME_VOLUME[idx];
    if (IN_GAME_MUTE[idx]) {
      // Unmute: Restore backup value

      if (restore <= 0) {
        restore = 50; // Safety default
      }

      IN_GAME_VOLUME[idx] = restore;
      PREVIOUS_IN_GAME_VOLUME[idx] = restore;
      IN_GAME_MUTE[idx] = false;
    } else {
      // Mute: Backup current value then set to 0
      if (IN_GAME_VOLUME[idx] > 0) {
        PREVIOUS_IN_GAME_VOLUME[idx] = IN_GAME_VOLUME[idx];
      }
      IN_GAME_VOLUME[idx] = 0;
      IN_GAME_MUTE[idx] = true;
    }
  }
}
