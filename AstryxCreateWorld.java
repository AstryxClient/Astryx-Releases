package com.astryx.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.Random;

public class AstryxCreateWorld extends AstryxGuiScreen {

    private static final int PURPLE = 0xFF9B78E8;
    private static final int BLUE = 0xFF648BFA;

    // Shared fixed UI scale used by every Astryx menu.
    private static final float UI_SCALE = 1.10F;

    private static final int TAB_OVERVIEW = 0;
    private static final int TAB_GAMEPLAY = 1;
    private static final int TAB_GENERATION = 2;

    private final GuiScreen parent;
    private final Random random = new Random();

    private MainMenu.CustomFont regularFont;
    private MainMenu.CustomFont boldFont;

    private GuiTextField worldNameField;
    private GuiTextField seedField;
    private GuiTextField generatorOptionsField;

    private int selectedTab = TAB_OVERVIEW;
    private int selectedWorldType = 0;

    private String gameMode = "survival";

    private boolean allowCheats;
    private boolean generateStructures = true;
    private boolean bonusChest;
    private boolean creatingWorld;

    private long lastFrame;
    private float animation;

    private String statusMessage = "";

    private final float[] particleX = new float[28];
    private final float[] particleY = new float[28];
    private final float[] particleSpeed = new float[28];

    public AstryxCreateWorld(GuiScreen parent) {
        this.parent = parent;
    }

    // ============================================================
    // INITIALISATION
    // ============================================================

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        lastFrame = System.currentTimeMillis();

        if (regularFont == null) {
            regularFont = new MainMenu.CustomFont(false);
            boldFont = new MainMenu.CustomFont(true);
        }

        worldNameField = new GuiTextField(
                0, fontRendererObj, 0, 0, 100, 20
        );

        worldNameField.setMaxStringLength(32);
        worldNameField.setText(I18n.format("selectWorld.newWorld"));
        worldNameField.setFocused(true);
        worldNameField.setEnableBackgroundDrawing(false);

        seedField = new GuiTextField(
                1, fontRendererObj, 0, 0, 100, 20
        );

        seedField.setMaxStringLength(64);
        seedField.setEnableBackgroundDrawing(false);

        generatorOptionsField = new GuiTextField(
                2, fontRendererObj, 0, 0, 100, 20
        );

        generatorOptionsField.setMaxStringLength(2000);
        generatorOptionsField.setEnableBackgroundDrawing(false);

