package com.astryx.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MainMenu extends AstryxGuiScreen {

    private static final float UI_SCALE = 1.00F;
    private static final float DESIGN_SCALE = 0.80F;

    private static final ResourceLocation BACKGROUND =
            new ResourceLocation("astryx", "textures/gui/background.png");

    private Button singleplayer;
    private Button multiplayer;
    private Button settings;
    private Button quit;

    private CustomFont regularFont;
    private CustomFont boldFont;

    private float titleY;
    private float taglineY;
    private float dividerY;

    private float titleScale;
    private float taglineScale;
    private float buttonTextScale;
    private float layoutScale;

    private long lastFrame;
    private float animation;

    @Override
    public void initGui() {
        lastFrame = System.currentTimeMillis();

        if (boldFont == null) {
            boldFont = new CustomFont(true);
            regularFont = new CustomFont(false);
        }

        createButtons();
        prepareGuiRendering();
    }

    // ------------------------------------------------------------
    // RENDER STATE
    // ------------------------------------------------------------

    private void prepareGuiRendering() {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.disableCull();
        GlStateManager.disableLighting();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

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
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();

        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.disableLighting();
    }

    // ------------------------------------------------------------
    // LAYOUT
    // ------------------------------------------------------------

    private void createButtons() {
        int centerX = width / 2;

        /*
         * Preserve this menu's existing proportions at the shared
         * reference viewport. AstryxGuiScreen keeps that viewport
         * identical at every Minecraft GUI scale.
         */
        layoutScale = (UI_SCALE / 0.35F)
                * DESIGN_SCALE
                / REFERENCE_GUI_SCALE;

        titleScale = 0.43F * layoutScale;
        taglineScale = 0.245F * layoutScale;
        buttonTextScale = 0.30F * layoutScale;

        int horizontalMargin = Math.round(32 * layoutScale);

        int availableWidth = Math.max(
                1,
                width - horizontalMargin
        );

        int mainWidth = Math.min(
                Math.round(146 * layoutScale),
                availableWidth
        );

        mainWidth = Math.max(
                Math.min(
                        Math.round(80 * layoutScale),
                        availableWidth
                ),
                mainWidth
        );

        int mainHeight = Math.max(
                1,
                Math.round(24 * layoutScale)
        );

        int smallHeight = Math.max(
                1,
                Math.round(22 * layoutScale)
        );

        int verticalGap = Math.max(
                1,
                Math.round(6 * layoutScale)
        );

        int horizontalGap = Math.max(
                1,
                Math.round(7 * layoutScale)
        );

        int smallWidth = Math.max(
                1,
                (mainWidth - horizontalGap) / 2
        );

        int titleHeight = boldFont.getHeight(titleScale);
        int taglineHeight = regularFont.getHeight(taglineScale);

        int titleTaglineGap = Math.max(
                1,
                Math.round(4 * layoutScale)
        );

        int taglineDividerGap = Math.max(
                1,
                Math.round(8 * layoutScale)
        );

        int dividerHeight = Math.max(
                1,
                Math.round(2 * layoutScale)
        );

        int brandingHeight = titleHeight
                + titleTaglineGap
                + taglineHeight
                + taglineDividerGap
                + dividerHeight;

        int brandingButtonGap = Math.round(15 * layoutScale);

        int buttonsHeight = mainHeight * 2
                + smallHeight
                + verticalGap * 2;

        int totalHeight = brandingHeight
                + brandingButtonGap
                + buttonsHeight;

        float verticalCenter = height * 0.43F;

        int compositionTop = Math.round(
                verticalCenter - totalHeight / 2.0F
        );

        compositionTop = Math.max(
                Math.round(5 * layoutScale),
                compositionTop
        );

        titleY = compositionTop;

        taglineY = titleY
                + titleHeight
                + titleTaglineGap;

        dividerY = taglineY
                + taglineHeight
                + taglineDividerGap;

        int groupTop = compositionTop
                + brandingHeight
                + brandingButtonGap;

        int mainX = centerX - mainWidth / 2;

        singleplayer = new Button(
                mainX,
                groupTop,
                mainWidth,
                mainHeight,
                "Singleplayer"
        );

        multiplayer = new Button(
                mainX,
                groupTop + mainHeight + verticalGap,
                mainWidth,
                mainHeight,
                "Multiplayer"
        );

        int bottomY = groupTop
                + mainHeight * 2
                + verticalGap * 2;

        int rowWidth = smallWidth * 2 + horizontalGap;
        int rowLeft = centerX - rowWidth / 2;

        settings = new Button(
                rowLeft,
                bottomY,
                smallWidth,
                smallHeight,
                "Settings"
        );

        quit = new Button(
                rowLeft + smallWidth + horizontalGap,
                bottomY,
                smallWidth,
                smallHeight,
                "Quit"
        );
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
    }

    // ------------------------------------------------------------
    // MAIN RENDER
    // ------------------------------------------------------------

    @Override
    protected void drawAstryxScreen(int mouseX, int mouseY, float partialTicks) {
        prepareGuiRendering();

        try {
            drawBackgroundLayer();

            drawBranding();
            drawButtons(mouseX, mouseY);

            drawDefaultScreen(mouseX, mouseY, partialTicks);
        } finally {
            restoreGuiRendering();
        }
    }

    // ------------------------------------------------------------
    // PNG BACKGROUND
    // ------------------------------------------------------------

    private void drawBackgroundLayer() {
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        mc.getTextureManager().bindTexture(BACKGROUND);

        int imageWidth = 1713;
        int imageHeight = 918;

        int drawWidth;
        int drawHeight;

        if (org.lwjgl.opengl.Display.isFullscreen()) {
            drawWidth = width;
            drawHeight = height;
        } else {
            float scale = Math.min(
                    Math.min(
                            (float) width / imageWidth,
                            (float) height / imageHeight
                    ),
                    1.0F
            );

            drawWidth = Math.round(imageWidth * scale);
            drawHeight = Math.round(imageHeight * scale);
        }

        int x = (width - drawWidth) / 2;
        int y = (height - drawHeight) / 2;

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glTexCoord2f(0.0F, 1.0F);
        GL11.glVertex2f(x, y + drawHeight);

        GL11.glTexCoord2f(1.0F, 1.0F);
        GL11.glVertex2f(x + drawWidth, y + drawHeight);

        GL11.glTexCoord2f(1.0F, 0.0F);
        GL11.glVertex2f(x + drawWidth, y);

        GL11.glTexCoord2f(0.0F, 0.0F);
        GL11.glVertex2f(x, y);

        GL11.glEnd();

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
    }

    // ------------------------------------------------------------
    // BRANDING
    // ------------------------------------------------------------

    private void drawBranding() {
        float centerX = width / 2.0F;

        String title = "ASTRYX";

        float titleWidth = boldFont.getStringWidthFloat(
                title,
                titleScale
        );

        drawGradientString(
                title,
                centerX - titleWidth / 2.0F,
                titleY,
                titleScale,
                0xFFB18AFF,
                0xFF719CFF
        );

        drawShimmerString(
                "Your game, your way.",
                centerX,
                taglineY,
                taglineScale
        );

        float dividerWidth = 27 * layoutScale;
        float dividerHeight = Math.max(1, 2 * layoutScale);

        drawRoundedRect(
                centerX - dividerWidth / 2,
                dividerY,
                centerX + dividerWidth / 2,
                dividerY + dividerHeight,
                layoutScale,
                0xFF8061D8
        );
    }

    private void drawShimmerString(
            String text,
            float centerX,
            float y,
            float scale
    ) {
        float textWidth = regularFont.getStringWidthFloat(text, scale);
        float startX = centerX - textWidth / 2.0F;

        float shimmerPosition = (animation * 32 * layoutScale)
                % (textWidth + 45 * layoutScale);

        float cursorX = startX;

        for (int i = 0; i < text.length(); i++) {
            String character = String.valueOf(text.charAt(i));

            float characterWidth = regularFont.getStringWidthFloat(
                    character,
                    scale
            );

            float characterCenter = cursorX + characterWidth / 2.0F;

            float distance = Math.abs(
                    characterCenter
                            - (startX + shimmerPosition - 15 * layoutScale)
            );

            float intensity = Math.max(
                    0,
                    1.0F - distance / (18.0F * layoutScale)
            );

            intensity *= intensity;

            int color = interpolateColor(
                    0xFFBDB9CB,
                    0xFFFFFFFF,
                    intensity * 0.85F
            );

            regularFont.drawString(
                    character,
                    cursorX,
                    y,
                    scale,
                    color
            );

            cursorX += characterWidth;
        }
    }

    private void drawGradientString(
            String text,
            float x,
            float y,
            float scale,
            int startColor,
            int endColor
    ) {
        float currentX = x;

        for (int i = 0; i < text.length(); i++) {
            float progress = text.length() <= 1
                    ? 0
                    : (float) i / (text.length() - 1);

            int color = interpolateColor(
                    startColor,
                    endColor,
                    progress
            );

            String character = String.valueOf(text.charAt(i));

            boldFont.drawString(
                    character,
                    currentX,
                    y,
                    scale,
                    color
            );

            currentX += boldFont.getStringWidthFloat(
                    character,
                    scale
            );
        }
    }

    private int interpolateColor(
            int first,
            int second,
            float progress
    ) {
        progress = Math.max(0, Math.min(1, progress));

        int red = (int) (((first >> 16) & 255)
                + (((second >> 16) & 255)
                - ((first >> 16) & 255)) * progress);

        int green = (int) (((first >> 8) & 255)
                + (((second >> 8) & 255)
                - ((first >> 8) & 255)) * progress);

        int blue = (int) ((first & 255)
                + ((second & 255) - (first & 255)) * progress);

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

    // ------------------------------------------------------------
    // BUTTONS
    // ------------------------------------------------------------

    private void drawButtons(int mouseX, int mouseY) {
        drawButton(singleplayer, mouseX, mouseY);
        drawButton(multiplayer, mouseX, mouseY);
        drawButton(settings, mouseX, mouseY);
        drawButton(quit, mouseX, mouseY);
    }

    private void drawButton(
            Button button,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = button.contains(mouseX, mouseY);
        float target = hovered ? 1.0F : 0.0F;

        button.hoverAmount += (target - button.hoverAmount) * 0.22F;

        if (Math.abs(target - button.hoverAmount) < 0.005F) {
            button.hoverAmount = target;
        }

        float expansion = 1.0F + button.hoverAmount * 0.045F;

        float visualWidth = button.width * expansion;
        float visualHeight = button.height * expansion;

        float centerX = button.x + button.width / 2.0F;
        float centerY = button.y + button.height / 2.0F;

        float left = centerX - visualWidth / 2.0F;
        float right = centerX + visualWidth / 2.0F;
        float top = centerY - visualHeight / 2.0F;
        float bottom = centerY + visualHeight / 2.0F;

        float borderThickness = (0.9F + button.hoverAmount * 0.2F)
                * layoutScale;

        float cornerRadius = 6.0F * layoutScale;

        if (button.hoverAmount > 0.01F) {
            drawGlow(
                    centerX,
                    centerY,
                    visualWidth * 0.85F,
                    (int) (button.hoverAmount * 17) << 24 | 0x7653D8
            );
        }

        drawRoundedGradientRect(
                left,
                top,
                right,
                bottom,
                cornerRadius,
                0xFF9366E8,
                0xFF568DFF
        );

        drawRoundedRect(
                left + borderThickness,
                top + borderThickness,
                right - borderThickness,
                bottom - borderThickness,
                Math.max(1, cornerRadius - borderThickness),
                hovered ? 0xFF151321 : 0xFF090910
        );

        boolean mainButton = button == singleplayer
                || button == multiplayer;

        CustomFont font = mainButton ? boldFont : regularFont;

        int textHeight = font.getHeight(buttonTextScale);

        font.drawCenteredString(
                button.label,
                centerX,
                centerY - textHeight / 2.0F,
                buttonTextScale,
                hovered ? 0xFFFFFFFF : 0xFFDAD7E5
        );
    }

    // ------------------------------------------------------------
    // ROUNDED GRADIENT RENDERING
    // ------------------------------------------------------------

    private void drawRoundedGradientRect(
            float left,
            float top,
            float right,
            float bottom,
            float radius,
            int leftColor,
            int rightColor
    ) {
        if (right <= left || bottom <= top) {
            return;
        }

        radius = Math.max(
                0,
                Math.min(radius, Math.min(
                        (right - left) / 2.0F,
                        (bottom - top) / 2.0F
                ))
        );

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glBegin(GL11.GL_QUADS);

        float step = 0.5F;

        for (float y = top; y < bottom; y += step) {
            float nextY = Math.min(y + step, bottom);

            float insetTop = getRoundedInset(
                    y, top, bottom, radius
            );

            float insetBottom = getRoundedInset(
                    nextY, top, bottom, radius
            );

            float leftTop = left + insetTop;
            float rightTop = right - insetTop;
            float leftBottom = left + insetBottom;
            float rightBottom = right - insetBottom;

            setGradientVertexColor(
                    leftColor,
                    rightColor,
                    (leftTop - left) / (right - left)
            );

            GL11.glVertex2f(leftTop, y);

            setGradientVertexColor(
                    leftColor,
                    rightColor,
                    (rightTop - left) / (right - left)
            );

            GL11.glVertex2f(rightTop, y);

            setGradientVertexColor(
                    leftColor,
                    rightColor,
                    (rightBottom - left) / (right - left)
            );

            GL11.glVertex2f(rightBottom, nextY);

            setGradientVertexColor(
                    leftColor,
                    rightColor,
                    (leftBottom - left) / (right - left)
            );

            GL11.glVertex2f(leftBottom, nextY);
        }

        GL11.glEnd();

        finishRoundedRendering();
    }

    private void setGradientVertexColor(
            int leftColor,
            int rightColor,
            float progress
    ) {
        progress = Math.max(0, Math.min(1, progress));

        setGlColor(
                interpolateColor(leftColor, rightColor, progress)
        );
    }

    private float getRoundedInset(
            float y,
            float top,
            float bottom,
            float radius
    ) {
        if (radius <= 0) {
            return 0;
        }

        float centerY;

        if (y < top + radius) {
            centerY = top + radius;
        } else if (y > bottom - radius) {
            centerY = bottom - radius;
        } else {
            return 0;
        }

        float distance = Math.abs(y - centerY);
        float inside = radius * radius - distance * distance;

        return radius - (float) Math.sqrt(Math.max(0, inside));
    }

    // ------------------------------------------------------------
    // SOLID ROUNDED RECTANGLES
    // ------------------------------------------------------------

    private void drawRoundedRect(
            float left,
            float top,
            float right,
            float bottom,
            float radius,
            int color
    ) {
        if (right <= left || bottom <= top) {
            return;
        }

        radius = Math.max(
                0,
                Math.min(radius, Math.min(
                        (right - left) / 2.0F,
                        (bottom - top) / 2.0F
                ))
        );

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        setGlColor(color);

        GL11.glBegin(GL11.GL_QUADS);

        float step = 0.5F;

        for (float y = top; y < bottom; y += step) {
            float nextY = Math.min(y + step, bottom);

            float insetTop = getRoundedInset(
                    y, top, bottom, radius
            );

            float insetBottom = getRoundedInset(
                    nextY, top, bottom, radius
            );

            GL11.glVertex2f(left + insetTop, y);
            GL11.glVertex2f(right - insetTop, y);
            GL11.glVertex2f(right - insetBottom, nextY);
            GL11.glVertex2f(left + insetBottom, nextY);
        }

        GL11.glEnd();

        finishRoundedRendering();
    }

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
        GlStateManager.disableCull();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        GlStateManager.color(red, green, blue, alpha);
        GL11.glVertex2f(centerX, centerY);

        GlStateManager.color(red, green, blue, 0);

        for (int angle = 0; angle <= 360; angle += 5) {
            double radians = Math.toRadians(angle);

            GL11.glVertex2f(
                    centerX + (float) Math.cos(radians) * radius,
                    centerY + (float) Math.sin(radians) * radius
            );
        }

        GL11.glEnd();

        finishRoundedRendering();
    }

    private void setGlColor(int color) {
        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.color(red, green, blue, alpha);
    }

    private void finishRoundedRendering() {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.disableCull();
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

        if (singleplayer.contains(mouseX, mouseY)) {
            mc.displayGuiScreen(new AstryxSelectWorld(this));
            return;
        }

        if (multiplayer.contains(mouseX, mouseY)) {
            mc.displayGuiScreen(new AstryxMultiplayer(this));
            return;
        }

        if (settings.contains(mouseX, mouseY)) {
            mc.displayGuiScreen(new AstryxOptions(this));
            return;
        }

        if (quit.contains(mouseX, mouseY)) {
            mc.shutdown();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

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
    // BUTTON DATA
    // ------------------------------------------------------------

    private static class Button {
        int x;
        int y;
        int width;
        int height;

        String label;
        float hoverAmount;

        Button(
                int x,
                int y,
                int width,
                int height,
                String label
        ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
            this.hoverAmount = 0;
        }

        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x
                    && mouseX < x + width
                    && mouseY >= y
                    && mouseY < y + height;
        }
    }

    // ------------------------------------------------------------
    // CUSTOM FONT RENDERER
    // ------------------------------------------------------------

    public static class CustomFont {

        private static final int ATLAS_WIDTH = 2048;
        private static final int ATLAS_HEIGHT = 1024;

        private static final int COLUMNS = 16;
        private static final int PADDING = 12;

        private final Glyph[] glyphs = new Glyph[95];

        private final ResourceLocation location;

        private final int ascent;
        private final int fontHeight;

        CustomFont(boolean bold) {
            BufferedImage image = new BufferedImage(
                    ATLAS_WIDTH,
                    ATLAS_HEIGHT,
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D graphics = image.createGraphics();

            graphics.setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_FRACTIONALMETRICS,
                    RenderingHints.VALUE_FRACTIONALMETRICS_ON
            );

            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );

            Font font = new Font(
                    "Segoe UI",
                    bold ? Font.BOLD : Font.PLAIN,
                    52
            );

            graphics.setFont(font);

            FontRenderContext context = graphics.getFontRenderContext();

            ascent = graphics.getFontMetrics().getAscent();
            fontHeight = graphics.getFontMetrics().getHeight();

            for (int i = 0; i < glyphs.length; i++) {
                char character = (char) (i + 32);

                int column = i % COLUMNS;
                int row = i / COLUMNS;

                int cellX = column * 128;
                int cellY = row * 128;

                GlyphVector vector = font.createGlyphVector(
                        context,
                        String.valueOf(character)
                );

                Rectangle bounds = vector.getPixelBounds(
                        context,
                        0,
                        0
                );

                float advance = vector.getGlyphMetrics(0).getAdvanceX();

                if (bounds.width > 0 && bounds.height > 0) {
                    int drawX = cellX + PADDING - bounds.x;
                    int drawY = cellY + PADDING - bounds.y;

                    graphics.setColor(java.awt.Color.WHITE);

                    graphics.drawGlyphVector(
                            vector,
                            drawX,
                            drawY
                    );

                    glyphs[i] = new Glyph(
                            cellX + PADDING,
                            cellY + PADDING,
                            bounds.width,
                            bounds.height,
                            bounds.x,
                            bounds.y,
                            advance
                    );
                } else {
                    glyphs[i] = new Glyph(
                            0, 0, 0, 0, 0, 0, advance
                    );
                }
            }

            graphics.dispose();

            DynamicTexture texture = new DynamicTexture(image);

            location = Minecraft.getMinecraft()
                    .getTextureManager()
                    .getDynamicTextureLocation(
                            bold
                                    ? "astryx_bold_font"
                                    : "astryx_regular_font",
                            texture
                    );
        }

        int getHeight(float scale) {
            return (int) Math.ceil(fontHeight * scale);
        }

        float getStringWidthFloat(String text, float scale) {
            float result = 0;

            for (int i = 0; i < text.length(); i++) {
                result += getGlyph(text.charAt(i)).advance * scale;
            }

            return result;
        }

        int getStringWidth(String text, float scale) {
            return (int) Math.ceil(
                    getStringWidthFloat(text, scale)
            );
        }

        void drawCenteredString(
                String text,
                float centerX,
                float y,
                float scale,
                int color
        ) {
            float textWidth = getStringWidthFloat(text, scale);

            drawString(
                    text,
                    centerX - textWidth / 2.0F,
                    y,
                    scale,
                    color
            );
        }

        void drawString(
                String text,
                float x,
                float y,
                float scale,
                int color
        ) {
            if (text == null || text.isEmpty()) {
                return;
            }

            boolean hasVisibleGlyph = false;

            for (int i = 0; i < text.length(); i++) {
                Glyph glyph = getGlyph(text.charAt(i));

                if (glyph.width > 0 && glyph.height > 0) {
                    hasVisibleGlyph = true;
                    break;
                }
            }

            if (!hasVisibleGlyph) {
                return;
            }

            GlStateManager.disableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();

            GlStateManager.tryBlendFuncSeparate(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_SRC_ALPHA,
                    GL11.GL_ONE,
                    GL11.GL_ZERO
            );

            Minecraft.getMinecraft()
                    .getTextureManager()
                    .bindTexture(location);

            GL11.glTexParameteri(
                    GL11.GL_TEXTURE_2D,
                    GL11.GL_TEXTURE_MIN_FILTER,
                    GL11.GL_LINEAR
            );

            GL11.glTexParameteri(
                    GL11.GL_TEXTURE_2D,
                    GL11.GL_TEXTURE_MAG_FILTER,
                    GL11.GL_LINEAR
            );

            GL11.glTexParameteri(
                    GL11.GL_TEXTURE_2D,
                    GL11.GL_TEXTURE_WRAP_S,
                    GL11.GL_CLAMP
            );

            GL11.glTexParameteri(
                    GL11.GL_TEXTURE_2D,
                    GL11.GL_TEXTURE_WRAP_T,
                    GL11.GL_CLAMP
            );

            float red = ((color >> 16) & 255) / 255.0F;
            float green = ((color >> 8) & 255) / 255.0F;
            float blue = (color & 255) / 255.0F;
            float alpha = ((color >> 24) & 255) / 255.0F;

            GlStateManager.color(red, green, blue, alpha);

            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer renderer = tessellator.getWorldRenderer();

            renderer.begin(
                    GL11.GL_QUADS,
                    DefaultVertexFormats.POSITION_TEX
            );

            float cursorX = x;
            float baselineY = y + ascent * scale;

            for (int i = 0; i < text.length(); i++) {
                Glyph glyph = getGlyph(text.charAt(i));

                if (glyph.width > 0 && glyph.height > 0) {
                    float left = cursorX + glyph.offsetX * scale;
                    float top = baselineY + glyph.offsetY * scale;

                    float right = left + glyph.width * scale;
                    float bottom = top + glyph.height * scale;

                    float u1 = glyph.x / (float) ATLAS_WIDTH;
                    float v1 = glyph.y / (float) ATLAS_HEIGHT;

                    float u2 = (glyph.x + glyph.width)
                            / (float) ATLAS_WIDTH;

                    float v2 = (glyph.y + glyph.height)
                            / (float) ATLAS_HEIGHT;

                    renderer.pos(left, bottom, 0)
                            .tex(u1, v2)
                            .endVertex();

                    renderer.pos(right, bottom, 0)
                            .tex(u2, v2)
                            .endVertex();

                    renderer.pos(right, top, 0)
                            .tex(u2, v1)
                            .endVertex();

                    renderer.pos(left, top, 0)
                            .tex(u1, v1)
                            .endVertex();
                }

                cursorX += glyph.advance * scale;
            }

            tessellator.draw();

            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
        }

        private Glyph getGlyph(char character) {
            int index = character - 32;

            if (index < 0 || index >= glyphs.length) {
                index = '?' - 32;
            }

            return glyphs[index];
        }

        void dispose() {
            Minecraft.getMinecraft()
                    .getTextureManager()
                    .deleteTexture(location);
        }
    }

    // ------------------------------------------------------------
    // GLYPH DATA
    // ------------------------------------------------------------

    private static class Glyph {
        int x;
        int y;

        int width;
        int height;

        int offsetX;
        int offsetY;

        float advance;

        Glyph(
                int x,
                int y,
                int width,
                int height,
                int offsetX,
                int offsetY,
                float advance
        ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.advance = advance;
        }
    }
}