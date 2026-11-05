package com.astryx.client.core;

import com.astryx.client.gui.AstryxMultiplayer;
import com.astryx.client.gui.AstryxSelectWorld;
import com.astryx.client.gui.MainMenu;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiSelectWorld;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class MainMenuHandler {

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {

        if (event.gui == null) {
            return;
        }

        if (event.gui.getClass() == GuiMainMenu.class) {
            event.gui = new MainMenu();
            return;
        }

        if (event.gui.getClass() == GuiSelectWorld.class) {
            event.gui = new AstryxSelectWorld(new MainMenu());
            return;
        }

        if (event.gui.getClass() == GuiMultiplayer.class) {
            event.gui = new AstryxMultiplayer(new MainMenu());
        }
    }
}