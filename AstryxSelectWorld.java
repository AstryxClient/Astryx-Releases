package com.astryx.client.gui;

import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.storage.SaveFormatComparator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class AstryxSelectWorld extends GuiScreen implements GuiYesNoCallback {

    // Shared fixed scale used by Astryx menus.
    // Minecraft's GUI scale must not affect the size of this interface.
    private static final float UI_SCALE = 1.00F;

    private static final int PURPLE = 0xFF9B78E8;
    private static final int BLUE = 0xFF648BFA;

    private static final int BASE_CARD_HEIGHT = 58;
    private static final int BASE_CARD_GAP = 8;
    private static final int PARTICLE_COUNT = 32;

    private final GuiScreen parent;
    private final Random random = new Random();
    private final Particle[] particles = new Particle[PARTICLE_COUNT];

    private final DateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy");

    private final List<UiButton> buttons = new ArrayList<UiButton>();

    private List<SaveFormatComparator> worlds =
            new ArrayList<SaveFormatComparator>();

    private MainMenu.CustomFont regularFont;
    private MainMenu.CustomFont boldFont;

    private int selectedIndex = -1;
    private int listTop;
    private int listBottom;

    private float scrollOffset;
    private float animation;

    private long lastFrame;
    private long lastClickTime;

    private int lastClickedIndex = -1;

    private boolean confirmingDelete;

    public AstryxSelectWorld(GuiScreen parent) {
        this.parent = parent;
    }

    private int s(float value) {
        return Math.max(1, Math.round(value * UI_SCALE));
    }

    private float sf(float value) {
        return value * UI_SCALE;
    }

    private int getCardHeight() {
        return Math.max(30, s(BASE_CARD_HEIGHT));
    }

    private int getCardGap() {
        return Math.max(3, s(BASE_CARD_GAP));
    }

    private int getHeaderHeight() {
        return Math.max(32, s(57));
    }

    private int getFooterHeight() {
        return Math.max(65, s(77));
    }

    private int getFooterTop() {
        return Math.max(
                getHeaderHeight() + 30,
                height - getFooterHeight()
        );
    }

    @Override
    public void initGui() {
        lastFrame = System.currentTimeMillis();

        if (regularFont == null) {
            regularFont = new MainMenu.CustomFont(false);
            boldFont = new MainMenu.CustomFont(true);
        }

        listTop = Math.min(
                Math.max(getHeaderHeight() + s(12), s(42)),
                Math.max(0, height - 60)
        );

        listBottom = Math.max(
                listTop + 20,
                getFooterTop() - s(8)
        );

        scrollOffset = 0;
        selectedIndex = -1;
        lastClickedIndex = -1;
        lastClickTime = 0;

        createParticles();
        rebuildButtons();
        loadWorlds();

        resetRenderState();
    }

    @Override
    public void onGuiClosed() {
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
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles[i] = new Particle(
                    random.nextFloat() * width,
                    random.nextFloat() * height,
                    0.12f + random.nextFloat() * 0.2f,
                    0.08f + random.nextFloat() * 0.16f,
                    random.nextBoolean(),
                    0.4f + random.nextFloat() * 0.6f
            );
        }
    }

    private void loadWorlds() {
        try {
            worlds = mc.getSaveLoader().getSaveList();
            Collections.sort(worlds);
        } catch (AnvilConverterException exception) {
            exception.printStackTrace();

            mc.displayGuiScreen(
                    new net.minecraft.client.gui.GuiErrorScreen(
                            "Unable to load worlds",
                            exception.getMessage()
                    )
            );
        }
    }

    // ============================================================
    // RENDER STATE
    // ============================================================

    private void resetRenderState() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );
    }

    private void finishScreenRendering() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );
    }

    // ============================================================
    // WORLD DATA
    // ============================================================

    private boolean hasSelection() {
        return selectedIndex >= 0
                && selectedIndex < worlds.size();
    }

    private String getWorldName(int index) {
        if (index < 0 || index >= worlds.size()) {
            return "Unknown World";
        }

        String name = worlds.get(index).getDisplayName();

        if (name == null || name.trim().isEmpty()) {
            return "World " + (index + 1);
        }

        return name;
    }

    private int getListWidth() {
        int availableWidth = Math.max(1, width - s(24));

        return Math.min(
                availableWidth,
                Math.max(
                        Math.min(s(180), availableWidth),
                        Math.min(s(620), availableWidth)
                )
        );
    }

    private int getListLeft() {
        return (width - getListWidth()) / 2;
    }

    private int getMaximumScroll() {
        int contentHeight =
                worlds.size() * (getCardHeight() + getCardGap());

        return Math.max(
                0,
                contentHeight - (listBottom - listTop)
        );
    }

    private void clampScroll() {
        scrollOffset = Math.max(
                0,
                Math.min(getMaximumScroll(), scrollOffset)
        );
    }

    // ============================================================
    // BUTTON LAYOUT
    // ============================================================

    private void rebuildButtons() {
        buttons.clear();

        int center = width / 2;

        int buttonWidth = Math.min(
                Math.max(65, s(126)),
                Math.max(45, (width - s(30)) / 2 - s(5))
        );

        int buttonHeight = Math.max(19, s(25));
        int gap = Math.max(4, s(10));

        int footerTop = getFooterTop();

        if (hasSelection()) {
            int totalWidth = buttonWidth * 2 + gap;
            int startX = center - totalWidth / 2;

            int topButtonY = Math.max(
                    footerTop + s(23),
                    height - Math.max(47, s(53))
            );

            int bottomButtonY = height - Math.max(24, s(23));

            buttons.add(new UiButton(
                    1,
                    startX,
                    topButtonY,
                    buttonWidth,
                    buttonHeight,
                    "PLAY WORLD",
                    true
            ));

            buttons.add(new UiButton(
                    3,
                    startX + buttonWidth + gap,
                    topButtonY,
                    buttonWidth,
                    buttonHeight,
                    "CREATE WORLD",
                    false
            ));

            int smallButtonWidth = Math.max(48, s(86));
            int smallButtonHeight = Math.max(16, s(19));

            int smallGap = Math.max(4, s(10));
            int smallTotalWidth = smallButtonWidth * 2 + smallGap;

            buttons.add(new UiButton(
                    2,
                    center - smallTotalWidth / 2,
                    bottomButtonY,
                    smallButtonWidth,
                    smallButtonHeight,
                    "DELETE",
                    false
            ));

            buttons.add(new UiButton(
                    4,
                    center - smallTotalWidth / 2
                            + smallButtonWidth + smallGap,
                    bottomButtonY,
                    smallButtonWidth,
                    smallButtonHeight,
                    "BACK",
                    false
            ));
        } else {
            int totalWidth = buttonWidth * 2 + gap;
            int startX = center - totalWidth / 2;

            int buttonY = height - Math.max(38, s(43));

            buttons.add(new UiButton(
                    3,
                    startX,
                    buttonY,
                    buttonWidth,
                    buttonHeight,
                    "CREATE WORLD",
                    true
            ));

            buttons.add(new UiButton(
                    4,
                    startX + buttonWidth + gap,
                    buttonY,
                    buttonWidth,
                    buttonHeight,
                    "BACK",
                    false
            ));
        }
    }

    private boolean handleButtonClick(int mouseX, int mouseY) {
        for (UiButton button : buttons) {
            if (!button.contains(mouseX, mouseY)) {
                continue;
            }

            switch (button.id) {
                case 1:
                    launchWorld(selectedIndex);
                    return true;

                case 2:
                    deleteWorld();
                    return true;

                case 3:
                    createWorld();
                    return true;

                case 4:
                    mc.displayGuiScreen(parent);
                    return true;
            }
        }

        return false;
    }

    // ============================================================
    // WORLD ACTIONS
    // ============================================================

    private void launchWorld(int index) {
        if (index < 0 || index >= worlds.size()) {
            return;
        }

        SaveFormatComparator world = worlds.get(index);

        String fileName = world.getFileName();
        String displayName = getWorldName(index);

        if (mc.getSaveLoader().canLoadWorld(fileName)) {
            mc.displayGuiScreen(null);

            mc.launchIntegratedServer(
                    fileName,
                    displayName,
                    null
            );
        }
    }

    private void createWorld() {
        mc.displayGuiScreen(new AstryxCreateWorld(this));
    }

    private void deleteWorld() {
        if (!hasSelection()) {
            return;
        }

        confirmingDelete = true;

        String name = getWorldName(selectedIndex);

        mc.displayGuiScreen(
                new GuiYesNo(
                        this,
                        I18n.format("selectWorld.deleteQuestion"),
                        "'" + name + "' "
                                + I18n.format("selectWorld.deleteWarning"),
                        I18n.format("selectWorld.deleteButton"),
                        I18n.format("gui.cancel"),
                        selectedIndex
                )
        );
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        if (!confirmingDelete) {
            return;
        }

        confirmingDelete = false;

        if (result && id >= 0 && id < worlds.size()) {
            mc.getSaveLoader().flushCache();

            mc.getSaveLoader().deleteWorldDirectory(
                    worlds.get(id).getFileName()
            );
        }

        mc.displayGuiScreen(this);
    }

    // ============================================================
    // BUTTON RENDERING
    // ============================================================

    private void drawButtons(int mouseX, int mouseY) {
        for (UiButton button : buttons) {
            drawButton(button, mouseX, mouseY);
        }
    }

    private void drawButton(
            UiButton button,
            int mouseX,
            int mouseY
    ) {
        boolean hovered = button.contains(mouseX, mouseY);

        float radius = button.height >= s(22)
                ? sf(7.0F)
                : sf(5.0F);

        if (button.primary) {
            drawRoundedGradient(
                    button.x,
                    button.y,
                    button.width,
                    button.height,
                    radius,
                    hovered ? 0xFF9875EF : 0xFF7553D0,
                    hovered ? 0xFF7197FF : 0xFF4D70D5
            );

            drawRoundedOutline(
                    button.x,
                    button.y,
                    button.width,
                    button.height,
                    radius,
                    0x887F72C6
            );
        } else {
            drawRoundedRect(
                    button.x,
                    button.y,
                    button.width,
                    button.height,
                    radius,
                    hovered ? 0xFF292538 : 0xFF191923
            );

            drawRoundedOutline(
                    button.x,
                    button.y,
                    button.width,
                    button.height,
                    radius,
                    hovered ? 0xFF8B73C9 : 0xFF454153
            );
        }

        float textScale = 0.19F * UI_SCALE;

        float textY = button.y
                + (button.height - regularFont.getHeight(textScale)) / 2.0f;

        regularFont.drawCenteredString(
                button.label,
                button.x + button.width / 2.0f,
                textY,
                textScale,
                0xFFF3F0FF
        );
    }

    // ============================================================
    // WORLD LIST
    // ============================================================

    private void drawWorldList(int mouseX, int mouseY) {
        if (worlds.isEmpty()) {
            return;
        }

        clampScroll();

        int left = getListLeft();
        int listWidth = getListWidth();

        float scaleY = (float) mc.displayHeight / height;

        int scissorY = (int) ((height - listBottom) * scaleY);
        int scissorHeight =
                (int) ((listBottom - listTop) * scaleY);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        GL11.glScissor(
                0,
                scissorY,
                mc.displayWidth,
                scissorHeight
        );

        try {
            for (int i = 0; i < worlds.size(); i++) {
                int cardY = listTop
                        + i * (getCardHeight() + getCardGap())
                        - (int) scrollOffset;

                if (cardY + getCardHeight() < listTop
                        || cardY > listBottom) {
                    continue;
                }

                drawWorldCard(
                        i,
                        left,
                        cardY,
                        listWidth,
                        mouseX,
                        mouseY
                );
            }
        } finally {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            finishRendering();
        }

        drawScrollIndicator();
    }

    private void drawWorldCard(
            int index,
            int listLeft,
            int y,
            int listWidth,
            int mouseX,
            int mouseY
    ) {
        SaveFormatComparator world = worlds.get(index);

        int x = listLeft + s(5);
        int cardWidth = listWidth - s(17);
        int cardHeight = getCardHeight();

        boolean selected = selectedIndex == index;

        boolean hovered = isInside(
                mouseX,
                mouseY,
                x,
                y,
                cardWidth,
                cardHeight
        );

        drawRoundedRect(
                x,
                y,
                cardWidth,
                cardHeight,
                sf(8),
                selected
                        ? 0xEE29233F
                        : hovered
                        ? 0xDD211F30
                        : 0xCC15151F
        );

        drawRoundedOutline(
                x,
                y,
                cardWidth,
                cardHeight,
                sf(8),
                selected
                        ? PURPLE
                        : hovered
                        ? 0xFF77708F
                        : 0xFF363442
        );

        if (selected) {
            drawRoundedRect(
                    x + s(1),
                    y + s(9),
                    sf(3),
                    cardHeight - s(18),
                    sf(2),
                    PURPLE
            );
        }

        float nameScale = 0.205F * UI_SCALE;
        float detailScale = 0.145F * UI_SCALE;

        String name = trimText(
                getWorldName(index),
                cardWidth - s(30),
                nameScale,
                true
        );

        regularFont.drawString(
                name,
                x + s(15),
                y + s(7),
                nameScale,
                0xFFFFFFFF
        );

        String date = dateFormat.format(
                new Date(world.getLastTimePlayed())
        );

        String saveInfo = world.getFileName() + "  /  " + date;

        regularFont.drawString(
                trimText(saveInfo, cardWidth - s(30), detailScale, false),
                x + s(15),
                y + s(24),
                detailScale,
                0xFF9290A7
        );

        String mode;

        if (world.requiresConversion()) {
            mode = "NEEDS CONVERSION";
        } else if (world.isHardcoreModeEnabled()) {
            mode = "HARDCORE";
        } else {
            mode = world.getEnumGameType().getName().toUpperCase();
        }

        if (world.getCheatsEnabled()) {
            mode += "  /  CHEATS";
        }

        regularFont.drawString(
                mode,
                x + s(15),
                y + s(40),
                detailScale,
                world.isHardcoreModeEnabled()
                        ? 0xFFE77D8C
                        : 0xFFAA92E8
        );

        if (selected) {
            drawRightAligned(
                    "SELECTED",
                    x + cardWidth - s(12),
                    y + s(8),
                    0xFFC7B5FF,
                    detailScale
            );
        }
    }

    private void drawScrollIndicator() {
        int maximum = getMaximumScroll();

        if (maximum <= 0) {
            return;
        }

        int trackHeight = listBottom - listTop;

        int contentHeight =
                worlds.size() * (getCardHeight() + getCardGap());

        int thumbHeight = Math.max(
                Math.min(s(22), trackHeight),
                (int) ((float) trackHeight
                        * trackHeight / contentHeight)
        );

        int thumbY = listTop + (int) (
                (trackHeight - thumbHeight)
                        * (scrollOffset / maximum)
        );

        int x = getListLeft() + getListWidth() - s(5);

        drawRoundedRect(
                x,
                listTop,
                s(3),
                trackHeight,
                s(2),
                0x553F3C50
        );

        drawRoundedRect(
                x,
                thumbY,
                s(3),
                thumbHeight,
                s(2),
                0xFF8068C6
        );
    }

    // ============================================================
    // HEADER AND FOOTER
    // ============================================================

    private void drawHeader() {
        drawRect(0, 0, width, getHeaderHeight(), 0xCC090A12);
        drawRect(0, getHeaderHeight() - 1, width, getHeaderHeight(), 0x443F3970);

        drawRoundedRect(
                s(22),
                s(17),
                s(2),
                s(21),
                sf(1),
                PURPLE
        );

        boldFont.drawString(
                "ASTRYX",
                s(31),
                s(14),
                0.20F * UI_SCALE,
                0xFFEDEAF8
        );

        regularFont.drawString(
                "Your game, your way.",
                s(31),
                s(29),
                0.135F * UI_SCALE,
                0xFF85839C
        );

        drawTrackedCentered(
                "WORLD LIBRARY",
                width / 2,
                s(12),
                0.16F * UI_SCALE,
                s(2),
                0xFFF4F0FF,
                true
        );

        regularFont.drawCenteredString(
                worlds.size() + (
                        worlds.size() == 1
                                ? " SAVED WORLD"
                                : " SAVED WORLDS"
                ),
                width / 2.0f,
                s(31),
                0.14F * UI_SCALE,
                0xFF9995AD
        );

        drawRoundedRect(
                width - s(25),
                s(19),
                s(2),
                s(17),
                sf(1),
                BLUE
        );
    }

    private void drawFooter() {
        int top = getFooterTop();

        drawRect(0, top, width, height, 0xE8090A12);
        drawRect(0, top, width, top + 1, 0x443F3970);

        String status = hasSelection()
                ? "SELECTED  /  " + getWorldName(selectedIndex)
                : "SELECT A WORLD TO CONTINUE";

        float statusScale = 0.15F * UI_SCALE;

        regularFont.drawString(
                trimText(
                        status.toUpperCase(),
                        width - s(40),
                        statusScale,
                        false
                ),
                s(22),
                top + s(8),
                statusScale,
                hasSelection() ? 0xFFC9B9FF : 0xFF77758D
        );
    }

    private void drawEmptyState() {
        if (!worlds.isEmpty()) {
            return;
        }

        int centerY = (listTop + listBottom) / 2;

        drawTrackedCentered(
                "NO WORLDS FOUND",
                width / 2,
                centerY - s(12),
                0.19F * UI_SCALE,
                s(2),
                0xFFE5E0F3,
                true
        );

        regularFont.drawCenteredString(
                "Create a world to get started.",
                width / 2.0f,
                centerY + s(12),
                0.16F * UI_SCALE,
                0xFF88859D
        );
    }

    // ============================================================
    // TEXT HELPERS
    // ============================================================

    private String trimText(
            String text,
            int maxWidth,
            float scale,
            boolean bold
    ) {
        MainMenu.CustomFont font = bold ? boldFont : regularFont;

        if (maxWidth <= 0) {
            return "";
        }

        if (font.getStringWidth(text, scale) <= maxWidth) {
            return text;
        }

        while (text.length() > 3
                && font.getStringWidth(text + "...", scale) > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }

        return text + "...";
    }

    private void drawTrackedCentered(
            String text,
            int centerX,
            int y,
            float scale,
            int spacing,
            int color,
            boolean bold
    ) {
        MainMenu.CustomFont font = bold ? boldFont : regularFont;

        float textWidth = font.getStringWidthFloat(text, scale)
                + Math.max(0, text.length() - 1) * spacing;

        float currentX = centerX - textWidth / 2.0f;

        for (int i = 0; i < text.length(); i++) {
            String character = String.valueOf(text.charAt(i));

            font.drawString(
                    character,
                    currentX,
                    y,
                    scale,
                    color
            );

            currentX += font.getStringWidthFloat(character, scale)
                    + spacing;
        }
    }

    private void drawRightAligned(
            String text,
            int right,
            int y,
            int color,
            float scale
    ) {
        regularFont.drawString(
                text,
                right - regularFont.getStringWidth(text, scale),
                y,
                scale,
                color
        );
    }

    // ============================================================
    // BACKGROUND
    // ============================================================

    private void drawAstryxBackground() {
        drawGradientRect(
                0, 0, width, height,
                0xFF11111C, 0xFF080910
        );

        float radius = Math.min(width, height) * 0.45f;

        drawGlow(
                width * 0.48f,
                height * 0.29f,
                radius,
                0x267B45D5
        );

        drawGlow(
                width * 0.84f,
                height * 0.72f,
                radius * 0.75f,
                0x18506FEA
        );

        drawGlow(
                width * 0.15f,
                height * 0.74f,
                radius * 0.65f,
                0x155E3AB6
        );

        drawBackgroundGrid();
        drawParticles();
    }

    private void drawBackgroundGrid() {
        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glBegin(GL11.GL_LINES);

        for (int y = s(58); y < height - s(80); y += Math.max(1, s(42))) {
            float wave = (float) Math.sin(
                    animation * 0.35f + y * 0.015f
            );

            int alpha = 4 + (int) ((wave + 1) * 2);

            setGlColor((alpha << 24) | 0x7777A5);

            GL11.glVertex2f(0, y);
            GL11.glVertex2f(width, y);
        }

        for (int x = s(35); x < width; x += Math.max(1, s(55))) {
            setGlColor(0x067C79C5);

            GL11.glVertex2f(x, 0);
            GL11.glVertex2f(x, height);
        }

        GL11.glEnd();

        GlStateManager.popMatrix();
        finishRendering();
    }

    private void drawParticles() {
        for (Particle particle : particles) {
            int baseColor = particle.purple ? PURPLE : BLUE;
            int alpha = (int) (particle.opacity * 75);

            int color = (alpha << 24)
                    | (baseColor & 0xFFFFFF);

            int size = particle.opacity > 0.85f ? s(2) : s(1);

            drawRect(
                    (int) particle.x,
                    (int) particle.y,
                    (int) particle.x + size,
                    (int) particle.y + size,
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
        float alpha = ((color >> 24) & 255) / 255.0f;
        float red = ((color >> 16) & 255) / 255.0f;
        float green = ((color >> 8) & 255) / 255.0f;
        float blue = (color & 255) / 255.0f;

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();

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

        for (int angle = 0; angle <= 360; angle += 10) {
            double radians = Math.toRadians(angle);

            GL11.glVertex2f(
                    centerX + (float) Math.cos(radians) * radius,
                    centerY + (float) Math.sin(radians) * radius
            );
        }

        GL11.glEnd();

        GlStateManager.popMatrix();
        finishRendering();
    }

    // ============================================================
    // ROUNDED SHAPES
    // ============================================================

    private void drawRoundedRect(
            float x,
            float y,
            float boxWidth,
            float boxHeight,
            float radius,
            int color
    ) {
        if (boxWidth <= 0 || boxHeight <= 0) {
            return;
        }

        radius = Math.min(
                radius,
                Math.min(boxWidth / 2.0f, boxHeight / 2.0f)
        );

        float alpha = ((color >> 24) & 255) / 255.0f;
        float red = ((color >> 16) & 255) / 255.0f;
        float green = ((color >> 8) & 255) / 255.0f;
        float blue = (color & 255) / 255.0f;

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GlStateManager.color(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_POLYGON);

        for (int corner = 0; corner < 4; corner++) {
            float centerX;
            float centerY;
            int startAngle;

            if (corner == 0) {
                centerX = x + boxWidth - radius;
                centerY = y + radius;
                startAngle = -90;
            } else if (corner == 1) {
                centerX = x + boxWidth - radius;
                centerY = y + boxHeight - radius;
                startAngle = 0;
            } else if (corner == 2) {
                centerX = x + radius;
                centerY = y + boxHeight - radius;
                startAngle = 90;
            } else {
                centerX = x + radius;
                centerY = y + radius;
                startAngle = 180;
            }

            for (int angle = startAngle; angle <= startAngle + 90; angle += 6) {
                double radians = Math.toRadians(angle);

                GL11.glVertex2f(
                        centerX + (float) Math.cos(radians) * radius,
                        centerY + (float) Math.sin(radians) * radius
                );
            }
        }

        GL11.glEnd();

        GlStateManager.popMatrix();
        finishRendering();
    }

    private void drawRoundedOutline(
            float x,
            float y,
            float boxWidth,
            float boxHeight,
            float radius,
            int color
    ) {
        if (boxWidth <= 0 || boxHeight <= 0) {
            return;
        }

        radius = Math.min(
                radius,
                Math.min(boxWidth / 2.0f, boxHeight / 2.0f)
        );

        float alpha = ((color >> 24) & 255) / 255.0f;
        float red = ((color >> 16) & 255) / 255.0f;
        float green = ((color >> 8) & 255) / 255.0f;
        float blue = (color & 255) / 255.0f;

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GL11.glLineWidth(1.0f);

        GlStateManager.color(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_LINE_LOOP);

        for (int corner = 0; corner < 4; corner++) {
            float centerX;
            float centerY;
            int startAngle;

            if (corner == 0) {
                centerX = x + boxWidth - radius;
                centerY = y + radius;
                startAngle = -90;
            } else if (corner == 1) {
                centerX = x + boxWidth - radius;
                centerY = y + boxHeight - radius;
                startAngle = 0;
            } else if (corner == 2) {
                centerX = x + radius;
                centerY = y + boxHeight - radius;
                startAngle = 90;
            } else {
                centerX = x + radius;
                centerY = y + radius;
                startAngle = 180;
            }

            for (int angle = startAngle; angle < startAngle + 90; angle += 6) {
                double radians = Math.toRadians(angle);

                GL11.glVertex2f(
                        centerX + (float) Math.cos(radians) * radius,
                        centerY + (float) Math.sin(radians) * radius
                );
            }
        }

        GL11.glEnd();

        GlStateManager.popMatrix();
        finishRendering();
    }

    private void drawRoundedGradient(
            float x,
            float y,
            float boxWidth,
            float boxHeight,
            float radius,
            int leftColor,
            int rightColor
    ) {
        if (boxWidth <= 0 || boxHeight <= 0) {
            return;
        }

        radius = Math.min(
                radius,
                Math.min(boxWidth / 2.0f, boxHeight / 2.0f)
        );

        GlStateManager.pushMatrix();

        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );

        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        GL11.glBegin(GL11.GL_POLYGON);

        for (int corner = 0; corner < 4; corner++) {
            float cornerX;
            float cornerY;
            int startAngle;

            if (corner == 0) {
                cornerX = x + boxWidth - radius;
                cornerY = y + radius;
                startAngle = -90;
            } else if (corner == 1) {
                cornerX = x + boxWidth - radius;
                cornerY = y + boxHeight - radius;
                startAngle = 0;
            } else if (corner == 2) {
                cornerX = x + radius;
                cornerY = y + boxHeight - radius;
                startAngle = 90;
            } else {
                cornerX = x + radius;
                cornerY = y + radius;
                startAngle = 180;
            }

            for (int angle = startAngle; angle <= startAngle + 90; angle += 6) {
                double radians = Math.toRadians(angle);

                float vertexX = cornerX
                        + (float) Math.cos(radians) * radius;

                float vertexY = cornerY
                        + (float) Math.sin(radians) * radius;

                float progress = Math.max(
                        0,
                        Math.min(1, (vertexX - x) / boxWidth)
                );

                GlStateManager.color(
                        blendChannel(leftColor, rightColor, progress, 16),
                        blendChannel(leftColor, rightColor, progress, 8),
                        blendChannel(leftColor, rightColor, progress, 0),
                        1.0f
                );

                GL11.glVertex2f(vertexX, vertexY);
            }
        }

        GL11.glEnd();

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
        finishRendering();
    }

    private float blendChannel(
            int left,
            int right,
            float progress,
            int shift
    ) {
        float a = (left >> shift) & 255;
        float b = (right >> shift) & 255;

        return (a + (b - a) * progress) / 255.0f;
    }

    private void setGlColor(int color) {
        float alpha = ((color >> 24) & 255) / 255.0f;
        float red = ((color >> 16) & 255) / 255.0f;
        float green = ((color >> 8) & 255) / 255.0f;
        float blue = (color & 255) / 255.0f;

        GlStateManager.color(red, green, blue, alpha);
    }

    private void finishRendering() {
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    // ============================================================
    // INPUT
    // ============================================================

    private boolean isInside(
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
    protected void mouseClicked(
            int mouseX,
            int mouseY,
            int mouseButton
    ) throws IOException {
        if (mouseButton != 0) {
            return;
        }

        if (handleButtonClick(mouseX, mouseY)) {
            return;
        }

        if (mouseY < listTop || mouseY >= listBottom) {
            return;
        }

        int left = getListLeft() + s(5);
        int cardWidth = getListWidth() - s(17);

        if (mouseX < left || mouseX >= left + cardWidth) {
            return;
        }

        int relativeY = mouseY - listTop + (int) scrollOffset;

        int rowHeight = getCardHeight() + getCardGap();

        int index = relativeY / rowHeight;
        int withinCard = relativeY % rowHeight;

        if (withinCard >= getCardHeight()
                || index < 0
                || index >= worlds.size()) {
            return;
        }

        long now = System.currentTimeMillis();

        if (lastClickedIndex == index
                && now - lastClickTime < 350L) {
            selectedIndex = index;
            rebuildButtons();
            launchWorld(index);
            return;
        }

        selectedIndex = index;
        lastClickedIndex = index;
        lastClickTime = now;

        rebuildButtons();
    }

    @Override
    public void handleMouseInput() throws IOException {
        int wheel = Mouse.getDWheel();

        super.handleMouseInput();

        if (wheel != 0) {
            scrollOffset -= wheel > 0 ? s(42) : -s(42);
            clampScroll();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode)
            throws IOException {

        if (keyCode == 1) {
            mc.displayGuiScreen(parent);
            return;
        }

        if (keyCode == 28 || keyCode == 156) {
            if (hasSelection()) {
                launchWorld(selectedIndex);
            }

            return;
        }

        if (keyCode == 200 && !worlds.isEmpty()) {
            selectedIndex = Math.max(
                    0,
                    selectedIndex < 0 ? 0 : selectedIndex - 1
            );

            rebuildButtons();
            return;
        }

        if (keyCode == 208 && !worlds.isEmpty()) {
            selectedIndex = Math.min(
                    worlds.size() - 1,
                    selectedIndex < 0 ? 0 : selectedIndex + 1
            );

            rebuildButtons();
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    // ============================================================
    // ANIMATION
    // ============================================================

    @Override
    public void updateScreen() {
        super.updateScreen();

        long now = System.currentTimeMillis();

        float delta = Math.min(
                (now - lastFrame) / 16.0f,
                3.0f
        );

        lastFrame = now;
        animation += delta * 0.008f;

        for (Particle particle : particles) {
            particle.x += particle.speedX * delta;
            particle.y -= particle.speedY * delta;

            if (particle.y < -s(5)) {
                particle.y = height + s(5);
                particle.x = random.nextFloat() * width;
            }

            if (particle.x > width + s(5)) {
                particle.x = -s(5);
            }
        }
    }

    // ============================================================
    // MAIN DRAW
    // ============================================================

    @Override
    public void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        resetRenderState();

        drawAstryxBackground();
        drawHeader();
        drawWorldList(mouseX, mouseY);
        drawEmptyState();
        drawFooter();
        drawButtons(mouseX, mouseY);

        finishScreenRendering();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // ============================================================
    // DATA CLASSES
    // ============================================================

    private static class UiButton {
        final int id;
        final int x;
        final int y;
        final int width;
        final int height;
        final String label;
        final boolean primary;

        UiButton(
                int id,
                int x,
                int y,
                int width,
                int height,
                String label,
                boolean primary
        ) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.label = label;
            this.primary = primary;
        }

        boolean contains(int mouseX, int mouseY) {
            return mouseX >= x
                    && mouseX < x + width
                    && mouseY >= y
                    && mouseY < y + height;
        }
    }

    private static class Particle {
        float x;
        float y;
        float speedX;
        float speedY;
        boolean purple;
        float opacity;

        Particle(
                float x,
                float y,
                float speedX,
                float speedY,
                boolean purple,
                float opacity
        ) {
            this.x = x;
            this.y = y;
            this.speedX = speedX;
            this.speedY = speedY;
            this.purple = purple;
            this.opacity = opacity;
        }
    }
}