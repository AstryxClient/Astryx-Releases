package com.astryx.modern;
import java.util.EnumMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

final class HudLayout {
    private static final EnumMap<HudElement,String> values=new EnumMap<>(HudElement.class);
    private static long refresh;
    static double number(String k,double fallback){
        try{double v=Double.parseDouble(ModernClient.settings.getProperty(k,""));return Double.isFinite(v)?v:fallback;}catch(NumberFormatException e){return fallback;}
    }
    static double clamp(double v,double low,double high){return Math.max(low,Math.min(v,high));}
    static final class Box{
        final int x,y,w,h;
        Box(int x,int y,int w,int h){this.x=x;this.y=y;this.w=w;this.h=h;}
        boolean hit(double px,double py){return px>=x&&px<x+w&&py>=y&&py<y+h;}
    }
    static Box paint(GuiGraphicsExtractor c,int sw,int sh,HudElement e,String text,boolean selected){
        var font=Minecraft.getInstance().font;
        float scale=(float)clamp(number(e.key+".scale",1),.65,2.5);
        int width=font.width(text)+12,height=16;
        int w=Math.round(width*scale),h=Math.round(height*scale);
        boolean right=ModernClient.settings.getProperty("right","false").equals("true");
        double dx=right?1:(e.ordinal()/8)*.34;
        double dy=clamp((5+18*(e.ordinal()%8))/(double)Math.max(1,sh-h),0,1);
        int x=(int)Math.round(clamp(number(e.key+".x",dx),0,1)*Math.max(0,sw-w));
        int y=(int)Math.round(clamp(number(e.key+".y",dy),0,1)*Math.max(0,sh-h));
        c.pose().pushMatrix();c.pose().translate(x,y);c.pose().scale(scale,scale);
        Glass.panel(c,0,0,width,height,selected?0xffedbaff:0x9d714d92,0xd51e142c);
        c.fill(1,3,3,height-3,Glass.ACCENT);c.text(font,text,7,4,Glass.WHITE);
        c.pose().popMatrix();return new Box(x,y,w,h);
    }
    static void live(GuiGraphicsExtractor c){
        Minecraft mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.gui.hud.isHidden()||mc.gui.screen()!=null)return;
        long now=System.nanoTime();
        if(now>=refresh){refresh=now+100_000_000;values.clear();for(HudElement e:HudElement.values())if(ModernClient.enabled(e.key))values.put(e,e.value(mc));}
        for(HudElement e:HudElement.values()){String text=values.get(e);if(text!=null&&!text.isEmpty()&&ModernClient.enabled(e.key))paint(c,c.guiWidth(),c.guiHeight(),e,text,false);}
    }
}
