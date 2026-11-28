package com.astryx.client.features;

import java.util.*;
import com.astryx.client.features.CosmeticModels.*;

/** Shared authored details keep beds, shields and emblems consistent at every accessory size. */
final class CosmeticThemes {
    static final int WHITE=0xFFF1EBDD,OAK=0xFF997345,GOLD=0xFFE2BD65,IRON=0xFFB9C6D3,DARK=0xFF18152A,SILVER=0xFFD4D9EC,PURPLE=0xFF955DDA;
    static void add(List<Part> parts,Builder b,Anchor anchor,Motion motion,float x,float y,float z,float phase,float scale){parts.add(new Part(b,anchor,motion,x,y,z,phase,scale));}
    static Model single(Builder b,Anchor anchor){List<Part> parts=new ArrayList<Part>();add(parts,b,anchor,Motion.STILL,0,0,0,0,1);return new Model(parts);}
    static void bedIcon(Builder b,double x,double y,double z,double scale,boolean front,int red){
        Builder icon=new Builder();
        // A side elevation: white pillow at the head, blanket to the right, timber below and two legs.
        icon.box(-3.6,-1.4,0,7.2,2,.3,red);icon.box(-3.6,-1.4,.32,1.9,1.65,.15,WHITE);
        icon.box(-3.9,.65,0,7.8,.45,.5,OAK);icon.box(-3.4,1.1,0,.55,1.2,.5,OAK);icon.box(2.85,1.1,0,.55,1.2,.5,OAK);
        icon.box(-4.05,-1.7,0,.4,2.8,.5,OAK);
        b.append(icon.mesh(),x,y,z,scale,scale,front?-scale:scale);
    }
    static void uprightBed(Builder b,int color){
        b.box(-3.3,0,0,6.6,12.6,.65,OAK);b.box(-3,.2,.65,6,12,1.15,WHITE);
        b.box(-3,3.05,1.82,6,9.15,.25,color);b.box(-2.6,.65,1.83,5.2,2.1,.5,WHITE);
        b.box(-3.5,-.1,-.2,.45,12.8,1.2,OAK);b.box(3.05,-.1,-.2,.45,12.8,1.2,OAK);
        b.box(-3.5,-.35,-.2,7,.65,1.25,OAK);b.box(-3.5,12.1,-.2,7,.55,1.25,OAK);
        for(int side:new int[]{-1,1})for(int end:new int[]{0,1})b.box(side<0?-3.35:2.7,end==0?.2:11.5,-1.4,.65,.65,1.5,OAK);
        b.box(-2.9,3,2.08,5.8,.22,.1,0xFFE39495);
        for(int row=0;row<3;row++)b.box(-2.7,5+row*2,2.08,5.4,.09,.08,0xFF8D2937);
    }
    static void shield(Builder b,double x,double y,double z,double scale,int color,int trim){
        Builder icon=new Builder();icon.plate(new double[]{-2,-2,2,-2,1.8,1,0,2.6,-1.8,1},0,.35,trim);
        icon.plate(new double[]{-1.55,-1.55,1.55,-1.55,1.3,.8,0,2,-1.3,.8},.37,.18,color);
        icon.beam(0,-1.4,.65,0,1.6,.65,.24,trim);icon.beam(-1.3,-.2,.65,1.3,-.2,.65,.24,trim);
        b.append(icon.mesh(),x,y,z,scale,scale,scale);
    }
    static void star(Builder b,double x,double y,double z,double r,int color){
        b.plate(new double[]{x,y-r,x+r*.25,y-r*.25,x+r,y,x+r*.25,y+r*.25,x,y+r,x-r*.25,y+r*.25,x-r,y,x-r*.25,y-r*.25},z,.14,color);
        b.gem(x,y,z+.2,r*.22,r*.4,WHITE);
    }
    static void crescent(Builder b,double x,double y,double z,double r,int color){
        b.plate(new double[]{x+.4*r,y-1.3*r,x-.65*r,y-1.05*r,x-1.2*r,y-.3*r,x-r,y+.7*r,x-.3*r,y+1.3*r,x+.6*r,y+1.2*r,x+.2*r,y+.7*r,x-.15*r,y,x+.05*r,y-.7*r},z,.18,color);
    }
    static void logo(Builder b,double x,double y,double z,double scale,boolean front,int color){
        Builder icon=new Builder();icon.beam(-1.6,1.8,0,0,-1.9,0,.3,SILVER);icon.beam(0,-1.9,0,1.6,1.8,0,.3,SILVER);
        icon.beam(-.85,.5,0,.85,.5,0,.22,SILVER);icon.beam(-1.6,1.8,0,0,1.2,0,.23,color);icon.beam(0,1.2,0,1.6,1.8,0,.23,color);
        icon.gem(0,.1,.2,.38,.75,color);b.append(icon.mesh(),x,y,z,scale,scale,front?-scale:scale);
    }
    static void band(Builder b,int color,int trim){
        b.box(-4.3,-6.75,-4.3,8.6,1.6,.3,color);b.box(-4.3,-6.75,4,8.6,1.6,.3,color);
        b.box(-4.3,-6.75,-4.3,.3,1.6,8.6,color);b.box(4,-6.75,-4.3,.3,1.6,8.6,color);
        for(double yy:new double[]{-6.74,-5.34}){b.box(-4.3,yy,-4.34,8.6,.14,.1,trim);b.box(-4.3,yy,4.24,8.6,.14,.1,trim);}
        b.box(2,-6.4,4.2,1.5,1.3,.8,color);b.plate(new double[]{2.1,-5.2,3.2,-5.2,3.7,-1.6,2.5,-2.2},4.7,.18,color);
        b.plate(new double[]{2,-5.2,2.7,-5.2,1.3,-2.7,.5,-3.4},4.9,.18,trim);
    }
    static void harness(Builder b,int color){
        for(int s:new int[]{-1,1}){double x=s<0?-3.1:2.45;b.box(x,.2,-2.4,.65,6.5,.3,color);b.box(x,-.15,-2.4,.65,.55,5,color);b.box(x,.2,2.3,.65,7,.35,color);}
    }
    static void chain(Builder b,int color){
        b.beam(-3,.1,-2.5,-1.5,2.2,-2.7,.2,color);b.beam(-1.5,2.2,-2.7,0,2.8,-2.8,.2,color);
        b.beam(3,.1,-2.5,1.5,2.2,-2.7,.2,color);b.beam(1.5,2.2,-2.7,0,2.8,-2.8,.2,color);
        b.box(-3,-.15,-2.5,.2,.3,5,color);b.box(2.8,-.15,-2.5,.2,.3,5,color);
    }
    static void bootShell(Builder b,int color,int trim){
        b.plate(new double[]{-2,10.7,-1.7,10,1.7,10,2,10.7,1.8,11.5,-1.8,11.5},-3.08,.3,color);
        b.box(-1.1,8,-2.65,2.2,2.9,.3,CosmeticModels.shade(color,1.18f));
        for(int row=0;row<3;row++){
            b.beam(-.9,8.5+row*.65,-2.98,.9,8.9+row*.65,-2.98,.12,trim);
            b.beam(.9,8.5+row*.65,-2.98,-.9,8.9+row*.65,-2.98,.12,trim);
            for(int side:new int[]{-1,1})b.gem(side*1.3,8.6+row*.65,-2.74,.13,.15,trim);
        }
        for(int n=0;n<4;n++)b.box(-2.22+n*1.15,12,-2.8,.75,.15,5.1,CosmeticModels.shade(trim,.65f));
        b.box(-2.22,8.2,-2.28,4.44,3.3,.25,color);b.box(-2.22,8.2,2.03,4.44,3.3,.25,color);
        b.box(-2.22,8.2,-2.28,.25,3.3,4.56,color);b.box(1.97,8.2,-2.28,.25,3.3,4.56,color);
        b.box(-2.3,11.5,-2.75,4.6,.55,5.05,trim);b.box(-2.2,10.8,-2.75,4.4,.7,.47,color);
        b.box(-2.3,7.7,-2.35,4.6,.5,.3,trim);b.box(-2.3,7.7,2.05,4.6,.5,.3,trim);
    }
    static void hood(Builder b,int color,int trim){
        b.box(-4.45,-9,-4.45,8.9,.65,8.9,color);b.box(-4.45,-8.35,4,8.9,8.7,.45,color);
        b.box(-4.45,-8.35,-4.45,.45,8.7,8.45,color);b.box(4,-8.35,-4.45,.45,8.7,8.45,color);
        b.box(-4.55,-8.65,-4.65,9.1,.3,.3,trim);b.beam(-4.55,-8.5,-4.6,-4.35,-.3,-4.6,.2,trim);b.beam(4.55,-8.5,-4.6,4.35,-.3,-4.6,.2,trim);
    }
    static void vessel(Builder b){
        harness(b,DARK);b.plate(new double[]{-2.7,1.5,2.7,1.5,3.5,5,2.5,10,-2.5,10,-3.5,5},2.7,2.7,DARK);
        b.plate(new double[]{-1.6,2.2,1.6,2.2,2.2,6,1.3,9,-1.3,9,-2.2,6},5.45,.3,PURPLE);
        for(int side:new int[]{-1,1}){double xx=side*2.1;b.ring(xx,8.6,6.2,.95,1.25,.32,SILVER);b.gem(xx,8.6,6.25,.55,.8,0xFF9A87F2);
            b.plate(new double[]{side*2,3,side*4.2,6,side*3.5,9,side*2.2,7},4.2,.35,SILVER);}
        logo(b,0,4.7,5.9,.85,false,0xFFB992FA);b.box(-1.5,1.2,3,3,.5,1.7,SILVER);
    }
    static Model astryx(CosmeticCatalog.Item i){
        List<Part> parts=new ArrayList<Part>();Builder b=new Builder();
        switch(i.slot){
            case HAT:hood(b,DARK,SILVER);logo(b,0,-9,-4.7,.55,true,PURPLE);return single(b,Anchor.HEAD);
            case BANDANA:band(b,0xFF24172E,SILVER);logo(b,0,-5.95,-4.5,.33,true,PURPLE);return single(b,Anchor.HEAD);
            case BACKPACK:vessel(b);return single(b,Anchor.BODY);
            case MINI_ME:b.bind(Anchor.HEAD);hood(b,DARK,SILVER);logo(b,0,-9,-4.7,.55,true,PURPLE);b.bind(Anchor.BODY);vessel(b);add(parts,b,Anchor.BODY,Motion.MINIATURE,6.25f,-4.67f,0,0,.18f);break;
            case AURA:for(int n=0;n<6;n++){Builder mote=new Builder();if(n%2==0)crescent(mote,0,0,0,.8,SILVER);else star(mote,0,0,0,.9,PURPLE);add(parts,mote,Anchor.BODY,Motion.ORBIT,0,0,0,n*(float)Math.PI/3,1);}break;
            case NECKLACE:chain(b,SILVER);b.plate(new double[]{-1.3,2.7,1.3,2.7,1.1,4.7,0,5.4,-1.1,4.7},-2.95,.18,DARK);logo(b,0,4,-3.18,.65,true,PURPLE);return single(b,Anchor.BODY);
            case BELT:
                b.box(-4.25,9.25,-2.38,8.5,1.6,.3,DARK);b.box(-4.25,9.25,2.08,8.5,1.6,.3,DARK);b.box(-4.25,9.25,-2.38,.3,1.6,4.76,DARK);b.box(3.95,9.25,-2.38,.3,1.6,4.76,DARK);
                b.plate(new double[]{-1.5,9,1.5,9,1.7,10.8,0,11.8,-1.7,10.8},-2.6,.3,SILVER);logo(b,0,10.2,-3,.4,true,PURPLE);
                for(int side:new int[]{-1,1}){b.plate(new double[]{side*2,10.6,side*3.7,10.6,side*3.5,14,side*2.4,13},-2.5,.3,DARK);b.beam(side*2.2,10.8,-2.85,side*3.2,13,-2.85,.18,PURPLE);}return single(b,Anchor.BODY);
            case BOOTS:
                for(Anchor leg:new Anchor[]{Anchor.LEFT_LEG,Anchor.RIGHT_LEG}){Builder boot=new Builder();bootShell(boot,DARK,SILVER);
                    boot.plate(new double[]{-1.8,8.4,0,7.5,1.8,8.4,1.5,10.6,0,11.1,-1.5,10.6},-2.6,.25,PURPLE);
                    boot.beam(-1.4,9,-2.9,0,10.5,-2.9,.18,SILVER);boot.beam(0,10.5,-2.9,1.4,9,-2.9,.18,SILVER);
                    boot.gem(0,10,2.5,.55,.9,PURPLE);add(parts,boot,leg,Motion.STILL,0,0,0,0,1);}break;
            default:throw new IllegalArgumentException("Unsupported astral slot "+i.slot);
        }
        return new Model(parts);
    }
    static void defenderHelmet(Builder b,int color){
        b.box(-4.4,-10,-4.4,8.8,1.6,8.8,color);b.box(-4.55,-8.5,-4.6,9.1,.35,.45,IRON);
        b.box(-4.5,-8.5,-2,.4,2.7,4,IRON);b.box(4.1,-8.5,-2,.4,2.7,4,IRON);
        bedIcon(b,0,-9.1,-4.65,.5,true,color);
        b.box(-.6,-10.4,-2,1.2,.45,4,GOLD);
    }
    static Model bedwars(CosmeticCatalog.Item i){
        List<Part> parts=new ArrayList<Part>();Builder b=new Builder();int red=0xFFB8394B;
        switch(i.slot){
            case HAT:defenderHelmet(b,red);return single(b,Anchor.HEAD);
            case BANDANA:band(b,red,WHITE);bedIcon(b,0,-6,-4.5,.27,true,red);return single(b,Anchor.HEAD);
            case BACKPACK:
                harness(b,0xFF694D36);Builder bed=new Builder();uprightBed(bed,red);b.append(bed.mesh(),0,0,3,1,1,1);
                shield(b,0,9,5.6,.55,red,GOLD);return single(b,Anchor.BODY);
            case MINI_ME:
                b.bind(Anchor.HEAD);defenderHelmet(b,red);b.bind(Anchor.BODY);b.box(-4.2,.25,-2.45,8.4,4,.3,red);bedIcon(b,0,2.2,-2.8,.48,true,red);
                Builder pack=new Builder();uprightBed(pack,red);b.append(pack.mesh(),0,1.5,2.7,.65,.65,.65);
                add(parts,b,Anchor.BODY,Motion.MINIATURE,6.25f,-4.67f,0,0,.18f);break;
            case AURA:
                for(int n=0;n<6;n++){Builder mote=new Builder();if(n%3==0)bedIcon(mote,0,0,0,.3,false,red);else if(n%3==1)shield(mote,0,0,0,.4,red,GOLD);else mote.gem(0,0,0,.65,1.1,0xFF84D9E5);add(parts,mote,Anchor.BODY,Motion.ORBIT,0,0,0,n*(float)Math.PI/3,1);}break;
            case NECKLACE:chain(b,GOLD);bedIcon(b,0,3.8,-3,.35,true,red);return single(b,Anchor.BODY);
            case BELT:
                b.box(-4.3,9.4,-2.45,8.6,1.2,.35,0xFF755238);b.box(-4.3,9.4,2.1,8.6,1.2,.35,0xFF755238);b.box(-4.3,9.4,-2.45,.35,1.2,4.9,0xFF755238);b.box(3.95,9.4,-2.45,.35,1.2,4.9,0xFF755238);
                bedIcon(b,0,10,-2.9,.3,true,red);
                for(int side:new int[]{-1,1}){double xx=side<0?-3.8:2.1;b.box(xx,10.7,-2.7,1.7,2,1.3,side<0?red:WHITE);b.box(xx+.15,11.2,-2.82,1.4,.12,.12,side<0?WHITE:IRON);b.box(xx+.15,11.9,-2.82,1.4,.12,.12,side<0?WHITE:IRON);}return single(b,Anchor.BODY);
            case BOOTS:
                for(Anchor leg:new Anchor[]{Anchor.LEFT_LEG,Anchor.RIGHT_LEG}){Builder boot=new Builder();bootShell(boot,red,IRON);bedIcon(boot,0,9.3,-2.65,.28,true,red);boot.box(-1.8,10.8,-2.85,3.6,.35,.2,WHITE);add(parts,boot,leg,Motion.STILL,0,0,0,0,1);}break;
            default:throw new IllegalArgumentException("Unsupported defender slot "+i.slot);
        }
        return new Model(parts);
    }
    private CosmeticThemes(){}
}
