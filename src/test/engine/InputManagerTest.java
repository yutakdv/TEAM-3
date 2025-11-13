package engine;

import org.junit.jupiter.api.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class InputManagerTest {

  private InputManager input;

  @BeforeEach
  void setup() {
    input = InputManager.getInstance();
    InputManager.resetKeys();

    // 초기 key 설정 다시 강제
    input.setPlayer1Keys(new int[] {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE});
    input.setPlayer2Keys(new int[] {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER});
  }

  // ------------------------------------------------------------
  //  1) keyPressed / keyReleased
  // ------------------------------------------------------------
  @Test
  void testKeyPressedAndReleased() {
    KeyEvent pressA = new KeyEvent(new java.awt.Label(), 0, 0, 0, KeyEvent.VK_A, 'A');
    input.keyPressed(pressA);
    assertTrue(input.isKeyDown(KeyEvent.VK_A));

    KeyEvent releaseA = new KeyEvent(new java.awt.Label(), 0, 0, 0, KeyEvent.VK_A, 'A');
    input.keyReleased(releaseA);
    assertFalse(input.isKeyDown(KeyEvent.VK_A));
  }

  // ------------------------------------------------------------
  //  2) getLastPressedKey()
  // ------------------------------------------------------------
  @Test
  void testLastPressedKey() {
    KeyEvent press = new KeyEvent(new java.awt.Label(), 0, 0, 0, KeyEvent.VK_D, 'D');
    input.keyPressed(press);

    int last = input.getLastPressedKey();
    assertEquals(KeyEvent.VK_D, last);

    // 호출 후 리셋되어야 함
    assertEquals(-1, input.getLastPressedKey());
  }

  // ------------------------------------------------------------
  //  3) keyTyped / getLastCharTyped
  // ------------------------------------------------------------
  @Test
  void testGetLastCharTyped() {
    KeyEvent typeA = new KeyEvent(new java.awt.Label(), 0, 0, 0, KeyEvent.VK_A, 'A');
    input.keyTyped(typeA);

    assertEquals('A', input.getLastCharTyped());
    assertEquals('\0', input.getLastCharTyped()); // 두번째는 리셋됨
  }

  // ------------------------------------------------------------
  //  4) isKeyPressed (edge-trigger test)
  // ------------------------------------------------------------
  @Test
  void testIsKeyPressedEdge() {
    // 처음 프레임: key down
    KeyEvent pressSpace = new KeyEvent(new java.awt.Label(), 0, 0, 0, KeyEvent.VK_SPACE, ' ');
    input.keyPressed(pressSpace);

    // updatekeystatus() 호출 전
    assertTrue(input.isKeyPressed(KeyEvent.VK_SPACE)); // ✔ 첫 프레임 true

    // 상태 업데이트
    InputManager.updatekeystatus();

    // 두번째 프레임은 false
    assertFalse(input.isKeyPressed(KeyEvent.VK_SPACE));
  }

  // ------------------------------------------------------------
  //  5) Player1 / Player2 key mapping
  // ------------------------------------------------------------
  @Test
  void testPlayer1KeyMapping() {
    assertArrayEquals(
        new int[] {KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE}, input.getPlayer1Keys());
  }

  @Test
  void testPlayer2KeyMapping() {
    assertArrayEquals(
        new int[] {KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER}, input.getPlayer2Keys());
  }

  // ------------------------------------------------------------
  //  6) mousePressed / mouseReleased / isMouseClicked / isMousePressed
  // ------------------------------------------------------------
  @Test
  void testMousePressReleaseClick() {
    MouseEvent press = new MouseEvent(new java.awt.Label(), 0, 0, 0, 100, 200, 1, false);
    input.mousePressed(press);

    assertTrue(input.isMousePressed());
    assertEquals(100, input.getMouseX());
    assertEquals(200, input.getMouseY());

    MouseEvent release = new MouseEvent(new java.awt.Label(), 0, 0, 0, 150, 250, 1, false);
    input.mouseReleased(release);

    assertFalse(input.isMousePressed());
    assertEquals(150, input.getMouseX());
    assertEquals(250, input.getMouseY());

    assertTrue(input.isMouseClicked()); // 한 번만 true
    assertFalse(input.isMouseClicked());
  }

  // ------------------------------------------------------------
  //  7) mouseMoved / mouseDragged
  // ------------------------------------------------------------
  @Test
  void testMouseMoveAndDrag() {
    MouseEvent move = new MouseEvent(new java.awt.Label(), 0, 0, 0, 120, 300, 0, false);
    input.mouseMoved(move);

    assertEquals(120, input.getMouseX());
    assertEquals(300, input.getMouseY());

    MouseEvent drag = new MouseEvent(new java.awt.Label(), 0, 0, 0, 400, 500, 0, false);
    input.mouseDragged(drag);

    assertEquals(400, input.getMouseX());
    assertEquals(500, input.getMouseY());
  }

  // ------------------------------------------------------------
  //  8) resetKeys()
  // ------------------------------------------------------------
  @Test
  void testResetKeys() {
    KeyEvent pressA = new KeyEvent(new java.awt.Label(), 0, 0, 0, KeyEvent.VK_A, 'A');
    input.keyPressed(pressA);

    InputManager.resetKeys();
    assertFalse(input.isKeyDown(KeyEvent.VK_A));
  }

  // ------------------------------------------------------------
  //  9) saveKeyConfig / loadKeyConfig
  // ------------------------------------------------------------
  @Test
  void testSaveLoadKeyConfig() throws Exception {

    // 테스트용 temp 디렉토리 생성
    File tempDir = new File("test_res/");
    tempDir.mkdirs();

    System.setProperty("user.dir", tempDir.getAbsolutePath());

    // 새 키 설정
    input.setPlayer1Keys(new int[] {1, 2, 3});
    input.setPlayer2Keys(new int[] {4, 5, 6});

    // 저장
    input.saveKeyConfig();

    // 기존 값 초기화 후 load
    input.setPlayer1Keys(new int[] {10, 11, 12});
    input.setPlayer2Keys(new int[] {13, 14, 15});
    input.loadKeyConfig();

    assertArrayEquals(new int[] {1, 2, 3}, input.getPlayer1Keys());
    assertArrayEquals(new int[] {4, 5, 6}, input.getPlayer2Keys());
  }
}
