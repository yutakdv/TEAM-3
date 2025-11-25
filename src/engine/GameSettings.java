package engine;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.nio.file.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Implements an object that stores a single game's difficulty settings.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
@SuppressWarnings("PMD.LawOfDemeter")
public class GameSettings {

  private static final Logger LOGGER = Logger.getLogger(Core.class.getSimpleName());

  /** Width of the level's enemy formation. */
  private final int formationWidth;

  /** Height of the level's enemy formation. */
  private final int formationHeight;

  /** Speed of the enemies, function of the remaining number. */
  private final int baseSpeed;

  /** Frequency of enemy shootings, +/- 30%. */
  private final int shootingFrequency;

  private List<ChangeData> changeDataList;

  // 추가 사항
  public static class ChangeData { // NOPMD - Data class design is intentional
    // 바꿀 적 위치
    public int x;
    public int y;

    // 적 체력
    public int hp;
    // 적 색상
    public Color color;

    // 보상 배율
    public int multiplier;

    public ChangeData(final int x, final int y, final int hp, final int multiplier) {
      this.x = x;
      this.y = y;
      this.hp = hp;
      this.multiplier = multiplier;
    }

    public ChangeData(
        final int x, final int y, final int hp, final int multiplier, final Color color) {
      this.x = x;
      this.y = y;
      this.hp = hp;
      this.multiplier = multiplier;
      this.color = color;
    }
  }

  // StageData: Structure of GameSettings + List<ChangeData> at each stage
  public static class StageData {
    public final GameSettings settings;
    public final List<ChangeData> changeList;

    public StageData(final GameSettings settings, final List<ChangeData> changeList) {
      this.settings = settings;
      this.changeList = changeList;
    }
  }

  public static Color hexToColor(final String hex) {
      String cleanedHex = hex;
    if (cleanedHex.startsWith("#")) {
        cleanedHex = cleanedHex.substring(1);
    }
    if (cleanedHex.length() > 6) {
      final int alpha = Integer.parseInt(cleanedHex.substring(7), 16);
        cleanedHex = cleanedHex.substring(0, 6);
      final int red = Integer.parseInt(cleanedHex.substring(0, 2), 16);
      final int green = Integer.parseInt(cleanedHex.substring(3, 5), 16);
      final int blue = Integer.parseInt(cleanedHex.substring(5), 16);
      // if it is hex code with 8 digits(like #AAFFAABB)
      return new Color(red, green, blue, alpha);
    }
    final int rgb = Integer.parseInt(cleanedHex, 16);
    // if it is hex code with 6 digits(like #AAFFAA)
    return new Color(rgb);
  }

  public static List<StageData> parseStages(final InputStream in) throws Exception { // NOPMD
    String raw;

    try (BufferedReader br =
        new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      final StringBuilder sb = new StringBuilder();
      String line;
      line = br.readLine();
      while (line != null) {
        sb.append(line).append('\n');
        line = br.readLine();
      }
      raw = sb.toString();
    }

    raw = raw.replace("\uFEFF", "");
    raw = raw.replaceAll("(?m)^\\s*//.*$", "");

    final String[] stageBlocks = raw.split("&");
    final List<StageData> result = new ArrayList<>();
    for (final String block : stageBlocks) {
      final List<String> lines =
          Arrays.stream(block.split("\n"))
              .map(String::trim)
              .filter(s -> !s.isEmpty() && !s.startsWith("//")) // Deleting comments and empty lines
              .toList();
      if (lines.isEmpty()) {
        continue;
      }

      final String[] ints = lines.get(0).split(",");
      final GameSettings settings =
          new GameSettings( // NOPMD - per-row object creation required
              Integer.parseInt(ints[0].trim()),
              Integer.parseInt(ints[1].trim()),
              Integer.parseInt(ints[2].trim()),
              Integer.parseInt(ints[3].trim()));

      final List<ChangeData> changeList = new ArrayList<>(); // NOPMD - per-row object creation required
      for (int i = 1; i < lines.size(); i++) {
        final String[] parts = lines.get(i).split(",");
        final int x = Integer.parseInt(parts[0].trim());
        final int y = Integer.parseInt(parts[1].trim());
        final int z = Integer.parseInt(parts[2].trim());
        final int w = Integer.parseInt(parts[3].trim());
        Color color = null;
        if (parts.length >= 5) {
          String hex = parts[4].trim();
          if (hex.startsWith("#")) {
            hex = hex.substring(1);
          }
          color = hexToColor(hex);
        }
        changeList.add(
            new ChangeData(x, y, z, w, color)); // NOPMD - object creation required per CSV row
      }
      result.add(new StageData(settings, changeList));
    }

    return result;
  }

