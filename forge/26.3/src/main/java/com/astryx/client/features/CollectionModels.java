package com.astryx.client.features;

import java.util.*;
import com.astryx.client.features.CosmeticModels.*;

final class CollectionModels {
    static void cup(Builder b,int coffee,int cream){
        // A tapered ceramic mug, open dark coffee surface, handle and steam.
        int ceramic=0xFFF1E6D6;
        for(int n=0;n<20;n++){
            double t=n*Math.PI/10,next=(n+1)*Math.PI/10;
            b.quad(Math.cos(t)*1.2,-1.5,Math.sin(t)*1.2,Math.cos(next)*1.2,-1.5,Math.sin(next)*1.2,Math.cos(next)*.9,1.4,Math.sin(next)*.9,Math.cos(t)*.9,1.4,Math.sin(t)*.9,ceramic);
            b.triangle(0,-1.45,0,Math.cos(next)*1.09,-1.45,Math.sin(next)*1.09,Math.cos(t)*1.09,-1.45,Math.sin(t)*1.09,0xFF492A20);
            b.beam(Math.cos(t)*1.2,-1.5,Math.sin(t)*1.2,Math.cos(next)*1.2,-1.5,Math.sin(next)*1.2,.12,cream);
        }
        b.ring(1.65,-.2,.05,.72,1,.24,ceramic);
        b.plate(new double[]{-1.25,-1.6,1.25,-1.6,1.15,-1.25,-1.15,-1.25},.36,.16,0xFF492A20);
        b.beam(-.4,-1.8,.4,-.7,-2.6,.4,.1,cream);b.beam(-.7,-2.6,.4,-.3,-3.3,.4,.1,cream);
        b.beam(.5,-1.9,.4,.8,-2.6,.4,.1,cream);b.beam(.8,-2.6,.4,.5,-3.4,.4,.1,cream);
        b.gem(0,0,.6,.47,.72,coffee);b.beam(-.08,-.5,.72,.1,.5,.72,.08,cream);
    }
    static Model rock(CosmeticCatalog.Item item){
        List<Part> parts=new ArrayList<>();Builder b=new Builder();int c=item.primary,a=item.accent;
        switch(item.slot){
            case CLOAK:
                for(int row=0;row<5;row++)for(int column=0;column<3;column++)stone(b,-3.1+column*3.1,row*3.2+.9,0,1.25,1.15,.8,c,a,item.variant+row+column);
                parts.add(new Part(b,Anchor.BODY,Motion.CLOTH,0,0,2.7f,0,1));break;
            case HAT:
                for(int n=0;n<8;n++){double angle=n*Math.PI/4;stone(b,Math.cos(angle)*3.7,-8.2-Math.sin(n*2)*.5,Math.sin(angle)*3.7,1,1.2,1,c,a,n+item.variant);}parts.add(new Part(b,Anchor.HEAD,Motion.STILL,0,0,0,0,1));break;
            case BACKPACK:stone(b,0,6,5,1.8,2.1,1.7,c,a,item.variant);parts.add(new Part(b,Anchor.BODY,Motion.STILL,0,0,0,0,1));break;
            case TAIL:
                for(int n=0;n<8;n++)stone(b,Math.sin(n*.6)*.6,n*1.5,n*.65,Math.max(.25,.8-n*.07),.65,.6,c,a,n);parts.add(new Part(b,Anchor.BODY,Motion.TAIL,0,9,2.5f,0,1));break;
            case WRISTBANDS:
                for(Anchor arm:new Anchor[]{Anchor.LEFT_ARM,Anchor.RIGHT_ARM}){Builder cuff=new Builder();ExpandedCosmetics.wristStrap(cuff,c);for(int n=0;n<6;n++){double t=n*Math.PI/3;stone(cuff,Math.cos(t)*2.15,8.6,Math.sin(t)*2.15,.32,.42,.32,c,a,n);}parts.add(new Part(cuff,arm,Motion.STILL,0,0,0,0,1));}break;
            case EARRINGS:
                for(int side:new int[]{-1,1})stone(b,side*4.6,-1.8,0,.3,.6,.3,c,a,item.variant);parts.add(new Part(b,Anchor.HEAD,Motion.STILL,0,0,0,0,1));break;
            case BANDANA:
                for(int n=0;n<7;n++)stone(b,-3.6+n*1.2,-5.5,-4.35,.34,.4,.25,c,a,n);parts.add(new Part(b,Anchor.HEAD,Motion.STILL,0,0,0,0,1));break;
            case AURA:for(int n=0;n<7;n++){Builder mote=new Builder();stone(mote,0,0,0,.45,.55,.45,c,a,n);parts.add(new Part(mote,Anchor.BODY,Motion.ORBIT,0,0,0,n*.9f,1));}break;
            case MINI_ME:
                b.bind(Anchor.HEAD);CosmeticThemes.hood(b,c,a);stone(b,0,-9.5,0,1.5,1,1.4,c,a,item.variant);parts.add(new Part(b,Anchor.BODY,Motion.MINIATURE,6.25f,-4.67f,0,0,.18f));break;
            case BOOTS:for(Anchor anchor:new Anchor[]{Anchor.LEFT_LEG,Anchor.RIGHT_LEG}){Builder boot=new Builder();for(int n=0;n<4;n++)stone(boot,(n%2==0?-1:1),9.8,n<2?-1.8:1.8,.8,1.1,.85,c,a,n);parts.add(new Part(boot,anchor,Motion.STILL,0,0,0,0,1));}break;
            default:
                boolean head=item.slot==CosmeticCatalog.Slot.BANDANA||item.slot==CosmeticCatalog.Slot.EARRINGS;double y=head?-3.5:item.slot==CosmeticCatalog.Slot.BELT?10:1.7;
                int count=item.slot==CosmeticCatalog.Slot.EARRINGS?2:7;for(int n=0;n<count;n++){double x=count==2?(n==0?-4.6:4.6):-3.6+n*1.2;stone(b,x,y,-2.7,.4,.45,.35,c,a,n+item.variant);}parts.add(new Part(b,head?Anchor.HEAD:Anchor.BODY,item.slot==CosmeticCatalog.Slot.SCARF?Motion.SCARF:Motion.STILL,0,0,0,0,1));break;
        }
        return new Model(parts);
    }
    private static void stone(Builder target,double x,double y,double z,double sx,double sy,double sz,int c,int a,int seed){Builder piece=new Builder();DetailMesh.stone(piece,CosmeticModels.shade(c,.83f+(seed%4)*.09f),a);target.append(piece.mesh(),x,y,z,sx,sy,sz);}
    private CollectionModels(){}
}
