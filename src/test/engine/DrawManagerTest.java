package engine;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DrawManagerTest {
  private DrawManager drawManager;

  @BeforeEach
  void setUp() {}

  @AfterEach
  void tearDown() {}

  // -----------------------------------------GetCountdownMessage----------------------------------
  @Test
  void testGetCountdownMessage() {
    // finite stage, Not Bonus Life
    String message1 = drawManager.getCountdownMessage(5, 5, false);
    assertEquals("Stage 5", message1);
    // finite stage, Bonus Life
    String message2 = drawManager.getCountdownMessage(3, 5, true);
    assertEquals("Stage 3 - Bonus Life", message2);
    // infinite stage,Not Bonus Life
    String message3 = drawManager.getCountdownMessage(6, 5, false);
    assertEquals("Infinity Stage", message3);
    // infinite stage 6,Not Bonus Life
    String message4 = drawManager.getCountdownMessage(6, 5, true);
    assertEquals("Infinity Stage - Bonus Life", message4);
    // infinite stage 7 or higher, Bonus Life
    String message5 = drawManager.getCountdownMessage(7, 5, true);
    assertEquals("Bonus Life", message5);
    // infinite stage 7 or higher, Bonus Life
    String message6 = drawManager.getCountdownMessage(7, 5, false);
    assertNull(message6);
    // countdown
    String message7 = drawManager.getCountdownMessage(7, 3, false);
    assertNull(message7);
  }
}
