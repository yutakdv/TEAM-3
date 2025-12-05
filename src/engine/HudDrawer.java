package engine;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import java.awt.RenderingHints;

import java.util.List;

import entity.Entity;
import screen.Screen;

/** Draws in-game HUD, results, countdown, and achievement toasts. */
@SuppressWarnings({"PMD.LawOfDemeter"})
public final class HudDrawer { // NOPMD

  private static final String FOUR_DIGIT_FORMAT = "%04d";

  private final DrawManager drawManager;

  HudDrawer(final DrawManager drawManager) {
    this.drawManager = drawManager;
  }

  // --- helpers to access DrawManager state ---

  private Graphics2D g2d() {
    return drawManager.getBackBufferGraphics();
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

  // ================= HUD / INFO =================

  /** Draws current score on screen. */
  public void drawScore(final Screen screen, final int score) {
    g2d().setFont(fontRegular());
    g2d().setColor(Color.WHITE);
    final String scoreString = String.format(FOUR_DIGIT_FORMAT, score);
    g2d().drawString(scoreString, screen.getWidth() - 60, 25);
  }

  /** Draws number of remaining lives on screen. */
  public void drawLives(final int lives, final boolean isCoop) {
    g2d().setFont(fontRegular());
    g2d().setColor(Color.WHITE);

    final Entity heart = new Entity(0, 0, 11 * 2, 10 * 2, Color.RED);
    heart.setSpriteType(DrawManager.SpriteType.Heart);

    if (isCoop) {
      g2d().drawString(Integer.toString(lives), 20, 25);
      for (int i = 0; i < lives; i++) {
        if (i < 3) {
          drawManager.drawEntity(heart, 40 + 35 * i, 9);
        } else {
          drawManager.drawEntity(heart, 40 + 35 * (i - 3), 9 + 25);
        }
      }
    } else {
      g2d().drawString(Integer.toString(lives), 20, 40);
      for (int i = 0; i < lives; i++) {
        drawManager.drawEntity(heart, 40 + 35 * i, 23);
      }
    }
  }

  /** Draws current coin count on screen. */
  public void drawCoins(final Screen screen, final int coins) {
    g2d().setFont(fontRegular());
    g2d().setColor(Color.YELLOW);
    final String coinString = String.format(FOUR_DIGIT_FORMAT, coins);
    g2d().drawString(coinString, screen.getWidth() - 60, 52);
    g2d().drawString("COIN : ", screen.getWidth() - 115, 52);
  }

  /** In ship selection screen, show coin info on top-right. */
  public void drawShipSelectionCoins(final Screen screen, final int coins) {
    g2d().setFont(fontRegular());
    g2d().setColor(Color.YELLOW);

    final String text = "COIN : " + String.format(FOUR_DIGIT_FORMAT, coins);

    final int padding = 10;
    final int textWidth = fmRegular().stringWidth(text);
    final int x = screen.getWidth() - padding - textWidth;
    final int y = padding + fmRegular().getAscent();

    g2d().drawString(text, x, y);
  }

  /** Draws a thick line from side to side of the screen. */
  public void drawHorizontalLine(final Screen screen, final int positionY) {
    g2d().setColor(Color.GREEN);
    g2d().drawLine(0, positionY, screen.getWidth(), positionY);
    g2d().drawLine(0, positionY + 1, screen.getWidth(), positionY + 1);
  }

  /** Draws current level text. */
  public void drawLevel(final Screen screen, final int level) {
    g2d().setColor(Color.WHITE);
    final String levelString = level > GameState.FINITE_LEVEL ? "Infinity Stage" : "Stage " + level;
    final FontMetrics fontMetrics = fmRegular();
    g2d()
        .drawString(
            levelString, screen.getWidth() / 2 - fontMetrics.stringWidth(levelString) / 2, 25);
  }

  /** Draws remaining enemy ship count. */
  public void drawShipCount(final Screen screen, final int shipCount) {
    g2d().setColor(Color.GREEN);
    final Entity enemyIcon = new Entity(0, 0, 12 * 2, 8 * 2, Color.GREEN);
    enemyIcon.setSpriteType(DrawManager.SpriteType.EnemyShipB2);
    final int iconX = screen.getWidth() - 252;
    final int iconY = 37;
    drawManager.drawEntity(enemyIcon, iconX, iconY);
    final String shipString = ": " + shipCount;
    g2d().drawString(shipString, iconX + 30, 52);
  }

  // ================= RESULTS / NAME INPUT =================

  public void drawResults(
      final Screen screen,
      final int score,
      final int coins,
      final int livesRemaining,
      final int shipsDestroyed,
      final float accuracy,
      final boolean isNewRecord,
      final boolean accuracy1P) {

    final String scoreString = String.format("score %04d", score);
    final String coinString = String.format("coins %04d", coins);
    final String livesRemainingString = String.format("lives remaining %d", livesRemaining);
    final String shipsDestroyedString = "enemies destroyed " + shipsDestroyed;
    final String accuracyString =
        String.format("accuracy %.2f%%", Float.isNaN(accuracy) ? 0.0 : accuracy * 100);

    final int height = 4;

    if (isNewRecord) {
      g2d().setColor(Color.RED);
    } else {
      g2d().setColor(Color.WHITE);
    }

    drawManager.drawCenteredRegularString(screen, scoreString, screen.getHeight() / height);
    drawManager.drawCenteredRegularString(
        screen, coinString, screen.getHeight() / height + fmRegular().getHeight() * 2);
    drawManager.drawCenteredRegularString(
        screen, livesRemainingString, screen.getHeight() / height + fmRegular().getHeight() * 4);
    drawManager.drawCenteredRegularString(
        screen, shipsDestroyedString, screen.getHeight() / height + fmRegular().getHeight() * 6);
    if (accuracy1P) {
      drawManager.drawCenteredRegularString(
          screen, accuracyString, screen.getHeight() / height + fmRegular().getHeight() * 8);
    }
  }

  public void drawNameInput(
      final Screen screen, final StringBuilder name, final boolean isNewRecord) {
    final String newRecordString = "New Record!";
    final String introduceNameString = "Name: ";
    final String nameStr = name.toString();

    if (isNewRecord) {
      g2d().setColor(Color.GREEN);
      drawManager.drawCenteredRegularString(
          screen, newRecordString, screen.getHeight() / 4 + fmRegular().getHeight() * 11);
    }

    final String displayName = name.isEmpty() ? "" : nameStr;
    final boolean showCursor = (System.currentTimeMillis() / 500) % 2 == 0;
    final String cursor = showCursor ? "|" : " ";
    final String displayText = introduceNameString + displayName + cursor;

    g2d().setColor(Color.WHITE);
    drawManager.drawCenteredRegularString(
        screen, displayText, screen.getHeight() / 4 + fmRegular().getHeight() * 12);
  }

  public void drawNameInputError(final Screen screen) {
    final String alert = "Enter at least 3 chars!";
    g2d().setColor(Color.YELLOW);
    drawManager.drawCenteredRegularString(
        screen, alert, screen.getHeight() / 4 + fmRegular().getHeight() * 13);
  }

  // ================= GAME OVER / PAUSE =================

  public void drawGameOver(final Screen screen, final boolean acceptsInput) {
    final String gameOverString = "Game Over";
    final String continueOrExitString = "Press Space to play again, Escape to exit";

    final int height = 4;

    g2d().setColor(Color.GREEN);
    drawManager.drawCenteredBigString(
        screen, gameOverString, screen.getHeight() / height - fmBig().getHeight() * 2);

    if (acceptsInput) {
      g2d().setColor(Color.GREEN);
    } else {
      g2d().setColor(Color.GRAY);
    }
    drawManager.drawCenteredRegularString(
        screen, continueOrExitString, screen.getHeight() / 2 + fmRegular().getHeight() * 10);
  }

  public void drawPauseOverlay(final Screen screen) {
    g2d().setColor(new Color(0, 0, 0, 200));
    g2d().fillRect(0, 0, screen.getWidth(), screen.getHeight());

    final String pauseString = "PAUSED";
    g2d().setFont(fontBig());
    g2d().setColor(Color.WHITE);
    drawManager.drawCenteredBigString(screen, pauseString, screen.getHeight() - 400);

    final String returnMenu = "PRESS BACKSPACE TO RETURN TO TITLE";
    final String mute = "press space to mute";
    g2d().setFont(fontRegular());
    g2d().setColor(Color.WHITE);
    drawManager.drawCenteredRegularString(screen, returnMenu, screen.getHeight() - 50);
    drawManager.drawCenteredRegularString(screen, mute, screen.getHeight() - 70);
  }

  // ================= COUNTDOWN =================

  public void drawCountDown(
      final Screen screen, final int level, final int number, final boolean bonusLife) {
    final int rectWidth = screen.getWidth();
    final int rectHeight = screen.getHeight() / 6;
    g2d().setColor(Color.BLACK);
    g2d().fillRect(0, screen.getHeight() / 2 - rectHeight / 2, rectWidth, rectHeight);
    g2d().setColor(Color.GREEN);
    final String message = getCountdownMessage(level, number, bonusLife);

    if (message != null) {
      drawManager.drawCenteredBigString(
          screen, message, screen.getHeight() / 2 + fmBig().getHeight() / 3);
      return;
    }
    final int y = screen.getHeight() / 2 + fmBig().getHeight() / 3;
    final String text = (number == 0) ? "GO!" : Integer.toString(number);

    drawManager.drawCenteredBigString(screen, text, y);
  }

  public String getCountdownMessage(final int level, final int number, final boolean bonusLife) {
    if (number < 4) {
      return null;
    }

    final String levelString = level > GameState.FINITE_LEVEL ? "Infinity Stage" : "Stage " + level;

    String message = null;
    if (level <= GameState.FINITE_LEVEL + 1) {
      message = levelString;
      if (bonusLife) {
        message += " - Bonus Life";
      }
    } else if (bonusLife) {
      message = "Bonus Life";
    }
    return message;
  }

  public void drawNewHighScoreNotice() {
    // 이전 DrawManager에서 주석 처리되어 있던 메서드라 그대로 비워둠.
  }

  // ================= ACHIEVEMENT TOASTS =================

  @SuppressWarnings("PMD.LawOfDemeter")
  public void drawAchievementToasts(final Screen screen, final List<Achievement> toasts) { // NOPMD
    if (toasts == null || toasts.isEmpty()) {
      return;
    }

    final Achievement achievement = toasts.getLast();
    final Graphics2D g2d = (Graphics2D) g2d().create();

    try {
      final int boxWidth = 350;
      final int boxHeight = 110;
      final int cornerRadius = 15;

      final int x = (screen.getWidth() - boxWidth) / 2;
      final int y = (screen.getHeight() - boxHeight) / 2;

      final boolean unlocked = achievement.isUnlocked();

      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
      g2d.setColor(Color.BLACK);
      g2d.fillRoundRect(x, y, boxWidth, boxHeight, cornerRadius, cornerRadius);

      g2d.setColor(Color.GREEN);
      g2d.setStroke(new BasicStroke(2));
      g2d.drawRoundRect(x, y, boxWidth, boxHeight, cornerRadius, cornerRadius);

      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));

      g2d.setFont(fontBig());

      if (unlocked) {
        g2d.setColor(Color.YELLOW);
        final FontMetrics bigMetrics = g2d.getFontMetrics(fontBig());
        final int titleWidth = bigMetrics.stringWidth("Achievement Clear!");
        g2d.drawString("Achievement Clear!", (screen.getWidth() - titleWidth) / 2, y + 35);

        g2d.setFont(fontRegular());
        g2d.setColor(Color.WHITE);
        final FontMetrics regularMetrics = g2d.getFontMetrics(fontRegular());
        final int nameWidth = regularMetrics.stringWidth(achievement.getName());
        g2d.drawString(achievement.getName(), (screen.getWidth() - nameWidth) / 2, y + 60);

        g2d.setColor(Color.LIGHT_GRAY);

        if (achievement.getDescription().length() < 30) {
          final int descWidth = regularMetrics.stringWidth(achievement.getDescription());
          g2d.drawString(
              achievement.getDescription(),
              (screen.getWidth() - descWidth) / 2,
              y + 80 + regularMetrics.getHeight() / 2);
        } else {
          final String line1 =
              achievement.getDescription().substring(0, achievement.getDescription().length() / 2);
          final String line2 =
              achievement.getDescription().substring(achievement.getDescription().length() / 2);

          final int line1Width = regularMetrics.stringWidth(line1);
          g2d.drawString(line1, (screen.getWidth() - line1Width) / 2, y + 80);

          final int line2Width = regularMetrics.stringWidth(line2);
          g2d.drawString(
              line2, (screen.getWidth() - line2Width) / 2, y + 80 + regularMetrics.getHeight());
        }
      } else {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        g2d.setColor(Color.YELLOW);
        final FontMetrics bigMetrics = g2d.getFontMetrics(fontBig());
        final String title = achievement.getName();
        final int titleWidth = bigMetrics.stringWidth(title);
        g2d.drawString(title, (screen.getWidth() - titleWidth) / 2, y + 65);

        g2d.setFont(fontRegular());
        g2d.setColor(Color.LIGHT_GRAY);
        final FontMetrics regularMetrics = g2d.getFontMetrics(fontRegular());

        final String[] lines = achievement.getDescription().split("\n");
        int lineY = y + 60;
        for (final String line : lines) {
          final int w = regularMetrics.stringWidth(line);
          g2d.drawString(line, (screen.getWidth() - w) / 2, lineY);
          lineY += regularMetrics.getHeight();
        }
      }
    } finally {
      g2d.dispose();
    }
  }
}
