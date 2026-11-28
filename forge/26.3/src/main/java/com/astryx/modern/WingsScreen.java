package com.astryx.modern;

import com.astryx.client.features.CosmeticCatalog;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class WingsScreen extends Screen {
    private final Screen parent;
    private final List<CosmeticCatalog.Item> wings = CosmeticCatalog.ITEMS.stream()
        .filter(item -> item.slot == CosmeticCatalog.Slot.WINGS).toList();
    private int page;
    public WingsScreen(Screen parent) {
        super(Component.literal("Wings"));
        this.parent = parent;
    }
    @Override protected void init() {
        int rows = Math.max(1, Math.min(7, (height - 80)/30));
        int widthUsed = Math.min(400, width - 18);
        int column = widthUsed/2;
        int left = (width-widthUsed)/2;
        int top = (height-rows*30)/2;
        int count = rows*2;
        int pages = (wings.size()+count-1)/count;
        page = Math.min(page,pages-1);
        for (int i=0; i<count && page*count+i<wings.size(); i++) {
            var item = wings.get(page*count+i);
            String name = font.plainSubstrByWidth(item.name,column-37);
            addRenderableWidget(new Glass(left+column*(i%2),top+30*(i/2),column-3,28,name,item.theme,
                () -> equipped(item) ? "On" : "Off", () -> {
                    if (equipped(item)) ModernClient.settings.remove("wardrobe.wings");
                    else ModernClient.settings.setProperty("wardrobe.wings",item.id);
                    ModernClient.save();
                }));
        }
        int y = top+rows*30+4;
        var prev = addRenderableWidget(Glass.action(left,y,54,20,"‹ Prev",()->setPage(page-1)));
        prev.active = page>0;
        var next = addRenderableWidget(Glass.action(left+widthUsed-54,y,54,20,"Next ›",()->setPage(page+1)));
        next.active = page+1<pages;
        addRenderableWidget(Glass.action(width/2-35,y,70,20,"Done",this::onClose));
    }
    private boolean equipped(CosmeticCatalog.Item item) {
        return item.id.equals(ModernClient.settings.getProperty("wardrobe.wings"));
    }
    private void setPage(int next) { page=next; rebuildWidgets(); }
    @Override public void onClose() { GameApi.open(minecraft,parent); }
    @Override public void extractRenderState(GuiGraphicsExtractor c,int x,int y,float tick) {
        Glass.sky(c,width,height);
        c.centeredText(font,title,width/2,10,Glass.WHITE);
        for (var child : children()) if (child instanceof Glass g) g.extractRenderState(c,x,y,tick);
    }
}
