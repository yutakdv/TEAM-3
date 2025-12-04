package engine;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import entity.Ship;
import screen.Screen;

/** Draws title, main menu, play menu, high scores, achievements, and ship selection. */
@SuppressWarnings({"PMD.LawOfDemeter"})
public final class MenuDrawer { // NOPMD

  private static final String BACK_LABEL = "< Back";
  private static final int MENU_HITBOX_OFFSET = 20;

  private final DrawManager drawManager;

  MenuDrawer(final DrawManager drawManager) {
    this.drawManager = drawManager;
  }

  private Graphics2D g2d() {
    return (Graphics2D) drawManager.getBackBufferGraphics();
  }

  private Font fontRegular() {
    return drawManager.getFontRegular();
  }

  private FontMetrics fmRegular() {
    return drawManager.getFontRegularMetrics();
  }

  private Font fontBig() {
    return drawManager.getFontBig();
  }

  private FontMetrics fmBig() {
    return drawManager.getFontBigMetrics();
  }

  // ================ TITLE / MAIN MENU =================

  /** Draws game title. */
  public void drawTitle(final Screen screen) {
    final String titleString = "Invaders";
    final String instructionsString = "select with w+s / arrows, confirm with space";

    g2d().setColor(Color.GRAY);
    drawManager.drawCenteredRegularString(screen, instructionsString, screen.getHeight() / 2);

    g2d().setColor(Color.GREEN);
    drawManager.drawCenteredBigString(screen, titleString, screen.getHeight() / 3);
  }

  /**
   * Draws main menu. - remodified for 2P mode, using string array for efficiency
   *
   * @param option legacy parameter (not used for highlight anymore)
   * @param hoverOption mouse hover index (nullable)
   * @param selectedIndex keyboard selection index
   */
  public void drawMenu(
      final Screen screen, final int option, final Integer hoverOption, final int selectedIndex) {
    final String[] items = {"Play", "Achievements", "High scores", "Settings", "Exit"};

    final int baseY = screen.getHeight() / 3 * 2 - 20;
    final int spacing = (int) (fmRegular().getHeight() * 1.5);

    for (int i = 0; i < items.length; i++) {
      final boolean highlight = (hoverOption != null) ? i == hoverOption : i == selectedIndex;
      g2d().setColor(highlight ? Color.GREEN : Color.WHITE);
      drawManager.drawCenteredRegularString(screen, items[i], baseY + spacing * i);
    }
  }

  // ================ HIGH SCORE & ACHIEVEMENTS =================

  /** Draws high score screen title and instructions. */
  public void drawHighScoreMenu(final Screen screen) {
    final String highScoreString = "High Scores";
    final String instructionsString = "Press ESC to return";

    final int midX = screen.getWidth() / 2;
    final int startY = screen.getHeight() / 3;
    g2d().setFont(fontBig());
    g2d().setColor(Color.GREEN);
    drawManager.drawCenteredBigString(screen, highScoreString, screen.getHeight() / 8);

    g2d().setFont(fontRegular());
    g2d().setColor(Color.GRAY);
    drawManager.drawCenteredRegularString(screen, instructionsString, screen.getHeight() / 5);

    g2d().setColor(Color.GREEN);

    g2d().drawString(
            "1-PLAYER MODE", midX / 2 - fmBig().stringWidth("1-PLAYER MODE") / 2 + 40, startY);
    g2d().drawString(
            "2-PLAYER MODE",
            midX + midX / 2 - fmBig().stringWidth("2-PLAYER MODE") / 2 + 40,
            startY);

    // draw back button at top-left
    drawBackButton(screen, false);
  }

  /** Draws high scores for 1P or 2P column. */
  public void drawHighScores(final Screen screen, final List<Score> highScores, final String mode) {
    g2d().setFont(fontRegular());
    g2d().setColor(Color.WHITE);
    int i = 0;
    String scoreString;

    final int midX = screen.getWidth() / 2;
    final int startY = screen.getHeight() / 3 + fmBig().getHeight() + 20;
    final int lineHeight = fmRegular().getHeight() + 5;

    for (final Score score : highScores) {
      scoreString = String.format("%s        %04d", score.getName(), score.getScore());
      final int x;
      if ("1p".equalsIgnoreCase(mode)) {
        x = midX / 2 - fmRegular().stringWidth(scoreString) / 2;
      } else {
        x = midX + midX / 2 - fmRegular().stringWidth(scoreString) / 2;
      }
      g2d().drawString(scoreString, x, startY + lineHeight * i);
      i++;
    }
  }

