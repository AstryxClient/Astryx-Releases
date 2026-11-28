package com.astryx.modern;
import java.util.EnumMap;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HudEditor extends Screen {
    private final Screen parent;
    private final EnumMap<HudElement,HudLayout.Box> boxes=new EnumMap<>(HudElement.class);
    private HudElement selected;
    private double offsetX,offsetY;
    private boolean dragging,dirty;
    HudEditor(Screen parent){super(Component.literal("HUD editor"));this.parent=parent;}
    @Override public void onClose(){minecraft.gui.setScreen(parent);}
    @Override public void removed(){if(dirty)ModernClient.save();}
    @Override public void extractRenderState(GuiGraphicsExtractor c,int mx,int my,float delta){
        if(minecraft.level==null)Glass.sky(c,width,height);
        c.fill(0,0,width,height,0x75130d20);
        for(int x=width/2%16;x<width;x+=16)c.fill(x,0,x+1,height,0x1cbea4dc);
        for(int y=height/2%16;y<height;y+=16)c.fill(0,y,width,y+1,0x1cbea4dc);
        boxes.clear();
        for(HudElement e:HudElement.values())if(ModernClient.enabled(e.key)){
            String text=minecraft.player==null?e.sample:e.value(minecraft);
            boxes.put(e,HudLayout.paint(c,width,height,e,text.isEmpty()?e.sample:text,e==selected));
        }
        if(!dragging){
            String hint=selected==null?"Drag a module · Esc to finish":"Drag to move · + / − to resize · Esc to finish";
            int w=font.width(hint);
            c.fill((width-w)/2-6,height-17,(width+w)/2+6,height-3,0xdd21152e);
            c.centeredText(font,hint,width/2,height-14,Glass.LILAC);
        }
    }
    @Override public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent click,boolean twice){double x=click.x(),y=click.y();int button=click.button();
        if(button!=com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT)return false;
        HudElement[] list=HudElement.values();
        for(int i=list.length-1;i>=0;i--){var box=boxes.get(list[i]);if(box!=null&&box.hit(x,y)){
            selected=list[i];offsetX=x-box.x;offsetY=y-box.y;dragging=true;return true;
        }}return super.mouseClicked(click,twice);
    }
    @Override public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent click,double dx,double dy){double x=click.x(),y=click.y();int button=click.button();
        if(!dragging||selected==null||button!=com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT)return false;
        var box=boxes.get(selected);if(box==null)return false;
        put(".x",HudLayout.clamp((x-offsetX)/Math.max(1,width-box.w),0,1));
        put(".y",HudLayout.clamp((y-offsetY)/Math.max(1,height-box.h),0,1));return true;
    }
    private void put(String field,double value){ModernClient.settings.setProperty(selected.key+field,Double.toString(value));dirty=true;}
    @Override public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent click){int button=click.button();if(button==com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT&&dragging){dragging=false;return true;}return super.mouseReleased(click);}
    @Override public boolean keyPressed(net.minecraft.client.input.KeyEvent input){int key=input.key();
        if(selected!=null&&(key==46||key==45||key==87||key==86)){
            put(".scale",HudLayout.clamp(HudLayout.number(selected.key+".scale",1)+(key==46||key==87?.1:-.1),.65,2.5));return true;
        }
        return super.keyPressed(input);
    }
}
