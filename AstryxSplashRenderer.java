
package com.astryx.client.core.splash;

import net.minecraftforge.fml.common.ProgressManager;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class AstryxSplashRenderer {

    private static final int BAR_WIDTH = 400;
    private static final int BAR_HEIGHT = 8;

    private static final int TEXT_COLOR = 0xFFFFFF;

    private static boolean fontWarningDisplayed = false;

    private AstryxSplashRenderer() {
    }

    public static void render(
            Object splashRenderer,
            ProgressManager.ProgressBar progressBar
    ) {
        if (splashRenderer == null || progressBar == null) {
            return;
        }

        try {
            int width = Display.getWidth();
            int height = Display.getHeight();

            if (width <= 0 || height <= 0) {
                return;
            }

            String title = progressBar.getTitle();

            if (title == null || title.isEmpty()) {
                title = "Loading";
            }

            String message = progressBar.getMessage();

            String status = title;

            if (message != null && !message.isEmpty()) {
                status += " - " + message;
            }

            int steps = progressBar.getSteps();
            int step = progressBar.getStep();

            float progress = 0.0f;

            if (steps > 0) {
                progress = (step + 1.0f) / (steps + 1.0f);
            }

            progress = Math.max(0.0f, Math.min(1.0f, progress));

            int percentage = Math.round(progress * 100.0f);

            int x = (width - BAR_WIDTH) / 2;
            int y = (height / 2) + 115;

            // Draw status text.
            drawText(
                    status,
                    x,
                    y - 24,
                    TEXT_COLOR
            );

            // Draw percentage.
            drawText(
                    percentage + "%",
                    x + BAR_WIDTH - 35,
                    y - 24,
                    TEXT_COLOR
            );

            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);

            GL11.glEnable(GL11.GL_BLEND);

            GL11.glBlendFunc(
                    GL11.GL_SRC_ALPHA,
                    GL11.GL_ONE_MINUS_SRC_ALPHA
            );

            // Outer border.
            drawRectangle(
                    x - 2,
                    y - 2,
                    BAR_WIDTH + 4,
                    BAR_HEIGHT + 4,
                    0.71f,
                    0.48f,
                    1.0f,
                    1.0f
            );

            // Progress track.
            drawRectangle(
                    x,
                    y,
                    BAR_WIDTH,
                    BAR_HEIGHT,
                    0.188f,
                    0.125f,
                    0.251f,
                    1.0f
            );

            // Progress fill.
            int fillWidth = Math.round(BAR_WIDTH * progress);

            if (fillWidth > 0) {
                drawRectangle(
                        x,
                        y,
                        fillWidth,
                        BAR_HEIGHT,
                        0.753f,
                        0.518f,
                        0.988f,
                        1.0f
                );
            }

            GL11.glPopAttrib();

        } catch (Throwable throwable) {

            System.err.println(
                    "[Astryx] Error rendering custom splash bar."
            );

            throwable.printStackTrace();
        }
    }

    private static void drawRectangle(
            int x,
            int y,
            int width,
            int height,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        GL11.glColor4f(red, green, blue, alpha);

        GL11.glBegin(GL11.GL_QUADS);

        GL11.glVertex2f(x, y);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x, y + height);

        GL11.glEnd();
    }

    private static void drawText(
            String text,
            int x,
            int y,
            int color
    ) {
        try {
            Object fontRenderer = findFontRenderer();

            if (fontRenderer == null) {
                warnAboutFont();
                return;
            }

            Method drawString = findDrawStringMethod(
                    fontRenderer.getClass()
            );

            if (drawString == null) {
                warnAboutFont();
                return;
            }

            drawString.setAccessible(true);

            drawString.invoke(
                    fontRenderer,
                    text,
                    x,
                    y,
                    color
            );

        } catch (Throwable throwable) {
            warnAboutFont();
        }
    }

    private static Object findFontRenderer()
            throws IllegalAccessException, ClassNotFoundException {

        Class<?> splashProgressClass = Class.forName(
                "net.minecraftforge.fml.client.SplashProgress",
                false,
                AstryxSplashRenderer.class.getClassLoader()
        );

        Class<?> current = splashProgressClass;

        while (current != null) {

            for (Field field : current.getDeclaredFields()) {

                if (field.getType().getName().contains(
                        "SplashFontRenderer"
                )) {

                    if (!Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }

                    field.setAccessible(true);

                    return field.get(null);
                }
            }

            current = current.getSuperclass();
        }

        return null;
    }

    private static Method findDrawStringMethod(Class<?> type) {

        Class<?> current = type;

        while (current != null) {

            for (Method method : current.getDeclaredMethods()) {

                Class<?>[] parameters = method.getParameterTypes();

                if ("a".equals(method.getName())
                        && parameters.length == 4
                        && parameters[0] == String.class
                        && parameters[1] == int.class
                        && parameters[2] == int.class
                        && parameters[3] == int.class) {

                    return method;
                }
            }

            current = current.getSuperclass();
        }

        return null;
    }

    private static void warnAboutFont() {

        if (!fontWarningDisplayed) {

            fontWarningDisplayed = true;

            System.err.println(
                    "[Astryx] Could not access Forge's splash font renderer."
            );
        }
    }
}
