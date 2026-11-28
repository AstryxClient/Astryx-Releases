package com.astryx.client.features;

import java.util.*;

/** Authored, cached triangle meshes in vanilla model pixels. Also used by wardrobe previews. */
public final class CosmeticModels {
    public enum Anchor { BODY, HEAD, LEFT_LEG, RIGHT_LEG, LEFT_ARM, RIGHT_ARM, MINI_TAIL }
    public enum Motion { STILL, CLOTH, LEFT_WING, RIGHT_WING, ORBIT, MINIATURE, SCARF, TAIL }
    public static final class Mesh {
        public final float[] vertices;
        public final int[] colors;
        public final float[] surfaceUV;
        public final byte[] bones;
        Mesh(List<Float> vertices,List<Integer> colors,List<Byte> joints) {
            bones=new byte[joints.size()];for(int n=0;n<bones.length;n++)bones[n]=joints.get(n);
            this.vertices=new float[vertices.size()];for(int n=0;n<vertices.size();n++)this.vertices[n]=vertices.get(n);
            this.colors=new int[colors.size()];for(int n=0;n<colors.size();n++)this.colors[n]=colors.get(n);
            surfaceUV=new float[this.colors.length*6];
            for(int t=0;t<this.colors.length;t++){
                int at=t*9;float[] v=this.vertices;
                float ux=v[at+3]-v[at],uy=v[at+4]-v[at+1],uz=v[at+5]-v[at+2],vx=v[at+6]-v[at],vy=v[at+7]-v[at+1],vz=v[at+8]-v[at+2];
                float nx=Math.abs(uy*vz-uz*vy),ny=Math.abs(uz*vx-ux*vz),nz=Math.abs(ux*vy-uy*vx);
                for(int j=0;j<3;j++){int pos=at+3*j,uv=t*6+2*j;
                    surfaceUV[uv]=nx>nz&&nx>ny?v[pos+2]:v[pos];
                    surfaceUV[uv+1]=ny>nx&&ny>nz?v[pos+2]:v[pos+1];
                }
            }
        }
        public int triangles(){return colors.length;}
    }
    public static final class Part {
        public final Mesh mesh;public final Anchor anchor;public final Motion motion;
        public final float x,y,z,phase,scale;
        Part(Builder b,Anchor anchor,Motion motion,float x,float y,float z,float phase,float scale) {
            mesh=b.mesh();this.anchor=anchor;this.motion=motion;this.x=x;this.y=y;this.z=z;this.phase=phase;this.scale=scale;
        }
    }
    public static final class Model {
        public final List<Part> parts;
        Model(List<Part> parts){this.parts=Collections.unmodifiableList(parts);}
    }
    private static final Map<String,Model> MODELS=new LinkedHashMap<String,Model>(128,.75f,true){protected boolean removeEldestEntry(Map.Entry<String,Model> entry){return size()>256;}};
    public static Model get(CosmeticCatalog.Item item) {synchronized(MODELS){Model m=MODELS.get(item.id);if(m!=null)return m;}Model m=build(item);if(!item.expanded())m=DetailMesh.finishLegacy(item,m);synchronized(MODELS){MODELS.put(item.id,m);}return m;}
    private static final java.util.concurrent.ExecutorService meshWorker=java.util.concurrent.Executors.newSingleThreadExecutor(r->{Thread t=new Thread(r,"Astryx cosmetic meshes");t.setDaemon(true);return t;});
    private static final Set<String> queued=new HashSet<String>();
    public static Model ready(CosmeticCatalog.Item item){synchronized(MODELS){Model model=MODELS.get(item.id);if(model!=null)return model;if(!queued.add(item.id))return null;}
        meshWorker.execute(()->{try{Model built=build(item);if(!item.expanded())built=DetailMesh.finishLegacy(item,built);synchronized(MODELS){MODELS.put(item.id,built);}}finally{synchronized(MODELS){queued.remove(item.id);}}});return null;}
    public static final class Builder {
        private final List<Float> vertices=new ArrayList<Float>();private final List<Integer> colors=new ArrayList<Integer>();
        private final List<Byte> joints=new ArrayList<>();private byte joint;
        void bind(Anchor anchor){joint=(byte)anchor.ordinal();}
        public void triangle(double x,double y,double z,double X,double Y,double Z,double u,double v,double w,int color) {
            double nx=(Y-y)*(w-z)-(Z-z)*(v-y),ny=(Z-z)*(u-x)-(X-x)*(w-z),nz=(X-x)*(v-y)-(Y-y)*(u-x);
            double length=Math.sqrt(nx*nx+ny*ny+nz*nz);if(length<.000001)return;
            float light=(float)(.74+.18*Math.abs(ny/length)+.10*Math.abs(nz/length)+.09*nx/length);
            joints.add(joint);colors.add(shade(color,light));double[] values={x,y,z,X,Y,Z,u,v,w};for(double value:values)vertices.add((float)value);
        }
        public void quad(double x,double y,double z,double X,double Y,double Z,double u,double v,double w,double U,double V,double W,int color) {
            triangle(x,y,z,X,Y,Z,u,v,w,color);triangle(x,y,z,u,v,w,U,V,W,color);
        }
        public void box(double x,double y,double z,double w,double h,double d,int c) {
            double X=x+w,Y=y+h,Z=z+d;
            quad(x,y,z,X,y,z,X,Y,z,x,Y,z,c);quad(X,y,Z,x,y,Z,x,Y,Z,X,Y,Z,c);
            quad(x,y,Z,x,y,z,x,Y,z,x,Y,Z,c);quad(X,y,z,X,y,Z,X,Y,Z,X,Y,z,c);
            quad(x,y,Z,X,y,Z,X,y,z,x,y,z,c);quad(x,Y,z,X,Y,z,X,Y,Z,x,Y,Z,c);
        }
        /** Closed plate in the XY plane, with ear clipping for concave outlines. */
        public void plate(double[] xy,double z,double depth,int c) {
            int count=xy.length/2;
            List<Integer> polygon=new ArrayList<Integer>();double area=0;
            for(int n=0;n<count;n++){polygon.add(n);int k=(n+1)%count;area+=xy[n*2]*xy[k*2+1]-xy[k*2]*xy[n*2+1];}
            double orientation=area>=0?1:-1;
            while(polygon.size()>2){
                boolean clipped=false;
                for(int at=0;at<polygon.size();at++){
                    int prev=polygon.get((at+polygon.size()-1)%polygon.size()),cur=polygon.get(at),next=polygon.get((at+1)%polygon.size());
                    double ax=xy[prev*2],ay=xy[prev*2+1],bx=xy[cur*2],by=xy[cur*2+1],cx=xy[next*2],cy=xy[next*2+1];
                    if(((bx-ax)*(cy-ay)-(by-ay)*(cx-ax))*orientation<=.0000001)continue;
                    boolean occupied=false;
                    for(int v:polygon){if(v==prev||v==cur||v==next)continue;double px=xy[v*2],py=xy[v*2+1];
                        if(((bx-ax)*(py-ay)-(by-ay)*(px-ax))*orientation>=0&&((cx-bx)*(py-by)-(cy-by)*(px-bx))*orientation>=0&&((ax-cx)*(py-cy)-(ay-cy)*(px-cx))*orientation>=0){occupied=true;break;}}
                    if(occupied)continue;
                    triangle(ax,ay,z,bx,by,z,cx,cy,z,c);triangle(ax,ay,z+depth,cx,cy,z+depth,bx,by,z+depth,c);
                    polygon.remove(at);clipped=true;break;
                }
                if(!clipped)throw new IllegalArgumentException("Cosmetic plate must have a simple outline");
            }
            for(int n=0;n<count;n++){int next=(n+1)%count;quad(xy[n*2],xy[n*2+1],z,xy[next*2],xy[next*2+1],z,xy[next*2],xy[next*2+1],z+depth,xy[n*2],xy[n*2+1],z+depth,shade(c,.8f));}
        }
        public void beam(double x,double y,double z,double X,double Y,double Z,double width,int c) {
            double dx=X-x,dy=Y-y,dz=Z-z,len=Math.sqrt(dx*dx+dy*dy+dz*dz);if(len<.00001)return;
            double ax=-dy,ay=dx,az=0,aLen=Math.sqrt(ax*ax+ay*ay);if(aLen<.0001){ax=1;ay=0;aLen=1;}
            ax=ax/aLen*width/2;ay=ay/aLen*width/2;double bx=(dy*az-dz*ay)/len,by=(dz*ax-dx*az)/len,bz=(dx*ay-dy*ax)/len;
            double[][] points={{x+ax+bx,y+ay+by,z+az+bz},{x-ax+bx,y-ay+by,z-az+bz},{x-ax-bx,y-ay-by,z-az-bz},{x+ax-bx,y+ay-by,z+az-bz},
                {X+ax+bx,Y+ay+by,Z+az+bz},{X-ax+bx,Y-ay+by,Z-az+bz},{X-ax-bx,Y-ay-by,Z-az-bz},{X+ax-bx,Y+ay-by,Z+az-bz}};
            int[][] faces={{0,1,2,3},{7,6,5,4},{0,4,5,1},{1,5,6,2},{2,6,7,3},{3,7,4,0}};
            for(int[] f:faces){double[] a=points[f[0]],b=points[f[1]],d=points[f[2]],e=points[f[3]];quad(a[0],a[1],a[2],b[0],b[1],b[2],d[0],d[1],d[2],e[0],e[1],e[2],c);}
        }
        public void gem(double x,double y,double z,double radius,double height,int c) {
            double[][] rim={{x-radius,y,z},{x,y,z-radius*.7},{x+radius,y,z},{x,y,z+radius*.7}};
            for(int n=0;n<4;n++){double[] a=rim[n],b=rim[(n+1)%4];triangle(x,y-height,z,a[0],a[1],a[2],b[0],b[1],b[2],n%2==0?c:shade(c,1.18f));triangle(x,y+height*.8,z,b[0],b[1],b[2],a[0],a[1],a[2],shade(c,.78f));}
        }
        public void ring(double x,double y,double z,double rx,double ry,double width,int c) {
            for(int n=0;n<16;n++){double a=n*Math.PI/8,b=(n+1)*Math.PI/8;beam(x+Math.cos(a)*rx,y+Math.sin(a)*ry,z,x+Math.cos(b)*rx,y+Math.sin(b)*ry,z,width,c);}
        }
        public void append(Mesh mesh,double x,double y,double z,double sx,double sy,double sz){
            for(int n=0;n<mesh.vertices.length;n+=3){vertices.add((float)(x+mesh.vertices[n]*sx));vertices.add((float)(y+mesh.vertices[n+1]*sy));vertices.add((float)(z+mesh.vertices[n+2]*sz));}
            for(int t=0;t<mesh.colors.length;t++){colors.add(mesh.colors[t]);joints.add(joint==0?mesh.bones[t]:joint);}
        }
        public Mesh mesh(){return new Mesh(vertices,colors,joints);}
    }
    private static final int GOLD=0xFFE6BD67,SILVER=0xFFCAD5E8,DARK=0xFF17202C,WHITE=0xFFF5ECD9,LEATHER=0xFF694631;
    public static int shade(int c,float amount){return (c&0xFF000000)|Math.min(255,(int)((c>>16&255)*amount))<<16|Math.min(255,(int)((c>>8&255)*amount))<<8|Math.min(255,(int)((c&255)*amount));}
    private static void part(List<Part> out,Builder b,Anchor a,Motion m,float x,float y,float z,float phase,float scale){out.add(new Part(b,a,m,x,y,z,phase,scale));}
    private static void part(List<Part> out,Builder b,Anchor a){part(out,b,a,Motion.STILL,0,0,0,0,1);}
    private static Model build(CosmeticCatalog.Item i) {
        if(i.slot==CosmeticCatalog.Slot.WINGS)return WingForms.build(i);
        if(i.expanded())return ExpandedCosmetics.build(i);
        if(i.slot==CosmeticCatalog.Slot.SCARF)return CosmeticVariants.scarf(i);
        if(i.variant>0)return CosmeticVariants.build(i);
        if(i.design==CosmeticCatalog.Theme.ASTRYX&&i.slot!=CosmeticCatalog.Slot.CLOAK)return CosmeticThemes.astryx(i);
        if(i.design==CosmeticCatalog.Theme.BEDWARS&&i.slot!=CosmeticCatalog.Slot.CLOAK)return CosmeticThemes.bedwars(i);
        List<Part> out=new ArrayList<Part>();Builder b=new Builder();
        switch(i.slot) {
            case CLOAK:cloak(b,i);part(out,b,Anchor.BODY,Motion.CLOTH,0,0,2.65f,0,1);break;
            case HAT:hat(b,i);part(out,b,Anchor.HEAD);break;
            case BANDANA:bandana(b,i);part(out,b,Anchor.HEAD);break;
            case BACKPACK:backpack(b,i);part(out,b,Anchor.BODY);break;
            case MINI_ME:miniatureAccessories(b,i);part(out,b,Anchor.BODY,Motion.MINIATURE,6.25f,-4.67f,0,0,.18f);break;
            case AURA:
                for(int n=0;n<6;n++){Builder mote=new Builder();mote(mote,i,n);part(out,mote,Anchor.BODY,Motion.ORBIT,0,0,0,n*(float)Math.PI/3,1);}break;
            case NECKLACE:necklace(b,i);part(out,b,Anchor.BODY);break;
            case BELT:belt(b,i);part(out,b,Anchor.BODY);break;
            case BOOTS:
                for(Anchor anchor:new Anchor[]{Anchor.LEFT_LEG,Anchor.RIGHT_LEG}){Builder boot=new Builder();boot(boot,i);part(out,boot,anchor);}break;
        }
        return new Model(out);
    }
    private static void pixels(Builder b,String[] pattern,double x,double y,double z,double unit,int color) {
        for(int row=0;row<pattern.length;row++)for(int col=0;col<pattern[row].length();col++)if(pattern[row].charAt(col)!=' ')
            b.box(x+col*unit,y+row*unit,z,unit,unit,.08,color);
    }
    private static String[] emblem(CosmeticCatalog.Theme t) {
        switch(t){
            case MINECRAFT:return new String[]{" ##   ## "," ##   ## ","    #    ","   ###   ","  #####  ","  #   #  ","  #   #  "};
            case BEDWARS:return new String[]{"         "," ##      "," ########"," ########"," ########"," #      #"," #      #"};
            case SKYWARS:return new String[]{"    #    ","  #####  "," ####### ","#########"," ####### ","  #####  ","   ###   ","    #    "};
            case SKYBLOCK:return new String[]{"    #    ","   ###   ","  #####  "," ####### "," ####### ","  #####  ","   ###   ","    #    "};
            case SMP:return new String[]{"    #    ","   ###   ","  ## ##  "," ### ### ","###   ###","#########","#########"};
            default:return new String[]{"    #    ","   # #   ","   # #   ","  #   #  ","  # # #  "," # ### # "," #     # ","#  ###  #","###   ###"};
        }
    }
    private static void cloak(Builder b,CosmeticCatalog.Item i) {
        int base=i.design==CosmeticCatalog.Theme.ASTRYX?0xFF201332:i.primary;
        for(int row=0;row<16;row++) {
            double inset=row<13?0:i.design==CosmeticCatalog.Theme.ASTRYX?(row-12)*.65:i.design==CosmeticCatalog.Theme.SKYWARS?(row%2)*.6:0;
            int c=shade(base,.78f+row*.016f);
            b.box(-5+inset,row,0,10-inset*2,1,.24,c);
            b.box(-5+inset,row,.25,.28,1,.04,i.accent);b.box(4.72-inset,row,.25,.28,1,.04,i.accent);
        }
        b.box(-5,0,.26,10,.35,.05,i.design==CosmeticCatalog.Theme.ASTRYX?SILVER:i.accent);
        b.box(-1.2,.35,.28,2.4,.45,.15,i.accent);
        if(i.design==CosmeticCatalog.Theme.ASTRYX){
            b.beam(-3.3,10.5,.34,0,3,.34,.8,0xFF513C78);b.beam(0,3,.34,3.3,10.5,.34,.8,0xFF513C78);
            b.beam(-3.3,10.5,.46,0,3,.46,.4,SILVER);b.beam(0,3,.46,3.3,10.5,.46,.4,SILVER);
            b.beam(-1.9,8,.46,1.9,8,.46,.3,SILVER);
            b.beam(-3.3,10.5,.46,0,9.5,.46,.35,i.accent);b.beam(0,9.5,.46,3.3,10.5,.46,.35,i.accent);
            b.gem(0,7.3,.65,.55,1.2,i.accent);
            b.box(-2.2,13,.28,4.4,.22,.1,i.accent);
            for(int n=0;n<7;n++)b.box((n*37%9)-4,1.8+(n*13%10),.29,.18,.18,.1,n%2==0?WHITE:i.accent);
        }else{
            if(i.design==CosmeticCatalog.Theme.BEDWARS)CosmeticThemes.bedIcon(b,0,7,.35,.9,false,i.primary);
            else pixels(b,emblem(i.design),-3.15,4,.3,.7,i.design==CosmeticCatalog.Theme.MINECRAFT?0xFF152F24:i.accent);
            if(i.design==CosmeticCatalog.Theme.BEDWARS){for(int x=-4;x<=4;x+=2)for(int y=2;y<15;y+=2)b.box(x,y,.28,.2,.45,.05,GOLD);}
            if(i.design==CosmeticCatalog.Theme.SKYWARS){b.box(-3.4,7.5,.4,6.8,.6,.06,0xFF68B597);for(int n=0;n<4;n++)b.box(-2.7+n*1.7,8,.4,1,2.4-n*.4,.07,LEATHER);}
            if(i.design==CosmeticCatalog.Theme.SKYBLOCK){b.beam(-4,2,.35,-2,6,.35,.2,SILVER);b.beam(4,12,.35,2,8,.35,.2,SILVER);b.gem(0,7,.6,1,2,i.accent);}
            if(i.design==CosmeticCatalog.Theme.SMP){b.box(-4,12,.3,8,.45,.1,0xFF75975B);b.box(2,2,.3,1.2,1.2,.1,GOLD);b.box(-.15,13,.35,.3,3,.12,DARK);}
            if(i.design==CosmeticCatalog.Theme.MINECRAFT)for(int n=0;n<14;n++)b.box((n*17%9)-4,1+(n*7%14),.27,.65,.55,.04,shade(i.primary,n%2==0?.7f:1.25f));
        }
    }
    private static void leaf(Builder b,double x,double y,double z,double w,double h,int c,double sign) {
        b.plate(new double[]{x,y,x+sign*w,y+h*.25,x+sign*(w-.5),y+h*.65,x,y+h,x-sign*.8,y+h*.5},z,.22,c);
        b.beam(x,y,z+.3,x,y+h,z+.3,.15,shade(c,1.3f));
    }
    private static void star(Builder b,double x,double y,double z,double r,int c){
        b.plate(new double[]{x,y-r,x+r*.3,y-r*.3,x+r,y,x+r*.3,y+r*.3,x,y+r,x-r*.3,y+r*.3,x-r,y,x-r*.3,y-r*.3},z,.3,c);
        b.gem(x,y,z+.2,r*.28,r*.5,WHITE);
    }
    private static void hat(Builder b,CosmeticCatalog.Item i) {
        int c=i.primary,a=i.accent;
        switch(i.design){
            case MINECRAFT:
                b.box(-4.5,-9,-4.5,9,1,9,c);b.box(-4,-12,-4,8,3,8,c);b.box(-4.5,-9,-6,9,.5,3,c);
                b.box(-2.6,-11,-4.15,1.5,1.4,.2,DARK);b.box(1.1,-11,-4.15,1.5,1.4,.2,DARK);b.box(-.6,-9.8,-4.2,1.2,1,.2,DARK);break;
            case BEDWARS:
                b.box(-4.5,-9,-4.5,9,1,9,WHITE);b.plate(new double[]{-4,-9,3,-9,2,-15,-1,-17,-3,-14},-3.5,7,c);b.box(-1.8,-17.5,-.5,2,2,2,WHITE);
                b.box(-4,-11,-3.65,7,.3,.2,GOLD);break;
            case SKYWARS:
                b.box(-4.4,-9,-4.4,8.8,1,8.8,0xFF242138);for(int n=0;n<5;n++){double x=-4+n*2;b.plate(new double[]{x,-9,x+.6,-13-(n%2),x+1.3,-9},-4.5,.6,a);}
                b.gem(0,-10,-4.7,.8,1.7,0xFF6AE3BC);b.box(-4.4,-9,3.8,8.8,1,.6,SILVER);break;
            case SKYBLOCK:
                b.box(-4.5,-10,-4.5,9,2,9,0xFFC7A04E);b.box(-5,-8.7,-5,10,.7,10,DARK);b.box(-1.5,-10.5,-5.2,3,2.8,1.5,DARK);b.box(-1,-10,-5.4,2,1.8,.3,WHITE);
                b.gem(3,-12,1,1.2,3,a);b.gem(1.3,-11,2.5,.8,2,c);break;
            case SMP:
                b.box(-6,-8.8,-6,12,.7,12,LEATHER);b.box(-4,-11.5,-4,8,3,8,c);b.box(-4.1,-9.6,-4.1,8.2,.7,8.2,DARK);leaf(b,3.8,-14,0,1.6,5,a,1);break;
            case ASTRYX:
                b.box(-4.4,-9,-4.4,8.8,.65,.5,SILVER);b.box(-4.4,-9,3.9,8.8,.65,.5,SILVER);b.box(-4.4,-9,-4.4,.5,.65,8.8,SILVER);b.box(3.9,-9,-4.4,.5,.65,8.8,SILVER);
                b.plate(new double[]{-2,-9,0,-12,2,-9},-4.6,.4,c);star(b,0,-10.4,-4.9,1.7,a);b.gem(-3.8,-10,0,.6,1.4,a);b.gem(3.8,-10,0,.6,1.4,a);break;
        }
    }
    private static void bandana(Builder b,CosmeticCatalog.Item i) {
        int c=i.primary,a=i.accent;
        b.box(-4.28,-6.7,-4.28,8.56,1.55,.32,c);b.box(-4.28,-6.7,3.96,8.56,1.55,.32,c);
        b.box(-4.28,-6.7,-4.28,.32,1.55,8.56,c);b.box(3.96,-6.7,-4.28,.32,1.55,8.56,c);
        b.box(-4.28,-6.7,-4.31,8.56,.18,.12,a);b.box(-4.28,-5.3,-4.31,8.56,.15,.12,a);
        b.box(1.8,-6.3,4.2,1.6,1.5,1,c);b.plate(new double[]{2,-5.2,3.2,-5.2,4,-1,2.6,-1.7},4.6,.25,c);b.plate(new double[]{1.8,-5,2.8,-5,1.5,-2,.5,-2.7},4.8,.25,a);
        if(i.design==CosmeticCatalog.Theme.MINECRAFT)for(int n=0;n<7;n++)b.box(-3.8+n*1.1,-6.4+(n%2)*.5,-4.4,.5,.5,.1,a);
        else if(i.design==CosmeticCatalog.Theme.SKYWARS){b.box(-2.5,-6.2,-4.45,1.4,.5,.1,a);b.box(1.1,-6.2,-4.45,1.4,.5,.1,a);}
        else if(i.design==CosmeticCatalog.Theme.SMP)leaf(b,0,-6.4,-4.5,1,1.3,a,1);
        else if(i.design==CosmeticCatalog.Theme.ASTRYX)star(b,0,-5.95,-4.5,.65,a);
        else if(i.design==CosmeticCatalog.Theme.BEDWARS){b.box(-1.2,-6.3,-4.45,2.4,.75,.1,a);b.box(-1.2,-6.3,-4.55,.5,.55,.1,WHITE);b.box(-1.2,-5.55,-4.45,.25,.4,.1,a);b.box(.95,-5.55,-4.45,.25,.4,.1,a);}
        else b.gem(0,-5.9,-4.6,.6,.7,a);
    }
    private static void straps(Builder b,int c){
        for(int s:new int[]{-1,1}){b.box(s<0?-3.2:2.5,.5,-2.4,.7,7,.35,c);b.box(s<0?-3.2:2.5,.5,2.3,.7,8,.4,c);b.box(s<0?-3.2:2.5,-.25,-2.4,.7,.7,5.1,c);}
    }
    private static void backpack(Builder b,CosmeticCatalog.Item i) {
        int c=i.primary,a=i.accent;straps(b,LEATHER);
        switch(i.design){
            case MINECRAFT:
                b.box(-3,3,2.65,6,6,6,0xFF9B7245);
                b.box(-3,2.98,2.65,6,.08,6,0xFF6A4328);
                for(int row=0;row<3;row++)for(int col=0;col<3;col++)b.box(-2.7+col*1.8,2.92,2.95+row*1.8,1.68,.06,1.68,0xFFC99A5E);
                b.box(-2.8,3.5,8.67,5.6,.35,.08,DARK);b.box(-2.8,8.3,8.67,5.6,.35,.08,DARK);
                b.beam(-1.5,7.5,8.85,1.2,4.5,8.85,.4,LEATHER);b.beam(-.3,4.2,8.9,2,5.7,8.9,.5,SILVER);
                b.box(-3.08,3.6,3.1,.08,.35,5.1,DARK);b.box(-3.08,8.3,3.1,.08,.35,5.1,DARK);break;
            case BEDWARS:
                b.box(-3,3,2.7,6,7,2.3,LEATHER);b.box(-4.4,1,3,8.8,2.4,3.6,WHITE);b.box(-2.8,1,3.1,7.2,2.3,3.6,c);
                b.box(-3.2,.8,2.9,.7,2.8,3.9,LEATHER);b.box(2.5,.8,2.9,.7,2.8,3.9,LEATHER);
                b.box(-2.8,5.5,5.1,2.4,3,1.1,c);b.box(.5,5.5,5.1,2.4,3,1.1,a);break;
            case SKYWARS:
                b.plate(new double[]{-4,5,4,5,3,8,0,11,-3,8},3,3.8,LEATHER);b.box(-4.1,4,2.8,8.2,1.2,4.2,0xFF61945F);
                b.box(-1.5,2.1,4.2,3,2,2,0xFF7D7068);b.gem(1,8,6.8,.7,1.7,a);b.beam(-2,8,4,-1,12,4,.35,DARK);b.beam(2,8,4,3,11,4,.35,DARK);break;
            case SKYBLOCK:
                b.plate(new double[]{-3,2,3,2,4.2,6,3,10,-3,10,-4.2,6},2.7,3.8,0xFF4A515B);
                b.plate(new double[]{-2.5,3,2.5,3,3,6,2,9,-2,9,-3,6},6.55,.4,DARK);
                b.gem(0,5.8,7.1,1.2,3,a);b.gem(-2,6.4,7,.7,1.8,c);b.gem(2,7,7,.8,1.5,0xFFBB8BE8);break;
            case SMP:
                b.box(-3.5,2,2.7,7,8,3.5,c);b.box(-3.7,2,2.7,7.4,2,3.7,0xFFAE9C66);b.box(-2.7,6,6.25,5.4,3,1,LEATHER);
                b.box(-4.1,.5,3,8.2,2,3.4,0xFF6B8055);b.box(-2.8,.3,2.9,.55,2.4,3.6,LEATHER);b.box(2.25,.3,2.9,.55,2.4,3.6,LEATHER);
                b.box(3.6,5,3.3,1.4,4,1.7,SILVER);b.box(3.8,4.5,3.7,1,.6,1,LEATHER);break;
            case ASTRYX:
                b.plate(new double[]{-3,2,3,2,4,6,2.5,10,-2.5,10,-4,6},2.7,2.5,c);
                b.ring(0,5.6,6,3.4,3.4,.5,SILVER);b.gem(0,5.6,6.2,1.6,2.5,a);b.box(-1.5,9,4,3,1.2,2,DARK);
                for(int s:new int[]{-1,1})b.plate(new double[]{s*2,9,s*3,9,s*2.8,12,s*1.7,11},4.5,.5,a);break;
        }
    }
    private static void miniatureAccessories(Builder b,CosmeticCatalog.Item i) {
        b.bind(Anchor.HEAD);hat(b,i);b.bind(Anchor.BODY);
        if(i.design==CosmeticCatalog.Theme.BEDWARS){b.box(-4,2,2.4,8,10,.4,i.primary);b.box(-3,3,2.9,6,.4,.1,GOLD);}
        else if(i.design==CosmeticCatalog.Theme.SKYBLOCK){b.box(-3,2,2.4,6,7,2,LEATHER);pickaxe(b,-1,2,4.5,.65,i.accent);}
        else if(i.design==CosmeticCatalog.Theme.SMP){b.box(-3,2,2.4,6,7,2,LEATHER);b.box(-4,.2,3,8,2,2.5,i.accent);}
        else if(i.design==CosmeticCatalog.Theme.SKYWARS){b.box(-4.3,0,-2.4,8.6,5,.4,DARK);b.gem(0,2,-2.7,1,1.4,i.accent);}
        else if(i.design==CosmeticCatalog.Theme.ASTRYX){b.box(-3,2,2.5,6,6,.5,i.primary);star(b,0,4,3,1.7,i.accent);}
    }
    private static void mote(Builder b,CosmeticCatalog.Item i,int index) {
        switch(i.design){
            case MINECRAFT:b.box(-.5,-.5,-.5,1,1,1,0xFFB93931);b.box(-.18,-.9,-.18,.36,.4,.36,0xFFFFC66E);break;
            case BEDWARS:b.plate(new double[]{0,-.1,-.9,-1,-1.7,-.5,-1.5,.2,0,1.5,1.5,.2,1.7,-.5,.9,-1},0,.3,0xFFE65B72);break;
            case SKYWARS:b.gem(0,0,0,.8,1.1,0xFF63CAB8);b.ring(0,0,0,1,1,.16,i.accent);break;
            case SKYBLOCK:b.gem(0,0,0,.7,1.6,index%2==0?i.accent:0xFFAA7CE6);break;
            case SMP:leaf(b,-.4,-1,0,1.3,2.3,index%2==0?0xFFE6B35E:0xFFB66F46,1);break;
            case ASTRYX:star(b,0,0,0,index%2==0?1.3:.8,i.accent);break;
        }
    }
    private static void chain(Builder b,int c) {
        b.beam(-3.1,.1,-2.5,-1.5,2.3,-2.6,.24,c);b.beam(-1.5,2.3,-2.6,0,2.8,-2.8,.24,c);
        b.beam(3.1,.1,-2.5,1.5,2.3,-2.6,.24,c);b.beam(1.5,2.3,-2.6,0,2.8,-2.8,.24,c);
        b.box(-3.1,-.15,-2.5,.25,.3,5,c);b.box(2.85,-.15,-2.5,.25,.3,5,c);
    }
    private static void necklace(Builder b,CosmeticCatalog.Item i) {
        chain(b,i.design==CosmeticCatalog.Theme.SMP?LEATHER:GOLD);
        switch(i.design){
            case MINECRAFT:b.plate(new double[]{-.9,3,0,2.5,.9,3,.9,4.5,0,5,-.9,4.5},-3.1,.5,GOLD);b.gem(0,3.8,-3.4,.55,1,0xFF6FCD83);break;
            case BEDWARS:b.box(-1.3,3.3,-3.2,2.6,1.1,.6,i.primary);b.box(-1.3,3.3,-3.3,.65,.7,.1,WHITE);b.box(-1.3,4.4,-3.2,.35,.5,.6,GOLD);b.box(.95,4.4,-3.2,.35,.5,.6,GOLD);break;
            case SKYWARS:b.ring(0,3.8,-3.2,1.1,1.3,.2,SILVER);b.gem(0,3.8,-3.3,.65,.85,0xFF5EC9AF);break;
            case SKYBLOCK:b.gem(0,3.8,-3.2,.95,1.6,i.accent);b.beam(-1,3,-3.3,0,2.3,-3.3,.2,SILVER);break;
            case SMP:b.ring(0,3.8,-3.1,1.2,1.2,.25,GOLD);b.plate(new double[]{0,2.8,.35,3.8,0,4.8,-.35,3.8},-3.3,.18,WHITE);b.plate(new double[]{-1,3.8,0,3.5,1,3.8,0,4.1},-3.35,.18,i.primary);break;
            case ASTRYX:star(b,0,3.8,-3.2,1.4,SILVER);b.gem(0,3.8,-3.5,.5,.8,i.accent);break;
        }
    }
    private static void pickaxe(Builder b,double x,double y,double z,double size,int c) {
        b.beam(x-1.5*size,y+3*size,z,x+1.3*size,y-1.2*size,z,.45*size,LEATHER);
        b.beam(x-.4*size,y-1.9*size,z,x+2.6*size,y+.1*size,z,.65*size,c);b.box(x+2.3*size,y,z,.6*size,1.2*size,.5*size,c);
    }
    private static void belt(Builder b,CosmeticCatalog.Item i) {
        int c=i.primary,a=i.accent;b.box(-4.3,9.5,-2.4,8.6,1.1,.35,LEATHER);b.box(-4.3,9.5,2.05,8.6,1.1,.35,LEATHER);b.box(-4.3,9.5,-2.4,.35,1.1,4.8,LEATHER);b.box(3.95,9.5,-2.4,.35,1.1,4.8,LEATHER);
        if(i.design==CosmeticCatalog.Theme.ASTRYX){b.ring(0,10,-2.7,1.1,.9,.25,SILVER);b.gem(0,10,-2.8,.5,.7,a);b.plate(new double[]{-2,10,-.6,10,-1.3,16,-2.8,15},-2.6,.3,c);b.plate(new double[]{.8,10,2,10,3,14,1.5,15},-2.6,.3,a);return;}
        b.box(-.8,9.4,-2.8,1.6,1.3,.4,GOLD);b.box(-.4,9.7,-2.85,.8,.65,.1,DARK);
        b.box(-4.8,10,-1.5,1.2,2.7,2,c);b.box(3.6,10,-1.5,1.2,2.7,2,c);b.box(-4.85,10,-1.6,1.3,.6,2.2,a);b.box(3.55,10,-1.6,1.3,.6,2.2,a);
        if(i.design==CosmeticCatalog.Theme.MINECRAFT)pickaxe(b,3.9,10,1.9,.6,SILVER);
        else if(i.design==CosmeticCatalog.Theme.SKYBLOCK){pickaxe(b,3.9,10,1.9,.7,a);b.gem(-4.7,9.6,-.5,.6,1.6,a);b.gem(-4.7,10.2,.6,.5,1.3,0xFFB895EC);}
        else if(i.design==CosmeticCatalog.Theme.BEDWARS){b.box(-3,10.6,-2.7,1.5,2,1.2,c);b.box(1.5,10.6,-2.7,1.5,2,1.2,WHITE);}
        else if(i.design==CosmeticCatalog.Theme.SKYWARS){b.gem(-4.9,11,0,.6,.8,a);b.beam(4,10,1.8,4,14.5,1.8,.4,SILVER);}
        else{b.box(-3,10.6,-2.7,1.5,2,1.1,WHITE);b.box(2,10.5,-2.7,1.2,2.5,1.1,0xFF677C73);}
    }
    private static void boot(Builder b,CosmeticCatalog.Item i) {
        int c=i.primary,a=i.accent;
        // Hollow cuffs: do not draw a solid box over the player's lower legs.
        b.box(-2.28,8.35,-2.3,4.56,3.2,.3,c);b.box(-2.28,8.35,2,4.56,3.2,.3,c);b.box(-2.28,8.35,-2.3,.3,3.2,4.6,c);b.box(1.98,8.35,-2.3,.3,3.2,4.6,c);
        b.box(-2.35,11.55,-2.8,4.7,.55,5.15,i.design==CosmeticCatalog.Theme.ASTRYX?SILVER:DARK);b.box(-2.2,10.8,-2.8,4.4,.75,.8,c);
        b.box(-2.3,7.8,-2.35,4.6,.55,.35,a);b.box(-2.3,7.8,2,4.6,.55,.35,a);
        CosmeticThemes.bootShell(b,i.primary,i.accent);
        switch(i.design){
            case MINECRAFT:for(int n=0;n<3;n++)b.box(-1.8+n*1.3,8.6+(n%2)*.8,-2.65,1,1.1,.4,0xFF8DAD63);break;
            case BEDWARS:b.box(-1.6,9,-2.65,3.2,.4,.3,GOLD);b.box(-1.6,10,-2.65,3.2,.4,.3,GOLD);b.box(-1.2,10.7,-2.95,2.4,.6,.3,WHITE);break;
            case SKYWARS:leaf(b,2.3,7.3,.7,1.7,3,a,1);leaf(b,-2.3,7.3,.7,1.7,3,a,-1);break;
            case SKYBLOCK:b.gem(0,9.3,-2.7,.8,1.2,a);b.plate(new double[]{-2,8.3,0,8.8,2,8.3,1.4,10.5,-1.4,10.5},-2.65,.3,SILVER);break;
            case SMP:for(int n=0;n<3;n++)b.beam(-1,8.5+n*.7,-2.65,1,8.9+n*.7,-2.65,.17,WHITE);b.box(-2.35,10.7,1.3,4.7,1,1,c);break;
            case ASTRYX:star(b,0,9.3,-2.7,.8,a);b.plate(new double[]{-1.5,8,0,7,1.5,8,1,9,-1,9},-2.6,.25,SILVER);b.gem(0,10.3,2.45,.6,1.2,a);break;
        }
    }
    private CosmeticModels() {}
}
