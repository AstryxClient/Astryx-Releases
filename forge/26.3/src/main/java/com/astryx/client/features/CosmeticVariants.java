package com.astryx.client.features;

import java.util.*;
import com.astryx.client.features.CosmeticModels.*;

/** Alternative silhouettes and theme-specific equipment, rather than palette swaps. */
final class CosmeticVariants {
    static Model build(CosmeticCatalog.Item i){
        Builder b=new Builder();List<Part> parts=new ArrayList<Part>();
        switch(i.slot){
            case CLOAK:
                cloak(b,i);CosmeticThemes.add(parts,b,Anchor.BODY,Motion.CLOTH,0,0,2.65f,0,1);break;
            case HAT:hat(b,i);return CosmeticThemes.single(b,Anchor.HEAD);
            case BANDANA:
                CosmeticThemes.band(b,color(i),i.accent);motif(b,i,-.3,-5.9,-4.5,.24,true);
                if(i.variant==1)b.box(-4.35,-5.1,-3.8,.35,.55,7.8,i.accent);else b.plate(new double[]{-2.2,-5.2,2.2,-5.2,0,-3.5},-4.45,.15,color(i));
                return CosmeticThemes.single(b,Anchor.HEAD);
            case BACKPACK:pack(b,i);return CosmeticThemes.single(b,Anchor.BODY);
            case MINI_ME:
                b.bind(Anchor.HEAD);hat(b,i);b.bind(Anchor.BODY);pack(b,i);CosmeticThemes.add(parts,b,Anchor.BODY,Motion.MINIATURE,6.25f,-4.67f,0,0,.18f);break;
            case AURA:
                for(int n=0;n<(i.variant==1?6:8);n++){
                    Builder mote=new Builder();
                    if(i.design==CosmeticCatalog.Theme.MINECRAFT&&i.variant==2){mote.box(-.7,-.6,-.5,1.4,1.2,1,0xFFE6B84E);mote.box(-.72,-.2,-.52,1.44,.3,1.04,0xFF6C4C2E);mote.box(-1,-.8,-.3,.7,.1,.7,CosmeticThemes.WHITE);mote.box(.3,-.8,-.3,.7,.1,.7,CosmeticThemes.WHITE);}
                    else motif(mote,i,0,0,0,.38,false);
                    if(i.variant==2&&i.design!=CosmeticCatalog.Theme.MINECRAFT)mote.gem(1.4,-.6,.1,.3,.5,i.accent);
                    CosmeticThemes.add(parts,mote,Anchor.BODY,Motion.ORBIT,0,0,0,n*(float)Math.PI*2/(i.variant==1?6:8),1);
                }break;
            case NECKLACE:
                CosmeticThemes.chain(b,i.design==CosmeticCatalog.Theme.SMP?0xFF886340:CosmeticThemes.SILVER);
                if(i.variant==1)b.ring(0,4,-3.1,1.25,1.4,.22,i.accent);
                else {b.plate(new double[]{-1.5,2.7,1.5,2.7,1.7,4,0,5.6,-1.7,4},-3,.25,color(i));for(int n=0;n<5;n++)b.box(-2.7+n*1.35,.9+Math.abs(2-n)*-.3,-2.85,.3,.35,.3,i.accent);}
                motif(b,i,0,4,-3.4,i.variant==1?.4:.48,true);return CosmeticThemes.single(b,Anchor.BODY);
            case BELT:belt(b,i);return CosmeticThemes.single(b,Anchor.BODY);
            case BOOTS:
                for(Anchor leg:new Anchor[]{Anchor.LEFT_LEG,Anchor.RIGHT_LEG}){
                    Builder boot=new Builder();CosmeticThemes.bootShell(boot,color(i),i.accent);
                    if(i.variant==1){for(int n=0;n<3;n++)boot.beam(-1.2,8.6+n*.7,-2.6,1.2,9+n*.7,-2.6,.16,CosmeticThemes.WHITE);boot.box(-2.4,8.4,.4,.4,2.4,1.4,i.accent);boot.box(2,8.4,.4,.4,2.4,1.4,i.accent);}
                    else {boot.plate(new double[]{-1.8,8.3,1.8,8.3,2,10.7,0,11.3,-2,10.7},-2.6,.3,CosmeticThemes.DARK);boot.box(-2.25,10.8,-3,4.5,.5,.3,i.accent);}
                    motif(boot,i,0,9.3,-2.96,.28,true);CosmeticThemes.add(parts,boot,leg,Motion.STILL,0,0,0,0,1);
                }break;
            default:throw new IllegalArgumentException("Unsupported alternate slot "+i.slot);
        }
        return new Model(parts);
    }
    private static int color(CosmeticCatalog.Item i){
        if(i.design==CosmeticCatalog.Theme.ASTRYX)return i.variant==1?0xFF30203F:0xFF36365D;
        if(i.design==CosmeticCatalog.Theme.BEDWARS)return i.variant==1?0xFF9C333D:0xFF3F8A9E;
        if(i.design==CosmeticCatalog.Theme.MINECRAFT)return i.variant==1?0xFF3B444D:0xFF8C423B;
        return i.variant==1?CosmeticModels.shade(i.primary,.8f):CosmeticModels.shade(i.primary,1.1f);
    }
    private static void cloak(Builder b,CosmeticCatalog.Item i){
        int c=color(i);
        for(int row=0;row<17;row++){
            double inset=i.variant==2&&row>11?(row-11)*.5:0;
            int shade=CosmeticModels.shade(c,.78f+row*.018f);
            if(i.variant==1&&row>=13){b.box(-5,row,0,4.45,1,.23,shade);b.box(.55,row,0,4.45,1,.23,shade);}
            else b.box(-5+inset,row,0,10-2*inset,1,.23,shade);
            b.box(-5+inset,row,.24,.24,1,.06,i.accent);b.box(4.76-inset,row,.24,.24,1,.06,i.accent);
        }
        b.box(-5,0,.25,10,.35,.1,i.accent);b.box(-4,1,.25,8,.15,.08,i.accent);
        motif(b,i,0,7,.36,1.2,false);
        if(i.variant==1){b.beam(-3.8,11,.3,0,13,.3,.18,i.accent);b.beam(0,13,.3,3.8,11,.3,.18,i.accent);}
        else for(int n=0;n<4;n++){b.box(-4+n*.9,2+n*.8,.28,.3,.4,.06,i.accent);b.box(3.7-n*.9,2+n*.8,.28,.3,.4,.06,i.accent);}
    }
    static Model scarf(CosmeticCatalog.Item i){
        List<Part> parts=new ArrayList<Part>();Builder neck=new Builder();int c=i.variant==0?i.primary:color(i);
        neck.box(-4.15,.05,-2.65,8.3,1.55,.4,c);neck.box(-4.15,.05,2.25,8.3,1.55,.4,c);neck.box(-4.15,.05,-2.65,.4,1.55,5.3,c);neck.box(3.75,.05,-2.65,.4,1.55,5.3,c);
        neck.box(-4.15,.08,-2.72,8.3,.15,.1,i.accent);neck.box(-4.15,1.35,-2.72,8.3,.15,.1,i.accent);
        neck.box(1.9,1,-2.95,1.8,1.3,.6,c);motif(neck,i,-1.4,.8,-2.8,.25,true);
        for(int n=0;n<8;n++)neck.box(-3.7+n*.9,.5,-2.73,.2,.35,.08,CosmeticModels.shade(c,1.35f));
        CosmeticThemes.add(parts,neck,Anchor.BODY,Motion.STILL,0,0,0,0,1);
        for(int tail=0;tail<2;tail++){
            Builder ribbon=new Builder();int length=(i.variant==0?8:i.variant==1?10:7)-(tail==1?3:0);
            for(int row=0;row<length;row++){
                int stripe=(row+(i.design==CosmeticCatalog.Theme.BEDWARS?1:0))%(i.variant==2?2:3)==0?i.accent:c;
                ribbon.box(-.85,row,-.1,1.7,1,.18,stripe);
                ribbon.box(-.84,row,-.16,.12,1,.05,CosmeticModels.shade(i.accent,.8f));
            }
            for(int n=0;n<3;n++)ribbon.box(-.75+n*.55,length,-.1,.3,.7+(n%2)*.35,.18,i.accent);
            if(tail==0)motif(ribbon,i,0,length-2,-.2,.2,true);
            CosmeticThemes.add(parts,ribbon,Anchor.BODY,Motion.SCARF,tail==0?2.8f:1.1f,2.2f,-2.95f,tail==0?0:1.4f,1);
        }
        return new Model(parts);
    }
    private static void hat(Builder b,CosmeticCatalog.Item i){
        int c=color(i),a=i.accent;
        if(i.design==CosmeticCatalog.Theme.MINECRAFT&&i.variant==2){
            b.box(-4.4,-8.8,-4.4,8.8,1,.35,0xFF70473E);b.box(-4.4,-8.8,4.05,8.8,1,.35,0xFF70473E);b.box(-4.4,-8.8,-4.4,.35,1,8.8,0xFF70473E);b.box(4.05,-8.8,-4.4,.35,1,8.8,0xFF70473E);
            for(int side:new int[]{-1,1}){double xx=side<0?-3.2:.6;b.box(xx,-9.3,-4.8,2.6,2.1,.65,CosmeticThemes.IRON);b.box(xx+.3,-9,-4.95,2,1.5,.2,0xFF704E52);b.box(xx+.45,-8.85,-5,1.7,.2,.1,0xFFE79568);}b.box(-.6,-8.6,-4.7,1.2,.45,.4,CosmeticThemes.IRON);return;
        }
        if(i.design==CosmeticCatalog.Theme.SMP&&i.variant==2){
            b.box(-4.4,-10.2,-4.4,8.8,1.8,8.8,0xFFA6926E);b.box(-4.5,-8.5,-2.6,.4,5,5.2,0xFF74604A);b.box(4.1,-8.5,-2.6,.4,5,5.2,0xFF74604A);
            for(int side:new int[]{-1,1}){double xx=side<0?-3.2:.5;b.box(xx,-9.5,-4.7,2.7,1.6,.45,CosmeticThemes.IRON);b.box(xx+.25,-9.2,-4.85,2.2,1,.2,0xFF6D7D79);}b.box(-.5,-8.9,-4.75,1,.3,.4,0xFF74604A);return;
        }
        if(i.design==CosmeticCatalog.Theme.SKYBLOCK&&i.variant==2){
            b.box(-4.4,-9,-4.4,8.8,.8,.4,CosmeticThemes.IRON);b.box(-4.4,-9,4,8.8,.8,.4,CosmeticThemes.IRON);b.box(-4.4,-9,-4.4,.4,.8,8.8,CosmeticThemes.IRON);b.box(4,-9,-4.4,.4,.8,8.8,CosmeticThemes.IRON);
            b.gem(0,-11.5,-3.8,1,3,0xFF75DCBA);b.gem(-2.7,-10.4,-2.7,.75,2.3,0xFFB795E3);b.gem(2.7,-10.4,-2.7,.75,2.3,0xFFE3CA7B);b.gem(0,-10,3,.8,2,0xFF75DCBA);return;
        }
        if(i.design==CosmeticCatalog.Theme.SKYWARS&&i.variant==1){
            for(int n=0;n<16;n++){double a0=n*Math.PI/8,a1=(n+1)*Math.PI/8;b.beam(Math.cos(a0)*4.9,-9.5,Math.sin(a0)*4.9,Math.cos(a1)*4.9,-9.5,Math.sin(a1)*4.9,.35,CosmeticThemes.SILVER);}
            b.gem(0,-9.5,-5.1,.8,1.3,0xFF79D8C3);return;
        }
        if(i.design==CosmeticCatalog.Theme.MINECRAFT&&i.variant==1){
            b.box(-4.4,-12,-4.4,8.8,3.8,8.8,0xFFC67D32);b.box(-4.6,-8.7,-4.6,9.2,.5,9.2,0xFF91612D);
            b.box(-2.7,-11,-4.55,1.6,1,.2,CosmeticThemes.DARK);b.box(1.1,-11,-4.55,1.6,1,.2,CosmeticThemes.DARK);b.plate(new double[]{-2.6,-9.6,0,-10,2.6,-9.6,1.7,-8.8,-1.7,-8.8},-4.55,.2,CosmeticThemes.DARK);b.box(-.5,-13,-.5,1,1,1,0xFF6E7F44);return;
        }
        if(i.variant==1){
            if(i.design==CosmeticCatalog.Theme.SMP){CosmeticThemes.hood(b,0xFF665F49,0xFFCABA95);b.box(-2.4,-10,-4.5,4.8,1,.3,0xFFD7C29B);}
            else {b.box(-4.4,-9,-4.4,8.8,.75,.45,CosmeticThemes.SILVER);b.box(-4.4,-9,3.95,8.8,.75,.45,CosmeticThemes.SILVER);b.box(-4.4,-9,-4.4,.45,.75,8.8,CosmeticThemes.SILVER);b.box(3.95,-9,-4.4,.45,.75,8.8,CosmeticThemes.SILVER);
                for(int n=0;n<3;n++)b.plate(new double[]{-3+n*2.4,-9,-2.5+n*2.4,-11.5-(n%2),-2+n*2.4,-9},-4.4,.35,c);}
            motif(b,i,0,-10,-4.65,.5,true);
        }else{
            b.box(-4.45,-10.2,-4.4,8.9,1.9,8.8,c);b.box(-4.7,-8.6,-4.7,9.4,.45,9.4,CosmeticThemes.DARK);
            b.box(-4.65,-8.1,-2.7,.45,2.7,5.4,CosmeticThemes.SILVER);b.box(4.2,-8.1,-2.7,.45,2.7,5.4,CosmeticThemes.SILVER);
            motif(b,i,0,-9.2,-4.7,.55,true);
            if(i.design!=CosmeticCatalog.Theme.BEDWARS)for(int side:new int[]{-1,1})b.plate(new double[]{side*3,-9,side*5.6,-12.2,side*4.6,-8.6},1,.4,a);
            else b.gem(0,-10.4,0,.8,1.3,0xFF7AD6E2);
        }
    }
    private static void pack(Builder b,CosmeticCatalog.Item i){
        CosmeticThemes.harness(b,i.design==CosmeticCatalog.Theme.SMP?0xFF6B513A:CosmeticThemes.DARK);int c=color(i),a=i.accent;
        switch(i.design){
            case MINECRAFT:
                if(i.variant==1){b.box(-3.2,2,2.7,6.4,7.3,4.5,0xFFC29A4F);for(int n=0;n<4;n++)b.box(-3.25,2.5+n*1.6,7.25,6.5,.35,.15,0xFF866438);b.box(-1.3,6.6,7.4,2.6,1.1,.2,0xFF332B22);b.box(-3.2,9.2,2.7,6.4,.5,4.5,0xFF735935);}
                else {b.box(-3.1,2,2.8,6.2,7.7,3.8,0xFF625B5A);b.box(-2,3,6.65,4,5,.4,0xFF9A403B);b.beam(-2.5,3,7,-2.5,8.5,7,.3,0xFFCF6A54);b.beam(2.5,3,7,2.5,8.5,7,.3,0xFFCF6A54);b.gem(0,5.5,7.2,1.2,1.8,0xFFE04B3D);b.box(-1,1,3.5,2,1,1.4,CosmeticThemes.IRON);}break;
            case BEDWARS:
                if(i.variant==1){
                    b.box(-3.5,2.5,2.7,7,7,3.5,CosmeticThemes.OAK);for(int side:new int[]{-1,1})for(int row=0;row<3;row++)b.box(side<0?-3.1:.2,2.8+row*2.1,6.3,2.9,1.9,1.5,(row+(side<0?0:1))%2==0?0xFFB8394B:CosmeticThemes.WHITE);
                    b.box(-2.7,1.3,3.1,2.3,1,2,CosmeticThemes.GOLD);b.box(.4,1.3,3.1,2.3,1,2,CosmeticThemes.IRON);
                    CosmeticThemes.bedIcon(b,0,5,7.9,.5,false,0xFFB8394B);
                }else {b.box(-3.5,8,2.7,7,2,4.5,0xFF858B94);b.box(-2.8,7.4,3.1,5.6,.6,3.7,0xFFB7BCC5);b.gem(0,4.3,5.6,1.5,2.8,0xFF76D5E5);b.ring(0,4.3,6,2.1,3,.2,0xFFD5F4F4);CosmeticThemes.bedIcon(b,0,9,7.3,.4,false,0xFFB8394B);}break;
            case SKYWARS:
                if(i.variant==1){b.box(-3.4,3,2.7,6.8,6,4.3,0xFF89623C);b.box(-3.6,2,2.6,7.2,2,4.5,0xFF9F7849);b.box(-3.55,7.8,7.05,7.1,.45,.2,CosmeticThemes.IRON);b.box(-.7,4.4,7.1,1.4,1.9,.3,CosmeticThemes.IRON);b.gem(0,5.4,7.45,.45,.6,0xFF69CDB8);}
                else {b.box(-3,2.5,2.7,6,7,.6,CosmeticThemes.DARK);b.ring(0,5.8,6,2.9,3.3,.45,0xFF7E86A9);for(int side:new int[]{-1,1})b.beam(side*2.6,3,3.1,side*2.6,8.6,6,.3,0xFFA0A8C5);b.gem(0,5.8,6.3,1.7,2.3,0xFF5CBAA8);motif(b,i,0,5.8,7.3,.6,false);}break;
            case SKYBLOCK:
                if(i.variant==1){b.box(-3.3,3,2.7,6.6,6.5,3.6,0xFF635047);b.box(-3.5,2,2.7,7,2,3.8,0xFF86634D);for(int side:new int[]{-1,1})b.plate(new double[]{side*2.5,3,side*4,0,side*3.7,5},4,.6,0xFFD0AF78);motif(b,i,0,5.5,6.7,.9,false);}
                else {b.box(-3.3,2,2.7,6.6,8,3,0xFF454D59);b.box(-2.8,2.5,5.8,5.6,5.5,.3,CosmeticThemes.DARK);for(int n=0;n<3;n++)b.gem(-1.8+n*1.8,5.3,6.3,.65,1.7,n==0?0xFFB89BE8:n==1?0xFF78DEBB:0xFFE4C976);b.beam(2,2,3.3,3,-1,3.3,.3,CosmeticThemes.IRON);b.box(-2.8,8.8,5.9,5.6,.4,.35,a);}break;
            case SMP:
                if(i.variant==1){b.box(-2.7,8,2.7,5.4,1.3,4.3,0xFF645039);b.box(-2.7,2.3,2.7,5.4,.8,4.3,0xFF645039);for(int side:new int[]{-1,1}){b.beam(side*2.3,3,3,side*2.3,8,6.7,.35,0xFF625C4E);b.beam(side*2.3,3,6.7,side*2.3,8,3,.35,0xFF625C4E);}b.gem(0,5.7,5.4,1.4,2.1,0xFFE7C270);b.ring(0,1.1,4.5,1.5,1.1,.3,0xFF625C4E);}
                else {b.box(-3.4,2.5,2.7,6.8,7.2,3.8,0xFF8B8060);b.box(-3,3.5,6.55,6,5.5,.2,0xFFDFD2AE);motif(b,i,0,6,6.9,.8,false);b.box(-4,.8,3,8,1.7,2.8,0xFFE2D5B1);b.box(-2.6,.7,2.9,.45,2,3,0xFF6B5035);b.box(2.15,.7,2.9,.45,2,3,0xFF6B5035);}break;
            case ASTRYX:
                if(i.variant==1){b.box(-2.6,2.7,2.7,5.2,7,2.5,0xFF30273D);b.ring(0,4.5,6.3,2.9,3,.4,CosmeticThemes.SILVER);b.ring(0,4.5,6.5,2.2,2.3,.3,0xFF8268AA);b.gem(0,4.5,6.8,1.2,1.7,0xFFC5BAE7);CosmeticThemes.crescent(b,0,4.5,7.1,1.2,0xFFE0D9F1);b.box(-1.7,9,3,3.4,1.3,3,0xFF574775);}
                else {b.plate(new double[]{-3,2,3,2,3.6,6,2.5,10,-2.5,10,-3.6,6},2.7,3.3,0xFF24233B);b.box(-2.8,3,6.1,5.6,5.9,.2,0xFF656580);motif(b,i,0,6,6.4,1,false);b.box(-3.6,1.4,3.1,7.2,1.2,2.8,0xFFACA7C3);b.box(-2.4,1.3,3,.5,1.5,3,0xFF4B3A68);b.box(1.9,1.3,3,.5,1.5,3,0xFF4B3A68);}break;
        }
    }
    private static void belt(Builder b,CosmeticCatalog.Item i){
        int c=color(i);b.box(-4.3,9.4,-2.4,8.6,1.3,.35,c);b.box(-4.3,9.4,2.05,8.6,1.3,.35,c);b.box(-4.3,9.4,-2.4,.35,1.3,4.8,c);b.box(3.95,9.4,-2.4,.35,1.3,4.8,c);
        motif(b,i,0,10,-2.9,.4,true);
        if(i.variant==1){for(int side:new int[]{-1,1}){double x=side<0?-3.9:2.1;b.box(x,10.6,-2.65,1.8,2.8,1.3,c);b.box(x-.05,10.6,-2.7,1.9,.65,1.4,i.accent);}}
        else {b.plate(new double[]{-3,10.5,-1,10.5,-1.7,15,-3.3,14},-2.6,.25,c);b.plate(new double[]{1,10.5,3,10.5,3.2,13,1.7,14},-2.6,.25,i.accent);b.box(-3.5,11,-2.9,1.3,.4,.8,i.accent);}
        if(i.design==CosmeticCatalog.Theme.BEDWARS){b.box(2.5,11.4,-2.9,1.3,.6,.7,i.variant==1?CosmeticThemes.GOLD:0xFF79D4E3);}
    }
    static void motif(Builder target,CosmeticCatalog.Item i,double x,double y,double z,double size,boolean front){
        Builder b=new Builder();int a=i.accent;
        switch(i.design){
            case MINECRAFT:
                if(i.variant==0){b.box(-1.5,-1,0,1,1,.2,CosmeticThemes.DARK);b.box(.5,-1,0,1,1,.2,CosmeticThemes.DARK);b.box(-.5,0,0,1,1.5,.2,CosmeticThemes.DARK);b.box(-1,1,0,2,.5,.2,CosmeticThemes.DARK);}
                else if(i.variant==1){b.plate(new double[]{-2,0,-.8,-1.4,1,-1.4,2,0,1,1.3,-.8,1.3},0,.2,0xFFB9ADCE);b.gem(0,0,.3,.85,1.2,0xFF6EC2A5);b.box(-.18,-.8,.6,.36,1.6,.15,CosmeticThemes.DARK);}
                else {b.box(-1.2,-1.2,0,2.4,2.4,.25,0xFF714A43);b.beam(-2,-1.6,.3,-.7,-.4,.3,.35,0xFFE56B54);b.beam(.7,.4,.3,2,1.6,.3,.35,0xFFE56B54);b.gem(0,0,.45,.8,1.1,0xFFE84D3E);}break;
            case BEDWARS:
                if(i.variant==0)CosmeticThemes.bedIcon(b,0,0,0,.65,false,0xFFB8394B);
                else if(i.variant==1){CosmeticThemes.shield(b,0,0,0,.9,0xFFAB3746,CosmeticThemes.GOLD);b.box(-1.4,-.5,.7,2.8,.6,.3,CosmeticThemes.GOLD);}
                else {b.gem(0,0,.25,1.3,2,0xFF7EDCE8);b.beam(-1.8,1.6,.2,1.8,1.6,.2,.3,CosmeticThemes.IRON);b.box(-1.8,1.6,.1,3.6,.5,.5,0xFF7D8795);}break;
            case SKYWARS:
                b.ring(0,0,0,1.8,i.variant==2?1:1.8,.25,0xFF969FBE);b.gem(0,0,.4,1.1,1.4,0xFF65C1AE);
                if(i.variant==2){b.plate(new double[]{-1.7,-.6,-1,-2.8,-.3,-1},.2,.35,0xFF725B9A);b.plate(new double[]{1.7,-.6,1,-2.8,.3,-1},.2,.35,0xFF725B9A);}break;
            case SKYBLOCK:
                if(i.variant==1){b.plate(new double[]{-2,0,0,-1.2,2,0,0,1.2},0,.3,0xFFD7AF6D);b.gem(0,0,.4,.65,1.1,0xFFB4634C);b.beam(-1.5,-.5,0,-1.1,-2,0,.35,0xFFD7AF6D);b.beam(1.5,-.5,0,1.1,-2,0,.35,0xFFD7AF6D);}
                else {b.gem(0,0,0,1,2,a);b.gem(-1.4,.5,0,.55,1.2,0xFFB191DE);b.gem(1.4,.7,0,.55,1.1,0xFFE3C369);}break;
            case SMP:
                if(i.variant==1){b.ring(-.8,0,0,.8,1,.2,0xFFD1BB8A);b.ring(.8,0,0,.8,1,.2,0xFFD1BB8A);b.box(-1.05,-.25,.3,.35,.5,.2,CosmeticThemes.DARK);b.box(.7,-.25,.3,.35,.5,.2,CosmeticThemes.DARK);b.plate(new double[]{-.4,.5,.4,.5,0,1.3},.1,.3,0xFFAA8C58);}
                else if(i.variant==2){b.ring(0,0,0,1.6,1.6,.22,CosmeticThemes.GOLD);b.plate(new double[]{0,-1.4,.45,0,0,1.4,-.45,0},.1,.25,CosmeticThemes.WHITE);b.plate(new double[]{-1.4,0,0,-.3,1.4,0,0,.3},.15,.25,0xFF8AA475);}
                else {b.plate(new double[]{0,-1.7,1.5,-.4,1,1.3,0,1.7,-1,1.3,-1.5,-.4},0,.2,0xFF85A76E);b.beam(0,-1.5,.3,0,1.4,.3,.15,0xFFC1D19B);}break;
            case ASTRYX:
                if(i.variant==0)CosmeticThemes.logo(b,0,0,0,.85,false,0xFFB48BE9);
                else if(i.variant==1){CosmeticThemes.crescent(b,0,0,0,1.3,CosmeticThemes.SILVER);CosmeticThemes.star(b,1.1,.1,.3,.65,0xFFC4A0EE);}
                else {double[][] p={{-1.5,-1},{.4,-1.7},{1.8,.2},{-.5,1.6},{-1.5,-1}};for(int n=0;n<4;n++){b.beam(p[n][0],p[n][1],0,p[n+1][0],p[n+1][1],0,.12,0xFFB8ABDB);CosmeticThemes.star(b,p[n][0],p[n][1],.2,.45,0xFFDCCEF4);}}break;
        }
        target.append(b.mesh(),x,y,z,size,size,front?-size:size);
    }
    private CosmeticVariants(){}
}
