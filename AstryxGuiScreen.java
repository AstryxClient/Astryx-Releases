package com.astryx.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

/**
 * Gives every Astryx menu the same virtual coordinate space, independent
 * of Minecraft's selected GUI scale.
 *
 * The virtual viewport is always equivalent to Minecraft GUI scale 2.
 * Rendering is transformed back into the real GUI projection, while
 * mouse input is converted into the same virtual coordinate space.
 */
public abstract class AstryxGuiScreen extends GuiScreen {

    protected static final float REFERENCE_GUI_SCALE = 2.0F;

    private float coordinateScale = 1.0F;
    private int actualWidth;
    private int actualHeight;
    private int actualScaleFactor = 2;

    @Override
    public void setWorldAndResolution(
            Minecraft mc,
            int width,
            int height
    ) {
        ScaledResolution resolution = new ScaledResolution(mc);

        actualScaleFactor = Math.max(1, resolution.getScaleFactor());
        actualWidth = width;
        actualHeight = height;

        coordinateScale = actualScaleFactor / REFERENCE_GUI_SCALE;

        int virtualWidth = Math.max(
                1,
                Math.round(width * coordinateScale)
        );

        int virtualHeight = Math.max(
                1,
                Math.round(height * coordinateScale)
        );

        super.setWorldAndResolution(mc, virtualWidth, virtualHeight);
    }

    /**
     * Rebuild the screen if the user changes GUI scale while it is open.
     */
    private void refreshViewportIfNeeded() {
        if (mc == null) {
            return;
        }

        ScaledResolution resolution = new ScaledResolution(mc);

        int currentScaleFactor = Math.max(
                1,
                resolution.getScaleFactor()
        );

        int currentWidth = resolution.getScaledWidth();
        int currentHeight = resolution.getScaledHeight();

        if (currentScaleFactor != actualScaleFactor
                || currentWidth != actualWidth
                || currentHeight != actualHeight) {
            setWorldAndResolution(
                    mc,
                    currentWidth,
                    currentHeight
            );
        }
    }

    @Override
    public final void drawScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        refreshViewportIfNeeded();

        int virtualMouseX = Math.round(mouseX * coordinateScale);
        int virtualMouseY = Math.round(mouseY * coordinateScale);

        float renderScale = 1.0F / coordinateScale;

        GL11.glPushMatrix();
        GL11.glScalef(renderScale, renderScale, 1.0F);

        try {
            drawAstryxScreen(
                    virtualMouseX,
                    virtualMouseY,
                    partialTicks
            );
        } finally {
            GL11.glPopMatrix();
        }
    }

    /**
     * Each menu implements its existing rendering here. Coordinates and
     * mouse positions are always in the fixed virtual viewport.
     */
    protected abstract void drawAstryxScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    );

    /**
     * Draws GuiScreen's normal button list from the fixed virtual viewport.
     */
    protected final void drawDefaultScreen(
            int mouseX,
            int mouseY,
            float partialTicks
    ) {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
