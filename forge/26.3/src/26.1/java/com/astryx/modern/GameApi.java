package com.astryx.modern;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;

public final class GameApi {
    private GameApi() {}

    public static Screen screen(Minecraft mc) {
        return mc.screen;
    }

    public static Screen options(Minecraft mc, Screen parent) {
        return new OptionsScreen(parent, mc.options, false);
    }

    public static boolean hidden(Minecraft mc) {
        return mc.options.hideGui;
    }

    public static boolean singleplayer(Minecraft mc) {
        return mc.isSingleplayer();
    }

    public static boolean zoomKey(Minecraft mc) {
        return InputConstants.isKeyDown(mc.getWindow(), InputConstants.KEY_C);
    }

    public static void open(Minecraft mc, Screen next) {
        mc.setScreen(next);
    }

    public static double scaleStep(int key) {
        if (key == InputConstants.KEY_EQUALS || key == InputConstants.KEY_ADD) return 0.1;
        if (key == InputConstants.KEY_MINUS || key == 333) return -0.1;
        return 0;
    }
}
