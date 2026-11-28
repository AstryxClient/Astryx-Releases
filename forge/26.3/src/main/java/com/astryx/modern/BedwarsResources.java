package com.astryx.modern;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

enum BedwarsResources {
    IRON("Iron", Items.IRON_INGOT, 32), GOLD("Gold", Items.GOLD_INGOT, 6),
    DIAMOND("Diamonds", Items.DIAMOND, 2), EMERALD("Emeralds", Items.EMERALD, 1);

    final String label;
    final ItemStack icon;
    final int sample;
    BedwarsResources(String label, Item item, int sample) {
        this.label = label;
        this.icon = new ItemStack(item);
        this.sample = sample;
    }
    String key() { return "bedwars." + name().toLowerCase(java.util.Locale.ROOT); }
    static boolean option(String key) {
        return Boolean.parseBoolean(ModernClient.settings.getProperty(key, "true"));
    }

    record Row(ItemStack icon, String text) {}
    record Layout(List<Row> rows, int width, int height) {
        void draw(GuiGraphicsExtractor canvas, Font font) {
            int y = 4;
            for (Row row : rows) {
                canvas.item(row.icon(), 5, y);
                canvas.text(font, row.text(), 26, y + 4, Glass.WHITE);
                y += 18;
            }
        }
    }

    static Layout measure(Minecraft mc) {
        List<Row> rows = new ArrayList<>();
        int width = 38;
        for (var resource : values()) {
            if (!option(resource.key())) continue;
            int count = mc.player == null ? resource.sample :
                mc.player.getInventory().countItem(resource.icon.getItem());
            String text = option("bedwars.names") ? resource.label + "  " + count : Integer.toString(count);
            width = Math.max(width, 32 + mc.font.width(text));
            rows.add(new Row(resource.icon, text));
        }
        return new Layout(rows, width, Math.max(20, 8 + rows.size() * 18));
    }
}