  public final List<ChangeData> getChangeDataList() {
    return changeDataList;
  }

  public static List<GameSettings> getGameSettings() {
    final List<GameSettings> result = new ArrayList<>();
    List<StageData> stageDataList;

    GameSettings setting;

    try {
      InputStream in = GameSettings.class.getClassLoader().getResourceAsStream("res/level.csv"); // NOPMD
      if (in == null) {
        in = Files.newInputStream(Paths.get("res", "level.csv"));
      }
      stageDataList = parseStages(in);

      for (final StageData s : stageDataList) {
        setting = s.settings;
        setting.changeDataList = new ArrayList<>(); // NOPMD - per-row object creation required

        setting.changeDataList.addAll(s.changeList);
        result.add(setting);
      }
    } catch (Exception e) { // NOPMD - generic catch intended for fatal load fail
      if (LOGGER.isLoggable(Level.INFO)) {
        LOGGER.info("Failed Loading Data: There is no such file named " + e.getMessage());
        LOGGER.info("By the error, game is closing.");
      }
      System.exit(1); // NOPMD - allowed for desktop game fatal failure
      return Collections.emptyList();
    }

    if (stageDataList.isEmpty()) {
      LOGGER.info("Failed Loading Data: There is no data in level.csv file.");
      LOGGER.info("By the error, game is closing.");
      System.exit(1); // NOPMD
      return Collections.emptyList();
    }
    final GameSettings base1 = result.get(result.size() - 2);
    final GameSettings base2 = result.get(result.size() - 1);
    final int INCREASE_AMOUNT_SHOOTING_FREQUENCY = 10; // NOPMD - name intentionally descriptive
    final int INCREASE_AMOUNT_SPEED = 1; // NOPMD - name intentionally descriptive
    for (int level = result.size() + 1; level < GameState.INFINITE_LEVEL; level++) {
      GameSettings base;
      if (level % 2 == 0) {
        base = base1;
      } else {
        base = base2;
      }
      final GameSettings infinity_setting = // NOPMD - name intentionally descriptive
          calculateInfiniteSetting(
              base,
              level - result.size(),
              INCREASE_AMOUNT_SHOOTING_FREQUENCY,
              INCREASE_AMOUNT_SPEED);
      infinity_setting.changeDataList.addAll(base.getChangeDataList());
      result.add(infinity_setting);
    }
    return result;
  }

  public static GameSettings calculateInfiniteSetting(
      final GameSettings base,
      final int infinityLevel,
      final int increaseFreq,
      final int increaseSpeed) {

    int newShootingFrequency =
        base.getShootingFrequency()
            - (infinityLevel * increaseFreq); // NOPMD - name intentionally descriptive
    int newSpeed = base.getBaseSpeed() - (infinityLevel * increaseSpeed);
    if (infinityLevel % 2 != 0) {
      newShootingFrequency -= 10;
      newSpeed -= 1;
    }

    if (newSpeed <= 0) {
      newSpeed = 1;
    }
    if (newShootingFrequency < 100) {
      newShootingFrequency = 100;
    }

    return new GameSettings(
        base.getFormationWidth(), base.getFormationHeight(), newSpeed, newShootingFrequency);
  }

  /**
   * Constructor.
   *
   * @param formationWidth Width of the level's enemy formation.
   * @param formationHeight Height of the level's enemy formation.
   * @param baseSpeed Speed of the enemies.
   * @param shootingFrequency Frequency of enemy shootings, +/- 30%.
   */
  public GameSettings(
      final int formationWidth,
      final int formationHeight,
      final int baseSpeed,
      final int shootingFrequency) {
    this.formationWidth = formationWidth;
    this.formationHeight = formationHeight;
    this.baseSpeed = baseSpeed;
    this.shootingFrequency = shootingFrequency;
    this.changeDataList = new ArrayList<>();
  }

  /**
   * @return the formationWidth
   */
  public final int getFormationWidth() {
    return formationWidth;
  }

  /**
   * @return the formationHeight
   */
  public final int getFormationHeight() {
    return formationHeight;
  }

  /**
   * @return the baseSpeed
   */
  public final int getBaseSpeed() {
    return baseSpeed;
  }

  /**
   * @return the shootingFrequency
   */
  public final int getShootingFrequency() {
    return shootingFrequency;
  }
}
