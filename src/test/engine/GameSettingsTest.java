package engine;

import org.junit.jupiter.api.*;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class GameSettingsTest {

  // --------------------------- hexToColor ------------------------------
  @Test
  @DisplayName("hexToColor parses 6-digit hex correctly")
  void testHexToColor_rgb() {
    Color c = GameSettings.hexToColor("FF0000"); // red
    assertEquals(new Color(0xFF0000), c);
  }

  @Test
  @DisplayName("hexToColor parses # prefix correctly")
  void testHexToColor_withHash() {
    Color c = GameSettings.hexToColor("#00FF00"); // green
    assertEquals(new Color(0x00FF00), c);
  }

  // --------------------------- ChangeData Tests ------------------------------
  @Test
  void testChangeDataConstructor_basic() {
    GameSettings.ChangeData cd = new GameSettings.ChangeData(1, 2, 3, 4);
    assertEquals(1, cd.x);
    assertEquals(2, cd.y);
    assertEquals(3, cd.hp);
    assertEquals(4, cd.multiplier);
    assertNull(cd.color);
  }

  @Test
  void testChangeDataConstructor_withColor() {
    Color color = new Color(10, 20, 30);
    GameSettings.ChangeData cd = new GameSettings.ChangeData(1, 2, 3, 4, color);
    assertEquals(color, cd.color);
  }

  // --------------------------- StageData ------------------------------
  @Test
  void testStageDataConstructor() {
    GameSettings gs = new GameSettings(10, 20, 30, 40);
    GameSettings.ChangeData cd = new GameSettings.ChangeData(1, 2, 3, 4);
    GameSettings.StageData stage = new GameSettings.StageData(gs, List.of(cd));

    assertSame(gs, stage.settings);
    assertEquals(1, stage.changeList.size());
  }

  // --------------------------- GameSettings constructor & getters ------------------------------
  @Test
  void testGameSettingsConstructorAndGetters() {
    GameSettings gs = new GameSettings(11, 22, 33, 44);

    assertEquals(11, gs.getFormationWidth());
    assertEquals(22, gs.getFormationHeight());
    assertEquals(33, gs.getBaseSpeed());
    assertEquals(44, gs.getShootingFrequency());
    assertNotNull(gs.getChangeDataList());
  }

  // --------------------------- parseStages (Core test) ------------------------------
  @Test
  @DisplayName("parseStages parses a valid stage block correctly")
  void testParseStages_basic() throws Exception {

    // Build a fake CSV-like stage text
    String fakeLevel =
        "10,20,30,40\n"
            + // GameSettings
            "1,2,3,4,#FF0000\n"
            + // ChangeData
            "&\n"
            + // End of block
            "50,60,70,80\n"
            + "5,6,7,8,#00FF00\n";

    InputStream in = new ByteArrayInputStream(fakeLevel.getBytes(StandardCharsets.UTF_8));
    List<GameSettings.StageData> stages = GameSettings.parseStages(in);

    assertEquals(2, stages.size());

    GameSettings.StageData s1 = stages.get(0);
    assertEquals(10, s1.settings.getFormationWidth());
    assertEquals(1, s1.changeList.size());
    assertEquals(3, s1.changeList.get(0).hp);

    GameSettings.StageData s2 = stages.get(1);
    assertEquals(50, s2.settings.getFormationWidth());
    assertEquals(8, s2.changeList.get(0).multiplier);
  }

  @Test
  @DisplayName("parseStages ignores empty lines and comments correctly")
  void testParseStages_ignoreEmptyAndComments() throws Exception {

    String fake =
        "// comment line should be ignored\n"
            + "\n"
            + "10,20,30,40\n"
            + "// another comment\n"
            + "1,2,3,4,#ABCDEF\n"
            + "&";

    InputStream in = new ByteArrayInputStream(fake.getBytes(StandardCharsets.UTF_8));
    List<GameSettings.StageData> stages = GameSettings.parseStages(in);

    assertEquals(1, stages.size());
    assertEquals(10, stages.get(0).settings.getFormationWidth());
    assertEquals(1, stages.get(0).changeList.size());
  }

  @Test
  @DisplayName("parseStages supports multiple ChangeData rows")
  void testParseStages_multipleChangeData() throws Exception {
    String fake = "10,20,30,40\n" + "1,1,1,1,#111111\n" + "2,2,2,2,#222222\n" + "&";

    InputStream in = new ByteArrayInputStream(fake.getBytes(StandardCharsets.UTF_8));
    List<GameSettings.StageData> stages = GameSettings.parseStages(in);

    assertEquals(1, stages.size());
    assertEquals(2, stages.get(0).changeList.size());
  }

  @Test
  @DisplayName("parseStages returns empty list when only comments exist")
  void testParseStages_onlyComments() throws Exception {
    String fake = "// comment\n// comment 2\n&";
    InputStream in = new ByteArrayInputStream(fake.getBytes(StandardCharsets.UTF_8));

    List<GameSettings.StageData> stages = GameSettings.parseStages(in);
    assertEquals(0, stages.size());
  }
}