        createParticles();
        updateFieldLayout();
        resetRenderState();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        disposeFonts();
        resetRenderState();
    }

    private void disposeFonts() {
        if (regularFont != null) {
            regularFont.dispose();
            regularFont = null;
        }

        if (boldFont != null) {
            boldFont.dispose();
            boldFont = null;
        }
    }

    private void createParticles() {
        for (int i = 0; i < particleX.length; i++) {
            particleX[i] = random.nextFloat() * width;
            particleY[i] = random.nextFloat() * height;
            particleSpeed[i] = 0.15F + random.nextFloat() * 0.3F;
        }
    }

    // ============================================================
    // FIXED UI SCALE AND LAYOUT
    // ============================================================

    private int s(float value) {
        return Math.max(1, Math.round(value * UI_SCALE));
    }

    private float sf(float value) {
        return value * UI_SCALE;
    }

    private int getHeaderHeight() {
        return Math.min(height, Math.max(28, s(50)));
    }

    private int getFooterHeight() {
        return Math.min(
                Math.max(1, height - getHeaderHeight()),
                Math.max(36, s(48))
        );
    }

    private int getPanelWidth() {
        int availableWidth = Math.max(1, width - s(24));
        int preferredWidth = s(590);

        return Math.min(preferredWidth, availableWidth);
    }

    private int getPanelLeft() {
        return (width - getPanelWidth()) / 2;
    }

    private int getPanelTop() {
        return Math.min(
                height,
                getHeaderHeight() + s(9)
        );
    }

    private int getFooterTop() {
        return Math.max(
                getHeaderHeight(),
                height - getFooterHeight()
        );
    }

    private int getPanelBottom() {
        int panelTop = getPanelTop();
        int footerTop = getFooterTop();

        return Math.max(
                panelTop,
                footerTop - Math.min(s(8), Math.max(0, footerTop - panelTop))
        );
    }

    private int getContentLeft() {
        int inset = Math.min(
                s(17),
                Math.max(1, getPanelWidth() / 12)
        );

        return getPanelLeft() + inset;
    }

    private int getContentWidth() {
        int horizontalInset = Math.min(
                s(34),
                Math.max(2, getPanelWidth() / 6)
        );

        return Math.max(1, getPanelWidth() - horizontalInset);
    }

    private int getTabsY() {
        return getPanelTop() + s(5);
    }

    private int getTabsHeight() {
        return Math.max(16, s(31));
    }

    private int getContentTop() {
        return getTabsY() + getTabsHeight() + s(10);
    }

    private int getTabWidth() {
        return Math.max(1, getPanelWidth() / 3);
    }

    private int getButtonWidth() {
        int available = Math.max(2, width - s(40));
        int preferred = Math.max(70, s(124));
        int gap = getButtonGap();

        return Math.max(
                1,
                Math.min(preferred, (available - gap) / 2)
        );
    }

    private int getButtonHeight() {
        return Math.min(
                Math.max(1, getFooterHeight()),
                Math.max(20, s(28))
        );
    }

    private int getButtonGap() {
        return Math.max(4, s(10));
    }

    private int getButtonStartX() {
        int totalWidth = getButtonWidth() * 2 + getButtonGap();
        return (width - totalWidth) / 2;
    }

    private int getButtonY() {
        int footerTop = getFooterTop();
        int footerHeight = getFooterHeight();
        int buttonHeight = getButtonHeight();

        return footerTop + Math.max(
                0,
                (footerHeight - buttonHeight) / 2
        );
    }

    private int getAvailableContentHeight() {
        return Math.max(
                0,
                getPanelBottom() - getContentTop() - s(12)
        );
    }

    // ============================================================
    // GENERATION LAYOUT HELPERS
    // ============================================================

    private int getGenerationWorldTypeY() {
        return getContentTop() + s(isCustomWorldType() ? 38 : 48);
    }

    private int getGenerationWorldTypeHeight() {
        return Math.min(
                s(isCustomWorldType() ? 46 : 54),
                getAvailableContentHeight()
        );
    }

    private int getGenerationWorldTypeBottom() {
        return getGenerationWorldTypeY()
                + getGenerationWorldTypeHeight();
    }

    private int getGenerationGeneratorLabelY() {
        return getGenerationWorldTypeBottom()
                + s(isCustomWorldType() ? 3 : 6);
    }

    private int getGenerationGeneratorFieldY() {
        return getGenerationGeneratorLabelY()
                + s(isCustomWorldType() ? 11 : 13);
    }

    private int getGenerationGeneratorFieldHeight() {
        int remaining = Math.max(
                0,
                getPanelBottom()
                        - getGenerationGeneratorFieldY()
                        - s(8)
        );

        return Math.min(
                Math.max(12, s(isCustomWorldType() ? 25 : 29)),
                remaining
        );
    }

    private int getGenerationGeneratorDescriptionY() {
        return getGenerationGeneratorFieldY()
                + getGenerationGeneratorFieldHeight()
                + s(isCustomWorldType() ? 2 : 3);
    }

    private int getGenerationStructuresY() {
        int naturalY;

        if (!isCustomWorldType()) {
            naturalY = getGenerationWorldTypeBottom() + s(10);
        } else {
            naturalY = getGenerationGeneratorDescriptionY() + s(5);
        }

        int minimumHeight = Math.min(s(22), getAvailableContentHeight());
        int bottomPadding = s(8);

        int maximumY = Math.max(
                getGenerationWorldTypeBottom(),
                getPanelBottom() - minimumHeight - bottomPadding
        );

        return Math.max(
                getGenerationWorldTypeBottom(),
                Math.min(naturalY, maximumY)
        );
    }

    private int getGenerationStructuresHeight() {
        int available = Math.max(
                0,
                getPanelBottom()
                        - getGenerationStructuresY()
                        - s(8)
        );

        int preferred = s(isCustomWorldType() ? 46 : 54);

        return Math.min(preferred, available);
    }

    private int getGenerationInfoY() {
        return getGenerationStructuresY()
                + getGenerationStructuresHeight()
                + s(8);
    }

    private int getGenerationInfoHeight() {
        int available = Math.max(
                0,
                getPanelBottom()
                        - getGenerationInfoY()
                        - s(8)
        );

        return Math.min(s(48), available);
    }

    private void updateFieldLayout() {
        if (worldNameField == null) {
            return;
        }

        int left = getContentLeft();
        int contentWidth = getContentWidth();

        int fieldWidth = Math.max(
                1,
                contentWidth - s(12)
        );

        int fieldX = left + Math.min(s(10), Math.max(0, contentWidth / 10));

        int nameY = getContentTop() + s(62);
        int seedY = getContentTop() + s(116);

        int overviewFieldHeight = Math.max(12, s(29));

        worldNameField.xPosition = fieldX;
        worldNameField.yPosition = nameY;
        worldNameField.width = fieldWidth;
        worldNameField.height = overviewFieldHeight;

        seedField.xPosition = fieldX;
        seedField.yPosition = seedY;
        seedField.width = fieldWidth;
        seedField.height = overviewFieldHeight;

        generatorOptionsField.xPosition = left + s(6);
        generatorOptionsField.yPosition = getGenerationGeneratorFieldY();
        generatorOptionsField.width = Math.max(
                1,
                contentWidth - s(12)
        );
        generatorOptionsField.height = Math.max(
                12,
                getGenerationGeneratorFieldHeight()
        );
    }

    private void updateFieldVisibility() {
        if (worldNameField == null) {
            return;
        }

        if (selectedTab != TAB_OVERVIEW) {
            worldNameField.setFocused(false);
            seedField.setFocused(false);
        }

        if (selectedTab != TAB_GENERATION || !isCustomWorldType()) {
            generatorOptionsField.setFocused(false);
        }
    }

    // ============================================================
    // RENDER STATE
    // ============================================================

    private void resetRenderState() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);

        net.minecraft.client.renderer.GlStateManager.disableCull();
        net.minecraft.client.renderer.GlStateManager.shadeModel(GL11.GL_FLAT);
        net.minecraft.client.renderer.GlStateManager.color(1, 1, 1, 1);
        net.minecraft.client.renderer.GlStateManager.enableTexture2D();
        net.minecraft.client.renderer.GlStateManager.enableBlend();

        net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );
    }

    private void finishRenderState() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);

        net.minecraft.client.renderer.GlStateManager.disableCull();
        net.minecraft.client.renderer.GlStateManager.shadeModel(GL11.GL_FLAT);
        net.minecraft.client.renderer.GlStateManager.color(1, 1, 1, 1);
        net.minecraft.client.renderer.GlStateManager.enableTexture2D();
        net.minecraft.client.renderer.GlStateManager.disableBlend();
    }

    // ============================================================
    // BACKGROUND
    // ============================================================

    private void drawBackground() {
        drawGradientRect(
                0, 0, width, height,
                0xFF11111C, 0xFF080910
        );

        drawGlow(
                width * 0.5F,
                height * 0.25F,
                Math.min(width, height) * 0.42F,
                0x267B45D5
        );

        drawGlow(
                width * 0.87F,
                height * 0.78F,
                Math.min(width, height) * 0.30F,
                0x18506FEA
        );

        drawParticles();
    }

    private void drawParticles() {
        for (int i = 0; i < particleX.length; i++) {
            int alpha = 25 + (i % 5) * 8;
            int color = (alpha << 24)
                    | (i % 2 == 0 ? 0x9B78E8 : 0x648BFA);

            drawRect(
                    (int) particleX[i],
                    (int) particleY[i],
                    (int) particleX[i] + s(2),
                    (int) particleY[i] + s(2),
                    color
            );
        }
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

        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.disableTexture2D();

        net.minecraft.client.renderer.GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);

        net.minecraft.client.renderer.GlStateManager.color(
                red, green, blue, alpha
        );

        GL11.glVertex2f(centerX, centerY);

        net.minecraft.client.renderer.GlStateManager.color(
                red, green, blue, 0
        );

        for (int angle = 0; angle <= 360; angle += 10) {
            double radians = Math.toRadians(angle);

            GL11.glVertex2f(
                    centerX + (float) Math.cos(radians) * radius,
                    centerY + (float) Math.sin(radians) * radius
            );
        }

        GL11.glEnd();
        net.minecraft.client.renderer.GlStateManager.popMatrix();

        resetRenderState();
    }

    // ============================================================
    // SHAPES
    // ============================================================

    private void drawCircle(
            float centerX,
            float centerY,
            float radius,
            int color
    ) {
        float alpha = ((color >> 24) & 255) / 255.0F;
        float red = ((color >> 16) & 255) / 255.0F;
        float green = ((color >> 8) & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        net.minecraft.client.renderer.GlStateManager.pushMatrix();
        net.minecraft.client.renderer.GlStateManager.enableBlend();
        net.minecraft.client.renderer.GlStateManager.disableTexture2D();

        net.minecraft.client.renderer.GlStateManager.color(
                red, green, blue, alpha
        );

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(centerX, centerY);

        for (int angle = 0; angle <= 360; angle += 10) {
            double radians = Math.toRadians(angle);

            GL11.glVertex2f(
                    centerX + (float) Math.cos(radians) * radius,
                    centerY + (float) Math.sin(radians) * radius
            );
        }

        GL11.glEnd();
        net.minecraft.client.renderer.GlStateManager.popMatrix();

        resetRenderState();
    }

    private void drawRoundedRect(
            float x1,
            float y1,
            float x2,
            float y2,
            float radius,
            int color
    ) {
        radius = Math.min(
                radius,
                Math.min((x2 - x1) / 2.0F, (y2 - y1) / 2.0F)
        );

        if (radius <= 0) {
            drawRect((int) x1, (int) y1, (int) x2, (int) y2, color);
            return;
        }

        drawRect(
                (int) (x1 + radius),
                (int) y1,
                (int) (x2 - radius),
                (int) y2,
                color
        );

        drawRect(
                (int) x1,
                (int) (y1 + radius),
                (int) x2,
                (int) (y2 - radius),
                color
        );

        drawCircle(x1 + radius, y1 + radius, radius, color);
        drawCircle(x2 - radius, y1 + radius, radius, color);
        drawCircle(x1 + radius, y2 - radius, radius, color);
        drawCircle(x2 - radius, y2 - radius, radius, color);
    }

    // ============================================================
    // HEADER
    // ============================================================

    private void drawHeader() {
        int headerHeight = getHeaderHeight();

        drawRect(0, 0, width, headerHeight, 0xCC090A12);
        drawRect(0, headerHeight - 1, width, headerHeight, 0x443F3970);

        drawRect(s(18), s(13), s(20), s(36), PURPLE);

        boldFont.drawString(
                "ASTRYX",
                s(26),
                s(10),
                sf(0.19F),
                0xFFEDEAF8
        );

        regularFont.drawString(
                "Your game, your way.",
                s(26),
                s(29),
                sf(0.135F),
                0xFF85839C
        );

        drawTrackedCentered(
                "CREATE WORLD",
                width / 2,
                s(10),
                sf(0.165F),
                s(2),
                0xFFF4F0FF
        );

        regularFont.drawCenteredString(
                "CONFIGURE YOUR NEXT ADVENTURE",
                width / 2.0F,
                s(29),
                sf(0.12F),
                0xFF9995AD
        );

        drawRect(
                width - s(21),
                s(14),
                width - s(19),
                s(35),
                BLUE
        );
    }

    // ============================================================
    // PANEL AND TABS
    // ============================================================

    private void drawPanel(int mouseX, int mouseY) {
        int left = getPanelLeft();
        int top = getPanelTop();
        int right = left + getPanelWidth();
        int bottom = getPanelBottom();

        if (bottom <= top) {
            return;
        }

        drawRoundedRect(
                left, top, right, bottom,
                sf(6),
                0xE812121D
        );

        drawRect(
                left + s(6), top,
                right - s(6), top + s(1),
                0x667F69C8
        );

        drawRect(
                left, top + s(6),
                left + s(1), bottom - s(6),
                0x334D70D5
        );

        drawRect(
                left + s(1),
                getTabsY() + getTabsHeight() + s(4),
                right - s(1),
                getTabsY() + getTabsHeight() + s(5),
                0x443F3970
        );

        drawTabs(mouseX, mouseY);
    }

    private String[] getTabNames() {
        return new String[] {
                "OVERVIEW",
                "GAMEPLAY",
                "GENERATION"
        };
    }

    private void drawTabs(int mouseX, int mouseY) {
        String[] names = getTabNames();

        int panelLeft = getPanelLeft();
        int tabWidth = getTabWidth();
        int tabY = getTabsY();
        int tabHeight = getTabsHeight();

        for (int i = 0; i < names.length; i++) {
            int x = panelLeft + i * tabWidth;
            int currentWidth = i == names.length - 1
                    ? panelLeft + getPanelWidth() - x
                    : tabWidth;

            boolean selected = selectedTab == i;
            boolean hovered = inside(
                    mouseX, mouseY,
                    x, tabY, currentWidth, tabHeight
            );

            if (selected) {
                drawRoundedRect(
                        x + s(3),
                        tabY + s(2),
                        x + currentWidth - s(3),
                        tabY + tabHeight - s(2),
                        sf(4),
                        0xFF272239
                );

                drawRoundedRect(
                        x + s(12),
                        tabY + tabHeight - s(4),
                        x + currentWidth - s(12),
                        tabY + tabHeight - s(2),
                        sf(1),
                        PURPLE
                );
            } else if (hovered) {
                drawRoundedRect(
                        x + s(3),
                        tabY + s(2),
                        x + currentWidth - s(3),
                        tabY + tabHeight - s(2),
                        sf(4),
                        0xFF1D1B29
                );
            }

            regularFont.drawCenteredString(
                    names[i],
                    x + currentWidth / 2.0F,
                    tabY + s(10),
                    sf(0.145F),
                    selected ? 0xFFFFFFFF
                            : hovered ? 0xFFC9B9FF : 0xFF858198
            );
        }
    }

    private void selectTab(int tab) {
        if (tab < TAB_OVERVIEW || tab > TAB_GENERATION) {
            return;
        }

        selectedTab = tab;
        statusMessage = "";

        updateFieldVisibility();
        updateFieldLayout();
    }

    // ============================================================
    // OVERVIEW TAB
    // ============================================================

    private void drawOverview() {
        int left = getContentLeft();
        int top = getContentTop();
        int fieldWidth = getContentWidth();

        drawSectionTitle("WORLD IDENTITY", left, top);

        drawDescription(
                "Choose a name and an optional world seed.",
                left, top + s(19)
        );

        drawFieldLabel(
                "WORLD NAME",
                left, top + s(40)
        );

        drawInputBox(
                left,
                top + s(55),
                fieldWidth,
                Math.max(12, s(29)),
                worldNameField
        );

        drawFieldLabel(
                "WORLD SEED",
                left, top + s(94)
        );

        drawInputBox(
                left,
                top + s(109),
                fieldWidth,
                Math.max(12, s(29)),
                seedField
        );

        drawDescription(
                "Leave the seed empty to generate a random world.",
                left, top + s(145)
        );
    }

    // ============================================================
    // GAMEPLAY TAB
    // ============================================================

    private int getGameplayCardY() {
        return getContentTop() + s(48);
    }

    private int getGameplayCardHeight() {
        int available = getAvailableContentHeight();

        int remaining = Math.max(
                0,
                available - s(130)
        );

        return Math.min(
                s(76),
                Math.max(0, remaining / 2)
        );
    }

    private int getGameplayOptionsY() {
        return getGameplayCardY()
                + getGameplayCardHeight()
                + s(35);
    }

    private int getGameplayOptionHeight() {
        int remaining = Math.max(
                0,
                getPanelBottom()
                        - getGameplayOptionsY()
                        - s(8)
        );

        return Math.min(s(62), remaining);
    }

    private void drawGameplay() {
        int left = getContentLeft();
        int top = getContentTop();
        int contentWidth = getContentWidth();

        int gap = Math.max(2, s(7));
        int cardWidth = Math.max(1, (contentWidth - gap * 2) / 3);
        int cardY = getGameplayCardY();
        int cardHeight = getGameplayCardHeight();

        drawSectionTitle(
                "GAMEPLAY SETTINGS",
                left, top
        );

        drawDescription(
                "Choose the way you want to play.",
                left, top + s(19)
        );

        if (cardHeight > 0) {
            drawGameModeCard(
                    left,
                    cardY,
                    cardWidth,
                    cardHeight,
                    "SURVIVAL",
                    "Gather, craft and survive.",
                    "survival".equals(gameMode)
            );

            drawGameModeCard(
                    left + cardWidth + gap,
                    cardY,
                    cardWidth,
                    cardHeight,
                    "CREATIVE",
                    "Build without limits.",
                    "creative".equals(gameMode)
            );

            drawGameModeCard(
                    left + (cardWidth + gap) * 2,
                    cardY,
                    Math.max(1, contentWidth - (cardWidth + gap) * 2),
                    cardHeight,
                    "ADVENTURE",
                    "Explore and experience.",
                    "adventure".equals(gameMode)
            );
        }

        int optionsY = getGameplayOptionsY();
        int optionHeight = getGameplayOptionHeight();
        int optionWidth = Math.max(1, (contentWidth - gap) / 2);

        if (optionHeight > 0) {
            drawSectionTitle(
                    "WORLD OPTIONS",
                    left, optionsY - s(22)
            );

            drawOptionCard(
                    left,
                    optionsY,
                    optionWidth,
                    optionHeight,
                    "ALLOW CHEATS",
                    allowCheats ? "ENABLED" : "DISABLED",
                    "Commands in your world",
                    allowCheats
            );

            drawOptionCard(
                    left + optionWidth + gap,
                    optionsY,
                    Math.max(1, contentWidth - optionWidth - gap),
                    optionHeight,
                    "BONUS CHEST",
                    bonusChest ? "ENABLED" : "DISABLED",
                    "Starting items near spawn",
                    bonusChest
            );
        }
    }

    private void drawGameModeCard(
            int x,
            int y,
            int cardWidth,
            int cardHeight,
            String title,
            String description,
            boolean selected
    ) {
        if (cardWidth <= 0 || cardHeight <= 0) {
            return;
        }

        drawRoundedRect(
                x, y,
                x + cardWidth,
                y + cardHeight,
                sf(5),
                selected ? 0xFF29233D : 0xFF191824
        );

        drawRoundedRect(
                x + s(4),
                y,
                x + cardWidth - s(4),
                y + Math.min(s(2), cardHeight),
                sf(1),
                selected ? PURPLE : 0xFF454153
        );

        if (selected && cardHeight > s(10)) {
            drawRoundedRect(
                    x + s(7),
                    y + s(7),
                    x + s(10),
                    y + cardHeight - s(7),
                    sf(1),
                    PURPLE
            );
        }

        int textLeft = x + s(13);
        int textWidth = Math.max(1, cardWidth - s(20));

        boldFont.drawString(
                trimText(title, textWidth, sf(0.145F)),
                textLeft,
                y + Math.min(s(16), cardHeight),
                sf(0.145F),
                selected ? 0xFFD5C2FF : 0xFFEDEAF8
        );

        if (cardHeight >= s(65)) {
            regularFont.drawString(
                    trimText(description, textWidth, sf(0.115F)),
                    textLeft,
                    y + s(43),
                    sf(0.115F),
                    0xFF9290A7
            );
        }

        if (selected && cardHeight >= s(20)) {
            drawCircle(
                    x + cardWidth - s(12),
                    y + s(13),
                    sf(3),
                    PURPLE
            );
        }
    }

    private void setGameMode(String mode) {
        gameMode = mode;
        statusMessage = "";
    }

    // ============================================================
    // GENERATION TAB
    // ============================================================

    private void drawGeneration() {
        int left = getContentLeft();
        int top = getContentTop();
        int contentWidth = getContentWidth();

        drawSectionTitle(
                "WORLD GENERATION",
                left, top
        );

        drawDescription(
                "Configure how Minecraft builds your world.",
                left, top + s(19)
        );

        int worldTypeY = getGenerationWorldTypeY();
        int worldTypeHeight = getGenerationWorldTypeHeight();

        if (worldTypeHeight > 0) {
            drawOptionCard(
                    left,
                    worldTypeY,
                    contentWidth,
                    worldTypeHeight,
                    "WORLD TYPE",
                    getWorldTypeName(),
                    "Click to cycle available types",
                    true
            );
        }

        if (isCustomWorldType()
                && getGenerationGeneratorFieldHeight() > 0) {
            drawFieldLabel(
                    "GENERATOR OPTIONS",
                    left,
                    getGenerationGeneratorLabelY()
            );

            drawInputBox(
                    left,
                    getGenerationGeneratorFieldY(),
                    contentWidth,
                    getGenerationGeneratorFieldHeight(),
                    generatorOptionsField
            );
        }

        int structuresY = getGenerationStructuresY();
        int structuresHeight = getGenerationStructuresHeight();

        if (structuresHeight > 0) {
            drawOptionCard(
                    left,
                    structuresY,
                    contentWidth,
                    structuresHeight,
                    "GENERATE STRUCTURES",
                    generateStructures ? "ENABLED" : "DISABLED",
                    "Villages, temples and other structures",
                    generateStructures
            );
        }

        if (!isCustomWorldType()) {
            int infoY = getGenerationInfoY();
            int infoHeight = getGenerationInfoHeight();

            if (infoHeight > s(20)) {
                drawInfoCard(
                        left,
                        infoY,
                        contentWidth,
                        infoHeight,
                        "WORLD TYPE INFORMATION",
                        getWorldTypeDescription()
                );
            }
        }
    }

    private String getWorldTypeName() {
        WorldType type = getSelectedWorldType();

        if (type == null) {
            return "DEFAULT";
        }

        return type.getWorldTypeName().toUpperCase();
    }

    private String getWorldTypeDescription() {
        WorldType type = getSelectedWorldType();

        if (type == WorldType.FLAT) {
            return "Creates a flat world with custom terrain settings.";
        }

        if (type == WorldType.LARGE_BIOMES) {
            return "Generates larger biome regions.";
        }

        if (type == WorldType.AMPLIFIED) {
            return "Creates dramatic terrain and extreme mountains.";
        }

        if (type == WorldType.DEFAULT_1_1) {
            return "Uses the older 1.1 world-generation style.";
        }

        if (isCustomWorldType()) {
            return "Allows custom generator parameters.";
        }

        return "Uses standard Minecraft world generation.";
    }

    private WorldType getSelectedWorldType() {
        if (selectedWorldType < 0
                || selectedWorldType >= WorldType.worldTypes.length) {
            return WorldType.DEFAULT;
        }

        WorldType type = WorldType.worldTypes[selectedWorldType];

        return type == null ? WorldType.DEFAULT : type;
    }

    private boolean isCustomWorldType() {
        WorldType type = getSelectedWorldType();

        if (type == null) {
            return false;
        }

        String name = type.getWorldTypeName();

        return name != null
                && name.toLowerCase().startsWith("custom");
    }

    private void cycleWorldType() {
        int start = selectedWorldType;

        for (int i = 0; i < WorldType.worldTypes.length; i++) {
            selectedWorldType++;

            if (selectedWorldType >= WorldType.worldTypes.length) {
                selectedWorldType = 0;
            }

            WorldType type = WorldType.worldTypes[selectedWorldType];

            if (type != null && type.getCanBeCreated()) {
                generatorOptionsField.setFocused(false);
                updateFieldLayout();
                updateFieldVisibility();
                return;
            }
        }

        selectedWorldType = start;
    }

    // ============================================================
    // REUSABLE UI ELEMENTS
    // ============================================================

    private void drawSectionTitle(String text, int x, int y) {
        boldFont.drawString(
                text,
                x, y,
                sf(0.175F),
                0xFFEDEAF8
        );
    }

    private void drawDescription(String text, int x, int y) {
        regularFont.drawString(
                text,
                x, y,
                sf(0.135F),
                0xFF9290A7
        );
    }

    private void drawFieldLabel(String text, int x, int y) {
        boldFont.drawString(
                text,
                x, y,
                sf(0.135F),
                0xFFC6B9E8
        );
    }

    private void drawInputBox(
            int x,
            int y,
            int boxWidth,
            int boxHeight,
            GuiTextField field
    ) {
        if (boxWidth <= 0 || boxHeight <= 0) {
            return;
        }

        drawRoundedRect(
                x, y,
                x + boxWidth,
                y + boxHeight,
                sf(4),
                0xFF191824
        );

        drawRoundedRect(
                x + Math.min(s(4), boxWidth / 2),
                y,
                x + boxWidth - Math.min(s(4), boxWidth / 2),
                y + Math.min(s(1), boxHeight),
                sf(1),
                field.isFocused() ? PURPLE : 0xFF454153
        );

        field.drawTextBox();
    }

    private void drawOptionCard(
            int x,
            int y,
            int cardWidth,
            int cardHeight,
            String title,
            String value,
            String description,
            boolean enabled
    ) {
        if (cardWidth <= 0 || cardHeight <= 0) {
            return;
        }

        drawRoundedRect(
                x, y,
                x + cardWidth,
                y + cardHeight,
                sf(5),
                0xFF191824
        );

        drawRoundedRect(
                x + Math.min(s(4), cardWidth / 2),
                y,
                x + cardWidth - Math.min(s(4), cardWidth / 2),
                y + Math.min(s(1), cardHeight),
                sf(1),
                0xFF454153
        );

        drawRoundedRect(
                x,
                y + Math.min(s(4), cardHeight),
                x + Math.min(s(2), cardWidth),
                y + Math.max(1, cardHeight - s(4)),
                sf(1),
                enabled ? PURPLE : 0xFF454153
        );

        int textX = x + s(10);
        int textWidth = Math.max(1, cardWidth - s(18));

        regularFont.drawString(
                trimText(title, textWidth, sf(0.125F)),
                textX,
                y + Math.min(s(7), cardHeight),
                sf(0.125F),
                0xFF9290A7
        );

        if (cardHeight >= s(20)) {
            boldFont.drawString(
                    trimText(value, textWidth, sf(0.155F)),
                    textX,
                    y + Math.min(s(23), cardHeight),
                    sf(0.155F),
                    enabled ? 0xFFC9B5FF : 0xFFEDEAF8
            );
        }

        if (cardHeight >= s(54)) {
            regularFont.drawString(
                    trimText(description, textWidth, sf(0.11F)),
                    textX,
                    y + cardHeight - s(15),
                    sf(0.11F),
                    0xFF77758D
            );
        }
    }

    private void drawInfoCard(
            int x,
            int y,
            int cardWidth,
            int cardHeight,
            String title,
            String description
    ) {
        if (cardWidth <= 0 || cardHeight <= 0) {
            return;
        }

        drawRoundedRect(
                x, y,
                x + cardWidth,
                y + cardHeight,
                sf(5),
                0xCC171622
        );

        drawRoundedRect(
                x,
                y + Math.min(s(4), cardHeight),
                x + Math.min(s(2), cardWidth),
                y + Math.max(1, cardHeight - s(4)),
                sf(1),
                BLUE
        );

        int textWidth = Math.max(1, cardWidth - s(18));

        regularFont.drawString(
                trimText(title, textWidth, sf(0.125F)),
                x + s(10),
                y + Math.min(s(6), cardHeight),
                sf(0.125F),
                0xFFB9A7EF
        );

        if (cardHeight >= s(39)) {
            regularFont.drawString(
                    trimText(description, textWidth, sf(0.12F)),
                    x + s(10),
                    y + s(24),
                    sf(0.12F),
                    0xFFAAA7BA
            );
        }
    }

    private String trimText(
            String text,
            int maxWidth,
            float scale
    ) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        if (regularFont.getStringWidth(text, scale) <= maxWidth) {
            return text;
        }

        String trimmed = text;

        while (trimmed.length() > 3
                && regularFont.getStringWidth(trimmed + "...", scale) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return trimmed + "...";
    }

    // ============================================================
    // FOOTER AND BUTTONS
    // ============================================================

    private void drawFooter(int mouseX, int mouseY) {
        int top = getFooterTop();

        drawRect(0, top, width, height, 0xE8090A12);
        drawRect(0, top, width, Math.min(height, top + s(1)), 0x443F3970);

        if (!statusMessage.isEmpty()) {
            regularFont.drawCenteredString(
                    statusMessage,
                    width / 2.0F,
                    top + s(3),
                    sf(0.115F),
                    0xFFE78B9B
            );
        }

        int buttonWidth = getButtonWidth();
        int buttonHeight = getButtonHeight();
        int startX = getButtonStartX();
        int buttonY = getButtonY();

        drawButton(
                startX,
                buttonY,
                buttonWidth,
                buttonHeight,
                "CREATE WORLD",
                true,
                mouseX,
                mouseY
        );

        drawButton(
                startX + buttonWidth + getButtonGap(),
                buttonY,
                buttonWidth,
                buttonHeight,
                "BACK",
                false,
                mouseX,
                mouseY
        );
    }

    private void drawButton(
            int x,
            int y,
            int buttonWidth,
            int buttonHeight,
            String label,
            boolean primary,
            int mouseX,
            int mouseY
    ) {
        if (buttonWidth <= 0 || buttonHeight <= 0) {
            return;
        }

        boolean hovered = inside(
                mouseX, mouseY,
                x, y,
                buttonWidth, buttonHeight
        );

        float radius = Math.min(
                sf(5),
                Math.min(buttonWidth / 2.0F, buttonHeight / 2.0F)
        );

        if (primary) {
            drawRoundedRect(
                    x, y,
                    x + buttonWidth,
                    y + buttonHeight,
                    radius,
                    hovered ? 0xFF8D6CE5 : 0xFF7050C8
            );

            drawRoundedRect(
                    x + Math.min(s(1), buttonWidth / 2),
                    y + Math.min(s(1), buttonHeight / 2),
                    x + buttonWidth - Math.min(s(1), buttonWidth / 2),
                    y + buttonHeight - Math.min(s(1), buttonHeight / 2),
                    Math.max(0, radius - sf(1)),
                    hovered ? 0xFF8066DC : 0xFF624BC0
            );

            drawRoundedRect(
                    x + Math.min(s(5), buttonWidth / 2),
                    y + Math.min(s(1), buttonHeight / 2),
                    x + buttonWidth - Math.min(s(5), buttonWidth / 2),
                    y + Math.min(s(2), buttonHeight),
                    sf(1),
                    0x557F72C6
            );
        } else {
            drawRoundedRect(
                    x, y,
                    x + buttonWidth,
                    y + buttonHeight,
                    radius,
                    hovered ? 0xFF393448 : 0xFF252431
            );

            drawRoundedRect(
                    x + Math.min(s(1), buttonWidth / 2),
                    y + Math.min(s(1), buttonHeight / 2),
                    x + buttonWidth - Math.min(s(1), buttonWidth / 2),
                    y + buttonHeight - Math.min(s(1), buttonHeight / 2),
                    Math.max(0, radius - sf(1)),
                    hovered ? 0xFF302B40 : 0xFF1D1C27
            );
        }

        regularFont.drawCenteredString(
                label,
                x + buttonWidth / 2.0F,
                y + (buttonHeight - s(9)) / 2.0F,
                sf(0.14F),
                0xFFF3F0FF
        );
    }

    private boolean isCreateButton(int mouseX, int mouseY) {
        return inside(
                mouseX, mouseY,
                getButtonStartX(),
                getButtonY(),
                getButtonWidth(),
                getButtonHeight()
        );
    }

    private boolean isBackButton(int mouseX, int mouseY) {
        return inside(
                mouseX, mouseY,
                getButtonStartX() + getButtonWidth() + getButtonGap(),
                getButtonY(),
                getButtonWidth(),
                getButtonHeight()
        );
    }

    // ============================================================
    // WORLD CREATION
    // ============================================================

    private void createWorld() {
        if (creatingWorld) {
            return;
        }

        String worldName = worldNameField.getText().trim();

        if (worldName.isEmpty()) {
            statusMessage = "Please enter a world name.";
            selectTab(TAB_OVERVIEW);
            return;
        }

        WorldType worldType = getSelectedWorldType();

        if (worldType == null || !worldType.getCanBeCreated()) {
            statusMessage = "The selected world type cannot be created.";
            return;
        }

        creatingWorld = true;

        String saveName = createSaveDirectoryName(worldName);

        long seed = new Random().nextLong();
        String seedText = seedField.getText().trim();

        if (!seedText.isEmpty()) {
            try {
                long parsedSeed = Long.parseLong(seedText);

                if (parsedSeed != 0L) {
                    seed = parsedSeed;
                }
            } catch (NumberFormatException exception) {
                seed = seedText.hashCode();
            }
        }

        WorldSettings.GameType type =
                WorldSettings.GameType.getByName(gameMode);

        WorldSettings settings = new WorldSettings(
                seed,
                type,
                generateStructures,
                false,
                worldType
        );

        if (isCustomWorldType()) {
            settings.setWorldName(generatorOptionsField.getText());
        }

        if (bonusChest) {
            settings.enableBonusChest();
        }

        if (allowCheats) {
            settings.enableCommands();
        }

        mc.displayGuiScreen(null);

        mc.launchIntegratedServer(
                saveName,
                worldName,
                settings
        );
    }

    private String createSaveDirectoryName(String name) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            boolean allowed = false;

            for (char allowedCharacter : ChatAllowedCharacters.allowedCharactersArray) {
                if (character == allowedCharacter) {
                    allowed = true;
                    break;
                }
            }

            result.append(allowed ? character : '_');
        }

        String saveName = result.toString().trim()
                .replaceAll("[\\./\"]", "_");

        if (saveName.isEmpty()) {
            saveName = "World";
        }

        String[] restrictedNames = {
                "CON", "COM", "PRN", "AUX", "CLOCK$", "NUL",
                "COM1", "COM2", "COM3", "COM4", "COM5",
                "COM6", "COM7", "COM8", "COM9",
                "LPT1", "LPT2", "LPT3", "LPT4", "LPT5",
                "LPT6", "LPT7", "LPT8", "LPT9"
        };

        for (String restricted : restrictedNames) {
            if (saveName.equalsIgnoreCase(restricted)) {
                saveName = "_" + saveName + "_";
                break;
            }
        }

        while (mc.getSaveLoader().getWorldInfo(saveName) != null) {
            saveName += "-";
        }

        return saveName;
    }

    // ============================================================
    // INPUT
    // ============================================================

    @Override
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) throws IOException {
        if (mouseButton != 0) {
            return;
        }

        updateFieldLayout();

        if (isCreateButton(mouseX, mouseY)) {
            createWorld();
            return;
        }

        if (isBackButton(mouseX, mouseY)) {
            mc.displayGuiScreen(parent);
            return;
        }

        int panelLeft = getPanelLeft();
        int tabWidth = getTabWidth();
        int tabsY = getTabsY();
        int tabsHeight = getTabsHeight();

        if (mouseY >= tabsY
                && mouseY <= tabsY + tabsHeight
                && mouseX >= panelLeft
                && mouseX < panelLeft + getPanelWidth()) {

            int clickedTab = (mouseX - panelLeft) / tabWidth;
            selectTab(clickedTab);
            return;
        }

        int left = getContentLeft();
        int contentWidth = getContentWidth();

        if (selectedTab == TAB_OVERVIEW) {
            worldNameField.mouseClicked(mouseX, mouseY, mouseButton);
            seedField.mouseClicked(mouseX, mouseY, mouseButton);

            boolean nameClicked = inside(
                    mouseX, mouseY,
                    worldNameField.xPosition,
                    worldNameField.yPosition,
                    worldNameField.width,
                    worldNameField.height
            );

            boolean seedClicked = inside(
                    mouseX, mouseY,
                    seedField.xPosition,
                    seedField.yPosition,
                    seedField.width,
                    seedField.height
            );

            worldNameField.setFocused(nameClicked);
            seedField.setFocused(seedClicked);

            return;
        }

        if (selectedTab == TAB_GAMEPLAY) {
            int gap = Math.max(2, s(7));
            int cardWidth = Math.max(1, (contentWidth - gap * 2) / 3);
            int cardY = getGameplayCardY();
            int cardHeight = getGameplayCardHeight();

            if (cardHeight > 0 && inside(
                    mouseX, mouseY,
                    left, cardY,
                    cardWidth, cardHeight
            )) {
                setGameMode("survival");
                return;
            }

            if (cardHeight > 0 && inside(
                    mouseX, mouseY,
                    left + cardWidth + gap, cardY,
                    cardWidth, cardHeight
            )) {
                setGameMode("creative");
                return;
            }

            if (cardHeight > 0 && inside(
                    mouseX, mouseY,
                    left + (cardWidth + gap) * 2,
                    cardY,
                    contentWidth - (cardWidth + gap) * 2,
                    cardHeight
            )) {
                setGameMode("adventure");
                return;
            }

            int optionY = getGameplayOptionsY();
            int optionHeight = getGameplayOptionHeight();
            int optionWidth = Math.max(1, (contentWidth - gap) / 2);

            if (optionHeight > 0 && inside(
                    mouseX, mouseY,
                    left, optionY,
                    optionWidth, optionHeight
            )) {
                allowCheats = !allowCheats;
                return;
            }

            if (optionHeight > 0 && inside(
                    mouseX, mouseY,
                    left + optionWidth + gap, optionY,
                    contentWidth - optionWidth - gap, optionHeight
            )) {
                bonusChest = !bonusChest;
                return;
            }
        }

        if (selectedTab == TAB_GENERATION) {
            if (inside(
                    mouseX, mouseY,
                    left,
                    getGenerationWorldTypeY(),
                    contentWidth,
                    getGenerationWorldTypeHeight()
            )) {
                cycleWorldType();
                updateFieldVisibility();
                updateFieldLayout();
                return;
            }

            if (isCustomWorldType()) {
                generatorOptionsField.mouseClicked(
                        mouseX, mouseY, mouseButton
                );

                boolean fieldClicked = inside(
                        mouseX, mouseY,
                        generatorOptionsField.xPosition,
                        generatorOptionsField.yPosition,
                        generatorOptionsField.width,
                        generatorOptionsField.height
                );

                generatorOptionsField.setFocused(fieldClicked);
            } else {
                generatorOptionsField.setFocused(false);
            }

            if (getGenerationStructuresHeight() > 0 && inside(
                    mouseX, mouseY,
                    left,
                    getGenerationStructuresY(),
                    contentWidth,
                    getGenerationStructuresHeight()
            )) {
                generatorOptionsField.setFocused(false);
                generateStructures = !generateStructures;
            }
        }
    }

    private boolean inside(
            int mouseX,
            int mouseY,
            int x,
            int y,
            int boxWidth,
            int boxHeight
    ) {
        return mouseX >= x
                && mouseX < x + boxWidth
                && mouseY >= y
                && mouseY < y + boxHeight;
    }

    @Override
    protected void keyTyped(
            char typedChar,
            int keyCode
    ) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(parent);
            return;
        }

        if (keyCode == Keyboard.KEY_RETURN
                || keyCode == Keyboard.KEY_NUMPADENTER) {
            if (selectedTab == TAB_OVERVIEW) {
                createWorld();
            }

            return;
        }

        if (selectedTab == TAB_OVERVIEW) {
            if (worldNameField.isFocused()) {
                worldNameField.textboxKeyTyped(typedChar, keyCode);
            } else if (seedField.isFocused()) {
                seedField.textboxKeyTyped(typedChar, keyCode);
            }

            return;
        }

        if (selectedTab == TAB_GENERATION && isCustomWorldType()) {
            if (generatorOptionsField.isFocused()) {
                generatorOptionsField.textboxKeyTyped(typedChar, keyCode);
            }

            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    // ============================================================
    // UPDATE
    // ============================================================

    @Override
    public void updateScreen() {
        super.updateScreen();

        if (worldNameField != null) {
            worldNameField.updateCursorCounter();
            seedField.updateCursorCounter();
            generatorOptionsField.updateCursorCounter();

            updateFieldLayout();
        }

        long now = System.currentTimeMillis();

        float delta = Math.min(
                (now - lastFrame) / 16.0F,
                3.0F
        );

        lastFrame = now;
        animation += delta * 0.008F;

        for (int i = 0; i < particleX.length; i++) {
            particleY[i] -= particleSpeed[i] * delta;

            if (particleY[i] < -5) {
                particleY[i] = height + 5;
                particleX[i] = random.nextFloat() * width;
            }
        }
    }

    // ============================================================
    // TEXT HELPERS
    // ============================================================

    private void drawTrackedCentered(
            String text,
            int centerX,
            int y,
            float scale,
            int spacing,
            int color
    ) {
        float textWidth = boldFont.getStringWidthFloat(text, scale)
                + Math.max(0, text.length() - 1) * spacing;

        float currentX = centerX - textWidth / 2.0F;

        for (int i = 0; i < text.length(); i++) {
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
            ) + spacing;
        }
    }

    // ============================================================
    // MAIN RENDER
    // ============================================================

    @Override
    protected void drawAstryxScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        resetRenderState();

        updateFieldLayout();

        drawBackground();
        drawHeader();
        drawPanel(mouseX, mouseY);

        switch (selectedTab) {
            case TAB_OVERVIEW:
                drawOverview();
                break;

            case TAB_GAMEPLAY:
                drawGameplay();
                break;

            case TAB_GENERATION:
                drawGeneration();
                break;
        }

        drawFooter(mouseX, mouseY);

        finishRenderState();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}