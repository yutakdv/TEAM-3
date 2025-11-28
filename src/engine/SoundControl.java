package engine;

public final class SoundControl {
  private static final int[] ingameVolume = {50, 50};
  private static final boolean[] ingameMute = {false, false};
  private static int ingameVolumetype;

  private static final int[] volumearray = {50, 50, 50};
  private static final boolean[] Mute = {false, false, false};
  private static int volumetype;

  private SoundControl() {}

  public static int getVolumeLevel(final int w) {
    return volumearray[w];
  }

  public static int getVolumetype() {
    return volumetype;
  }

  public static void setVolumeLevel(final int w, final int v) {
    volumearray[w] = Math.max(0, Math.min(100, v));
    volumetype = w;
  }

  public static boolean isMuted(final int index) {
    return Mute[index];
  }

  public static void setMute(final int index, final boolean m) {
    Mute[index] = m;
  }

  public static int getIngameVolumeLevel(final int idx) {
    return ingameVolume[idx];
  }

  public static int getIngameVolumetype() {
    return ingameVolumetype;
  }

  public static void setIngameVolumeLevel(final int idx, final int v) {
    ingameVolume[idx] = Math.max(0, Math.min(100, v));
    ingameVolumetype = idx;
  }

  public static boolean isIngameMuted(final int idx) {
    return ingameMute[idx];
  }

  public static void setIngameMute(final int idx, final boolean m) {
    ingameMute[idx] = m;
  }

  public static void setIngameVolumetype(final int idx) {
    ingameVolumetype = idx;
  }
}
