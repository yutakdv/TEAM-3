package Animations; // NOPMD - PackageCase

import java.awt.*;
import java.util.Random;

public class Explosion { // NOPMD - DataClass

  private static final int NUM_PARTICLES = 40;
  private Particle[] particles;
  private boolean active;
  private final boolean enemy;
  private final int size;

  private static final Random random = new Random();

  public Explosion(final double startX, final double startY, final boolean enemy, final boolean finalExplosion) {
    this.particles = new Particle[NUM_PARTICLES];
    this.active = true;
    this.enemy = enemy;
    if (finalExplosion) {
        this.size = 20;
    } else {
        this.size = 4;
    }

    for (int i = 0; i < NUM_PARTICLES; i++) {
      final double angle = 2 * Math.PI * random.nextDouble();
      final double speed = 2 + random.nextDouble() * 2;
      final double dx = Math.cos(angle) * speed;
      final double dy = Math.sin(angle) * speed;

      final Color color = new Color(255, random.nextInt(150), 0, 255); // NOPMD - AvoidInstantiatingObjectsInLoops
      final int life = 60;

      particles[i] = new Particle(startX, startY, dx, dy, color, life); // NOPMD - AvoidInstantiatingObjectsInLoops
    }
  }

  public void update() {
    if (!active) {
        return;
    }

    boolean anyAlive = false;
    for (Particle p : particles) {
      if (!p.active) {
          continue;
      }

      // update position
      p.x += p.dx;
      p.y += p.dy;

      // gravity and damping
      p.dy += 0.1;
      p.dx *= 0.98;
      p.dy *= 0.98;

      // fade color
      int alpha = (int) (255 * ((double) p.life / 60));
      alpha = Math.max(alpha, 0);
      p.color = new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), alpha); // NOPMD - AvoidInstantiatingObjectsInLoops - LawOfDemeter

      // decrease life
      p.life--;
      if (p.life <= 0) {
          p.active = false;
      }

      if (p.active) {
          anyAlive = true;
      }
    }

    // deactivate explosion if all particles are dead
    if (!anyAlive) {
        active = false;
    }
  }

  public Particle[] getParticles() {
    return particles;
  }

  public boolean isEnemy() {
    return this.enemy;
  }

  public boolean isActive() {
    return active;
  }

  public int getSize() {
    return this.size;
  }

  public static class Particle { // NOPMD - DataClass
    public double x;
    public double y;
    public double dx;
    public double dy;
    public Color color;
    public int life;
    public boolean active;

    public Particle(final double x, final double y, final double dx, final double dy, final Color color, final int life) {
      this.x = x;
      this.y = y;
      this.dx = dx;
      this.dy = dy;
      this.color = color;
      this.life = life;
      this.active = true;
    }
  }
}
