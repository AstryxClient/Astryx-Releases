package com.astryx.modern;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
import java.util.Properties;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import com.mojang.blaze3d.platform.InputConstants;
import org.slf4j.LoggerFactory;

@Mod("astryx")
public final class ModernClient {
    static final Properties settings = new Properties();
    private static final Path path = FMLPaths.CONFIGDIR.get().resolve("astryx.properties");
    private static final ArrayDeque<Long> clicks = new ArrayDeque<>();
    public ModernClient() {
        if (Files.exists(path)) try (var in = Files.newInputStream(path)) {
            settings.load(in);
        } catch (IOException | IllegalArgumentException ex) {
            LoggerFactory.getLogger("Astryx").warn("Could not load HUD preferences", ex);
        }
        LoggerFactory.getLogger("Astryx").info("Astryx initialized");
    }
    static boolean enabled(String key) {
        return Boolean.parseBoolean(settings.getProperty(key, (key.equals("fps")||key.equals("ping")||key.equals("cps")||key.equals("coordinates")||key.equals("zoom"))?"true":"false"));
    }
    static void toggle(String key) {
        settings.setProperty(key, Boolean.toString(!enabled(key)));
        save();
    }
    static void save(){
        try {
            Files.createDirectories(path.getParent());
            Path temp = Files.createTempFile(path.getParent(), "astryx-", ".tmp");
            try {
                try (var out = Files.newOutputStream(temp)) { settings.store(out, "Astryx HUD"); }
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            } finally { Files.deleteIfExists(temp); }
        } catch (IOException ex) { LoggerFactory.getLogger("Astryx").warn("Could not save HUD preferences", ex); }
    }
    public static void rememberServer(Minecraft mc) {
        var server = mc.getCurrentServer();
        if (mc.level == null || mc.player == null || server == null || server.isLan() || server.isRealm()) return;
        if (server.ip.equals(settings.getProperty("server.address"))
            && server.name.equals(settings.getProperty("server.name"))) return;
        settings.setProperty("server.address", server.ip);
        settings.setProperty("server.name", server.name);
        save();
    }

    public static boolean zooming() {
        Minecraft mc = Minecraft.getInstance();
        return enabled("zoom") && mc.player != null && GameApi.screen(mc) == null
            && GameApi.zoomKey(mc);
    }
    public static void click() { trim(); clicks.addLast(System.nanoTime()); }
    private static void trim() {
        long cutoff = System.nanoTime() - 1_000_000_000L;
        while (!clicks.isEmpty() && clicks.peekFirst() < cutoff) clicks.removeFirst();
    }
    public static void hud(GuiGraphicsExtractor draw) {
        HudLayout.live(draw);
    }
    static int cps(){trim();return clicks.size();}
    static String resources(Minecraft mc){
        int iron=0,gold=0,diamonds=0,emeralds=0;
        var inventory=mc.player.getInventory();
        for(int i=0;i<inventory.getContainerSize();i++){
            var stack=inventory.getItem(i);
            if(stack.is(net.minecraft.world.item.Items.IRON_INGOT))iron+=stack.getCount();
            else if(stack.is(net.minecraft.world.item.Items.GOLD_INGOT))gold+=stack.getCount();
            else if(stack.is(net.minecraft.world.item.Items.DIAMOND))diamonds+=stack.getCount();
            else if(stack.is(net.minecraft.world.item.Items.EMERALD))emeralds+=stack.getCount();
        }
        return "Iron "+iron+"   Gold "+gold+"   Dia "+diamonds+"   Em "+emeralds;
    }
    private static void metric(StringBuilder line,String value) {
        if(line.length()>0)line.append("  ·  ");
        line.append(value);
    }
}
