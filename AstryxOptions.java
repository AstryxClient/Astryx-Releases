package com.astryx.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiLanguage;
import net.minecraft.client.gui.GuiScreenResourcePacks;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AstryxOptions extends AstryxGuiScreen {

    private static final float UI_SCALE = 1.30F;

    private final GuiScreen parent;

    private MainMenu.CustomFont regularFont;
    private MainMenu.CustomFont boldFont;

    private final List<OptionButton> buttons = new ArrayList<OptionButton>();
    private final List<StarParticle> particles = new ArrayList<StarParticle>();

    private float layoutScale;
    private float titleScale;
    private float buttonTextScale;
    private float titleY;

    private long lastFrame;
    private float animation;

    private int panelLeft;
    private int panelRight;
    private int panelTop;
    private int panelBottom;

    private float galaxyRotation;

    public AstryxOptions(GuiScreen parent) {
        this.parent = parent;
    }

    // ------------------------------------------------------------
    // INITIALISATION
    // ------------------------------------------------------------

    @Override
    public void initGui() {
        lastFrame = System.currentTimeMillis();

        if (boldFont == null) {
            boldFont = new MainMenu.CustomFont(true);
            regularFont = new MainMenu.CustomFont(false);
        }

        createParticles();
        createLayout();
        prepareGuiRendering();
    }

    // ------------------------------------------------------------
    // LAYOUT
    // ------------------------------------------------------------

    private void createLayout() {
        buttons.clear();

        panelLeft = 0;
        panelTop = 0;
        panelRight = width;
        panelBottom = height;

        // AstryxGuiScreen supplies a fixed viewport equivalent to GUI scale 2.
        layoutScale = UI_SCALE;

        titleScale = 0.39F * layoutScale;
        buttonTextScale = 0.30F * layoutScale;

        titleY = height * 0.075F;

        // --------------------------------------------------------
        // BUTTON WIDTHS
        // --------------------------------------------------------

        int availableWidth = Math.max(1, width - Math.round(32 * layoutScale));

        int fullWidth = Math.min(
                availableWidth,
                Math.round(560 * layoutScale)
        );

        fullWidth = Math.max(1, fullWidth);

        int horizontalGap = Math.max(
                1,
                Math.round(18 * layoutScale)
        );

        horizontalGap = Math.min(
                horizontalGap,
                Math.max(0, fullWidth - 2)
        );

        int buttonWidth = Math.max(
                1,
                (fullWidth - horizontalGap) / 2
        );

        fullWidth = buttonWidth * 2 + horizontalGap;

        int leftX = (width - fullWidth) / 2;
        int rightX = leftX + buttonWidth + horizontalGap;

        // --------------------------------------------------------
        // BUTTON HEIGHTS AND VERTICAL SPACING
        // --------------------------------------------------------

        final int rows = 4;

        int desiredButtonHeight = Math.max(
                1,
                Math.round(52 * layoutScale)
        );

        int desiredVerticalGap = Math.max(
                1,
                Math.round(18 * layoutScale)
        );

        int minimumContentTop = Math.max(
                Math.round(height * 0.27F),
                Math.round(titleY + 65 * layoutScale)
        );

        int bottomPadding = Math.max(
                1,
                Math.round(30 * layoutScale)
        );

        int availableHeight = Math.max(
                1,
                height - minimumContentTop - bottomPadding
        );

        int verticalGap = desiredVerticalGap;
        int buttonHeight = desiredButtonHeight;

        int requiredHeight = rows * buttonHeight
                + (rows - 1) * verticalGap;

        /*
         * Shrink the gaps first. Only reduce button height when
         * there genuinely isn't enough room for the full layout.
         */
        if (requiredHeight > availableHeight) {
            int minimumButtonHeight = Math.max(
                    1,
                    Math.round(14 * layoutScale)
            );

            verticalGap = Math.min(
                    desiredVerticalGap,
                    Math.max(
                            0,
                            (availableHeight - rows * minimumButtonHeight)
                                    / (rows - 1)
                    )
            );

            buttonHeight = Math.min(
                    desiredButtonHeight,
                    Math.max(
                            1,
                            (availableHeight - (rows - 1) * verticalGap) / rows
                    )
            );
        }

        // Final safety check for unusually small windows.
        if (rows * buttonHeight + (rows - 1) * verticalGap > availableHeight) {
            buttonHeight = Math.max(
                    1,
                    (availableHeight - (rows - 1) * verticalGap) / rows
            );
        }

        int totalHeight = rows * buttonHeight
                + (rows - 1) * verticalGap;

        int contentTop = minimumContentTop
                + Math.max(0, (availableHeight - totalHeight) / 2);

        // --------------------------------------------------------
        // BUTTON CREATION
        // --------------------------------------------------------

        String[] labels = {
                "Video Settings",
                "Music & Sounds",
                "Language",
                "Controls",
                "Resource Packs",
                "Back"
        };

        // First row.
        buttons.add(new OptionButton(
                leftX,
                contentTop,
                buttonWidth,
                buttonHeight,
                labels[0],
                0
        ));

        buttons.add(new OptionButton(
                rightX,
                contentTop,
                buttonWidth,
                buttonHeight,
                labels[1],
                1
        ));

        // Second row.
        int secondRowY = contentTop + buttonHeight + verticalGap;

        buttons.add(new OptionButton(
                leftX,
                secondRowY,
                buttonWidth,
                buttonHeight,
                labels[2],
                2
        ));

        buttons.add(new OptionButton(
                rightX,
                secondRowY,
                buttonWidth,
                buttonHeight,
                labels[3],
                3
        ));

        // Third row.
        int resourcePacksY = secondRowY + buttonHeight + verticalGap;

        buttons.add(new OptionButton(
                leftX,
                resourcePacksY,
                fullWidth,
                buttonHeight,
                labels[4],
                4
        ));

        // Fourth row.
        int backY = resourcePacksY + buttonHeight + verticalGap;

        buttons.add(new OptionButton(
                leftX,
                backY,
                fullWidth,
                buttonHeight,
                labels[5],
                5
        ));
    }

    // ------------------------------------------------------------
    // PARTICLE GENERATION
    // ------------------------------------------------------------

    private void createParticles() {
        particles.clear();

        Random random = new Random(481927L);

        int particleCount = Math.max(
                120,
                Math.min(260, (width * height) / 4200)
        );

        for (int i = 0; i < particleCount; i++) {
            float x = random.nextFloat() * width;
            float y = random.nextFloat() * height;

            float size;

            if (random.nextFloat() < 0.12F) {
                size = 2.0F + random.nextFloat() * 1.2F;
            } else {
                size = 0.6F + random.nextFloat() * 1.1F;
            }

            float speed = 0.08F + random.nextFloat() * 0.32F;
            float twinkleSpeed = 0.5F + random.nextFloat() * 2.0F;
            float phase = random.nextFloat() * 6.28318F;

            int colorChoice = random.nextInt(100);

            int color;

            if (colorChoice < 45) {
                color = 0xFFFFFFFF;
            } else if (colorChoice < 70) {
                color = 0xFFB9A7FF;
            } else if (colorChoice < 88) {
                color = 0xFF91B8FF;
            } else {
                color = 0xFFFFD8FA;
            }

            particles.add(new StarParticle(
                    x,
                    y,
                    size,
                    speed,
                    twinkleSpeed,
                    phase,
                    color
            ));
        }

        float centerX = width * 0.5F;
        float centerY = height * 0.47F;

        for (int i = 0; i < 150; i++) {
            float angle = random.nextFloat() * 6.28318F;
            float distance = 25.0F + random.nextFloat()
                    * Math.min(width, height) * 0.42F;

            float spiralAngle = angle + distance * 0.012F;

            float spread = 5.0F + random.nextFloat() * 20.0F;

            float x = centerX
                    + (float) Math.cos(spiralAngle) * distance
                    + (random.nextFloat() - 0.5F) * spread;

            float y = centerY
                    + (float) Math.sin(spiralAngle) * distance * 0.55F
                    + (random.nextFloat() - 0.5F) * spread;

            float size = 0.5F + random.nextFloat() * 1.1F;

            int color = random.nextBoolean()
                    ? 0xFFB69AFF
                    : 0xFF8BAFFF;

            particles.add(new StarParticle(
                    x,
                    y,
                    size,
                    0.025F + random.nextFloat() * 0.08F,
                    0.6F + random.nextFloat(),
                    random.nextFloat() * 6.28318F,
                    color
            ));
        }
    }

    // ------------------------------------------------------------
    // RENDER STATE
    // ------------------------------------------------------------

    private void prepareGuiRendering() {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.disableCull();
        GlStateManager.disableLighting();
        GlStateManager.color(1, 1, 1, 1);

        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );
    }

    private void restoreGuiRendering() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GlStateManager.disableCull();
        GlStateManager.color(1, 1, 1, 1);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.disableLighting();
    }

    // ------------------------------------------------------------
    // ANIMATION
    // ------------------------------------------------------------

    @Override
    public void updateScreen() {
        long now = System.currentTimeMillis();

        float delta = Math.min(
                (now - lastFrame) / 16.0F,
                3.0F
        );

        lastFrame = now;

        animation += delta * 0.008F;
        galaxyRotation += delta * 0.0008F;

        for (StarParticle particle : particles) {
            particle.phase += particle.twinkleSpeed * delta * 0.025F;

            particle.y -= particle.speed * delta;
            particle.x += (float) Math.sin(particle.phase * 0.3F)
                    * particle.speed * delta * 0.25F;

            if (particle.y < -3) {
                particle.y = height + 3;
                particle.x = (float) (Math.random() * width);
            }

            if (particle.x < -3) {
                particle.x = width + 3;
            } else if (particle.x > width + 3) {
                particle.x = -3;
            }
        }

        for (OptionButton button : buttons) {
            float target = button.hovered ? 1.0F : 0.0F;

            button.hoverAmount +=
                    (target - button.hoverAmount) * 0.22F;

            if (Math.abs(target - button.hoverAmount) < 0.005F) {
                button.hoverAmount = target;
            }
        }
    }

    // ------------------------------------------------------------
    // MAIN RENDER
    // ------------------------------------------------------------

    @Override
    protected void drawAstryxScreen(int mouseX, int mouseY, float partialTicks) {
        prepareGuiRendering();

        try {
            drawGalaxyBackground();
            drawNebulae();
            drawGalaxyDust();
            drawStars();

            drawPanel();
            drawTitle();
            drawButtons(mouseX, mouseY);

            drawDefaultScreen(mouseX, mouseY, partialTicks);
        } finally {
            restoreGuiRendering();
        }
    }

    // ------------------------------------------------------------
    // GALAXY BACKGROUND
    // ------------------------------------------------------------

    private void drawGalaxyBackground() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);

        setGlColor(0xFF080713);
        GL11.glVertex2f(0, 0);

        setGlColor(0xFF100C25);
        GL11.glVertex2f(width, 0);

        setGlColor(0xFF080B1C);
        GL11.glVertex2f(width, height);

        setGlColor(0xFF090714);
        GL11.glVertex2f(0, height);

        GL11.glEnd();

        GL11.glShadeModel(GL11.GL_FLAT);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
    }

    // ------------------------------------------------------------
    // NEBULA EFFECTS
    // ------------------------------------------------------------

    private void drawNebulae() {
        float centerX = width * 0.50F;
        float centerY = height * 0.46F;

        float pulse = (float) Math.sin(animation * 0.7F) * 0.035F;

        drawRadialGlow(
                centerX - width * 0.16F,
                centerY - height * 0.04F,
                width * (0.39F + pulse),
                height * 0.34F,
                0x302F176C,
                0x002F176C
        );

        drawRadialGlow(
                centerX + width * 0.23F,
                centerY + height * 0.12F,
                width * 0.34F,
                height * 0.31F,
                0x252047A0,
                0x002047A0
        );

        drawRadialGlow(
                centerX,
                centerY,
                width * 0.20F,
                height * 0.19F,
                0x252D236F,
                0x002D236F
        );

        drawRadialGlow(
                width * 0.12F,
                height * 0.77F,
                width * 0.27F,
                height * 0.22F,
                0x1D712E79,
                0x00712E79
        );

        GlStateManager.color(1, 1, 1, 1);
    }

    private void drawRadialGlow(
            float centerX,
            float centerY,
            float radiusX,
            float radiusY,
            int centerColor,
            int outerColor
    ) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        setGlColor(centerColor);
        GL11.glVertex2f(centerX, centerY);

        setGlColor(outerColor);

        for (int angle = 0; angle <= 360; angle += 6) {
            double radians = Math.toRadians(angle);

            GL11.glVertex2f(
                    centerX + (float) Math.cos(radians) * radiusX,
                    centerY + (float) Math.sin(radians) * radiusY
            );
        }

        GL11.glEnd();

        GL11.glShadeModel(GL11.GL_FLAT);

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
    }

    // ------------------------------------------------------------
    // SPIRAL GALAXY DUST
    // ------------------------------------------------------------

    private void drawGalaxyDust() {
        float centerX = width * 0.5F;
        float centerY = height * 0.47F;

        float rotation = galaxyRotation;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glPointSize(2.0F);
        GL11.glBegin(GL11.GL_POINTS);

        for (int arm = 0; arm < 2; arm++) {
            for (int i = 0; i < 180; i++) {
                float distance = 12 + i * Math.min(width, height) / 410.0F;

                float angle = rotation
                        + arm * 3.14159F
                        + distance * 0.021F;

                float x = centerX
                        + (float) Math.cos(angle) * distance;

                float y = centerY
                        + (float) Math.sin(angle) * distance * 0.48F;

                float alpha = 0.10F + 0.12F
                        * (float) Math.sin(i * 0.13F + animation);

                GlStateManager.color(
                        0.59F,
                        0.47F,
                        1.0F,
                        Math.max(0.0F, alpha)
                );

                GL11.glVertex2f(x, y);
            }
        }

        GL11.glEnd();

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
    }

    // ------------------------------------------------------------
    // ANIMATED STARS
    // ------------------------------------------------------------

    private void drawStars() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glBegin(GL11.GL_QUADS);

        for (StarParticle particle : particles) {
            float twinkle = 0.55F
                    + 0.45F * (float) Math.sin(particle.phase);

            float alpha = Math.max(
                    0.12F,
                    Math.min(1.0F, twinkle)
            );

            float size = particle.size;

            float red = ((particle.color >> 16) & 255) / 255.0F;
            float green = ((particle.color >> 8) & 255) / 255.0F;
            float blue = (particle.color & 255) / 255.0F;

            GL11.glColor4f(red, green, blue, alpha);

            GL11.glVertex2f(particle.x - size, particle.y - size);
            GL11.glVertex2f(particle.x + size, particle.y - size);
            GL11.glVertex2f(particle.x + size, particle.y + size);
            GL11.glVertex2f(particle.x - size, particle.y + size);
        }

        GL11.glEnd();

        GL11.glBegin(GL11.GL_LINES);

        for (StarParticle particle : particles) {
            if (particle.size < 2.0F) {
                continue;
            }

            float alpha = 0.2F + 0.25F
                    * (float) Math.sin(particle.phase);

            GL11.glColor4f(
                    0.78F,
                    0.73F,
                    1.0F,
                    Math.max(0.0F, alpha)
            );

            float glint = particle.size * 2.5F;

            GL11.glVertex2f(particle.x - glint, particle.y);
            GL11.glVertex2f(particle.x + glint, particle.y);

            GL11.glVertex2f(particle.x, particle.y - glint);
            GL11.glVertex2f(particle.x, particle.y + glint);
        }

        GL11.glEnd();

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
    }

    // ------------------------------------------------------------
    // PANEL
    // ------------------------------------------------------------

    private void drawPanel() {
        drawGradientRect(
                panelLeft,
                panelTop,
                panelRight,
                panelBottom,
                0x35100E19,
                0x350A0B14
        );

        drawGradientRect(
                0,
                0,
                width,
                Math.max(1, Math.round(3 * layoutScale)),
                0xFFB18AFF,
                0xFF568DFF
        );

        drawBorder(
                panelLeft,
                panelTop,
                panelRight,
                panelBottom,
                0x557F6BD1
        );

        int dividerY = Math.round(height * 0.225F);

        drawGradientRect(
                Math.round(width * 0.08F),
                dividerY,
                width - Math.round(width * 0.08F),
                dividerY + Math.max(1, Math.round(layoutScale)),
                0x007F6BD1,
                0x997F6BD1
        );
    }

    // ------------------------------------------------------------
    // TITLE
    // ------------------------------------------------------------

    private void drawTitle() {
        String title = "OPTIONS";

        float centerX = width / 2.0F;

        float textWidth = boldFont.getStringWidthFloat(
                title,
                titleScale
        );

        float cursorX = centerX - textWidth / 2.0F;

        for (int i = 0; i < title.length(); i++) {
            float progress = (float) i /
                    Math.max(1, title.length() - 1);

            int color = interpolateColor(
                    0xFFB18AFF,
                    0xFF719CFF,
                    progress
            );

            String character = String.valueOf(title.charAt(i));

            boldFont.drawString(
                    character,
                    cursorX,
                    titleY,
                    titleScale,
                    color
            );

            cursorX += boldFont.getStringWidthFloat(
                    character,
                    titleScale
            );
        }

        float subtitleScale = buttonTextScale * 0.55F;

        regularFont.drawCenteredString(
                "Customise your Astryx experience",
                centerX,
                titleY + Math.round(35 * layoutScale),
                subtitleScale,
                0xFFAAA5BC
        );
    }

    // ------------------------------------------------------------
    // BUTTONS
    // ------------------------------------------------------------

    private void drawButtons(int mouseX, int mouseY) {
        for (OptionButton button : buttons) {
            button.hovered = button.contains(mouseX, mouseY);
            drawButton(button);
        }
    }

    private void drawButton(OptionButton button) {
        float hover = Math.max(0.0F, Math.min(1.0F, button.hoverAmount));

        float expansion = 1.0F + hover * 0.025F;

        float visualWidth = button.width * expansion;
        float visualHeight = button.height * expansion;

        float centerX = button.x + button.width / 2.0F;
        float centerY = button.y + button.height / 2.0F;

        float left = centerX - visualWidth / 2.0F;
        float right = centerX + visualWidth / 2.0F;

        float top = centerY - visualHeight / 2.0F;
        float bottom = centerY + visualHeight / 2.0F;

        float cornerRadius = Math.min(
                visualHeight / 2.0F,
                9.0F * layoutScale
        );

        boolean primary = button.id == 5;

        // --------------------------------------------------------
        // PRIMARY BUTTON: BACK
        // --------------------------------------------------------

        if (primary) {
            int glowAlpha = (int) (12 + hover * 29);

            if (hover > 0.01F) {
                drawGlow(
                        centerX,
                        centerY,
                        visualWidth * (0.72F + hover * 0.12F),
                        (glowAlpha << 24) | 0x916BFF
                );
            }

            int outerColor = interpolateColor(
                    0xFF9B82E8,
                    0xFFC1ACFF,
                    hover
            );

            int innerLeft = interpolateColor(
                    0xFF493477,
                    0xFF6849A8,
                    hover
            );

            int innerRight = interpolateColor(
                    0xFF263E78,
                    0xFF3769B9,
                    hover
            );

            drawRoundedRect(
                    left,
                    top,
                    right,
                    bottom,
                    cornerRadius,
                    outerColor
            );

            drawRoundedRect(
                    left + 1.0F * layoutScale,
                    top + 1.0F * layoutScale,
                    right - 1.0F * layoutScale,
                    bottom - 1.0F * layoutScale,
                    Math.max(0.0F, cornerRadius - layoutScale),
                    0xFF211A36
            );

            drawGradientRect(
                    left + 2.0F * layoutScale,
                    top + 2.0F * layoutScale,
                    right - 2.0F * layoutScale,
                    bottom - 2.0F * layoutScale,
                    innerLeft,
                    innerRight
            );

            drawGradientRect(
                    left + cornerRadius * 0.65F,
                    top + 2.0F * layoutScale,
                    right - cornerRadius * 0.65F,
                    top + Math.max(1.0F, 4.0F * layoutScale),
                    interpolateColor(0x55FFFFFF, 0x99FFFFFF, hover),
                    interpolateColor(0x22FFFFFF, 0x66FFFFFF, hover)
            );

            drawGradientRect(
                    left + cornerRadius,
                    bottom - 2.0F * layoutScale,
                    right - cornerRadius,
                    bottom - 1.0F * layoutScale,
                    0x557E75D8,
                    0x6677A9FF
            );

            drawFittedButtonText(
                    button,
                    centerX,
                    centerY,
                    0xFFFFFFFF,
                    true
            );

            return;
        }

        // --------------------------------------------------------
        // STANDARD OPTION BUTTONS
        // --------------------------------------------------------

        if (hover > 0.01F) {
            int glowAlpha = (int) (8 + hover * 24);

            drawGlow(
                    centerX,
                    centerY,
                    visualWidth * (0.62F + hover * 0.08F),
                    (glowAlpha << 24) | 0x7653D8
            );
        }

        int borderColor = interpolateColor(
                0xFF7658B9,
                0xFFAB91F5,
                hover
        );

        int innerColor = interpolateColor(
                0xFF100F18,
                0xFF211A32,
                hover
        );

        drawRoundedRect(
                left,
                top,
                right,
                bottom,
                cornerRadius,
                borderColor
        );

        drawRoundedRect(
                left + 1.0F * layoutScale,
                top + 1.0F * layoutScale,
                right - 1.0F * layoutScale,
                bottom - 1.0F * layoutScale,
                Math.max(0.0F, cornerRadius - layoutScale),
                innerColor
        );

        drawGradientRect(
                left + cornerRadius * 0.55F,
                top + 2.0F * layoutScale,
                right - cornerRadius * 0.55F,
                top + Math.max(1.0F, 5.0F * layoutScale),
                interpolateColor(0x337F6BD1, 0x887F78E8, hover),
                interpolateColor(0x116B8DDA, 0x557A9FFF, hover)
        );

        float accentWidth = Math.max(
                1.0F,
                3.0F * layoutScale
        );

        float accentLeft = left + 3.0F * layoutScale;
        float accentRight = accentLeft + accentWidth;

        float accentTop = top + cornerRadius * 0.62F;
        float accentBottom = bottom - cornerRadius * 0.62F;

        int accentColor = interpolateColor(
                0xFF7052B5,
                0xFFB19AFF,
                hover
        );

        drawRoundedRect(
                accentLeft,
                accentTop,
                accentRight,
                accentBottom,
                accentWidth / 2.0F,
                accentColor
        );

        float dotRadius = Math.max(
                1.0F,
                2.4F * layoutScale
        );

        float dotX = right - cornerRadius * 0.72F;
        float dotY = centerY;

        int dotColor = interpolateColor(
                0xFF7663B7,
                0xFFB8A5FF,
                hover
        );

        drawGlow(
                dotX,
                dotY,
                dotRadius * (2.0F + hover),
                ((int) (12 + hover * 34) << 24) | (dotColor & 0x00FFFFFF)
        );

        drawRoundedRect(
                dotX - dotRadius,
                dotY - dotRadius,
                dotX + dotRadius,
                dotY + dotRadius,
                dotRadius,
                dotColor
        );

        int textColor = interpolateColor(
                0xFFE5E0F0,
                0xFFFFFFFF,
                hover
        );

        drawFittedButtonText(
                button,
                centerX,
                centerY,
                textColor,
                false
        );
    }

    // ------------------------------------------------------------
    // BUTTON TEXT FITTING
    // ------------------------------------------------------------

    private void drawFittedButtonText(
            OptionButton button,
            float centerX,
            float centerY,
            int textColor,
            boolean primary
    ) {
        float textScale = buttonTextScale;

        float sidePadding = primary
                ? 24.0F * layoutScale
                : 34.0F * layoutScale;

        float maxTextWidth = Math.max(
                1.0F,
                button.width - sidePadding
        );

        float measuredWidth = boldFont.getStringWidthFloat(
                button.label,
                textScale
        );

        if (measuredWidth > maxTextWidth && measuredWidth > 0.0F) {
            textScale *= maxTextWidth / measuredWidth;
        }

        float maxTextHeight = button.height * 0.72F;
        float measuredHeight = boldFont.getHeight(textScale);

        if (measuredHeight > maxTextHeight && measuredHeight > 0.0F) {
            textScale *= maxTextHeight / measuredHeight;
        }

        boldFont.drawCenteredString(
                button.label,
                centerX,
                centerY - boldFont.getHeight(textScale) / 2.0F,
                textScale,
                textColor
        );
    }

    // ------------------------------------------------------------
    // ROUNDED RECTANGLE
    // ------------------------------------------------------------

    private void drawRoundedRect(
            float left,
            float top,
            float right,
            float bottom,
            float radius,
            int color
    ) {
        radius = Math.max(
                0,
                Math.min(
                        radius,
                        Math.min(
                                (right - left) / 2.0F,
                                (bottom - top) / 2.0F
                        )
                )
        );

        if (radius <= 0.0F) {
            Gui.drawRect(
                    (int) left,
                    (int) top,
                    (int) right,
                    (int) bottom,
                    color
            );
            return;
        }

        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GlStateManager.color(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_POLYGON);

        drawCornerVertices(right - radius, top + radius, radius, 270);
        drawCornerVertices(right - radius, bottom - radius, radius, 0);
        drawCornerVertices(left + radius, bottom - radius, radius, 90);
        drawCornerVertices(left + radius, top + radius, radius, 180);

        GL11.glEnd();

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawCornerVertices(
            float centerX,
            float centerY,
            float radius,
            int startAngle
    ) {
        for (int angle = 0; angle <= 90; angle += 5) {
            double radians = Math.toRadians(startAngle + angle);

            GL11.glVertex2f(
                    centerX + (float) Math.cos(radians) * radius,
                    centerY + (float) Math.sin(radians) * radius
            );
        }
    }

    // ------------------------------------------------------------
    // GRADIENT
    // ------------------------------------------------------------

    private void drawGradientRect(
            float left,
            float top,
            float right,
            float bottom,
            int leftColor,
            int rightColor
    ) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glBegin(GL11.GL_QUADS);

        setGlColor(leftColor);
        GL11.glVertex2f(left, top);
        GL11.glVertex2f(left, bottom);

        setGlColor(rightColor);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);

        GL11.glEnd();

        GL11.glShadeModel(GL11.GL_FLAT);

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    private void drawBorder(
            int left,
            int top,
            int right,
            int bottom,
            int color
    ) {
        Gui.drawRect(left, top, right, top + 1, color);
        Gui.drawRect(left, bottom - 1, right, bottom, color);
        Gui.drawRect(left, top, left + 1, bottom, color);
        Gui.drawRect(right - 1, top, right, bottom, color);
    }

    // ------------------------------------------------------------
    // GLOW
    // ------------------------------------------------------------

    private void drawGlow(
            float centerX,
            float centerY,
            float radius,
            int color
    ) {
        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        GL11.glColor4f(red, green, blue, alpha);
        GL11.glVertex2f(centerX, centerY);

        GL11.glColor4f(red, green, blue, 0);

        for (int angle = 0; angle <= 360; angle += 5) {
            double radians = Math.toRadians(angle);

            GL11.glVertex2f(
                    centerX + (float) Math.cos(radians) * radius,
                    centerY + (float) Math.sin(radians) * radius
            );
        }

        GL11.glEnd();

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    // ------------------------------------------------------------
    // COLOUR HELPERS
    // ------------------------------------------------------------

    private void setGlColor(int color) {
        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.color(red, green, blue, alpha);
    }

    private int interpolateColor(
            int first,
            int second,
            float progress
    ) {
        progress = Math.max(0, Math.min(1, progress));

        int red = (int) (
                ((first >> 16) & 255)
                        + (((second >> 16) & 255)
                        - ((first >> 16) & 255)) * progress
        );

        int green = (int) (
                ((first >> 8) & 255)
                        + (((second >> 8) & 255)
                        - ((first >> 8) & 255)) * progress
        );

        int blue = (int) (
                (first & 255)
                        + ((second & 255) - (first & 255)) * progress
        );

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    // ------------------------------------------------------------
    // INPUT
    // ------------------------------------------------------------

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton != 0) {
            return;
        }

        for (OptionButton button : buttons) {
            if (!button.contains(mouseX, mouseY)) {
                continue;
            }

            switch (button.id) {
                case 0:
                    mc.displayGuiScreen(new AstryxVideoSettings(this));
                    return;

                case 1:
                    mc.displayGuiScreen(
                            new GuiScreenOptionsSounds(this, mc.gameSettings)
                    );
                    return;

                case 2:
                    mc.displayGuiScreen(
                            new GuiLanguage(
                                    this,
                                    mc.gameSettings,
                                    mc.getLanguageManager()
                            )
                    );
                    return;

                case 3:
                    mc.displayGuiScreen(
                            new GuiControls(this, mc.gameSettings)
                    );
                    return;

                case 4:
                    mc.displayGuiScreen(new GuiScreenResourcePacks(this));
                    return;

                case 5:
                    mc.displayGuiScreen(parent);
                    return;
            }
        }
    }

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode
    ) throws IOException {
        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // ------------------------------------------------------------
    // CLEANUP
    // ------------------------------------------------------------

    @Override
    public void onGuiClosed() {
        if (boldFont != null) {
            boldFont.dispose();
            boldFont = null;
        }

        if (regularFont != null) {
            regularFont.dispose();
            regularFont = null;
        }
    }

    // ------------------------------------------------------------
    // PARTICLE DATA
    // ------------------------------------------------------------

    private static class StarParticle {

        float x;
        float y;

        final float size;
        final float speed;
        final float twinkleSpeed;

        float phase;

        final int color;

        StarParticle(
                float x,
                float y,
                float size,
                float speed,
                float twinkleSpeed,
                float phase,
                int color
        ) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
            this.twinkleSpeed = twinkleSpeed;
            this.phase = phase;
            this.color = color;
        }
    }

    // ------------------------------------------------------------
    // BUTTON DATA
    // ------------------------------------------------------------

    private static class OptionButton {

        int x;
        int y;
        int width;
        int height;

        final String label;
        final int id;

        boolean hovered;
        float hoverAmount;

        OptionButton(
                int x,
                int y,
                int width,
                int height,
                String label,
                int id
        ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
            this.id = id;
        }

        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x
                    && mouseX < x + width
                    && mouseY >= y
                    && mouseY < y + height;
        }
    }
}