  /** Draws achievement menu & completer list. */
  public void drawAchievementMenu( // NOPMD
      final Screen screen, final Achievement achievement, final List<String> completer) {
    final String achievementsTitle = "Achievements";
    final String instructionsString = "Press ESC to return";
    final String playerModeString =
        "              1P                                      2P              ";
    final String prevNextString =
        "PREV                                                              NEXT";
    final String achievementName = achievement.getName();
    final String descriptionString = achievement.getDescription();

    g2d().setColor(Color.GREEN);
    drawManager.drawCenteredBigString(screen, achievementsTitle, screen.getHeight() / 10);
    drawManager.drawCenteredRegularString(screen, achievementName, screen.getHeight() / 7);
    g2d().setColor(Color.GRAY);
    drawManager.drawCenteredRegularString(screen, descriptionString, screen.getHeight() / 5);
    g2d().setColor(Color.GREEN);
    drawManager.drawCenteredRegularString(screen, playerModeString, screen.getHeight() / 4);
    g2d().setColor(Color.GRAY);
    drawManager.drawCenteredRegularString(
        screen, instructionsString, (int) (screen.getHeight() * 0.9));

    final int startY = (int) (screen.getHeight() * 0.3);
    final int lineHeight = 25;

    final int leftX = screen.getWidth() / 4;
    final int rightX = screen.getWidth() * 2 / 3;

    final List<String> team1 = new ArrayList<>();
    final List<String> team2 = new ArrayList<>();

    if (completer != null && !completer.isEmpty()) {
      for (final String entry : completer) {
        final String[] parts = entry.split(":");
        if (parts.length == 2) {
          final String modeString = parts[0].trim();
          final String numericPart = modeString.replaceAll("[^0-9]", "");
          final int mode = Integer.parseInt(numericPart);
          final String name = parts[1].trim();
          if (mode == 1) {
            team1.add(name);
          } else if (mode == 2) {
            team2.add(name);
          }
        }
      }

      final int maxLines = Math.max(team1.size(), team2.size());
      for (int i = 0; i < maxLines; i++) {
        final int y = startY + i * lineHeight;
        if (i < team1.size()) {
          g2d().setColor(Color.WHITE);
          g2d().drawString(team1.get(i), leftX, y);
        }
        if (i < team2.size()) {
          g2d().setColor(Color.WHITE);
          g2d().drawString(team2.get(i), rightX, y);
        }
      }

    } else {
      g2d().setColor(Color.GREEN);
      drawManager.drawCenteredBigString(
          screen, "No achievers found.", (int) (screen.getHeight() * 0.5));
    }

    g2d().setColor(Color.GREEN);
    drawManager.drawCenteredRegularString(screen, prevNextString, (int) (screen.getHeight() * 0.8));

    drawBackButton(screen, false);
  }

  /** Hitboxes for PREV / NEXT in achievement menu. */
  public Rectangle[] getAchievementNavHitboxes(final Screen screen) {
    if (drawManager.getFontRegularMetrics() == null) {
      g2d().setFont(fontRegular());
    }

    final String fullText =
        "PREV                                                              NEXT";
    final int baseY = (int) (screen.getHeight() * 0.8);

    final int fullWidth = fmRegular().stringWidth(fullText);
    final int textHeight = fmRegular().getHeight();

    final int startX = screen.getWidth() / 2 - fullWidth / 2;

    final int prevWidth = fmRegular().stringWidth("PREV");
    final int nextWidth = fmRegular().stringWidth("NEXT");

    final int prevX = startX;
    final int nextX = startX + fullWidth - nextWidth;

    final int paddingX = 10;
    final int paddingY = 3;

    final Rectangle prevBox =
        new Rectangle(
            prevX - paddingX,
            baseY - textHeight / 2 + 20,
            prevWidth + paddingX * 2,
            textHeight + paddingY);

    final Rectangle nextBox =
        new Rectangle(
            nextX - paddingX,
            baseY - textHeight / 2 + 20,
            nextWidth + paddingX * 2,
            textHeight + paddingY);
    return new Rectangle[] {prevBox, nextBox};
  }

  // ================ PLAY MODE MENU & BACK BUTTON =================

  /** Draws the play mode selection menu (1P / 2P / Back). */
  public void drawPlayMenu(
      final Screen screen, final Integer hoverOption, final int selectedIndex) {
    final String[] items = {"1P mode", "2P mode"};

    g2d().setColor(Color.GREEN);
    drawManager.drawCenteredBigString(screen, "Select Play Mode", screen.getHeight() / 5);

    g2d().setColor(Color.GRAY);
    drawManager.drawCenteredRegularString(
        screen,
        "Press ESC to Return, Confirm with Space",
        screen.getHeight() / 5 + fmRegular().getHeight() * 2);

    // draw back button at top-left corner
    drawBackButton(screen, selectedIndex == 2);

    final int baseY = screen.getHeight() / 2 - 20;
    for (int i = 0; i < items.length; i++) {
      final boolean highlight = (hoverOption != null) ? i == hoverOption : i == selectedIndex;
      g2d().setColor(highlight ? Color.GREEN : Color.WHITE);
      drawManager.drawCenteredRegularString(
          screen, items[i], baseY + fmRegular().getHeight() * 3 * i);
    }
  }

