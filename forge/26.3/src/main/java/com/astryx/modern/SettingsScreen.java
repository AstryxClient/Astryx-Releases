package com.astryx.modern;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SettingsScreen extends Screen {
    private final Screen parent;
    private final List<Glass> buttons=new ArrayList<>();
    private int page,filter,perPage,pages,left,top,wide,tall;
    public SettingsScreen(){this(null);}
    public SettingsScreen(Screen parent){super(Component.literal("Astryx modules"));this.parent=parent;}
    @Override public void onClose(){GameApi.open(minecraft, parent);}
    private void add(Glass widget){buttons.add(widget);addRenderableWidget(widget);}
    private void rebuild(){clearWidgets();buttons.clear();init();}
    @Override protected void init(){
        buttons.clear();wide=Math.min(430,width-14);tall=Math.min(288,height-12);
        top=(height-tall)/2;left=(width-wide)/2;
        int rows=Math.max(1,(tall-100)/39);perPage=rows*2;
        List<String[]> modules=new ArrayList<>();
        for(HudElement e:HudElement.values())if(filter==0||filter==1||ModernClient.enabled(e.key))modules.add(new String[]{e.key,e.label,e.detail});
        if(filter==0||filter==2&&ModernClient.enabled("zoom"))modules.add(new String[]{"zoom","Zoom","Hold C for a closer view"});
        pages=Math.max(1,(modules.size()+perPage-1)/perPage);page=Math.min(page,pages-1);
        int right=left+wide,step=(wide-28)/2;
        add(Glass.action(right-107,top+8,69,19,"HUD editor",()->GameApi.open(minecraft, new HudEditor(this))));
        add(Glass.action(right-33,top+8,24,19,"×",this::onClose));
        String[] tabs={"All","HUD","Enabled"};
        for(int i=0;i<3;i++){final int index=i;add(Glass.action(left+11+60*i,top+36,56,18,(filter==i?"• ":"")+tabs[i],()->{filter=index;page=0;rebuild();}));}
        for(int i=0;i<perPage&&page*perPage+i<modules.size();i++){
            String[] m=modules.get(page*perPage+i);
            add(new Glass(left+11+(i%2)*(step+5),top+61+i/2*39,step,34,m[1],m[2],()->ModernClient.enabled(m[0])?"On":"Off",()->{ModernClient.toggle(m[0]);if(filter==2)rebuild();}));
        }
        if(pages>1){
            Glass prev=Glass.action(left+11,top+tall-25,49,18,"‹ Prev",()->{page--;rebuild();});prev.active=page>0;add(prev);
            Glass next=Glass.action(right-60,top+tall-25,49,18,"Next ›",()->{page++;rebuild();});next.active=page+1<pages;add(next);
        }
    }
    @Override public void extractRenderState(GuiGraphicsExtractor c,int mx,int my,float delta){
        if(minecraft.level==null)Glass.sky(c,width,height);else c.fill(0,0,width,height,0x93100a19);
        Glass.panel(c,left,top,wide,tall,0xff694682,0xf31b1228);
        Glass.heading(c,"Modules",left+12,top+10,1.65f);
        c.fill(left+11,top+30,left+wide-11,top+31,0xff49305f);
        if(pages>1)c.centeredText(font,(page+1)+" / "+pages,width/2,top+tall-20,Glass.LILAC);
        for(Glass b:buttons)b.extractRenderState(c,mx,my,delta);
    }
}
