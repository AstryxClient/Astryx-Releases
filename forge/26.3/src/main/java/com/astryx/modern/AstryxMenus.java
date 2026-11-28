package com.astryx.modern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
public final class AstryxMenus {
    private AstryxMenus(){}
    private static void backdrop(GuiGraphicsExtractor c,int w,int h){
        Glass.sky(c,w,h);
        c.fillGradient(0,0,w,32,0xe627153e,0x95352358);
        c.fill(0,31,w,32,0xc19057d0);
        if(w>=320)c.text(net.minecraft.client.Minecraft.getInstance().font,"ASTRYX",10,11,Glass.LILAC);
        c.fillGradient(0,h-52,w,h,0x52190c2c,0xcc191020);
    }
    public static final class Servers extends JoinMultiplayerScreen {
        public Servers(Screen parent){super(parent);}
        @Override protected void init() {
            super.init();
            String address = ModernClient.settings.getProperty("server.address", "");
            String name = ModernClient.settings.getProperty("server.name", address);
            int buttonWidth = Math.min(145, width / 3);
            String label = "Join last" + (name.isBlank() ? "" : " · " + name);
            Glass last = Glass.action(width-buttonWidth-6, 5, buttonWidth, 20,
                font.plainSubstrByWidth(label, buttonWidth-10), () -> {
                    var saved = getServers().get(address);
                    join(saved != null ? saved : new net.minecraft.client.multiplayer.ServerData(
                        name, address, net.minecraft.client.multiplayer.ServerData.Type.OTHER));
                });
            last.active = !address.isBlank();
            addRenderableWidget(last);
        }
        @Override public void extractBackground(GuiGraphicsExtractor c,int mx,int my,float delta){backdrop(c,width,height);}
    }
    public static final class Worlds extends SelectWorldScreen {
        public Worlds(Screen parent){super(parent);}
        @Override public void extractBackground(GuiGraphicsExtractor c,int mx,int my,float delta){backdrop(c,width,height);}
    }
}