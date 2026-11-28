package com.astryx.client.features;

import static com.astryx.client.features.CosmeticModels.*;


/** Geometry is authored outward from the shoulder in model pixels, then mirrored as a whole. */
final class WingForms {
    private enum Form { DRAGON, BIRD, LEAF, INSECT, FIN, FACET, FLAME, SAIL }

    private static Form form(CosmeticCatalog.Item item){
        String theme=item.theme;
        if(!item.expanded()){
            switch(item.design){
                case MINECRAFT:return new Form[]{Form.LEAF,Form.DRAGON,Form.SAIL}[item.variant];
                case BEDWARS:return item.variant==2?Form.FACET:Form.SAIL;
                case SKYWARS:return item.variant==2?Form.DRAGON:Form.BIRD;
                case SKYBLOCK:return item.variant==1?Form.DRAGON:Form.FACET;
                case SMP:return new Form[]{Form.INSECT,Form.BIRD,Form.SAIL}[item.variant];
                default:return new Form[]{Form.BIRD,Form.DRAGON,Form.SAIL}[item.variant];
            }
        }
        switch(theme){
            case "Shadow":return Form.DRAGON;
            case "Monarch":case "Bee":case "Mushroom":case "Donut":case "Cookie":return Form.INSECT;
            case "Ocean":case "Koi":case "Coral":case "Watermelon":case "Aurora":return Form.FIN;
            case "Frog":case "Sakura":case "Autumn":case "Pumpkin":return Form.LEAF;
            case "Lava":case "Solar":case "Storm":return Form.FLAME;
            case "Prism":case "Ice":case "Rock":case "Cyber":return Form.FACET;
            case "Pizza":case "Astrolabe":case "Candy":return Form.SAIL;
            default:return Form.BIRD;
        }
    }
    static Model build(CosmeticCatalog.Item item){
        java.util.List<Part> parts=new java.util.ArrayList<>(2);
        for(int side:new int[]{-1,1}){
            Builder surface=new Builder();
            make(surface,side,item);
            Part p=new Part(surface,Anchor.BODY,side<0?Motion.LEFT_WING:Motion.RIGHT_WING,side*2.65f,2.6f,2f,side,1);
            // Both sides share a continuous textile grain from the shoulder through the tips.
            for(int k=0,j=0;k<p.mesh.vertices.length;k+=3){
                p.mesh.surfaceUV[j++]=side*p.mesh.vertices[k];
                p.mesh.surfaceUV[j++]=p.mesh.vertices[k+1];
            }
            parts.add(p);
        }
        return new Model(parts);
    }
    private static void make(Builder target,int side,CosmeticCatalog.Item item){
        Form form=form(item);
        boolean ender=item.id.equals("minecraft_wings_v2");
        int base=item.primary, trim=item.accent;
        if(item.id.equals("bedwars_wings_v3")){base=0xff63bec9;trim=0xffddf6ed;}
        if(item.id.equals("bedwars_wings_v2")){base=0xffa95242;trim=0xffe9bc66;}
        if(item.id.equals("skywars_wings_v3")){base=0xff403651;trim=0xffb492dc;}
        if(item.id.equals("smp_wings")){base=0xff826848;trim=0xffd4bf80;}
        if(ender){base=0xff514257;trim=0xff271f2d;}
        if(item.theme.equals("Angel")){base=0xffe5dfd0;trim=0xfffffaf1;}
        if(item.theme.equals("Bee")){base=0xffabd8de;trim=0xfff6d065;}
        if(item.theme.equals("Monarch")){base=0xffdf802f;trim=0xff35282b;}
        if(item.theme.equals("Duck")){base=0xffe7ddbf;trim=0xffffecca;}
        if(item.theme.equals("Pizza")){base=0xffecc668;trim=0xffab6a38;}
        if(item.theme.equals("Cookie")){base=0xffc58b50;trim=0xff895932;}
        if(item.theme.equals("Watermelon")){base=0xffeb645a;trim=0xff68a557;}
        if(item.theme.equals("Koi")){base=0xfff0e5c9;trim=0xffd08555;}
        if(item.theme.equals("Skywars")&&item.variant==0){base=0xff363643;trim=0xff7f8497;}
        if(item.theme.equals("SMP")&&item.variant==1){base=0xffc7a580;trim=0xffefddba;}
        if(item.theme.equals("Minecraft")&&item.variant==2){base=0xff80433b;trim=0xffd0955f;}
        Builder b=new Builder();
        switch(form){
            case DRAGON:dragon(b,base,ender?0xff211e25:shade(trim,.57f));break;
            case INSECT:insect(b,base,trim,item.theme.equals("Bee"));break;
            case FIN:fin(b,base,trim,item.theme.equals("Coral")?7:5);break;
            case SAIL:sail(b,base,trim);break;
            default:vanes(b,form,base,trim,item.theme.equals("Angel"));break;
        }
        markings(b,item,base,trim);
        Mesh m=b.mesh();
        double span=1+(item.variant==1?.05:item.variant==2?.16:item.variant==3?.09:0);
        double sweep=item.variant==1?-.23:item.variant==2?-.07:item.variant==3?.10:0;
        double droop=0,aspect=1,tipCurl=0;
        switch(item.theme){
            case "Duck":aspect=.83;droop=8;tipCurl=3;break;
            case "Bread":aspect=.87;droop=6;tipCurl=2;break;
            case "Skyblock":aspect=1.12;droop=4;tipCurl=2;break;
            case "Skywars":aspect=.94;droop=1.8;tipCurl=-2;break;
            case "SMP":aspect=.93;droop=3;tipCurl=1;break;
            case "Astryx":aspect=1.06;droop=-1;tipCurl=-3;break;
            case "Bedwars":aspect=.97;droop=2;break;
            case "Solar":tipCurl=-4;droop=-1;break;
            case "Lava":tipCurl=4;droop=2;break;
            case "Aurora":aspect=1.15;tipCurl=-4;break;
            case "Donut":aspect=.90;droop=2;tipCurl=5;break;
            case "Panda":case "Cookie":aspect=.86;droop=5;tipCurl=4;break;
            case "Fox":case "Autumn":aspect=1.10;droop=4;tipCurl=-3;break;
            case "Cat":case "Lunar":tipCurl=-8;droop=5;break;
            case "Nebula":tipCurl=5;droop=3;break;
            case "Galaxy":aspect=1.10;tipCurl=-4;break;
            case "Stardust":aspect=1.07;tipCurl=-6;break;
            case "Bee":case "Ocean":aspect=1.04;droop=9;tipCurl=2;break;
            case "Coffee":case "Frog":aspect=.88;droop=4;tipCurl=5;break;
            case "Monarch":case "Sakura":tipCurl=-5;droop=3;break;
            case "Rock":case "Cyber":aspect=.88;tipCurl=6;break;
            case "Candy":aspect=.96;tipCurl=2;break;
            case "Shadow":aspect=1.07;tipCurl=-4;break;
            case "Pumpkin":aspect=.92;droop=3;break;
            case "Prism":aspect=1.09;tipCurl=-3;break;
            case "Astrolabe":aspect=.93;tipCurl=4;break;
            case "Angel":aspect=1.08;tipCurl=-5;break;
            default:break;
        }
        // Use one continuous transform for the surface, structural ribs, and inlay.
        for(int k=0;k<m.vertices.length;k+=3){
            double x=m.vertices[k],y=m.vertices[k+1],z=m.vertices[k+2];
            double t=Math.min(1,Math.max(0,x/30));
            m.vertices[k]=(float)(side*x*span*(1+(aspect-1)*t));
            double split=item.variant==3?Math.max(0,1-Math.abs(x-20)/4)*Math.max(0,y+2)*.72:0;
            m.vertices[k+1]=(float)(y+sweep*x*t-split+droop*t*t+tipCurl*t*t*t*t);
            // Tilt the whole trailing surface behind the shoulder. The old local fold
            // displaced neighbouring facets by different amounts and exposed pointed fins.
            m.vertices[k+2]=(float)(z*.64+x*.60+Math.max(0,y)*.70+(item.variant==3?3:1)*t*t);
        }
        target.append(m,0,0,0,1,1,1);
    }
    // A raised keel gives each vane two separately lit faces and a tapered point.
    private static void vane(Builder b,double x,double y,double z,double tx,double ty,double tz,double width,double bend,int c){
        double dx=tx-x,dy=ty-y,len=Math.hypot(dx,dy),nx=-dy/len,ny=dx/len;
        double mx=x+dx*.48+nx*bend,my=y+dy*.48+ny*bend,mz=(z+tz)*.5;
        double lx=mx+nx*width,ly=my+ny*width,rx=mx-nx*width*.78,ry=my-ny*width*.78;
        double keel=mz+Math.min(1.25,width*.34);
        b.triangle(x,y,z,lx,ly,mz,mx,my,keel,c);
        b.triangle(lx,ly,mz,tx,ty,tz,mx,my,keel,c);
        b.triangle(x,y,z,mx,my,keel,rx,ry,mz,shade(c,.83f));
        b.triangle(mx,my,keel,tx,ty,tz,rx,ry,mz,shade(c,.83f));
    }
    private static void vanes(Builder b,Form form,int c,int a,boolean angel){
        boolean crystal=form==Form.FACET,leaf=form==Form.LEAF,fire=form==Form.FLAME;
        int count=crystal?7:leaf?8:fire?9:11;
        b.plate(new double[]{0,0,9,-10,18,-15,24,-16,18,-7,11,-1,0,6},0,.48,c);
        for(int n=0;n<count;n++){
            double t=n/(double)(count-1),x=9+14*t,y=-8-9*t+2*t*t;
            double tx=10+23*t+(fire?Math.sin(n*2)*1.4:0),ty=9-29*Math.pow(t,1.38);
            vane(b,x,y,1+.7*t,tx,ty,1.6+2*t,crystal?2.9:leaf?3.1:2.25,
                 crystal?0:fire?-2.2:leaf?1.5:.85,n%4==2?mix(c,a,.38):c);
            if(leaf||angel)b.beam(x,y,2,tx*.84+x*.16,ty*.84+y*.16,3,.13,shade(a,.90f));
        }
        // Short coverts overlap along the full leading edge, including the shoulder.
        for(int n=0;n<10;n++){
            double t=n/9d,x=t*20,y=-13*Math.sin(t*1.40);
            vane(b,x,y,1.0, x+3.4+4*t,y-6.5+9*t,1.9,1.4+.65*t,leaf?.65:.32,mix(c,a,.22+.3*t));
        }
    }
    private static int mix(int c,int a,double t){return 0xff000000|((int)(((c>>16)&255)*(1-t)+((a>>16)&255)*t)<<16)|((int)(((c>>8)&255)*(1-t)+((a>>8)&255)*t)<<8)|(int)((c&255)*(1-t)+(a&255)*t);}
    private static void spar(Builder b,double[] p,double[] q,double w,double end,int c){
        double dx=q[0]-p[0],dy=q[1]-p[1],len=Math.hypot(dx,dy),nx=-dy/len,ny=dx/len;
        double[] widths={w,end};double[][][] rings=new double[2][4][3];
        for(int k=0;k<2;k++){
            double[] v=k==0?p:q;double r=widths[k];
            for(int j=0;j<4;j++){
                double lateral=(j==0||j==3?-1:1)*r,depth=(j<2?-.55:.65)*r;
                rings[k][j]=new double[]{v[0]+nx*lateral,v[1]+ny*lateral,v[2]+depth};
            }
        }
        for(int j=0;j<4;j++){
            int next=(j+1)%4;double[] u=rings[0][j],v=rings[1][j],w1=rings[1][next],s=rings[0][next];
            b.quad(u[0],u[1],u[2],v[0],v[1],v[2],w1[0],w1[1],w1[2],s[0],s[1],s[2],c);
        }
    }
    private static double[] knuckle(double[] wrist,double[] tip){
        return new double[]{(wrist[0]+tip[0])*.5+1.4,(wrist[1]+tip[1])*.5-1.3,(wrist[2]+tip[2])*.5+1.6};
    }
    private static void dragon(Builder b,int skin,int bone){
        double[] root={0,0,0}, heel={0,8,.3}, elbow={6,-7,2},wrist={13,-16,4};
        double[][] fingers={{31,-10,1},{32,0,2},{25,8,3},{16,12,3},heel};
        b.triangle(0,0,0,6,-7,2,0,8,.3,skin);
        double[] inner=knuckle(wrist,heel);
        b.triangle(6,-7,2,13,-16,4,inner[0],inner[1],inner[2],skin);
        b.triangle(6,-7,2,inner[0],inner[1],inner[2],0,8,.3,skin);
        for(int n=0;n<fingers.length-1;n++){
            double[] p=fingers[n],q=fingers[n+1],prev=p;
            double[] mp=knuckle(wrist,p),mq=knuckle(wrist,q),lastMid=mp;
            for(int k=1;k<=6;k++){
                double t=k/6d,arch=Math.sin(Math.PI*t),x=p[0]+(q[0]-p[0])*t,y=p[1]+(q[1]-p[1])*t;
                double[] next={x+(wrist[0]-x)*arch*.26,y+(wrist[1]-y)*arch*.26,p[2]+(q[2]-p[2])*t+arch*1.2};
                double[] mid={mp[0]+(mq[0]-mp[0])*t,mp[1]+(mq[1]-mp[1])*t,mp[2]+(mq[2]-mp[2])*t+arch*1.4};
                int panel=mix(skin,0xff94749e,.30*arch);
                b.triangle(wrist[0],wrist[1],wrist[2],lastMid[0],lastMid[1],lastMid[2],mid[0],mid[1],mid[2],panel);
                b.quad(lastMid[0],lastMid[1],lastMid[2],prev[0],prev[1],prev[2],next[0],next[1],next[2],mid[0],mid[1],mid[2],panel);
                prev=next;lastMid=mid;
            }
        }
        spar(b,root,elbow,.95,1.15,bone);spar(b,elbow,wrist,1.15,1.45,bone);
        for(int n=0;n<4;n++){
            double[] tip=fingers[n],mid=knuckle(wrist,tip);
            spar(b,wrist,mid,n==0?1.26:.76,n==0?.90:.49,bone);spar(b,mid,tip,n==0?.90:.49,.08,bone);
        }
        spar(b,wrist,new double[]{14.8,-18.7,4.5},.74,.10,0xff8b7795);
    }
    private static void insect(Builder b,int c,int a,boolean bee){
        double[] fore=bee?new double[]{0,0,9,-11,21,-18,28,-18,31,-14,30,-10,24,-5,13,-2,0,3}:new double[]{0,0,10,-12,23,-21,30,-22,32,-20,31,-12,26,-4,15,-1,0,3};
        double[] hind={0,2,13,-4,23,-4,25,0,22,7,17,11,12,10,10,6,0,6};
        b.plate(fore,1,.4,c);b.plate(hind,.45,.42,mix(c,a,.28));
        for(int k=2;k<fore.length-2;k+=2)b.beam(0,1,1.52,fore[k],fore[k+1],1.52,bee?.15:.28,a);
        for(int j=0;j<5;j++)b.gem(28-j*.95,-15+j*2.3,1.7,.55,.82,0xfffaf0c9);
        b.triangle(0,0,0,10,-12,1,0,4,1,c);
    }
    private static void fin(Builder b,int c,int a,int rays){
        for(int j=0;j<rays;j++){
            double t=j/(double)(rays-1),angle=-.70+t*1.48,x=13+19*Math.cos(angle)*(1-t*.55),y=-8+21*Math.sin(angle);
            vane(b,0,j*.9,0,x,y,3+t,3.9,2,mix(c,a,t*.7));
            vane(b,0,j*.9,.18,x,y,3.4+t,.5,1.5,a);
        }
    }
    private static void sail(Builder b,int c,int a){
        double[][] leading={{0,0,0},{10,-9,1.5},{20,-14,2.2},{31,-16,1.5}};
        double[][] trailing={{0,7,.3},{11,8,3},{20,2,4},{30,-9,2}};
        for(int j=0;j<3;j++){
            double[] p=leading[j],q=leading[j+1],r=trailing[j+1],s=trailing[j];
            double mx=(r[0]+s[0])*.5,my=(r[1]+s[1])*.5-1.8,mz=(r[2]+s[2])*.5+.55;
            int panel=mix(c,a,.12*j);
            b.triangle(p[0],p[1],p[2],q[0],q[1],q[2],mx,my,mz,panel);
            b.triangle(q[0],q[1],q[2],r[0],r[1],r[2],mx,my,mz,panel);
            b.triangle(p[0],p[1],p[2],mx,my,mz,s[0],s[1],s[2],panel);
            spar(b,p,q,.65,.46,a);spar(b,q,r,.35,.16,shade(a,.85f));
        }
    }
    // Large markings follow the flight panels and stay legible at nameplate distance.
    private static void markings(Builder b,CosmeticCatalog.Item item,int c,int a){
        switch(item.theme){
            case "Pizza":
                spar(b,new double[]{9,-9,3},new double[]{20,-14,4},1.1,.9,0xffc58c4d);
                spar(b,new double[]{20,-14,4},new double[]{31,-16,4},.9,.5,0xffe6ad67);
                for(double[] p:new double[][]{{12,-1,1.5},{17,-7,1.7},{22,-6,1.35},{25,-11,1.25}}){
                    spot(b,p[0],p[1],4.8,p[2],p[2]*.85,0xffac412d);
                    spot(b,p[0]-.30,p[1]-.25,4.9,.26,.18,0xffed965f);
                }break;
            case "Cookie":
                for(double[] p:new double[][]{{13,-8},{18,-11},{24,-15},{27,-12},{14,3},{19,4},{23,-8}})
                    b.gem(p[0],p[1],2.8,1,.85,0xff583725);
                break;
            case "Panda":case "Fox":case "Cat":
                for(int k=0;k<5;k++){
                    double x=12+3.4*k,y=-4-1.8*k;
                    int coat=item.theme.equals("Fox")?0xfff4e2c9:item.theme.equals("Panda")?0xff322e35:0xffbaa09e;
                    vane(b,x,y,4.2,x+1.9,y+3.8,4.7,1.2,.2,coat);
                }break;
            case "Coffee":
                for(int j=0;j<3;j++){
                    double x=13+j*4.9,y=-5-j*2;
                    spot(b,x,y,4.6,1.6,2,0xff673d2e);
                    b.beam(x-.45,y-1.5,4.9,x+.5,y+1.5,4.9,.23,0xffc69768);
                }break;
            case "Monarch":
                for(int i=0;i<6;i++){
                    spot(b,29-i*.91,-18+i*2.2,2.4,.75,.56,0xffffe9bf);
                    spot(b,22-i*1.5,3+i*.85,2.4,.58,.5,0xfff8daa4);
                }break;
            case "Ice":case "Prism":case "Rock":
                for(int i=0;i<4;i++){
                    double x=13+i*3,y=-8-i*1.8;
                    vane(b,x,y,3.3,x+3,y-3.4-i*.55,4.2,1.35,0,mix(c,a,.44+i*.14));
                }break;
            case "Frog":case "Autumn":case "Sakura":
                for(int i=0;i<4;i++){
                    double x=11+i*3.9,y=-7-i*1.65;
                    vane(b,x,y,3.65,x+4.6,y+4.8-i*.7,4.1,1.5,.5,mix(c,a,.32));
                }break;
            case "Ocean":case "Koi":case "Watermelon":
                for(int i=0;i<7;i++){
                    double x=15+(i%4)*3,y=-6+(i/4)*5-(i%3)*1.2;
                    if(item.theme.equals("Koi"))spot(b,x,y,4.4,1.15+(i%2)*.5,.92,i%3==1?0xff373536:0xffd15c38);
                    else if(item.theme.equals("Watermelon"))b.gem(x,y,4.4,.33,.63,0xff342824);
                    else spot(b,x,y,4.5,.48,.58,0xffbde9ed);
                }break;
            case "Galaxy":case "Stardust":case "Lunar":
                for(int i=0;i<4;i++)CosmeticThemes.star(b,13+i*4,-5-i*2.3+(i%2)*2.5,4.5,.62+(i%2)*.40,a);
                break;
            case "Cyber":
                for(int i=0;i<4;i++){double x=12+i*3.7,y=-5-i*2;b.beam(x,y,4.1,x+2.3,y-1.8,4.1,.32,a);}
                break;
            default:break;
        }
    }
    private static void spot(Builder b,double x,double y,double z,double rx,double ry,int color){
        double[] polygon=new double[20];
        for(int i=0;i<10;i++){double angle=i*Math.PI/5;polygon[2*i]=x+rx*Math.cos(angle);polygon[2*i+1]=y+ry*Math.sin(angle);}
        b.plate(polygon,z,.12,color);
    }
    private WingForms(){}
}
