package com.astryx.modern;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class Glass extends Button {
    public static final int WHITE=0xfff4edf9, LILAC=0xffc5afe1, ACCENT=0xffb075fa;
    private static final Identifier SKY=Identifier.fromNamespaceAndPath("astryx","textures/gui/sky.png");
    private final String subtitle;
    private final java.util.function.Supplier<String> status;

    public Glass(int x,int y,int w,int h,String label,String subtitle,java.util.function.Supplier<String> status,Runnable click){
        super(x,y,w,h,net.minecraft.network.chat.Component.literal(label),button->click.run(),DEFAULT_NARRATION);
        this.subtitle=subtitle;this.status=status;
    }
    public static Glass action(int x,int y,int w,int h,String title,Runnable click){
        return new Glass(x,y,w,h,title,"",null,click);
    }
    @Override public void extractContents(GuiGraphicsExtractor canvas,int mouseX,int mouseY,float delta){
        var font=Minecraft.getInstance().font;
        int x=getX(),y=getY(),w=getWidth(),h=getHeight();
        boolean over=isHovered()||isFocused();
        panel(canvas,x,y,w,h,over?0xffb07cec:0xff664880,over?0xf43a2455:0xec241832);
        String state=status==null?"":status.get();
        int textX=subtitle.isEmpty()&&state.isEmpty()?x+(w-font.width(getMessage()))/2:x+8;
        canvas.text(font,getMessage(),textX,y+(subtitle.isEmpty()?(h-8)/2:6),active?WHITE:0xff8c8297);
        if(!subtitle.isEmpty())canvas.text(font,font.plainSubstrByWidth(subtitle,w-15),x+8,y+h-13,LILAC);
        if(!state.isEmpty()){
            boolean on=state.equals("On");
            int end=x+w-7;
            canvas.fill(end-17,y+6,end,y+14,on?0xff7949ab:0xff51475c);
            canvas.fill(on?end-7:end-15,y+8,on?end-2:end-10,y+12,on?0xfff5dfff:0xffbab1c6);
        }
    }
    public static void panel(GuiGraphicsExtractor c,int x,int y,int w,int h,int edge,int fill){
        c.fill(x+2,y,x+w-2,y+h,edge);c.fill(x,y+2,x+w,y+h-2,edge);
        c.fill(x+2,y+1,x+w-2,y+h-1,fill);c.fill(x+1,y+2,x+w-1,y+h-2,fill);
    }
    public static void sky(GuiGraphicsExtractor canvas,int w,int h){
        canvas.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED,SKY,0,0,0f,0f,w,h,w,h);
        canvas.fillGradient(0,0,w,h,0x50150924,0xc7100b1c);
    }
    public static void heading(GuiGraphicsExtractor c,String text,int x,int y,float scale){
        c.pose().pushMatrix();c.pose().translate(x,y);c.pose().scale(scale,scale);
        c.text(Minecraft.getInstance().font,text,0,0,WHITE);c.pose().popMatrix();
    }
}