  /** Draw a "< Back" button at the top-left corner. */
  public void drawBackButton(final Screen screen, final boolean highlighted) {
    g2d().setFont(fontRegular());
    g2d().setColor(highlighted ? Color.GREEN : Color.WHITE);

    final int margin = 12;
    final int ascent = fmRegular().getAscent();
    g2d().drawString(BACK_LABEL, margin, margin + ascent);
  }

  /** Hitbox for main menu buttons. */
  public Rectangle[] getMenuHitboxes(final Screen screen) {
    if (drawManager.getFontRegularMetrics() == null) {
      g2d().setFont(fontRegular());
    }

    final String[] buttons = {"Play", "Achievement", "High scores", "Settings", "Exit"};

    final int baseY = screen.getHeight() / 3 * 2 - 20;
    final int spacing = (int) (fmRegular().getHeight() * 1.5);
    final Rectangle[] boxes = new Rectangle[buttons.length];

    for (int i = 0; i < buttons.length; i++) {
      final int baseline = baseY + spacing * i;
      boxes[i] = centeredStringBounds(screen, buttons[i], baseline);
    }

    return boxes;
  }

  /** Hitbox for back button. */
  public Rectangle getBackButtonHitbox(final Screen screen) {
    if (drawManager.getFontRegularMetrics() == null) {
      g2d().setFont(fontRegular());
    }

    final int margin = 12;
    final int ascent = fmRegular().getAscent();
    final int descent = fmRegular().getDescent();
    final int padTop = 2;

    final int y = margin - padTop;
    final int w = fmRegular().stringWidth(BACK_LABEL);
    final int h = ascent + descent + 25;

    return new Rectangle(margin, y, w, h);
  }

  /** Hitboxes for play mode menu (1P / 2P). */
  public Rectangle[] getPlayMenuHitboxes(final Screen screen) {
    if (drawManager.getFontRegularMetrics() == null) {
      g2d().setFont(fontRegular());
    }

    final String[] items = {"1 Player", "2 Players"};
    final int baseY = screen.getHeight() / 2 - 20;
    final Rectangle[] boxes = new Rectangle[items.length];

    for (int i = 0; i < items.length; i++) {
      final int baselineY = baseY + fmRegular().getHeight() * 3 * i;
      boxes[i] = centeredStringBounds(screen, items[i], baselineY);
    }

    return boxes;
  }

  // ================ SHIP SELECTION =================


