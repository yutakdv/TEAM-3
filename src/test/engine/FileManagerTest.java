package engine;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/** FileManager의 현재 실제 동작을 100% 기준으로 테스트하는 JUnit Test 클래스 */
public class FileManagerTest {

  @TempDir File tempDir;

  private static final String MODE = "1P";

  private File scoreFile;

  @BeforeEach
  public void setup() throws Exception {

    // FileManager를 테스트용 temp 디렉토리로 강제 변경
    FileManager.setTestDirectory(tempDir.getAbsolutePath() + File.separator);

    // 스코어 파일 초기화
    scoreFile = new File(tempDir, MODE + "scores.csv");
    if (scoreFile.exists() && !scoreFile.delete()) {
      throw new IOException("Failed to delete file: " + scoreFile.getAbsolutePath());
    }

    // 기본 헤더 생성
    try (BufferedWriter w =
        new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(scoreFile), StandardCharsets.UTF_8))) {
      w.write("player,score");
      w.newLine();
    }
  }

  // ---------------------------------------------------------
  // HighScore 테스트
  // ---------------------------------------------------------

  @Test
  @DisplayName("HighScore - 저장 후 로드하면 동일 개수 유지")
  public void testSaveAndLoadHighScore() throws Exception {
    FileManager fm = FileManager.getInstance();

    List<Score> in = new ArrayList<>();
    in.add(new Score("AAA", 100, MODE));
    in.add(new Score("BBB", 200, MODE));
    in.add(new Score("CCC", 300, MODE));

    fm.saveHighScores(in, MODE);

    List<Score> loaded = fm.loadHighScores(MODE);

    assertEquals(3, loaded.size());
    assertEquals("CCC", loaded.get(0).getName()); // 내림차순 정렬됨
  }

  @Test
  @DisplayName("HighScore - Top7 트림 없음: 12개 넣으면 12개 저장됨")
  public void testNoTop7Trim() throws Exception {
    FileManager fm = FileManager.getInstance();

    List<Score> list = new ArrayList<>();
    for (int i = 0; i < 12; i++) list.add(new Score("P" + i, i * 10, MODE));

    fm.saveHighScores(list, MODE);

    List<Score> loaded = fm.loadHighScores(MODE);

    assertEquals(12, loaded.size()); // 그대로 12개 유지
  }

  // ---------------------------------------------------------
  // 경로 테스트
  // ---------------------------------------------------------

  @Test
  @DisplayName("경로 분기 - getSaveDirectory()가 tempDir을 반환해야 함")
  public void testDirectoryOverride() {
    FileManager fm = FileManager.getInstance();
    String actual = invokeGetSaveDirectory(fm);
    String expected = tempDir.getAbsolutePath() + File.separator;

    assertEquals(expected, actual);
  }

  // private 메서드 호출용
  private String invokeGetSaveDirectory(FileManager fm) {
    try {
      var m = FileManager.class.getDeclaredMethod("getSaveDirectory");
      m.setAccessible(true);
      return (String) m.invoke(fm);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
