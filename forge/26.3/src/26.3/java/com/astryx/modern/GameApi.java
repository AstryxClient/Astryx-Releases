package com.astryx.modern;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;

public final class GameApi {
    private GameApi() {}

    public static Screen screen(Minecraft mc) {
        return mc.gui.screen();
    }

    public static Screen options(Minecraft mc, Screen parent) {
        return new OptionsScreen(parent, mc.options);
    }

    public static boolean hidden(Minecraft mc) {
        return mc.gui.hud.isHidden();
    }

    public static boolean singleplayer(Minecraft mc) {
        return mc.hasSingleplayerServer();
    }

    public static boolean zoomKey(Minecraft mc) {
        return InputConstants.isKeyDown(InputConstants.KEY_C);
    }

    public static void open(Minecraft mc, Screen next) {
        mc.gui.setScreen(next);
    }

    public static double scaleStep(int key) {
        if (key == InputConstants.KEY_EQUALS || key == InputConstants.KEY_ADD) return 0.1;
        if (key == InputConstants.KEY_MINUS || key == 86) return -0.1;
        return 0;
    }
}
