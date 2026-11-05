package com.astryx.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AstryxVideoSettings extends AstryxGuiScreen {

    private static final int DONE_BUTTON_ID = 200;

    // Fixed Astryx interface scale.
    // This will eventually be controlled through Client Settings.
    private static final float UI_SCALE = 1.65F;

    private static final GameSettings.Options[] VIDEO_OPTIONS = {
            GameSettings.Options.GRAPHICS,
            GameSettings.Options.RENDER_DISTANCE,
            GameSettings.Options.AMBIENT_OCCLUSION,
            GameSettings.Options.FRAMERATE_LIMIT,
            GameSettings.Options.VIEW_BOBBING,
            GameSettings.Options.GUI_SCALE,
            GameSettings.Options.GAMMA,
            GameSettings.Options.PARTICLES,
            GameSettings.Options.RENDER_CLOUDS,
            GameSettings.Options.MIPMAP_LEVELS,
            GameSettings.Options.ENTITY_SHADOWS
    };

    private static final int COLUMNS = 2;

    private final GuiScreen parent;

    private GameSettings settings;

    private MainMenu.CustomFont titleFont;
    private MainMenu.CustomFont regularFont;

    private final List<GalaxyParticle> particles = new ArrayList<GalaxyParticle>();
    private final Random random = new Random();

    private AstryxSlider draggingSlider;

    private int panelLeft;
    private int panelRight;
    private int panelTop;
    private int panelBottom;

    private int buttonWidth;
    private int buttonHeight;
    private int horizontalGap;
    private int verticalGap;
    private int startX;
    private int startY;

    private float layoutScale = UI_SCALE;
    private float scaleFactor = 1.0F;

    private float animation;
    private long lastFrame;

    public AstryxVideoSettings(GuiScreen parent) {
        this.parent = parent;
    }

    // ------------------------------------------------------------
    // SCALING
    // ------------------------------------------------------------

    private void updateScale() {
        // Preserve the original dimensions at the shared reference scale.
        // AstryxGuiScreen handles the conversion for every Minecraft GUI scale.
        scaleFactor = REFERENCE_GUI_SCALE;
        layoutScale = UI_SCALE / REFERENCE_GUI_SCALE;
    }

    /**
     * Converts a reference-screen pixel measurement into virtual GUI coordinates.
     */
    private int px(float physicalPixels) {
        return Math.max(1, Math.round(physicalPixels / scaleFactor));
    }

    // ------------------------------------------------------------
    // INITIALISATION
    // ------------------------------------------------------------

    @Override
    public void initGui() {
        super.initGui();

        settings = mc.gameSettings;
        lastFrame = System.currentTimeMillis();
        draggingSlider = null;

        updateScale();

        if (titleFont == null) {
            titleFont = new MainMenu.CustomFont(true);
        }

        if (regularFont == null) {
            regularFont = new MainMenu.CustomFont(false);
        }

        createParticles();
        createLayout();
    }

    private void createParticles() {
        particles.clear();
        random.setSeed(System.nanoTime());

        float physicalWidth = width * scaleFactor;
        float physicalHeight = height * scaleFactor;

        int count = Math.max(
                120,
                Math.min(220, (int) (physicalWidth * physicalHeight / 3500))
        );

        for (int i = 0; i < count; i++) {
            particles.add(new GalaxyParticle(
                    random.nextFloat() * width,
                    random.nextFloat() * height,
                    (0.35F + random.nextFloat()) / scaleFactor,
                    0.45F + random.nextFloat() * 0.5F,
                    random.nextFloat() * 6.28318F,
                    (random.nextFloat() * 0.3F - 0.15F) / scaleFactor
            ));
        }
    }

    // ------------------------------------------------------------
    // LAYOUT
    // ------------------------------------------------------------

    private void createLayout() {
        buttonList.clear();

        updateScale();

        int panelWidth = Math.min(
                width - px(20),
                Math.round(1000 * layoutScale)
        );

        int panelHeight = Math.min(
                height - px(20),
                Math.round(650 * layoutScale)
        );

        panelWidth = Math.max(1, panelWidth);
        panelHeight = Math.max(1, panelHeight);

        panelLeft = (width - panelWidth) / 2;
        panelRight = panelLeft + panelWidth;

        panelTop = (height - panelHeight) / 2;
        panelBottom = panelTop + panelHeight;

        horizontalGap = Math.max(
                px(14),
                Math.round(22 * layoutScale)
        );

        verticalGap = Math.max(
                px(9),
                Math.round(15 * layoutScale)
        );

        int horizontalMargin = Math.max(
                px(24),
                Math.round(48 * layoutScale)
        );

        buttonWidth = (
                panelWidth - horizontalMargin * 2
                        - horizontalGap * (COLUMNS - 1)
        ) / COLUMNS;

        buttonWidth = Math.max(1, buttonWidth);

        int rows = (VIDEO_OPTIONS.length + COLUMNS - 1) / COLUMNS;

        int headerSpace = Math.round(90 * layoutScale);

        // Done button footer.
        int doneWidth = Math.max(
                px(120),
                Math.round(160 * layoutScale)
        );

        int doneHeight = Math.max(
                px(25),
                Math.round(34 * layoutScale)
        );

        int footerPadding = Math.max(
                px(12),
                Math.round(25 * layoutScale)
        );

        int doneY = panelBottom - footerPadding - doneHeight;

        // Keep the options grid safely above the Done button.
        int optionsTop = panelTop + headerSpace;
        int optionsBottom = doneY - px(8);

        int availableHeight = Math.max(1, optionsBottom - optionsTop);

        int desiredButtonHeight = Math.max(
                px(25),
                Math.round(42 * layoutScale)
        );

        int desiredVerticalGap = Math.max(
                px(4),
                Math.round(15 * layoutScale)
        );

        // Calculate the maximum gap that still allows every row to fit.
        int maximumGap = rows > 1
                ? Math.max(0, (availableHeight - rows) / (rows - 1))
                : 0;

        verticalGap = Math.min(desiredVerticalGap, maximumGap);

        // Calculate the button height from the remaining available space.
        int maximumButtonHeight = (
                availableHeight - (rows - 1) * verticalGap
        ) / rows;

        buttonHeight = Math.max(
                1,
                Math.min(desiredButtonHeight, maximumButtonHeight)
        );

        int totalHeight = rows * buttonHeight
                + (rows - 1) * verticalGap;

        int totalWidth = buttonWidth * COLUMNS
                + horizontalGap * (COLUMNS - 1);

        startX = width / 2 - totalWidth / 2;

        // Centre the options grid within its allocated area.
        startY = optionsTop + Math.max(
                0,
                (availableHeight - totalHeight) / 2
        );

        for (int i = 0; i < VIDEO_OPTIONS.length; i++) {
            GameSettings.Options option = VIDEO_OPTIONS[i];

            int column = i % COLUMNS;
            int row = i / COLUMNS;

            int x = startX + column * (buttonWidth + horizontalGap);
            int y = startY + row * (buttonHeight + verticalGap);

            if (option.getEnumFloat()) {
                buttonList.add(new AstryxSlider(
                        i, x, y, buttonWidth, buttonHeight, option
                ));
            } else {
                buttonList.add(new AstryxOptionButton(
                        i, x, y, buttonWidth, buttonHeight, option
                ));
            }
        }

        buttonList.add(new AstryxButton(
                DONE_BUTTON_ID,
                width / 2 - doneWidth / 2,
                doneY,
                doneWidth,
                doneHeight,
                "Done"
        ));
    }

    // ------------------------------------------------------------
    // ANIMATION
    // ------------------------------------------------------------

    @Override
    public void updateScreen() {
        super.updateScreen();

        long now = System.currentTimeMillis();

        float delta = Math.min((now - lastFrame) / 16.0F, 3.0F);
        lastFrame = now;

        animation += delta * 0.008F;

        for (GalaxyParticle particle : particles) {
            particle.x += particle.speed * delta;
            particle.y += particle.drift * delta;

            if (particle.x > width + px(4)) {
                particle.x = -px(4);
                particle.y = random.nextFloat() * height;
            }

            if (particle.y < -px(4)) {
                particle.y = height + px(4);
            } else if (particle.y > height + px(4)) {
                particle.y = -px(4);
            }
        }
    }

    // ------------------------------------------------------------
    // RENDERING
    // ------------------------------------------------------------

    @Override
    protected void drawAstryxScreen(int mouseX, int mouseY, float partialTicks) {
        drawGalaxyBackground();
        drawPanel();
        drawTitle();

        drawDefaultScreen(mouseX, mouseY, partialTicks);
    }

    // ------------------------------------------------------------
    // GALAXY BACKGROUND
    // ------------------------------------------------------------

    private void drawGalaxyBackground() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glColor4f(0.025F, 0.025F, 0.075F, 1.0F);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(0, height);

        GL11.glColor4f(0.035F, 0.045F, 0.11F, 1.0F);
        GL11.glVertex2f(width, height);
        GL11.glVertex2f(width, 0);

        GL11.glEnd();

        drawRadialGlow(width * 0.18F, height * 0.34F, width * 0.48F, 0x704A218E);
        drawRadialGlow(width * 0.82F, height * 0.66F, width * 0.48F, 0x554044B5);
        drawRadialGlow(width * 0.51F, height * 0.42F, width * 0.29F, 0x483A237F);
        drawRadialGlow(width * 0.53F, height * 0.45F, width * 0.12F, 0x453C50B0);

        drawStars();

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void drawRadialGlow(float centerX, float centerY, float radius, int color) {
        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        GL11.glColor4f(red, green, blue, alpha);
        GL11.glVertex2f(centerX, centerY);

        GL11.glColor4f(red, green, blue, 0.0F);

        for (int angle = 0; angle <= 360; angle += 5) {
            double radians = Math.toRadians(angle);

            GL11.glVertex2f(
                    centerX + (float) Math.cos(radians) * radius,
                    centerY + (float) Math.sin(radians) * radius
            );
        }

        GL11.glEnd();

        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1, 1, 1, 1);
    }

    private void drawStars() {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBegin(GL11.GL_QUADS);

        for (GalaxyParticle particle : particles) {
            float twinkle = 0.55F + 0.45F * (float) Math.sin(
                    animation * particle.twinkleSpeed + particle.phase
            );

            float alpha = particle.brightness * twinkle;
            float size = particle.size;

            GL11.glColor4f(0.72F, 0.79F, 1.0F, alpha);

            GL11.glVertex2f(particle.x - size, particle.y - size);
            GL11.glVertex2f(particle.x - size, particle.y + size);
            GL11.glVertex2f(particle.x + size, particle.y + size);
            GL11.glVertex2f(particle.x + size, particle.y - size);
        }

        GL11.glEnd();

        GL11.glBegin(GL11.GL_QUADS);

        for (int i = 0; i < particles.size(); i += 15) {
            GalaxyParticle particle = particles.get(i);

            float twinkle = 0.55F + 0.45F * (float) Math.sin(
                    animation * 1.6F + particle.phase
            );

            float size = particle.size * 2.2F;

            GL11.glColor4f(0.82F, 0.82F, 1.0F, 0.65F * twinkle);

            GL11.glVertex2f(particle.x - size, particle.y - 0.5F / scaleFactor);
            GL11.glVertex2f(particle.x - size, particle.y + 0.5F / scaleFactor);
            GL11.glVertex2f(particle.x + size, particle.y + 0.5F / scaleFactor);
            GL11.glVertex2f(particle.x + size, particle.y - 0.5F / scaleFactor);

            GL11.glVertex2f(particle.x - 0.5F / scaleFactor, particle.y - size);
            GL11.glVertex2f(particle.x - 0.5F / scaleFactor, particle.y + size);
            GL11.glVertex2f(particle.x + 0.5F / scaleFactor, particle.y + size);
            GL11.glVertex2f(particle.x + 0.5F / scaleFactor, particle.y - size);
        }

        GL11.glEnd();

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    // ------------------------------------------------------------
    // PANEL
    // ------------------------------------------------------------

    private void drawPanel() {
        drawGradientRect(0, 0, width, height, 0x30100E19, 0x350A0B18);

        drawRoundedRect(
                panelLeft - px(1), panelTop - px(1),
                panelRight + px(1), panelBottom + px(1),
                13 * layoutScale, 0xAA7656B9
        );

        drawRoundedRect(
                panelLeft, panelTop,
                panelRight, panelBottom,
                12 * layoutScale, 0xC9100F1B
        );

        drawGradientRect(
                panelLeft + Math.round(12 * layoutScale),
                panelTop,
                panelRight - Math.round(12 * layoutScale),
                panelTop + Math.max(px(2), Math.round(3 * layoutScale)),
                0xFFB18AFF, 0xFF568DFF
        );

        drawGradientRect(
                panelLeft + Math.round(30 * layoutScale),
                panelTop + Math.round(78 * layoutScale),
                panelRight - Math.round(30 * layoutScale),
                panelTop + Math.round(79 * layoutScale),
                0x007E65B9, 0x887E65B9
        );
    }

    // ------------------------------------------------------------
    // TITLE
    // ------------------------------------------------------------

    private void drawTitle() {
        float titleScale = 0.37F * layoutScale;
        float subtitleScale = 0.19F * layoutScale;

        float titleY = panelTop + 19 * layoutScale;

        titleFont.drawCenteredString(
                "VIDEO SETTINGS",
                width / 2.0F,
                titleY,
                titleScale,
                0xFFFFFFFF
        );

        regularFont.drawCenteredString(
                "Customise your visual experience",
                width / 2.0F,
                titleY + 31 * layoutScale,
                subtitleScale,
                0xFFB8B2CC
        );
    }

    // ------------------------------------------------------------
    // BUTTON STYLING
    // ------------------------------------------------------------

    private float getButtonTextScale() {
        return 0.175F * layoutScale;
    }

    private boolean isHovered(int x, int y, int w, int h, int mouseX, int mouseY) {
        return mouseX >= x && mouseY >= y
                && mouseX < x + w && mouseY < y + h;
    }

    private void drawOptionButton(
            int x, int y, int w, int h,
            int mouseX, int mouseY,
            boolean enabled, String text
    ) {
        boolean hovered = enabled && isHovered(x, y, w, h, mouseX, mouseY);
        int radius = Math.max(px(5), Math.round(8 * layoutScale));

        if (hovered) {
            drawRoundedRect(
                    x - px(3), y - px(3),
                    x + w + px(3), y + h + px(3),
                    radius + px(2), 0x447F61C8
            );
        }

        drawRoundedRect(
                x, y, x + w, y + h,
                radius, hovered ? 0xFFB18AFF : 0xFF66547F
        );

        drawRoundedRect(
                x + px(1), y + px(1),
                x + w - px(1), y + h - px(1),
                Math.max(0, radius - px(1)),
                hovered ? 0xFF342B46 : 0xFF211D2D
        );

        drawGradientRect(
                x + Math.round(8 * layoutScale),
                y + px(2),
                x + w - Math.round(8 * layoutScale),
                y + Math.max(px(3), Math.round(5 * layoutScale)),
                hovered ? 0x66D7BDFF : 0x335E4B83,
                hovered ? 0x226B9EFF : 0x112D4777
        );

        int accentHeight = Math.max(
                px(12),
                h - Math.round(16 * layoutScale)
        );

        drawRoundedRect(
                x + Math.round(8 * layoutScale),
                y + (h - accentHeight) / 2.0F,
                x + Math.round(11 * layoutScale),
                y + (h + accentHeight) / 2.0F,
                2 * layoutScale,
                hovered ? 0xFFD0B5FF : 0xFF8871B2
        );

        float indicatorX = x + w - Math.round(15 * layoutScale);
        float indicatorY = y + h / 2.0F;

        drawRoundedRect(
                indicatorX - 2 * layoutScale,
                indicatorY - 2 * layoutScale,
                indicatorX + 2 * layoutScale,
                indicatorY + 2 * layoutScale,
                2 * layoutScale,
                hovered ? 0xFFD6C2FF : 0xFF817397
        );

        float textScale = getButtonTextScale();
        float maxTextWidth = w - 48 * layoutScale;
        float measuredWidth = titleFont.getStringWidthFloat(text, textScale);

        if (measuredWidth > maxTextWidth && measuredWidth > 0) {
            textScale *= maxTextWidth / measuredWidth;
        }

        float maxTextHeight = h * 0.78F;
        float measuredHeight = titleFont.getHeight(textScale);

        if (measuredHeight > maxTextHeight && measuredHeight > 0) {
            textScale *= maxTextHeight / measuredHeight;
        }

        titleFont.drawCenteredString(
                text,
                x + w / 2.0F,
                y + (h - titleFont.getHeight(textScale)) / 2.0F,
                textScale,
                enabled ? (hovered ? 0xFFFFFFFF : 0xFFF0ECFA) : 0xFF77717F
        );
    }

    private void drawDoneButton(
            int x, int y, int w, int h,
            int mouseX, int mouseY,
            boolean enabled, String text
    ) {
        boolean hovered = enabled && isHovered(x, y, w, h, mouseX, mouseY);
        int radius = Math.max(px(5), Math.round(9 * layoutScale));

        if (hovered) {
            drawRoundedRect(
                    x - px(3), y - px(3),
                    x + w + px(3), y + h + px(3),
                    radius + px(2), 0x667F61D8
            );
        }

        drawRoundedRect(
                x, y, x + w, y + h,
                radius, hovered ? 0xFFE8D4FF : 0xFFAA8AE5
        );

        drawGradientRect(
                x + px(1), y + px(1),
                x + w - px(1), y + h - px(1),
                hovered ? 0xFF9368D8 : 0xFF7652BD,
                hovered ? 0xFF648FE5 : 0xFF526FC6
        );

        drawGradientRect(
                x + Math.round(7 * layoutScale),
                y + px(2),
                x + w - Math.round(7 * layoutScale),
                y + Math.max(px(3), Math.round(5 * layoutScale)),
                0x77FFFFFF, 0x11FFFFFF
        );

        float textScale = getButtonTextScale() * 1.05F;
        float maxTextWidth = w - 20 * layoutScale;
        float measuredWidth = titleFont.getStringWidthFloat(text, textScale);

        if (measuredWidth > maxTextWidth && measuredWidth > 0) {
            textScale *= maxTextWidth / measuredWidth;
        }

        float maxTextHeight = h * 0.78F;
        float measuredHeight = titleFont.getHeight(textScale);

        if (measuredHeight > maxTextHeight && measuredHeight > 0) {
            textScale *= maxTextHeight / measuredHeight;
        }

        titleFont.drawCenteredString(
                text,
                x + w / 2.0F,
                y + (h - titleFont.getHeight(textScale)) / 2.0F,
                textScale,
                0xFFFFFFFF
        );
    }

    // ------------------------------------------------------------
    // OPTION BUTTON
    // ------------------------------------------------------------

    private class AstryxOptionButton extends GuiButton {

        private final GameSettings.Options option;

        public AstryxOptionButton(
                int id, int x, int y,
                int buttonWidth, int buttonHeight,
                GameSettings.Options option
        ) {
            super(id, x, y, buttonWidth, buttonHeight, settings.getKeyBinding(option));
            this.option = option;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) {
                return;
            }

            displayString = settings.getKeyBinding(option);

            drawOptionButton(
                    xPosition, yPosition, width, height,
                    mouseX, mouseY, enabled, displayString
            );
        }
    }

    // ------------------------------------------------------------
    // CUSTOM SLIDER
    // ------------------------------------------------------------

    private class AstryxSlider extends GuiButton {

        private final GameSettings.Options option;

        private float sliderValue;

        public AstryxSlider(
                int id, int x, int y,
                int buttonWidth, int buttonHeight,
                GameSettings.Options option
        ) {
            super(id, x, y, buttonWidth, buttonHeight, "");

            this.option = option;
            this.sliderValue = getCurrentSliderValue();
        }

        private float getCurrentSliderValue() {
            float value = option.normalizeValue(
                    settings.getOptionFloatValue(option)
            );

            if (Float.isNaN(value) || Float.isInfinite(value)) {
                return 0.0F;
            }

            return MathHelper.clamp_float(value, 0.0F, 1.0F);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) {
                return;
            }

            if (draggingSlider != this) {
                sliderValue = getCurrentSliderValue();
            }

            boolean hovered = isHovered(
                    xPosition, yPosition, width, height, mouseX, mouseY
            );

            int radius = Math.max(px(5), Math.round(8 * layoutScale));

            if (hovered) {
                drawRoundedRect(
                        xPosition - px(3), yPosition - px(3),
                        xPosition + width + px(3), yPosition + height + px(3),
                        radius + px(2), 0x447F61C8
                );
            }

            drawRoundedRect(
                    xPosition, yPosition,
                    xPosition + width, yPosition + height,
                    radius, hovered ? 0xFFB18AFF : 0xFF66547F
            );

            drawRoundedRect(
                    xPosition + px(1), yPosition + px(1),
                    xPosition + width - px(1), yPosition + height - px(1),
                    Math.max(0, radius - px(1)),
                    hovered ? 0xFF342B46 : 0xFF211D2D
            );

            displayString = settings.getKeyBinding(option);

            float textScale = getButtonTextScale() * 0.92F;
            float maxTextWidth = width - 32 * layoutScale;
            float measuredWidth = titleFont.getStringWidthFloat(displayString, textScale);

            if (measuredWidth > maxTextWidth && measuredWidth > 0) {
                textScale *= maxTextWidth / measuredWidth;
            }

            int trackLeft = getTrackLeft();
            int trackRight = getTrackRight();

            float trackY = yPosition + height - Math.max(
                    px(8),
                    Math.round(10 * layoutScale)
            );

            float trackHeight = Math.max(
                    px(2),
                    3 * layoutScale
            );

            float availableTextHeight = Math.max(
                    1.0F / scaleFactor,
                    trackY - yPosition - 5 * layoutScale
            );

            float measuredTextHeight = titleFont.getHeight(textScale);

            if (measuredTextHeight > availableTextHeight * 0.85F
                    && measuredTextHeight > 0) {
                textScale *= (availableTextHeight * 0.85F) / measuredTextHeight;
            }

            titleFont.drawCenteredString(
                    displayString,
                    xPosition + width / 2.0F,
                    yPosition + 3 * layoutScale,
                    textScale,
                    0xFFF0ECFA
            );

            drawRoundedRect(
                    trackLeft, trackY,
                    trackRight, trackY + trackHeight,
                    trackHeight / 2.0F, 0xFF494052
            );

            float knobX = trackLeft + (trackRight - trackLeft) * sliderValue;
            float trackCenterY = trackY + trackHeight / 2.0F;

            if (sliderValue > 0.0F && knobX > trackLeft) {
                drawRoundedRect(
                        trackLeft, trackY,
                        knobX, trackY + trackHeight,
                        trackHeight / 2.0F,
                        hovered ? 0xFFB18AFF : 0xFF9270D8
                );
            }

            float knobRadius = Math.max(
                    px(3),
                    3.5F * layoutScale
            );

            if (hovered || draggingSlider == this) {
                drawRoundedRect(
                        knobX - knobRadius - px(2),
                        trackCenterY - knobRadius - px(2),
                        knobX + knobRadius + px(2),
                        trackCenterY + knobRadius + px(2),
                        knobRadius + px(2),
                        0x557F61D8
                );
            }

            drawRoundedRect(
                    knobX - knobRadius,
                    trackCenterY - knobRadius,
                    knobX + knobRadius,
                    trackCenterY + knobRadius,
                    knobRadius,
                    hovered || draggingSlider == this
                            ? 0xFFFFFFFF
                            : 0xFFE0CCFF
            );
        }

        private int getTrackLeft() {
            return xPosition + Math.round(14 * layoutScale);
        }

        private int getTrackRight() {
            return xPosition + width - Math.round(14 * layoutScale);
        }

        private boolean isInside(int mouseX, int mouseY) {
            return visible && enabled
                    && mouseX >= xPosition
                    && mouseY >= yPosition
                    && mouseX < xPosition + width
                    && mouseY < yPosition + height;
        }

        private void beginDragging(int mouseX) {
            if (!visible || !enabled) {
                return;
            }

            draggingSlider = this;
            updateSlider(mouseX);
        }

        private void dragTo(int mouseX) {
            if (draggingSlider == this) {
                updateSlider(mouseX);
            }
        }

        private void stopDragging() {
            if (draggingSlider == this) {
                draggingSlider = null;
            }
        }

        private void updateSlider(int mouseX) {
            int trackLeft = getTrackLeft();
            int trackRight = getTrackRight();

            if (trackRight <= trackLeft) {
                return;
            }

            float value = (float) (mouseX - trackLeft)
                    / (float) (trackRight - trackLeft);

            if (Float.isNaN(value) || Float.isInfinite(value)) {
                return;
            }

            sliderValue = MathHelper.clamp_float(value, 0.0F, 1.0F);

            float actualValue = option.denormalizeValue(sliderValue);

            if (Float.isNaN(actualValue) || Float.isInfinite(actualValue)) {
                return;
            }

            settings.setOptionFloatValue(option, actualValue);

            displayString = settings.getKeyBinding(option);
        }
    }

    // ------------------------------------------------------------
    // DONE BUTTON
    // ------------------------------------------------------------

    private class AstryxButton extends GuiButton {

        public AstryxButton(
                int id, int x, int y,
                int buttonWidth, int buttonHeight,
                String text
        ) {
            super(id, x, y, buttonWidth, buttonHeight, text);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) {
                return;
            }

            drawDoneButton(
                    xPosition, yPosition, width, height,
                    mouseX, mouseY, enabled, displayString
            );
        }
    }

    // ------------------------------------------------------------
    // MOUSE INPUT
    // ------------------------------------------------------------

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) throws IOException {

        if (mouseButton == 0) {
            for (GuiButton button : buttonList) {
                if (button instanceof AstryxSlider) {
                    AstryxSlider slider = (AstryxSlider) button;

                    if (slider.isInside(mouseX, mouseY)) {
                        slider.beginDragging(mouseX);
                        return;
                    }
                }
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(
            int mouseX,
            int mouseY,
            int clickedMouseButton,
            long timeSinceLastClick
    ) {
        if (draggingSlider != null) {
            if (clickedMouseButton == 0) {
                draggingSlider.dragTo(mouseX);
            }

            return;
        }

        super.mouseClickMove(
                mouseX,
                mouseY,
                clickedMouseButton,
                timeSinceLastClick
        );
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (draggingSlider != null) {
            draggingSlider.stopDragging();
            return;
        }

        super.mouseReleased(mouseX, mouseY, state);
    }

    // ------------------------------------------------------------
    // ROUNDED RECTANGLE
    // ------------------------------------------------------------

    private void drawRoundedRect(
            float left, float top,
            float right, float bottom,
            float radius, int color
    ) {
        if (right <= left || bottom <= top) {
            return;
        }

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

        if (radius <= 0) {
            Gui.drawRect(
                    (int) left, (int) top,
                    (int) right, (int) bottom,
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
            float centerX, float centerY,
            float radius, int startAngle
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
            float left, float top,
            float right, float bottom,
            int leftColor, int rightColor
    ) {
        if (right <= left || bottom <= top) {
            return;
        }

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

    private void setGlColor(int color) {
        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.color(red, green, blue, alpha);
    }

    // ------------------------------------------------------------
    // BUTTON ACTIONS
    // ------------------------------------------------------------

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (!button.enabled) {
            return;
        }

        if (button instanceof AstryxSlider) {
            return;
        }

        if (button.id == DONE_BUTTON_ID) {
            mc.displayGuiScreen(parent);
            return;
        }

        if (button.id >= 0 && button.id < VIDEO_OPTIONS.length) {
            GameSettings.Options option = VIDEO_OPTIONS[button.id];

            settings.setOptionValue(option, 1);

            if (option == GameSettings.Options.GUI_SCALE) {
                mc.displayGuiScreen(new AstryxVideoSettings(parent));
                return;
            }

            button.displayString = settings.getKeyBinding(option);
        }
    }

    // ------------------------------------------------------------
    // KEYBOARD INPUT
    // ------------------------------------------------------------

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) {
            draggingSlider = null;
            mc.displayGuiScreen(parent);
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    // ------------------------------------------------------------
    // CLEANUP
    // ------------------------------------------------------------

    @Override
    public void onGuiClosed() {
        draggingSlider = null;

        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }

        if (regularFont != null) {
            regularFont.dispose();
            regularFont = null;
        }

        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // ------------------------------------------------------------
    // PARTICLE DATA
    // ------------------------------------------------------------

    private static class GalaxyParticle {

        private float x;
        private float y;

        private final float size;
        private final float brightness;
        private final float phase;
        private final float drift;
        private final float speed;
        private final float twinkleSpeed;

        private GalaxyParticle(
                float x, float y,
                float size, float brightness,
                float phase, float drift
        ) {
            this.x = x;
            this.y = y;

            this.size = size;
            this.brightness = brightness;
            this.phase = phase;
            this.drift = drift;

            this.speed = 0.035F + size * 0.035F;
            this.twinkleSpeed = 0.7F + size * 0.6F;
        }
    }
}