  public void drawShipSelectionMenu(
      final Screen screen,
      final Ship[] shipExamples,
      final int selectedShipIndex,
      final int playerIndex,
      final boolean[] unlockedStates) { //NOPMD - do not need varargs

    final String screenTitle = "PLAYER " + playerIndex + " : CHOOSE YOUR SHIP";

    final String[] shipNames = {"Bronze", "Silver", "Gold", "Platinum"};
    final String[] shipSpeeds = {"SPEED: ■□□□□", "SPEED: ■■□□□", "SPEED: ■■■□□", "SPEED: ■■■■□"};
    final String[] shipFireRates = {
      "FIRE RATE: ■□□□□", "FIRE RATE: ■■□□□", "FIRE RATE: ■■□□□", "FIRE RATE: ■■■■□"
    };

    for (int i = 0; i < shipExamples.length; i++) {
      final Ship s = shipExamples[i];
      final int x = s.getPositionX() - s.getWidth() / 2;
      final int y = s.getPositionY();

      final boolean unlocked =
          unlockedStates != null && unlockedStates.length > i && unlockedStates[i];

      final Color shipColor;
      if (unlocked) {
        shipColor = (playerIndex == 1) ? Color.BLUE : Color.RED;
      } else {
        shipColor = Color.GRAY.darker().darker();
      }

      drawManager.drawEntity(s, x, y, shipColor);

      if (i == selectedShipIndex) {
        final int padding = 5;
        final int frameX = x - padding;
        final int frameY = y - padding;
        final int frameW = s.getWidth() + padding * 2;
        final int frameH = s.getHeight() + padding * 2;
        drawFrame(frameX, frameY, frameW, frameH, Color.WHITE, 8, 1);
      }
    }

    g2d().setColor(Color.GREEN);
    drawManager.drawCenteredBigString(screen, screenTitle, screen.getHeight() / 4);

    g2d().setColor(Color.WHITE);
    drawManager.drawCenteredRegularString(
        screen, " > " + shipNames[selectedShipIndex] + " < ", screen.getHeight() / 2 - 40);

    final Font original = g2d().getFont();
    final Font statFont = new Font("Dialog", Font.PLAIN, 18);
    g2d().setFont(statFont);
    final FontMetrics fm = g2d().getFontMetrics(statFont);

    final String speedStr = shipSpeeds[selectedShipIndex];
    final String fireStr = shipFireRates[selectedShipIndex];

    final int speedY = screen.getHeight() / 2 + 60;
    final int fireY = screen.getHeight() / 2 + 80;

    final int speedX = screen.getWidth() / 2 - fm.stringWidth(speedStr) / 2;
    final int fireX = screen.getWidth() / 2 - fm.stringWidth(fireStr) / 2;

    g2d().setColor(Color.WHITE);
    g2d().drawString(speedStr, speedX, speedY);
    g2d().drawString(fireStr, fireX, fireY);

    g2d().setFont(original);

    final boolean selectedUnlocked =
        unlockedStates == null
            || selectedShipIndex >= 0
                && selectedShipIndex < unlockedStates.length
                && unlockedStates[selectedShipIndex];

    g2d().setFont(fontRegular());

    final int statusY = screen.getHeight() / 2 + 115;
    final int costY = statusY + fmRegular().getHeight();

    if (selectedUnlocked) {
      g2d().setColor(Color.GREEN);
      drawManager.drawCenteredRegularString(screen, "STATUS: UNLOCKED", statusY);
    } else {
      g2d().setColor(Color.RED);
      drawManager.drawCenteredRegularString(screen, "STATUS: LOCKED", statusY);

      g2d().setColor(Color.YELLOW);
      final String costText = "COST: " + getShipUnlockCost(selectedShipIndex) + " Coins";
      drawManager.drawCenteredRegularString(screen, costText, costY);
    }

    g2d().setColor(Color.GRAY);
    drawManager.drawCenteredRegularString(
        screen, "Press Esc to return, Confirm with Space", screen.getHeight() - 50);
  }

  private int getShipUnlockCost(final int index) {
    switch (index) {
      case 0:
        return 0;
      case 1:
        return 2000;
      case 2:
        return 3500;
      case 3:
        return 5000;
      default:
        return 0;
    }
  }


  public Rectangle[] getShipSelectionHitboxes(final Screen screen, final Ship[] ships) {//NOPMD - do not need varargs
    final Rectangle[] boxes = new Rectangle[ships.length];
    for (int i = 0; i < ships.length; i++) {
      final Ship s = ships[i];
      final int x = s.getPositionX() - s.getWidth() / 2;
      final int y = s.getPositionY() + 30;
      boxes[i] = new Rectangle(x - 10, y - 10, s.getWidth() + 20, s.getHeight() + 20);
    }
    return boxes;
  }

  // ================ INTERNAL HELPERS =================

  private Rectangle centeredStringBounds(
      final Screen screen, final String string, final int baselineY) {
    g2d().setFont(fontRegular());
    final int pad = 4;

    final int textWidth = fmRegular().stringWidth(string);
    final int ascent = fmRegular().getAscent();
    final int descent = fmRegular().getDescent();

    final int x = screen.getWidth() / 2 - textWidth / 2;
    final int y = baselineY - ascent + MENU_HITBOX_OFFSET - pad / 2;
    final int h = ascent + descent + pad;

    return new Rectangle(x, y, textWidth, h);
  }


  private void drawFrame(
      final int x,
      final int y,
      final int width,
      final int height,
      final Color color,
      final int cornerLength,
      final int thickness) {
    final Graphics2D g2d = g2d();
    g2d.setColor(color);
    g2d.setStroke(new java.awt.BasicStroke(thickness));

    // upper left
    g2d.drawLine(x, y, x + cornerLength, y);
    g2d.drawLine(x, y, x, y + cornerLength);

    // upper right
    g2d.drawLine(x + width - cornerLength, y, x + width, y);
    g2d.drawLine(x + width, y, x + width, y + cornerLength);

    // lower left
    g2d.drawLine(x, y + height, x + cornerLength, y + height);
    g2d.drawLine(x, y + height - cornerLength, x, y + height);

    // lower right
    g2d.drawLine(x + width - cornerLength, y + height, x + width, y + height);
    g2d.drawLine(x + width, y + height - cornerLength, x + width, y + height);
  }
}
