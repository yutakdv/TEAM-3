package engine;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import screen.Screen;
@SuppressWarnings({"PMD.LawOfDemeter"})
public final class SettingsDrawer { // NOPMD

    private final DrawManager drawManager;

    SettingsDrawer(final DrawManager drawManager) {
        this.drawManager = drawManager;
    }

    private Graphics2D g2d() {
        return (Graphics2D) drawManager.getBackBufferGraphics();
    }

    private Font fontRegular() {
        return drawManager.getFontRegular();
    }

    private java.awt.Frame frame() {
        return drawManager.getFrame();
    }

    // ================ SETTINGS TITLE =================

    public void drawSettingMenu(final Screen screen) {
        final String settingsString = "Settings";
        final String instructionsString = "Press ESC to return, Space to select";
        final String instructionsString2 = "Backspace to unselect";

        g2d().setColor(Color.GREEN);
        drawManager.drawCenteredBigString(screen, settingsString, screen.getHeight() / 8);
        g2d().setFont(fontRegular());
        g2d().setColor(Color.GRAY);
        drawManager.drawCenteredRegularString(screen, instructionsString, screen.getHeight() / 6);
        drawManager.drawCenteredRegularString(
                screen, instructionsString2, screen.getHeight() / 4 - 18);
    }

    // ================ KEY SETTINGS =================

    public void drawKeysettings(
            final Screen screen,
            final int playerNum,
            final int selectedSection,
            final int selectedKeyIndex,
            final boolean[] keySelected,
            final int[] currentKeys) {//NOPMD - do not need varargs

        final int panelWidth = 220;
        final int x = screen.getWidth() - panelWidth - 50;
        final int y = screen.getHeight() / 4;

        final String[] labels = {"MOVE LEFT :", "MOVE RIGHT:", "ATTACK :"};
        final String[] keys = new String[3];

        for (int i = 0; i < labels.length; i++) {
            final int textY = y + 70 + (i * 50);
            keys[i] = KeyEvent.getKeyText(currentKeys[i]);

            if (i < labels.length - 1) {
                g2d().setColor(Color.DARK_GRAY);
                g2d().drawLine(x + 20, textY + 20, x + panelWidth - 20, textY + 20);
            }

            if (keySelected[i]) {
                g2d().setColor(Color.YELLOW);
            } else if (selectedSection == 1 && selectedKeyIndex == i) {
                g2d().setColor(Color.GREEN);
            } else {
                g2d().setColor(Color.LIGHT_GRAY);
            }
            g2d().drawString(labels[i], x + 30, textY);
            g2d().setColor(Color.WHITE);
            g2d().drawString(keys[i], x + 150, textY);
        }
    }

    // ================ VOLUME BAR (SETTINGS) =================


    public void drawVolumeBar(
            final Screen screen,
            final int volumelevel,
            final boolean dragging,
            final int index,
            final String title,
            final int selectedSection,
            final int volumetype) {

        final int space = 70;
        final int baseY = screen.getHeight() * 3 / 10;
        final int presentY = baseY + (index * space);

        final int barstartWidth = screen.getWidth() / 2 - 10;
        final int barendWidth = screen.getWidth() - 50;

        final int iconSize = 16;
        final int iconBoxW = 24;
        final int iconX = barstartWidth - iconBoxW - 25;
        final int iconY = presentY - iconSize / 2;

        final boolean mutedVisual = volumelevel == 0 || SoundControl.isMuted(index);
        drawSpeakerIcon(iconX, iconY, iconSize, mutedVisual);

        g2d().setColor(Color.WHITE);
        g2d().drawLine(barstartWidth, presentY, barendWidth, presentY);

        if (selectedSection == 1 && index == volumetype) {
            g2d().setColor(Color.GREEN);
        } else {
            g2d().setColor(Color.WHITE);
        }
        g2d().setFont(fontRegular());
        g2d().drawString(title, barstartWidth - 60, presentY - 20);

        final int size = 14;
        final double ratio = volumelevel / 100.0;
        final int centerX = barstartWidth + (int) ((barendWidth - barstartWidth) * ratio);
        final int indicatorX = centerX - size / 2 - 3;
        final int indicatorY = presentY - size / 2;

        final InputManager inputManager = Core.getInputManager();
        final int rawX = inputManager.getMouseX();
        final int rawY = inputManager.getMouseY();
        final Insets insets = frame().getInsets();
        final int mouseX = rawX - insets.left;
        final int mouseY = rawY - insets.top;

        final boolean hoverIndicator =
                mouseX >= indicatorX
                        && mouseX <= indicatorX + size
                        && mouseY >= indicatorY
                        && mouseY <= indicatorY + size;

        if (hoverIndicator || dragging) {
            g2d().setColor(Color.GREEN);
        } else {
            g2d().setColor(Color.WHITE);
        }

        g2d().fillRect(indicatorX, indicatorY, size, size);

        g2d().setColor(Color.WHITE);
        final String volumeText = Integer.toString(volumelevel);
        g2d().drawString(volumeText, barendWidth + 10, presentY + 7);
    }

