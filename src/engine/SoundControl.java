package engine;

public final class SoundControl {
  private static final int[] ingameVolume = {50, 50};
  private static final boolean[] ingameMute = {false, false};
  private static int ingameVolumetype;

  private static final int[] previousIngameVolume = {50, 50};

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
    final int clamped = Math.max(0, Math.min(100, v));
    ingameVolume[idx] = clamped;
    ingameVolumetype = idx;

    // [핵심] 사용자가 직접 조절한 값은 '복구용 값'으로도 저장해둠
    if (clamped > 0) {
      previousIngameVolume[idx] = clamped;
    }
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

  public static void changeIngameVolume(final int idx, final int delta) {
    final int current = ingameVolume[idx];
    if ((delta < 0 && current > 0) || (delta > 0 && current < 100)) { // NOPMD
      setIngameVolumeLevel(idx, current + delta);
      setIngameMute(idx, false); // 조절하면 뮤트 해제
    }
  }

  public static void toggleIngameMute(final int idx) {
    if (!ingameMute[idx]) { // NOPMD
      // Mute 수행: 현재 값 백업 후 0으로
      if (ingameVolume[idx] > 0) {
        previousIngameVolume[idx] = ingameVolume[idx];
      }
      ingameVolume[idx] = 0;
      ingameMute[idx] = true;
    } else {
      // Unmute 수행: 백업된 값 복구
      int restore = previousIngameVolume[idx];
      if (restore <= 0) {
        restore = 50; // 안전장치
      }

      ingameVolume[idx] = restore;
      previousIngameVolume[idx] = restore;
      ingameMute[idx] = false;
    }
  }
}
