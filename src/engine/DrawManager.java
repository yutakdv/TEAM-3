package engine;

import Animations.BasicGameSpace;
import Animations.Explosion;
import Animations.MenuSpace;
import entity.Bullet;
import entity.Entity;
import entity.Ship;
import screen.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages screen drawing.
 *
 * @author <a href="mailto:RobertoIA1987@gmail.com">Roberto Izquierdo Amo</a>
 */
public final class DrawManager {

  /** Singleton instance of the class. */
  private static DrawManager instance;

  /** Current frame. */
  private Frame frame;

  /** FileManager instance. */
  private final FileManager fileManager; // NOPMD - used across methods

  /** Application logger. */
  private final Logger logger;

  /** Graphics context. */
  private Graphics graphics;

  /** Buffer Graphics. */
  private Graphics backBufferGraphics;

  /** Buffer image. */
  private BufferedImage backBuffer;

  /** Normal sized font. */
  private Font fontRegular;

  /** Normal sized font properties. */
  private FontMetrics fontRegularMetrics;

  /** Big sized font. */
  private Font fontBig;

  /** Big sized font properties. */
  private FontMetrics fontBigMetrics;

  private final HudDrawer hudDrawer = new HudDrawer(this);
  private final MenuDrawer menuDrawer = new MenuDrawer(this);
  private final SettingsDrawer settingsDrawer = new SettingsDrawer(this);

  public static final String FOUR_DIGIT_FORMAT = "%04d";

  /** Sprite types mapped to their images. */
  private Map<SpriteType, boolean[][]> spriteMap;

  private final List<Explosion> explosions = new ArrayList<>();

  private final Random explosionRandom = new Random();

  /** Stars background animations for both game and main menu Star density specified as argument. */
  BasicGameSpace basicGameSpace = new BasicGameSpace(100);

  MenuSpace menuSpace = new MenuSpace(50);
  int explosion_size = 2;

  /** Sprite types. */
  public enum SpriteType {
    /** Player ship. */
    Ship1,
    Ship2,
    Ship3,
    Ship4,
    /** Destroyed player ship. */
    ShipDestroyed1,
    ShipDestroyed2,
    ShipDestroyed3,
    ShipDestroyed4,
    /** Player bullet. */
    Bullet,
    /** Enemy bullet. */
    EnemyBullet,
    /** First enemy ship - first form. */
    EnemyShipA1,
    /** First enemy ship - second form. */
    EnemyShipA2,
    /** Second enemy ship - first form. */
    EnemyShipB1,
    /** Second enemy ship - second form. */
    EnemyShipB2,
    /** Third enemy ship - first form. */
    EnemyShipC1,
    /** Third enemy ship - second form. */
    EnemyShipC2,
    /** Bonus ship. */
    EnemyShipSpecial,
    /** Destroyed enemy ship. */
    Explosion,
    /** Heart for lives display. */
    Heart,
    /** Item Graphics Temp */
    ItemScore,
    ItemCoin,
    ItemHeal,
    ItemTripleShot,
    ItemScoreBooster,
    ItemBulletSpeedUp
  }

  /** Private constructor. */
  private DrawManager() {
    fileManager = Core.getFileManager();
    logger = Core.getLogger();
    logger.info("Started loading resources.");

    try {
      spriteMap = new LinkedHashMap<>();

      spriteMap.put(SpriteType.Ship1, new boolean[13][8]);
      spriteMap.put(SpriteType.Ship2, new boolean[13][8]);
      spriteMap.put(SpriteType.Ship3, new boolean[13][8]);
      spriteMap.put(SpriteType.Ship4, new boolean[13][8]);
      spriteMap.put(SpriteType.ShipDestroyed1, new boolean[13][8]);
      spriteMap.put(SpriteType.ShipDestroyed2, new boolean[13][8]);
      spriteMap.put(SpriteType.ShipDestroyed3, new boolean[13][8]);
      spriteMap.put(SpriteType.ShipDestroyed4, new boolean[13][8]);
      spriteMap.put(SpriteType.Bullet, new boolean[3][5]);
      spriteMap.put(SpriteType.EnemyBullet, new boolean[3][5]);
      spriteMap.put(SpriteType.EnemyShipA1, new boolean[12][8]);
      spriteMap.put(SpriteType.EnemyShipA2, new boolean[12][8]);
      spriteMap.put(SpriteType.EnemyShipB1, new boolean[12][8]);
      spriteMap.put(SpriteType.EnemyShipB2, new boolean[12][8]);
      spriteMap.put(SpriteType.EnemyShipC1, new boolean[12][8]);
      spriteMap.put(SpriteType.EnemyShipC2, new boolean[12][8]);
      spriteMap.put(SpriteType.EnemyShipSpecial, new boolean[16][7]);
      spriteMap.put(SpriteType.Explosion, new boolean[13][7]);
      spriteMap.put(SpriteType.Heart, new boolean[11][10]);

      // Item sprites
      spriteMap.put(SpriteType.ItemScore, new boolean[5][5]);
      spriteMap.put(SpriteType.ItemCoin, new boolean[5][5]);
      spriteMap.put(SpriteType.ItemHeal, new boolean[5][5]);
      spriteMap.put(SpriteType.ItemTripleShot, new boolean[5][5]);
      spriteMap.put(SpriteType.ItemScoreBooster, new boolean[5][5]);
      spriteMap.put(SpriteType.ItemBulletSpeedUp, new boolean[5][5]);

      fileManager.loadSprite(spriteMap);
      logger.info("Finished loading the sprites.");

      // Font loading.
      fontRegular = fileManager.loadFont(14f);
      fontBig = fileManager.loadFont(24f);
      logger.info("Finished loading the fonts.");

    } catch (IOException e) {
      logger.warning("Loading failed.");
    } catch (FontFormatException e) {
      logger.warning("Font formating failed.");
    }
  }

