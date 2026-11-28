package com.astryx.client.features;

import com.astryx.client.features.CosmeticModels.Builder;

final class DetailMesh {
    private static final int INK=0xFF28222B,CREAM=0xFFF7DFBC;
    private static void surface(Builder b,double[] a,double[] c,double[] d,double[] e,int color){
        b.triangle(a[0],a[1],a[2],c[0],c[1],c[2],d[0],d[1],d[2],color);
        b.triangle(a[0],a[1],a[2],d[0],d[1],d[2],e[0],e[1],e[2],color);
    }
    private static double[] vertex(double x,double y,double z,double rx,double ry,double rz,int ring,int seg,int sides,boolean rough){
        double lat=Math.PI*(ring/6d-.5),lon=Math.PI*2*seg/sides;
        double r=rough?1+.14*Math.sin(3*lon+ring*.85)+.07*Math.cos(5*lon-ring):1;
        return new double[]{x+rx*Math.cos(lat)*Math.cos(lon)*r,y+ry*Math.sin(lat),z+rz*Math.cos(lat)*Math.sin(lon)*r};
    }
    static void volume(Builder b,double x,double y,double z,double rx,double ry,double rz,int color,boolean rough){
        int sides=rough?9:12;
        for(int j=0;j<6;j++)for(int k=0;k<sides;k++){
            int tone=rough?CosmeticModels.shade(color,(float)(.96+.19*Math.sin(j*13+k*17))):color;
            surface(b,vertex(x,y,z,rx,ry,rz,j,k,sides,rough),vertex(x,y,z,rx,ry,rz,j+1,k,sides,rough),
                vertex(x,y,z,rx,ry,rz,j+1,k+1,sides,rough),vertex(x,y,z,rx,ry,rz,j,k+1,sides,rough),tone);
        }
    }
    static void loaf(Builder b,int c,int a){
        volume(b,0,0,0,2.12,2.55,1.12,c,false);
        for(int n=0;n<4;n++){
            double y=-1.65+n*.94;
            for(int j=0;j<7;j++){
                double x=-1.08+j*.29,yy=y-x*.32;
                double z=1.12*Math.sqrt(Math.max(0,1-x*x/4.5-yy*yy/6.5));
                b.beam(x,yy,z+.04,x+.26,yy-.09,z+.06,.16,a);
            }
        }
        for(int j=0;j<16;j++){double x=Math.sin(j*8.2)*1.7,y=Math.cos(j*5.4)*1.5;
            b.gem(x,y,1.12*Math.sqrt(Math.max(0,1-x*x/4.5-y*y/6.5)),.035,.09,CREAM);}
    }
    static void stone(Builder b,int c,int a){
        volume(b,0,0,0,2.22,2.35,1.15,c,true);
        for(int n=0;n<5;n++){
            double x=-1.2+n*.48,y=-1+n*.37;
            b.beam(x,y,1.03,x+.51,y+.28,1.09,.13,a);
            b.gem(x-.6,y+.48,.95,.18,.32,CosmeticModels.shade(a,.72f));
        }
        for(int n=0;n<3;n++)b.gem(1.12+n*.24,-1.12-n*.23,.64,.36,.61,0xFFB4C3C7);
    }
    static void animal(Builder b,String type,int c){
        boolean duck=type.equals("Duck"),bee=type.equals("Bee"),frog=type.equals("Frog"),panda=type.equals("Panda");
        volume(b,0,0,0,1.86,1.48,1.05,c,false);
        for(int side:new int[]{-1,1}){
            if(bee){
                volume(b,side*1.35,-1.27,-.2,.87,.73,.23,0xFFD3E6EB,false);
                b.beam(side*.51,-1,.4,side*.85,-2,.41,.15,INK);b.gem(side*.85,-2,.42,.14,.17,INK);
            }else if(!duck&&!frog&&!panda){
                b.plate(new double[]{side*.42,-1,side*1.67,-2.55,side*1.71,-.52},-.25,.66,c);
                b.plate(new double[]{side*.86,-1.06,side*1.52,-2.06,side*1.53,-.81},.43,.09,0xFFCE998C);
                for(int n=0;n<3;n++)b.beam(side*.7,.45+n*.12,1.06,side*2.15,.29+n*.27,.75,.06,CREAM);
            }else if(!duck)volume(b,side*1.08,-1.18,.18,.64,.67,.56,panda?INK:c,false);
            double eyeY=frog?-1.11:-.31;
            if(panda)volume(b,side*.78,eyeY,.83,.58,.54,.25,INK,false);
            volume(b,side*.82,eyeY,frog?.69:1,.23,.28,.16,INK,false);
            b.gem(side*.82-.06,eyeY-.09,frog?.85:1.16,.07,.07,-1);
            if(!duck&&!bee)volume(b,side*.39,.5,.93,.51,.38,.35,frog?c:CREAM,false);
        }
        if(duck)volume(b,0,.43,1.12,.92,.25,.7,0xFFF3973E,false);
        else if(bee)for(int n=0;n<3;n++)b.beam(-.69+n*.67,.59,.87,-.41+n*.67,1.08,.58,.19,INK);
        else b.gem(0,.28,1.25,.24,.18,INK);
    }
    private static double[] tailPoint(double t,double angle,int cut,boolean bushy){
        double radius=.035+Math.pow(Math.sin(Math.PI*t),.84)*(bushy?.84:.42)+.44*(1-t);
        double dx=Math.sin(t*Math.PI*(1.0+cut*.22))*(1+cut*.59)+Math.pow(t,2.4)*(bushy?3.3:1.2);
        return new double[]{dx+radius*Math.cos(angle),t*(12+cut)-9*t*t*t+radius*Math.sin(angle),
            Math.sin(t*Math.PI*.45)*(8+cut)+t*2-radius*.45*Math.sin(angle)};
    }
    static void tail(Builder b,CosmeticCatalog.Item item){
        boolean bushy=item.theme.equals("Fox")||item.theme.equals("Cat")||item.theme.equals("Panda");
        for(int k=0;k<16;k++)for(int j=0;j<10;j++){
            double t=k/16d,u=(k+1)/16d,v=j*Math.PI/5,w=(j+1)*Math.PI/5;
            int color=k>11?(bushy?CREAM:item.accent):item.primary;
            if(item.theme.equals("Bee")&&k%5<2)color=INK;
            surface(b,tailPoint(t,v,item.variant,bushy),tailPoint(t,w,item.variant,bushy),tailPoint(u,w,item.variant,bushy),tailPoint(u,v,item.variant,bushy),color);
            if(bushy&&k%4==1&&j%2==0){double[] p=tailPoint(t,v,item.variant,bushy);
                if(bushy){
                    double[] tip=tailPoint(Math.min(1,u+.095),v+.10,item.variant,true);
                    double[] q=tailPoint(t,w,item.variant,true);
                    b.triangle(p[0],p[1],p[2],q[0],q[1],q[2],tip[0]+.39*Math.cos(v),tip[1]+.4*Math.sin(v),tip[2]+.25,CosmeticModels.shade(color,1.17f));
                }else b.gem(p[0],p[1],p[2],.19,.33,CosmeticModels.shade(color,1.17f));}
        }
    }
    static CosmeticModels.Model finishLegacy(CosmeticCatalog.Item item,CosmeticModels.Model base){
        if(item.slot==CosmeticCatalog.Slot.WINGS)return base;
        java.util.List<CosmeticModels.Part> parts=new java.util.ArrayList<CosmeticModels.Part>(base.parts);
        for(CosmeticModels.Part host:base.parts){
            Builder trim=new Builder();if(host.motion==CosmeticModels.Motion.MINIATURE)trim.bind(CosmeticModels.Anchor.HEAD);int metal=item.accent,leather=CosmeticModels.shade(item.primary,.72f);
            switch(item.slot){
                case CLOAK:
                    for(int side:new int[]{-1,1})for(int j=0;j<12;j++)trim.beam(side*4.45,j+.45,.39,side*4.16,j+.79,.42,.11,metal);
                    break;
                case WINGS:break;
                case BOOTS:
                    for(int j=0;j<3;j++)for(int side:new int[]{-1,1})trim.beam(side*1.15,8+j*.75,-2.51,-side*.84,8.56+j*.75,-2.54,.14,metal);break;
                case BACKPACK:
                    for(int side:new int[]{-1,1}){trim.box(side*2.3-.34,3,6,.68,4.8,.3,leather);trim.ring(side*2.3,5.5,6.38,.52,.68,.17,metal);}break;
                case HAT:case BANDANA:case MINI_ME:
                    for(int side:new int[]{-1,1}){trim.gem(side*3,-6,-4.28,.35,.54,metal);trim.beam(side*2.2,-6.4,-4.21,side*3.7,-6.4,-4.21,.11,metal);}break;
                case AURA:trim.ring(0,0,-.14,1.1,.68,.1,metal);break;
                case NECKLACE:trim.ring(0,3,-2.95,.73,1,.13,metal);break;
                case BELT:for(int j=-3;j<=3;j++)trim.gem(j,10,-2.58,.13,.22,metal);break;
                case SCARF:for(int j=0;j<6;j++)trim.beam(-.55,j,.25,-.26,j+.28,.27,.10,metal);break;
                default:continue;
            }
            if(host.motion==CosmeticModels.Motion.MINIATURE){
                parts.remove(host);trim.bind(CosmeticModels.Anchor.BODY);trim.append(host.mesh,0,0,0,1,1,1);
            }
            parts.add(new CosmeticModels.Part(trim,host.anchor,host.motion,host.x,host.y,host.z,host.phase,host.scale));
        }
        return new CosmeticModels.Model(parts);
    }
    private DetailMesh(){}
}