    public Rectangle getVolumeBarHitbox(final Screen screen) {
        return getVolumeBarHitbox(screen, 0);
    }

    public Rectangle getVolumeBarHitbox(final Screen screen, final int index) {
        final int space = 70;
        final int baseY = screen.getHeight() * 3 / 10 + 30;
        final int presentY = baseY + (index * space);

        final int barstartWidth = screen.getWidth() / 2 - 10;
        final int barendWidth = screen.getWidth() - 50;

        final int barThickness = 20;

        final int x = barstartWidth;
        final int y = presentY - barThickness / 2;
        final int width = barendWidth - barstartWidth;
        final int height = barThickness;

        return new Rectangle(x, y, width, height);
    }

    // ================ SETTINGS MENU LAYOUT =================

    public void drawSettingLayout(
            final Screen screen, final String[] menuItems, final int selectedmenuItems) {
        final int splitPointX = screen.getWidth() * 3 / 10;
        g2d().setFont(fontRegular());
        final int menuY = screen.getHeight() * 3 / 10;
        for (int i = 0; i < menuItems.length; i++) {
            if (i == selectedmenuItems) {
                g2d().setColor(Color.GREEN);
            } else {
                g2d().setColor(Color.WHITE);
            }
            g2d().drawString(menuItems[i], 30, menuY + (i * 60));
            g2d().setColor(Color.GREEN);
        }
        g2d().drawLine(
                splitPointX, screen.getHeight() / 4, splitPointX, menuY + menuItems.length * 60);
    }

    public Rectangle getSettingMenuHitbox(final Screen screen, final int index) {
        final int screenWidth = screen.getWidth();
        final int screenHeight = screen.getHeight();

        final int baseY = screenHeight / 3;
        final int itemGap = 60;

        final int x = screenWidth / 2 - 290;
        final int y = baseY + (index * itemGap) - 10;
        final int width = 200;
        final int height = 27;

        return new Rectangle(x, y, width, height);
    }

    // ================ SPEAKER ICON & HITBOX =================


