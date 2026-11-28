package com.astryx.modern;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class ResourceSettingsScreen extends Screen {
    private final Screen parent;
    ResourceSettingsScreen(Screen parent) {
        super(Component.literal("Bed Wars Resources"));
        this.parent = parent;
    }
    @Override protected void init() {
        int y = height / 2 - 65;
        for (var resource : BedwarsResources.values()) {
            addOption(resource.key(), resource.label, y);
            y += 23;
        }
        addOption("bedwars.names", "Show resource names", y);
        addRenderableWidget(Glass.action(width / 2 - 75, y + 27, 150, 20, "Done", this::onClose));
    }
    private void addOption(String key, String label, int y) {
        addRenderableWidget(new Glass(width / 2 - 75, y, 150, 20, label, "",
            () -> BedwarsResources.option(key) ? "On" : "Off", () -> {
                ModernClient.settings.setProperty(key, Boolean.toString(!BedwarsResources.option(key)));
                ModernClient.save();
            }));
    }
    @Override public void onClose() { GameApi.open(minecraft, parent); }
    @Override public void extractRenderState(GuiGraphicsExtractor c, int x, int y, float tick) {
        Glass.sky(c, width, height);
        c.centeredText(font, title, width / 2, height / 2 - 82, Glass.WHITE);
        for (var child : children()) if (child instanceof Glass g) g.extractRenderState(c, x, y, tick);
    }
}