  /**
   * Returns shared instance of DrawManager.
   *
   * @return Shared instance of DrawManager.
   */
  static DrawManager getInstance() {
    if (instance == null) {
      instance = new DrawManager();
    }
    return instance;
  }

  /**
   * Sets the frame to draw the image on.
   *
   * @param currentFrame Frame to draw on.
   */
  public void setFrame(final Frame currentFrame) {
    this.frame = currentFrame;
  }

  /**
   * First part of the drawing process. Initialises buffers, draws the background and prepares the
   * images.
   *
   * @param screen Screen to draw in.
   */
  public void initDrawing(final Screen screen) {
    this.backBuffer =
            new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_INT_RGB);

    this.graphics = this.frame.getGraphics();
    this.backBufferGraphics = this.backBuffer.getGraphics();

    this.backBufferGraphics.setColor(Color.BLACK);
    this.backBufferGraphics.fillRect(0, 0, screen.getWidth(), screen.getHeight());

    this.fontRegularMetrics = this.backBufferGraphics.getFontMetrics(this.fontRegular);
    this.fontBigMetrics = this.backBufferGraphics.getFontMetrics(this.fontBig);
  }

  /**
   * Draws the completed drawing on screen.
   *
   * @param screen Screen to draw on.
   */
  public void completeDrawing(final Screen screen) {
    this.graphics.drawImage(
            this.backBuffer, this.frame.getInsets().left, this.frame.getInsets().top, this.frame);
  }

  /**
   * Draws an entity, using the appropriate image.
   *
   * @param entity Entity to be drawn.
   * @param positionX Coordinates for the left side of the image.
   * @param positionY Coordinates for the upper side of the image.
   */
  public void drawEntity(final Entity entity, final int positionX, final int positionY) {
    drawEntity(entity, positionX, positionY, null);
  }

  public void drawEntity(
          final Entity entity, final int positionX, final int positionY, final Color override) {
    final boolean[][] image = spriteMap.get(entity.getSpriteType());

    Color color = (override != null) ? override : entity.getColor();

    if (override == null) {
      // Color-code by player when applicable
      if (entity instanceof Ship) {
        final Ship ship = (Ship) entity;
        final int pid = ship.getPlayerId(); // NOPMD - LawOfDemeter
        if (pid == 1) {
          color = Color.BLUE;
        } else if (pid == 2) {
          color = Color.RED;
        }
      } else if (entity instanceof Bullet) {
        final Bullet bullet = (Bullet) entity;
        final int pid = bullet.getPlayerId(); // NOPMD - LawOfDemeter
        if (pid == 1) {
          color = Color.CYAN;
        } else if (pid == 2) {
          color = Color.MAGENTA;
        }
        // enemy bullets keep default color
      }
    }

    // --- Scaling logic ---
    final int spriteWidth = image.length;
    final int spriteHeight = image[0].length;

    final int entityWidth = entity.getWidth();
    final int entityHeight = entity.getHeight();

    final float widthRatio = (float) entityWidth / (spriteWidth * 2);
    final float heightRatio = (float) entityHeight / (spriteHeight * 2);
    // --- End of scaling logic ---

    backBufferGraphics.setColor(color);
    for (int i = 0; i < spriteWidth; i++) {
      for (int j = 0; j < spriteHeight; j++) {
        if (image[i][j]) {
          backBufferGraphics.fillRect(
                  positionX + (int) (i * 2 * widthRatio),
                  positionY + (int) (j * 2 * heightRatio),
                  (int) Math.ceil(widthRatio * 2),
                  (int) Math.ceil(heightRatio * 2));
        }
      }
    }
  }

  /** Main-menu starfield hover effect. */
  public void menuHover(final int state) {
    menuSpace.setColor(state);
    menuSpace.setSpeed(state == 4);
  }

  public void triggerExplosion(
          final int x, final int y, final boolean enemy, final boolean finalExplosion) {
    if (logger.isLoggable(Level.INFO)) {
      logger.info("Enemy: " + enemy);
      logger.info("final: " + finalExplosion);
    }
    explosions.add(new Explosion(x, y, enemy, finalExplosion));
  }

  @SuppressWarnings({"PMD.LawOfDemeter", "PMD.AvoidInstantiatingObjectsInLoops"})
  public void drawExplosions() {

    final Graphics2D g2d = (Graphics2D) backBufferGraphics;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2d.setColor(Color.WHITE);

    final Iterator<Explosion> iterator = explosions.iterator();

    while (iterator.hasNext()) {
      final Explosion e = iterator.next();
      e.update();

      if (!e.isActive()) {
        iterator.remove();
        continue;
      }

      for (final Explosion.Particle p : e.getParticles()) {
        if (!p.active) {
          continue;
        }

        int baseSize;

        if (e.getSize() == 4) {
          baseSize = explosionRandom.nextInt(5) + 2;
        } else {
          baseSize = explosionRandom.nextInt(6) + 18;
        }

        final int flickerAlpha =
                Math.max(0, Math.min(255, p.color.getAlpha() - explosionRandom.nextInt(50)));

        final float[] dist = {0.0f, 0.3f, 0.7f, 1.0f};
        Color[] colors;
        if (e.isEnemy()) {
          colors =
                  new Color[] {
                          new Color(255, 255, 250, flickerAlpha),
                          new Color(255, 250, 180, flickerAlpha),
                          new Color(255, 200, 220, flickerAlpha / 2),
                          new Color(0, 0, 0, 0)
                  };
        } else {
          colors =
                  new Color[] {
                          new Color(255, 255, 180, flickerAlpha),
                          new Color(255, 200, 0, flickerAlpha),
                          new Color(255, 80, 0, flickerAlpha / 2),
                          new Color(0, 0, 0, 0)
                  };
        }

        final RadialGradientPaint paint =
                new RadialGradientPaint(new Point((int) p.x, (int) p.y), baseSize, dist, colors);

        g2d.setPaint(paint);

        final int offsetX = explosionRandom.nextInt(4) - 2;
        final int offsetY = explosionRandom.nextInt(4) - 2;

        final double halfSize = baseSize / 2.0;

        g2d.fillOval(
                (int) (p.x - halfSize + offsetX), (int) (p.y - halfSize + offsetY), baseSize, baseSize);
      }
    }
  }

  /** Draws the main menu stars background animation */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public void updateMenuSpace() {
    menuSpace.updateStars();

    final Graphics2D g2d = (Graphics2D) backBufferGraphics;
    g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    backBufferGraphics.setColor(Color.WHITE);
    final int[][] positions = menuSpace.getStarLocations();

    for (int i = 0; i < menuSpace.getNumStars(); i++) {

      final int size = 1;
      final int radius = size * 2;

      final float[] dist = {0.0f, 1.0f};
      final Color[] colors = {menuSpace.getColor(), new Color(255, 255, 200, 0)};

      final RadialGradientPaint paint =
              new RadialGradientPaint(
                      new Point(positions[i][0], positions[i][1]), radius, dist, colors);
      g2d.setPaint(paint);
      g2d.fillOval(positions[i][0] - radius / 2, positions[i][1] - radius / 2, radius, radius);

      backBufferGraphics.fillOval(positions[i][0], positions[i][1], size, size);
    }
  }

  public void setLastLife(final boolean status) {
    basicGameSpace.setLastLife(status);
  }

  public void setDeath(final boolean status) {
    if (status) {
      explosion_size = 20;
    } else {
      explosion_size = 2;
    }
  }

  /** Draws the stars background animation during the game */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public void updateGameSpace() {
    basicGameSpace.update();

    final Graphics2D g2d = (Graphics2D) backBufferGraphics;
    g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    backBufferGraphics.setColor(Color.WHITE);
    final int[][] positions = basicGameSpace.getStarLocations();
    for (int i = 0; i < basicGameSpace.getNumStars(); i++) {

      final int size = positions[i][2] < 2 ? 2 : 1;
      final int radius = size * 2;

      final float[] dist = {0.0f, 1.0f};
      Color[] colors = new Color[2];
      if (basicGameSpace.isLastLife()) {
        colors[0] = new Color(255, 0, 0, 100);
        colors[1] = new Color(255, 0, 0, 50);
      } else {
        colors[0] = new Color(255, 255, 200, 50);
        colors[1] = new Color(255, 255, 200, 50);
      }

      final RadialGradientPaint paint =
              new RadialGradientPaint(
                      new Point(positions[i][0] + size / 2, positions[i][1] + size / 2),
                      radius,
                      dist,
                      colors);
      g2d.setPaint(paint);
      g2d.fillOval(positions[i][0] - radius / 2, positions[i][1] - radius / 2, radius, radius);

      backBufferGraphics.fillOval(positions[i][0], positions[i][1], size, size);
    }
  }

  /**
   * For debugging purposes, draws the canvas borders.
   *
   * @param screen Screen to draw in.
   */
  @SuppressWarnings("unused")
  private void drawBorders(final Screen screen) {
    backBufferGraphics.setColor(Color.GREEN);
    backBufferGraphics.drawLine(0, 0, screen.getWidth() - 1, 0);
    backBufferGraphics.drawLine(0, 0, 0, screen.getHeight() - 1);
    backBufferGraphics.drawLine(
            screen.getWidth() - 1, 0, screen.getWidth() - 1, screen.getHeight() - 1);
    backBufferGraphics.drawLine(
            0, screen.getHeight() - 1, screen.getWidth() - 1, screen.getHeight() - 1);
  }

  /**
   * For debugging purposes, draws a grid over the canvas.
   *
   * @param screen Screen to draw in.
   */
  @SuppressWarnings("unused")
  private void drawGrid(final Screen screen) {
    backBufferGraphics.setColor(Color.DARK_GRAY);
    for (int i = 0; i < screen.getHeight() - 1; i += 2) {
      backBufferGraphics.drawLine(0, i, screen.getWidth() - 1, i);
    }
    for (int j = 0; j < screen.getWidth() - 1; j += 2) {
      backBufferGraphics.drawLine(j, 0, j, screen.getHeight() - 1);
    }
  }

  /**
   * Draws a centered string on regular font.
   *
   * @param screen Screen to draw on.
   * @param string String to draw.
   * @param height Height of the drawing.
   */
  public void drawCenteredRegularString(
          final Screen screen, final String string, final int height) {
    backBufferGraphics.setFont(fontRegular);
    backBufferGraphics.drawString(
            string, screen.getWidth() / 2 - fontRegularMetrics.stringWidth(string) / 2, height);
  }

  /**
   * Draws a centered string on regular font at a specific coordinate.
   *
   * @param string String to draw.
   * @param x X coordinate to center the string on.
   * @param y Y coordinate of the drawing.
   */
  public void drawCenteredRegularString(final String string, final int x, final int y) {
    backBufferGraphics.setFont(fontRegular);
    backBufferGraphics.drawString(string, x - fontRegularMetrics.stringWidth(string) / 2, y);
  }

  /**
   * Draws a centered string on big font.
   *
   * @param screen Screen to draw on.
   * @param string String to draw.
   * @param height Height of the drawing.
   */
  public void drawCenteredBigString(final Screen screen, final String string, final int height) {
    backBufferGraphics.setFont(fontBig);
    backBufferGraphics.drawString(
            string, screen.getWidth() / 2 - fontBigMetrics.stringWidth(string) / 2, height);
  }

  /**
   * Countdown message helper (used by HUD/menu drawers).
   *
   * @param level Game difficulty level.
   * @param number Countdown number.
   * @param bonusLife Checks if a bonus life is received.
   */
  public static String getCountdownMessage(
          final int level, final int number, final boolean bonusLife) {
    if (number < 4) {
      return null;
    }
    String levelString;
    String message = null;

    if (level > GameState.FINITE_LEVEL) {
      levelString = "Infinity Stage";
    } else {
      levelString = "Stage " + level;
    }

    if (level <= GameState.FINITE_LEVEL + 1) {
      message = levelString;
      if (bonusLife) {
        message += " - Bonus Life";
      }
    } else {
      if (bonusLife) {
        message = "Bonus Life";
      }
    }
    return message;
  }

  // --- Delegation accessors for new drawer classes ---

  public HudDrawer hud() {
    return hudDrawer;
  }

  public MenuDrawer menu() {
    return menuDrawer;
  }

  public SettingsDrawer settings() {
    return settingsDrawer;
  }

  // --- Low-level accessors used by drawers ---

  Graphics2D getBackBufferGraphics() {
    return (Graphics2D) backBufferGraphics;
  }

  Font getFontRegular() {
    return fontRegular;
  }

  FontMetrics getFontRegularMetrics() {
    return fontRegularMetrics;
  }

  Font getFontBig() {
    return fontBig;
  }

  FontMetrics getFontBigMetrics() {
    return fontBigMetrics;
  }

  Frame getFrame() {
    return frame;
  }
}