    private void drawSpeakerIcon(
            final int x, final int y, final int size, final boolean muted) {
        final Graphics2D g2d = g2d();
        g2d.setRenderingHint(
                java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        final int squareW = size;
        final int squareH = size;
        final int squareX = x + 10;
        final int squareY = y;
        g2d.setColor(Color.WHITE);
        g2d.fillRect(squareX, squareY, squareW, squareH);

        final int x1 = x + squareW;
        final int x2 = x1 + 10;
        final int y1 = squareY + squareH;
        final int y2 = y1 + 5;
        final int y4 = squareY;
        final int y3 = y4 - 5;

        final int[] xpoints = {x1, x2, x2, x1};
        final int[] ypoints = {y1, y2, y3, y4};
        final Polygon trapezoid = new Polygon(xpoints, ypoints, 4);
        g2d.setColor(Color.WHITE);
        g2d.fillPolygon(trapezoid);

        g2d.setStroke(new BasicStroke(2));
        if (muted) {
            g2d.setColor(Color.RED);
            g2d.drawLine(x + 10, y, x + size + 10, y + size);
            g2d.drawLine(x + 10, y + size, x + size + 10, y);
        } else {
            g2d.setColor(Color.WHITE);

            final int cx = x2 + 6;
            final int cy = y + size / 2;

            final int r1 = (int) (size * 0.28);
            final int r3 = (int) (size * 0.56);

            g2d.drawArc(cx - r1, cy - r1, 2 * r1, 2 * r1, -60, 120);
            g2d.drawArc(cx - r3, cy - r3, 2 * r3, 2 * r3, -60, 120);
        }
    }

    public Rectangle getSpeakerHitbox(final Screen screen, final int index) {
        final int space = 70;
        final int baseY = screen.getHeight() * 3 / 10;
        final int presentY = baseY + (index * space);

        final int barstartWidth = screen.getWidth() / 2 - 10;
        final int iconBoxW = 24;
        final int iconX = barstartWidth - iconBoxW - 25;
        final int iconY = presentY - 16 / 2;

        final int iconSize = 16;

        return new Rectangle(iconX + 10, iconY + 30, iconSize + 10, iconSize);
    }

    // ================ PAUSE MENU VOLUME =================


    public void drawpauseVolumeBar(
            final Screen screen,
            final int ingamevolumelevel,
            final boolean dragging,
            final int index,
            final String title,
            final int selectedSection,
            final int ingamevolumetype) {

        final int space = 100;
        final int baseY = screen.getHeight() * 3 / 10 + 50;
        final int presentY = baseY + (index * space);

        final int barstartWidth = screen.getWidth() / 2 - 85;
        final int barendWidth = screen.getWidth() - 125;

        final int iconSize = 16;
        final int iconBoxW = 24;
        final int iconX = barstartWidth - iconBoxW - 25;
        final int iconY = presentY - iconSize / 2;

        final boolean mutedVisual = ingamevolumelevel == 0 || SoundControl.isIngameMuted(index);
        drawSpeakerIcon(iconX, iconY, iconSize, mutedVisual);

        g2d().setColor(Color.WHITE);
        g2d().drawLine(barstartWidth, presentY, barendWidth, presentY);

        if (selectedSection == 1 && index == ingamevolumetype) {
            g2d().setColor(Color.GREEN);
        } else {
            g2d().setColor(Color.WHITE);
        }
        g2d().setFont(fontRegular());
        drawManager.drawCenteredRegularString(screen, title, presentY - 30);

        final int size = 14;
        final double ratio = ingamevolumelevel / 100.0;
        final int centerX = barstartWidth + (int) ((barendWidth - barstartWidth) * ratio);
        final int indicatorX = centerX - size / 2 - 3;
        final int indicatorY = presentY - size / 2;

        final InputManager input = Core.getInputManager();
        final int rawX = input.getMouseX();
        final int rawY = input.getMouseY();
        final Insets insets = frame().getInsets();
        final int mouseX = rawX - insets.left;
        final int mouseY = rawY - insets.top;

        final boolean hoverIndicator =
                mouseX >= indicatorX
                        && mouseX <= indicatorX + size
                        && mouseY >= indicatorY
                        && mouseY <= indicatorY + size;

        if (hoverIndicator || dragging) {
            g2d().setColor(Color.GREEN);
        } else {
            g2d().setColor(Color.WHITE);
        }

        g2d().fillRect(indicatorX, indicatorY, size, size);

        g2d().setColor(Color.WHITE);
        final String volumeText = Integer.toString(ingamevolumelevel);
        g2d().drawString(volumeText, barendWidth + 10, presentY + 7);
    }

    public Rectangle getpauseVolumeBarHitbox(final Screen screen, final int index) {
        final int space = 100;
        final int baseY = screen.getHeight() * 3 / 10 + 80;
        final int presentY = baseY + (index * space);

        final int barstartWidth = screen.getWidth() / 2 - 85;
        final int barendWidth = screen.getWidth() - 125;

        final int barThickness = 20;

        final int x = barstartWidth;
        final int y = presentY - barThickness / 2;
        final int width = barendWidth - barstartWidth;
        final int height = barThickness;

        return new Rectangle(x, y, width, height);
    }

    public Rectangle getPauseSpeakerHitbox(final Screen screen, final int index) {
        final int space = 100;
        final int baseY = screen.getHeight() * 3 / 10 + 80;
        final int presentY = baseY + (index * space);

        final int barstartWidth = screen.getWidth() / 2 - 85;

        final int iconSize = 16;
        final int iconBoxW = 24;
        final int iconX = barstartWidth - iconBoxW - 15;
        final int iconY = presentY - iconSize / 2;

        return new Rectangle(iconX, iconY, iconBoxW, iconSize);
    }
}
