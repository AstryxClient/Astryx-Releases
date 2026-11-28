package com.astryx.client.features;

import java.util.*;
import com.astryx.client.features.CosmeticCatalog.*;
import com.astryx.client.features.CosmeticModels.*;

final class WardrobeAccessories {
    private static final String[] ANIMALS={"Cat","Fox","Panda","Lynx","Rabbit","Lop","Wolf","Fennec","Bear","Otter","Tiger","Snowleopard","Redpanda","Raccoon"};
    private static final int[] FUR={0x746c80,0xbc6538,0x292732,0xc49a66,0xe2cec2,0x946745,0x788491,0xe1be83,0x765443,0x805d47,0xdb8838,0xc9d1d2,0xac5638,0x65676c};
    private static final String[] BLADES={"Ice","Cyber","Sakura","Obsidian","Bamboo","Tide","Copper","Thorn","Amber","Moon","Slate","Glass","Iron","Bronze","Silver","Ash","Flint","Cobalt","Onyx","Birch"};
    private static final String[] WEAPONS={"Ice Sabre","Circuit Twins","Cherry Blossom Katana","Obsidian Cleaver","Bamboo Shinai","Tide Hook","Copper Rapier","Briar Kris","Amber Falchion","Crescent Glaive","Slate Greatsword","Glass Stiletto","Iron Longsword","Bronze Shortsword","Silver Twins","Ash Practice Pair","Flint Cutlasses","Cobalt Sabres","Onyx Daggers","Birch Training Swords"};
    private static final int[] STEEL={0x9bd3e8,0x458c91,0xd59aa6,0x473d59,0xa5aa62,0x43959e,0xb77e61,0x688c69,0xe6ae52,0xb5abde,0x7f8a9b,0xa8d8cf,0xa4a5a4,0xb17f54,0xd0d4d8,0x9b8072,0x66696b,0x526580,0x45444c,0xcbb68b};
    static boolean handles(Slot slot){return slot==Slot.EARS||slot==Slot.BACK_BLADES;}
    static void catalogue(List<Item> items,Map<String,Item> index){
        add(items,index,"Angel",Slot.WINGS,"Angel Wings",0xfff8ed,0xc1cedc);
        add(items,index,"Halo",Slot.HAT,"Halo",0xffce56,0xfff4bb);
        for(int n=0;n<ANIMALS.length;n++){
            Slot slot=Slot.EARS;
            String animal=ANIMALS[n],label=animal.equals("Snowleopard")?"Snow Leopard":animal.equals("Redpanda")?"Red Panda":animal;
            add(items,index,animal,slot,label+" "+slot.label,FUR[n],n==2?0xb2a6ae:0xe6b7aa);
        }
        for(int n=0;n<BLADES.length;n++)add(items,index,BLADES[n],Slot.BACK_BLADES,WEAPONS[n],STEEL[n],0xe8c998);
    }
    private static void add(List<Item> items,Map<String,Item> index,String theme,Slot slot,String name,int color,int accent){
        Item item=new Item(Theme.ASTRYX,theme,slot,name,slot==Slot.WINGS?"Layered ivory flight feathers.":slot==Slot.HAT?"A floating ring of warm gold.":slot==Slot.BACK_BLADES?"Worn across the back.":"A matching pair worn above the skin layer.",0xff000000|color,0xff000000|accent,0);
        items.add(item);index.put(item.id,item);
    }
    static Model build(Item item){
        List<Part> parts=new ArrayList<>();
        int n=Arrays.asList(ANIMALS).indexOf(item.theme);
        if(item.slot==Slot.BACK_BLADES){
            Builder b=new Builder();blade(b,item,Arrays.asList(BLADES).indexOf(item.theme));parts.add(new Part(b,Anchor.BODY,Motion.STILL,0,0,3,0,1));
        }else for(int side:new int[]{-1,1}){
            Builder b=new Builder();
            ears(b,item,n,side);parts.add(new Part(b,Anchor.HEAD,Motion.STILL,0,0,0,0,1));
        }
        return new Model(parts);
    }
    private static void ears(Builder b,Item i,int n,int s){
        int c=i.primary,a=i.accent;
        if(n==2||n>=8){
            double radius=n==9?1.18:n==12?1.95:1.55;
            DetailMesh.volume(b,s*2.8,-8.15,-.4,radius,radius*(n==10?.85:1.12),.95,c,false);
            DetailMesh.volume(b,s*2.85,-8.45,-1.14,radius*.67,radius*.7,.44,n==2?0xff737077:a,false);
            if(n==10||n==11) b.beam(s*2.55,-8.6,.43,s*3.4,-9,.43,.55,0xff343037);
        }else if(n==5){
            b.beam(s*3.1,-8.15,-.45,s*4.32,-8.48,-.62,.98,c);
            DetailMesh.volume(b,s*4.45,-6.65,-.65,.92,2.5,1.12,c,false);
            DetailMesh.volume(b,s*4.54,-6.65,-1.53,.66,2.1,.36,a,false);
        }else{
            double h=n==4?7:n==7?5.9:n==3?4.6:n==1?4.2:3.35,tip=n==7?5.8:n==6?3.15:4.15,w=n==4?1.5:2.45;
            // A curved outer pinna with a recessed inner surface and a rounded rim.
            for(int row=0;row<12;row++)for(int col=0;col<16;col++){
                double t=row/12d,u=(row+1)/12d,v=col*Math.PI/8,z=(col+1)*Math.PI/8;
                double[] p0=earPoint(s,t,v,h,w,tip),p1=earPoint(s,u,v,h,w,tip);
                double[] p2=earPoint(s,u,z,h,w,tip),p3=earPoint(s,t,z,h,w,tip);
                float rim=(float)Math.sin((v+z)*.5);
                boolean inside=rim<-.53&&row>1&&row<10;
                int tint=CosmeticModels.shade(inside?a:c,inside?.89f+.1f*(-rim):.89f+.11f*(float)Math.cos(v));
                b.triangle(p0[0],p0[1],p0[2],p1[0],p1[1],p1[2],p2[0],p2[1],p2[2],tint);
                b.triangle(p0[0],p0[1],p0[2],p2[0],p2[1],p2[2],p3[0],p3[1],p3[2],tint);
            }
            if(n==3||n==1)b.beam(s*tip,-7.9-h,-.75,s*(tip+.22),-9.1-h,-.55,.39,0xff423a3a);
        }
    }
    private static double[] earPoint(int side,double t,double phi,double height,double width,double tip){
        double r=Math.pow(1-t,.68),front=Math.max(0,-Math.sin(phi));
        return new double[]{side*(2.85+(tip-2.85)*t+width*.79*r*Math.cos(phi)),
            -7.55-(height+.45)*t, -.24+.42*t+r*(1.2*Math.sin(phi)+.52*front*front*front)};
    }
    private static void blade(Builder b,Item i,int n){
        int c=i.primary,a=i.accent;
        boolean paired=n==1||n>=14;
        double length=new double[]{14,10,15,13,15,12,15,12,14,14,17,12,16,11,12,11,10,13,8,12}[n];
        double width=new double[]{.9,.75,.65,1.5,.6,1.1,.4,.8,1.1,.9,1.5,.5,1,.9,.8,.9,1.1,.7,.75,.85}[n];
        double curve=n==2||n==17?.45:0;
        for(int side=0;side<(paired?2:1);side++){
            Builder steel=new Builder();
            // Two sloping faces meet at a raised ridge. Their normals reflect light independently.
            for(int face:new int[]{-1,1})for(int edge:new int[]{-1,1}){
                double ridge=.24+face*.37;
                int tone=edge>0?c:CosmeticModels.shade(c,.88f);
                steel.quad(edge*width,0,.24,0,0,ridge,curve,length-1.8,ridge,curve+edge*width,length-1.8,.24,tone);
                steel.triangle(curve+edge*width,length-1.8,.24,curve,length-1.8,ridge,curve,length,.24,tone);
            }
            // Swept quillons and a short collar frame the blade without obscuring its outline.
            for(int edge:new int[]{-1,1}){
                double sweep=n%3==0?.6:n%3==1?-.45:0;
                steel.plate(new double[]{0,-.54,edge*1.25,-.46,edge*2.13,sweep-.28,edge*2.23,sweep+.12,edge*1.23,.18,0,.22},-.06,.62,0xff65707a);
            }
            steel.box(-.59,-.92,-.11,1.18,.48,.73,0xffc5aa75);
            steel.gem(0,-3.42,.24,.65,.65,CosmeticModels.shade(c,.84f));
            for(int wrap=0;wrap<4;wrap++)steel.beam(-.42,-1.19-wrap*.49,.51,.42,-1.42-wrap*.49,.51,.13,0xff98785c);
            steel.box(-.42,-3.3,-.05,.84,3,.55,0xff5a4840);
            Mesh mesh=steel.mesh();
            for(int k=0;k<mesh.vertices.length;k+=3){float x=mesh.vertices[k],y=mesh.vertices[k+1];mesh.vertices[k]=(side==0?1:-1)*(3+x*.86f-y*.5f);mesh.vertices[k+1]=1+x*.5f+y*.86f;}
            b.append(mesh,0,0,side*.8,1,1,1);
        }
    }
}
