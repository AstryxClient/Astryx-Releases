package com.astryx.client.features;

import java.util.*;
import com.astryx.client.features.CosmeticModels.*;

/** Additional collections have two silhouettes per slot and stable, shareable IDs. */
final class ExpandedCosmetics {
    static final String[] THEMES={"Galaxy","Nebula","Aurora","Solar","Lunar","Stardust","Bread","Rock","Frog","Duck","Cat","Fox","Panda","Bee","Mushroom","Pumpkin","Watermelon","Donut","Coffee","Pizza","Candy","Cookie","Ocean","Coral","Ice","Lava","Storm","Sakura","Autumn","Cyber","Prism","Shadow","Koi","Monarch","Astrolabe"};
    private static final int[] COLORS={0x282049,0x532466,0x164B4D,0xC96B28,0x5C637E,0x343E76,0xBC793D,0x676972,0x4C914A,0xDFB537,0x7B7288,0xAE5C32,0xDFDDD4,0xD7AB32,0xA64545,0xB76E2F,0x4E985B,0xBD7A92,0x6C4639,0xFFC445,0xE34880,0xC08A56,0x24628C,0xB75F77,0x77B6D1,0xA34B28,0x3F5472,0xC688A4,0x966632,0x285665,0x736CB0,0x292835,0xBF5536,0xD68437,0x21365B};
    private static final int[] ACCENTS={0xE8D4FF,0xF4AADC,0x9AF8C0,0xFFE2A0,0xDCE7FF,0xC6D9FF,0xEFD397,0xCBD2D0,0xD4D990,0xFFF0B6,0xE4BCD2,0xEED5AD,0x393643,0x422B25,0xF0D7B2,0xDED086,0xF58B89,0xF1A6CD,0xC9A479,0xC54F38,0xF4CCC0,0x633F32,0x9DD4C5,0xF2B38C,0xE5F5FF,0xFFC35A,0xD3D9FC,0xEFD8C2,0xE9BD62,0x7CE7CF,0xD8C2FF,0x9D9AC1,0xF5E9CD,0x31252A,0xE9C58B};
    static void catalogue(List<CosmeticCatalog.Item> all,Map<String,CosmeticCatalog.Item> index){
        WardrobeAccessories.catalogue(all,index);
        for(int t=0;t<THEMES.length;t++)for(CosmeticCatalog.Slot slot:CosmeticCatalog.Slot.values())for(int variant=0;variant<4;variant++){
            if(WardrobeAccessories.handles(slot))continue;
            int primary=0xFF000000|COLORS[t],accent=0xFF000000|ACCENTS[t];
            if(t==32)accent=0xFFF4E8CF;if(t==33)accent=0xFF302825;if(t==34)accent=0xFFF0CC83;
            if(t<6)accent=0xFFE0C9FF;if(t==6)accent=0xFFF2D79B;if(t==7)accent=0xFFB6BBC4;
            String name=THEMES[t]+" "+slot.label;
            if(slot==CosmeticCatalog.Slot.MINI_ME)name=new String[]{"Little ","Trail Guide · ","Cape Kid · ","Pocket Pal · "}[variant]+THEMES[t];
            if(slot==CosmeticCatalog.Slot.WINGS)name=new String[]{"","Swept ","Long ","Split "}[variant]+THEMES[t]+" Wings";
            CosmeticCatalog.Item item=new CosmeticCatalog.Item(CosmeticCatalog.Theme.ASTRYX,THEMES[t],slot,name,"A "+THEMES[t].toLowerCase(Locale.ROOT)+" collection piece with "+(variant==0?"layered trim":"an alternate cut and inset detail")+(t<6?" and a living shimmer.":"."),primary,accent,variant);
            if(variant==0||slot==CosmeticCatalog.Slot.MINI_ME||slot==CosmeticCatalog.Slot.WINGS)all.add(item);index.put(item.id,item);
        }
    }
    private static void part(List<Part> out,Builder b,Anchor a,Motion m,float x,float y,float z,float phase,float scale){out.add(new Part(b,a,m,x,y,z,phase,scale));}
    static Model build(CosmeticCatalog.Item i){
        if(i.id.equals("halo_hat")){
            Builder ring=new Builder();
            // Nested round shells carry emission strength in the vertex colour alpha.
            double[] radii={.77,.63,.53,.43,.35};
            int[] emission={0x09ffe560,0x11ffeb63,0x26fff074,0x48fff588,0xffffffe0};
            for(int shell=0;shell<radii.length;shell++){
                for(int k=0;k<64;k++)for(int j=0;j<8;j++){
                    double[] verts=new double[12];
                    for(int v=0;v<4;v++){
                        double a=(k+(v==1||v==2?1:0))*Math.PI/32;
                        double t=(j+(v>=2?1:0))*Math.PI/4;
                        double r=4.15+radii[shell]*Math.cos(t);
                        verts[v*3]=r*Math.cos(a);verts[v*3+1]=radii[shell]*Math.sin(t);verts[v*3+2]=r*Math.sin(a);
                    }
                    ring.quad(verts[0],verts[1],verts[2],verts[3],verts[4],verts[5],verts[6],verts[7],verts[8],verts[9],verts[10],verts[11],emission[shell]);
                }
            }
            return new Model(Collections.singletonList(new Part(ring,Anchor.HEAD,Motion.STILL,0,-10.2f,0,0,1)));
        }
        if(WardrobeAccessories.handles(i.slot))return WardrobeAccessories.build(i);
        if(i.theme.equals("Rock"))return CollectionModels.rock(i);
        List<Part> parts=new ArrayList<Part>();Builder b=new Builder();int c=i.primary,a=i.accent;int cut=i.variant;
        boolean food=i.theme.equals("Pizza")||i.theme.equals("Candy")||i.theme.equals("Cookie");
        if(i.slot==CosmeticCatalog.Slot.CLOAK&&(i.theme.equals("Pumpkin")||i.theme.equals("Watermelon"))){
            if(i.theme.equals("Watermelon")){
                // A red wedge with nested white and green rind along the curved edge.
                double[] slice={-5,2,5,2,4.85,7,4.25,11,2.7,14,0,16,-2.7,14,-4.25,11,-4.85,7};
                for(int layer=0;layer<3;layer++){
                    double[] contour=slice.clone();
                    for(int k=0;k<contour.length;k+=2){contour[k]*=1-layer*.075;contour[k+1]=2+(contour[k+1]-2)*(1-layer*.085);}
                    b.plate(contour,.18+layer*.29,.28,new int[]{0xff287447,0xffc8df88,0xffeb525a}[layer]);
                }
                for(int j=0;j<3;j++)for(int k=0;k<3-j;k++)
                    DetailMesh.volume(b,(k-(2-j)*.5)*2.5,4.5+j*3,.99,.25,.55,.11,0xff38252a,false);
            }else{
                for(int rib=-2;rib<=2;rib++){
                    double y=8.7+Math.abs(rib)*.18;
                    DetailMesh.volume(b,rib*1.84,y,.52,2.2,6.2-Math.abs(rib)*.48,.65,Math.abs(rib)%2==0?0xffdd761d:0xffed8b22,false);
                }
                b.beam(0,3,.55,.65,.35,.6,1.15,0xff59622e);
                b.plate(new double[]{.4,2.5,2.1,.8,3.7,1.2,2.5,2.4},.75,.25,0xff618345);
            }
            part(parts,b,Anchor.BODY,Motion.CLOTH,0,0,2.7f,0,1);return new Model(parts);
        }
        if((i.theme.equals("Cookie")||i.theme.equals("Pizza"))&&(i.slot==CosmeticCatalog.Slot.CLOAK||i.slot==CosmeticCatalog.Slot.HAT)){
            boolean pizza=i.theme.equals("Pizza");
            if(i.slot==CosmeticCatalog.Slot.HAT){
                bakeryCap(b,pizza);
                part(parts,b,Anchor.HEAD,Motion.STILL,0,-7.9f,0,0,1);
            }else{
                bakeryCape(b,i.theme,pizza);
                part(parts,b,Anchor.BODY,Motion.CLOTH,0,0,2.7f,0,1);
            }
            return new Model(parts);
        }
        if(food){
            Builder edible=new Builder();snack(edible,i.theme);
            Mesh shape=edible.mesh();
            switch(i.slot){
                case HAT:
                    b.append(shape,0,-8.6,0,1.65,1.25,1.6);
                    part(parts,b,Anchor.HEAD,Motion.STILL,0,0,0,0,1);return new Model(parts);
                case BACKPACK:
                    CosmeticThemes.harness(b,0xff765044);
                    b.append(shape,0,5.4,3.8,1.9,2,2.7);part(parts,b,Anchor.BODY,Motion.STILL,0,0,0,0,1);return new Model(parts);
                case CLOAK:
                    b.append(shape,0,7.8,0,2.35,3.5,.85);part(parts,b,Anchor.BODY,Motion.CLOTH,0,0,2.7f,0,1);return new Model(parts);
                case TAIL:
                    for(int j=0;j<3;j++)b.append(shape,0,j*1.6,1+j*2.5,1-j*.18,1-j*.18,1);
                    part(parts,b,Anchor.BODY,Motion.TAIL,0,9,2.4f,0,1);return new Model(parts);
                default:break;
            }
        }
        switch(i.slot){
            case EARS:
                ears(b,i);part(parts,b,Anchor.HEAD,Motion.STILL,0,0,0,0,1);break;
            case CLOAK:
                if(i.theme.equals("Bread")||i.theme.equals("Rock")){
                    Builder emblem=new Builder();motif(emblem,i,0,0,0,1);b.append(emblem.mesh(),0,8,1.65,2.1,3.2,1.3);
                }else{
                    for(int y=0;y<17;y++){double inset=cut%2==1&&y>11?(y-11)*.55:0;b.box(-5+inset,y,0,10-inset*2,1,.22,CosmeticModels.shade(c,.77f+y*.018f));b.box(-5+inset,y,.24,.25,1,.08,a);b.box(4.75-inset,y,.24,.25,1,.08,a);}
                    for(int side:new int[]{-1,1}){
                        b.plate(new double[]{0,0,side*5,0,side*4.7,3.3,side*2,2.0},.35,.18,CosmeticModels.shade(c,1.35f));
                        b.beam(0,1,.58,side*4.65,3.3,.58,.14,a);
                        b.gem(side*3.6,1.25,.56,.35,.42,a);
                    }
                    for(int side:new int[]{-1,1})for(int n=0;n<4;n++){
                        double yy=9+n*1.29,xx=side*(4.41-.34*n);
                        b.beam(xx,yy,.44,xx-side*.78,yy+1,.47,.15,a);
                    }
                    motif(b,i,0,8,.55,1.35);
                    if(cosmic(i))for(int n=0;n<24;n++){double x=((n*37)%91)/10d-4.5,y=((n*19)%150)/10d+1;CosmeticThemes.star(b,x,y,.34,.12+(n%3)*.06,0xFFDADCF8);}
                }
                part(parts,b,Anchor.BODY,Motion.CLOTH,0,0,2.7f,0,1);break;
            case HAT:
                if(cut==0){b.box(-4.6,-8.6,-4.6,9.2,.6,9.2,c);b.box(-3.4,-10.3,-3.4,6.8,1.7,6.8,c);b.box(-3.5,-8.9,-3.5,7,.4,7,a);}
                else {CosmeticThemes.hood(b,c,a);for(int side:new int[]{-1,1})b.gem(side*3,-10,0,.65,1.8,a);}
                motif(b,i,0,-9,-4.7,.7);part(parts,b,Anchor.HEAD,Motion.STILL,0,0,0,0,1);break;
            case BANDANA:CosmeticThemes.band(b,c,a);motif(b,i,0,-6,-4.5,.4);part(parts,b,Anchor.HEAD,Motion.STILL,0,0,0,0,1);break;
            case SCARF:
                b.box(-4.2,0,-2.6,8.4,1.6,.4,c);b.box(-4.2,0,2.2,8.4,1.6,.4,c);b.box(-4.2,0,-2.6,.4,1.6,5.2,c);b.box(3.8,0,-2.6,.4,1.6,5.2,c);part(parts,b,Anchor.BODY,Motion.STILL,0,0,0,0,1);
                for(int side:new int[]{-1,1}){Builder tail=new Builder();for(int y=0;y<8+cut*2;y++){tail.box(-.65,y,0,1.3,1,.2,c);tail.box(-.65,y,.23,.18,1,.06,a);}part(parts,tail,Anchor.BODY,Motion.SCARF,side*2,1.3f,2.7f,side,1);}break;
            case BACKPACK:
                CosmeticThemes.harness(b,a);b.box(-3.2,2,2.7,6.4,7+cut,3.5,c);b.box(-3.4,2,2.6,6.8,1.2,3.8,a);b.box(-2.7,7.5,6.2,5.4,1.5,.6,a);motif(b,i,0,5.5,6.3,.95);part(parts,b,Anchor.BODY,Motion.STILL,0,0,0,0,1);break;
            case MINI_ME:
                if(cut==1){b.box(-3,1,2.1,6,7,3,c);b.box(-2.4,4,4.8,4.8,2.8,.8,a);}
                else if(cut==2){b.plate(new double[]{-4,0,4,0,5,11,2,10,0,12,-5,11},2.4,.4,c);motif(b,i,0,6,2.9,.8);}
                else if(cut==3 && Arrays.asList("Cat","Fox","Panda").contains(i.theme)){
                    b.bind(Anchor.HEAD);ears(b,i);b.bind(Anchor.BODY);Builder tail=new Builder();DetailMesh.tail(tail,i);b.bind(Anchor.MINI_TAIL);b.append(tail.mesh(),0,9,2,1,1,1);
                    for(int side:new int[]{-1,1}){Builder paw=new Builder();paws(paw,c,a);b.bind(side>0?Anchor.LEFT_ARM:Anchor.RIGHT_ARM);b.append(paw.mesh(),side*6,10,0,.8,.8,.8);}
                }else if(cut==3){b.bind(Anchor.HEAD);b.box(-4.4,-3.1,-4.3,8.8,1,1,c);for(int side:new int[]{-1,1})b.ring(side*2,-3.1,-4.5,1.25,1,.32,a);b.bind(Anchor.BODY);b.box(2,3,-2.6,3,3.5,1.5,c);b.beam(-3,0,-2.5,3,6,-2.6,.48,a);}
                else{b.bind(Anchor.HEAD);CosmeticThemes.hood(b,c,a);motif(b,i,0,-9,-4.6,.65);}b.bind(Anchor.BODY);b.box(-4,0,-2.4,8,3,.3,c);part(parts,b,Anchor.BODY,Motion.MINIATURE,6.25f,-4.67f,0,0,.18f);break;
            case AURA:for(int n=0;n<6+cut*2;n++){Builder mote=new Builder();motif(mote,i,0,0,0,.32);part(parts,mote,Anchor.BODY,Motion.ORBIT,0,0,0,n*(float)Math.PI/(3+cut),1);}break;
            case NECKLACE:CosmeticThemes.chain(b,a);motif(b,i,0,3.6,-3,.5);part(parts,b,Anchor.BODY,Motion.STILL,0,0,0,0,1);break;
            case BELT:
                b.box(-4.3,9.5,-2.5,8.6,1.2,.35,c);b.box(-4.3,9.5,2.15,8.6,1.2,.35,c);b.box(-4.3,9.5,-2.5,.35,1.2,5,c);b.box(3.95,9.5,-2.5,.35,1.2,5,c);motif(b,i,0,10,-2.9,.45);part(parts,b,Anchor.BODY,Motion.STILL,0,0,0,0,1);break;
            case BOOTS:
                for(Anchor leg:new Anchor[]{Anchor.LEFT_LEG,Anchor.RIGHT_LEG}){Builder boot=new Builder();CosmeticThemes.bootShell(boot,c,a);motif(boot,i,0,9.3,-3.2,.35);boot.gem(0,10.2,2.7,.5,.8,a);if(cut==1)for(int side:new int[]{-1,1})boot.plate(new double[]{side*1.8,8,side*3,6.5,side*2.6,10.5,side*1.8,11},1,.2,a);part(parts,boot,leg,Motion.STILL,0,0,0,0,1);}break;
            case TAIL:
                DetailMesh.tail(b,i);
                part(parts,b,Anchor.BODY,Motion.TAIL,0,9,2.4f,0,1);break;
            case EARRINGS:
                for(int side:new int[]{-1,1}){b.ring(side*4.5,-2.4,0,.5,.7,.14,a);motif(b,i,side*4.5,-1.2,0,.25);}part(parts,b,Anchor.HEAD,Motion.STILL,0,0,0,0,1);break;
            case WRISTBANDS:
                for(Anchor arm:new Anchor[]{Anchor.LEFT_ARM,Anchor.RIGHT_ARM}){Builder cuff=new Builder();wristStrap(cuff,c);motif(cuff,i,0,8.5,-2.4,.3);part(parts,cuff,arm,Motion.STILL,0,0,0,0,1);}break;
        }
        return new Model(parts);
    }
    static void wristStrap(Builder cuff,int c){
        cuff.box(-2.2,8,-2.2,4.4,1,.3,c);
        cuff.box(-2.2,8,1.9,4.4,1,.3,c);
        cuff.box(-2.2,8,-2.2,.3,1,4.4,c);
        cuff.box(1.9,8,-2.2,.3,1,4.4,c);
    }
    private static void ears(Builder b,CosmeticCatalog.Item i){
        for(int side:new int[]{-1,1}){
            if(i.theme.equals("Panda")){DetailMesh.volume(b,side*3.4,-8.4,0,1.75,1.8,1.1,0xff25232c,false);continue;}
            double tip=i.theme.equals("Fox")?-13:-11.5;
            b.plate(new double[]{side*1.1,-7.7,side*4.2,tip,side*4.55,-7.5},-1,1.9,i.primary);
            b.plate(new double[]{side*2,-8,side*3.95,tip+1,side*4.05,-8},-1.1,.12,0xffd9a5a5);
        }
    }
    private static void paws(Builder b,int c,int a){
        DetailMesh.volume(b,0,.3,0,2.3,2.1,2.3,c,false);
        DetailMesh.volume(b,0,1,-2.1,1.1,.7,.25,a,false);
        for(int toe=0;toe<3;toe++)DetailMesh.volume(b,(toe-1)*1.05,-.25,-2.1,.43,.5,.26,a,false);
    }
    private static boolean cosmic(CosmeticCatalog.Item i){for(int t=0;t<6;t++)if(i.theme.equals(THEMES[t]))return true;return false;}
    static int animatedColor(CosmeticCatalog.Item i,int color,float age,int triangle){if(!i.expanded()||!cosmic(i))return color;float wave=.88f+(float)Math.sin(age*.045+triangle*.13)*.15f;return CosmeticModels.shade(color,wave);}
    private static void bakeryCape(Builder b,String theme,boolean pizza){
        int cloth=pizza?0xff963b32:0xff61423b;
        int binding=pizza?0xffedc785:0xffc28d60;
        double[] outline={-4.3,.4,4.3,.4,5,2.1,5.25,13.6,4.5,15.2,1.7,15.65,0,15.35,-1.7,15.65,-4.5,15.2,-5.25,13.6,-5,2.1};
        b.plate(outline,0,.35,cloth);
        for(int side:new int[]{-1,1}){
            b.beam(side*4.72,2,.42,side*4.95,13.4,.42,.24,binding);
            b.beam(side*4.95,13.4,.42,side*4.32,14.7,.42,.24,binding);
            b.beam(side*4.32,14.7,.42,side*1.7,15.13,.42,.24,binding);
            b.beam(side*1.7,15.13,.42,0,14.83,.42,.24,binding);
            // Two shoulder tabs and their buttons keep the neckline clear of the emblem.
            b.plate(new double[]{side*.5,.45,side*4.35,.45,side*4.75,1.8,side*2.2,2.9},.38,.14,binding);
            DetailMesh.volume(b,side*2.35,2.12,.57,.27,.27,.1,cloth,false);
        }
        Builder patch=new Builder();snack(patch,theme);
        // Equal planar scales preserve the round chips and crust when the cloak swings.
        b.append(patch.mesh(),0,7.3,1.37,1.86,1.86,1.86);
        for(int n=-3;n<=3;n++){
            double x=n*1.18;
            if(pizza)b.plate(new double[]{x-.48,12.4,x+.48,12.4,x+.48,13.18,x-.48,13.18},.37,.08,n%2==0?binding:0xffdd7957);
            else b.beam(x-.3,13.15,.45,x+.32,12.86,.45,.15,binding);
        }
    }

    private static void bakeryCap(Builder b,boolean pizza){
        // A turned brim supports a shallow crown fitted to the horizontal plane of the head.
        double[] radius={3.7,4.27,4.65,4.70,4.42,3.72,2.65,1.34,0};
        double[] height={.15,-.14,-.57,-.93,-1.36,-1.92,-2.36,-2.67,-2.76};
        int[] palette=pizza?new int[]{0xff92522d,0xffcd8941,0xffe8a852,0xffca8140,0xffbb4930,0xfff0ad48,0xfff4c459,0xffffcf65}:
            new int[]{0xff604036,0xff82573f,0xffaa7449,0xffb88250,0xffcb935c,0xffd09a63,0xffdba86d,0xffd8a269};
        for(int j=0;j<height.length-1;j++)for(int i=0;i<40;i++){
            double a=i*Math.PI/20,t=(i+1)*Math.PI/20;
            double r=radius[j],q=radius[j+1];
            b.quad(r*Math.cos(a),height[j],r*Math.sin(a),r*Math.cos(t),height[j],r*Math.sin(t),
                q*Math.cos(t),height[j+1],q*Math.sin(t),q*Math.cos(a),height[j+1],q*Math.sin(a),palette[j]);
        }
        for(int row=0;row<3;row++){
            int count=row==0?1:row==1?5:9;
            for(int n=0;n<count;n++){
                double t=n*Math.PI*2/count+.51*row, r=row==0?.42:row==1?2.20:3.78;
                double x=r*Math.cos(t),z=r*Math.sin(t),y=row==0?-2.74:row==1?-2.49:-1.88;
                double size=(pizza?.61:.41)*(1+.12*Math.sin(17*n+row));
                DetailMesh.volume(b,x,y,z,size,pizza?.24:.34,size*(pizza?1:.77),pizza?0xffb83f2e:0xff583628,!pizza);
                DetailMesh.volume(b,x-.12,y-(pizza?.19:.25),z-.08,size*.24,.065,size*.19,pizza?0xffed9763:0xffaa7754,false);
            }
        }
    }
    private static void snack(Builder b,String theme){
        if(theme.equals("Pizza")){
            double[] base={-2.22,-1.55,-1.75,-1.88,-.5,-2.02,.82,-1.95,1.84,-1.74,2.22,-1.48,.22,2.44,-.04,2.36};
            b.plate(base,-.29,.58,0xffbc743c);
            for(double side:new double[]{-1,1}){
                b.plate(new double[]{-2.01,-1.53,-.65,-1.84,1.06,-1.77,2.03,-1.48,.17,2.29},side*.31-.04,.08,0xffb33d29);
                b.plate(new double[]{-1.91,-1.48,-.51,-1.74,1.88,-1.47,1.34,-.28,1.23,-.2,.99,.43,.81,.51,.19,2.08,-.14,1.66},side*.37-.04,.08,0xffffce69);
                for(double[] spot:new double[][]{{-1.04,-.98,.41},{.89,-.91,.36},{.07,.31,.38}}){
                    DetailMesh.volume(b,spot[0],spot[1],side*.43,spot[2],spot[2]*.93,.09,0xffae3929,false);
                    DetailMesh.volume(b,spot[0]-.09,spot[1]-.1,side*.51,.09,.06,.025,0xffe88656,false);
                }
            }
            for(int k=0;k<8;k++){
                double x=-1.91+k*.54,y=-1.53-.28*Math.cos(x*.72);
                DetailMesh.volume(b,x,y,0,.47,.29+.035*Math.cos(k*2),.48,k%3==1?0xffd99449:0xffe8aa59,false);
            }
        }else if(theme.equals("Cookie")){
            // The stepped notch is a bite cut out of the top right edge.
            double[] rim={-2,-.9,-1.55,-1.8,-.55,-2.16,.4,-2.13,.39,-1.49,.91,-1.28,1.2,-1.45,1.24,-.94,1.65,-.69,2.09,-.75,2.14,.35,1.62,1.46,.56,2.04,-.65,2.04,-1.64,1.4,-2.12,.32};
            for(int k=0;k<rim.length;k+=2){int j=(k+2)%rim.length;
                b.plate(new double[]{-.28,.25,rim[k],rim[k+1],rim[j],rim[j+1]},-.36,.72,0xffca9159);}
            for(double face:new double[]{-.42,.42})for(double[] chip:new double[][]{{-1,-.8},{.02,-1.13},{.71,.22},{-.56,.4},{-.07,1.43},{1.25,.98}})
                {
                    double size=.20+.08*Math.abs(chip[0]);
                    DetailMesh.volume(b,chip[0],chip[1],face,size,size*.83,.13,0xff563126,false);
                    DetailMesh.volume(b,chip[0]-.07,chip[1]-.08,face*1.25,.095,.06,.035,0xff895338,false);
                }
        }else{
            // Peppermint wheel with two twisted wrapper ends.
            for(int k=0;k<12;k++){double u=k*Math.PI/6,v=(k+1)*Math.PI/6;
                b.plate(new double[]{0,0,Math.cos(u)*1.55,Math.sin(u)*1.55,Math.cos(v)*1.55,Math.sin(v)*1.55},-.44,.88,k%2==0?0xffdc3055:0xfffff0df);}
            for(int side:new int[]{-1,1})b.plate(new double[]{side*1.24,0,side*2.55,-1.03,side*2.33,-.14,side*2.62,.92},-.16,.32,0xff55caba);
        }
    }
    private static void motif(Builder target,CosmeticCatalog.Item i,double x,double y,double z,double scale){
        Builder b=new Builder();int c=i.primary,a=i.accent;String theme=i.theme;
        if(theme.equals("Pizza")||theme.equals("Candy")||theme.equals("Cookie")){snack(b,theme);}
        else if(theme.equals("Koi")){
            b.plate(new double[]{-2,0,-1,-.85,.35,-.7,1.15,-.1,2,-.9,1.7,0,2,.9,1.05,.35,.1,.85,-1,.7},0,.36,a);
            b.gem(-.6,-.14,.45,.49,.44,c);b.gem(.35,.2,.42,.32,.3,c);b.box(-1.62,-.18,.42,.2,.2,.12,0xFF28212A);
        }else if(theme.equals("Monarch")){
            for(int side:new int[]{-1,1}){
                b.plate(new double[]{0,0,side*2,-2.4,side*2.7,-1.8,side*2.1,-.45,side*.6,.2,side*1.6,.6,side*1.3,1.8,side*.4,1.4},0,.25,c);
                b.beam(0,0,.32,side*2,-1.85,.32,.17,a);b.beam(0,0,.32,side*1.2,1.3,.32,.12,a);
                for(int n=0;n<3;n++)b.gem(side*(2.31-.16*n),-1.65+.32*n,.34,.095,.12,0xFFFFEBD6);
            }b.beam(0,-.8,.42,0,1,.42,.26,a);
        }else if(theme.equals("Astrolabe")){
            b.ring(0,0,0,1.85,1.85,.22,a);b.ring(0,0,.21,1.55,.85,.13,a);
            b.beam(-1.25,1.25,.42,1.25,-1.25,.42,.18,a);CosmeticThemes.star(b,0,0,.48,.72,0xFFF4DFBB);
        }else if(theme.equals("Bread")){
            DetailMesh.loaf(b,c,a);
        }else if(theme.equals("Rock")){DetailMesh.stone(b,c,a);}
        else if(cosmic(i)){b.ring(0,0,0,2,1,.14,a);CosmeticThemes.crescent(b,0,0,.25,1.4,a);for(int n=0;n<5;n++)CosmeticThemes.star(b,Math.cos(n*2.4)*1.8,Math.sin(n*2.4)*1.7,.5,.2,a);}
        else if(Arrays.asList("Frog","Duck","Cat","Fox","Panda","Bee").contains(theme)){
            DetailMesh.animal(b,theme,c);
        }else if(theme.equals("Coffee")){CollectionModels.cup(b,c,a);
        }else if(theme.equals("Ice")){
            for(int n=0;n<3;n++)b.plate(new double[]{n-2,1.7,n-1.6,-1-n%2,n-.8,-2+n*.4,n-.3,1.4},n*.2,.55,n==1?0xffbcefff:0xff79bbdb);
        }else if(Arrays.asList("Donut","Cookie","Pizza","Candy","Watermelon","Coffee","Pumpkin","Mushroom").contains(theme)){
            if(theme.equals("Donut")){b.ring(0,0,0,1.6,1.6,.8,c);b.ring(0,0,.4,1.6,1.6,.3,a);}else {b.box(-1.6,-1.6,0,3.2,3.2,.4,c);b.box(-1.7,-1.7,.4,3.4,.5,.3,a);}for(int n=0;n<5;n++)b.gem(Math.sin(n*8)*1.2,Math.cos(n*6)*1.2,.6,.17,.2,a);
        }else{int points=3+Math.floorMod(theme.hashCode(),5);b.ring(0,0,0,1.7,1.7,.2,a);for(int n=0;n<points;n++){double angle=n*Math.PI*2/points;b.gem(Math.cos(angle)*1.1,Math.sin(angle)*1.1,.3,.4,.7,c);}b.gem(0,0,.5,.5,.85,a);}
        if(i.variant==1)b.ring(0,0,-.15,2.3,2.3,.13,a);
        if(i.variant==2){
            for(int side:new int[]{-1,1})for(int n=0;n<5;n++){
                double angle=(n+1)*.43;
                b.plate(new double[]{side*(1+Math.sin(angle)),n*.54-1.4,side*(2.55+Math.sin(angle)) ,n*.55-2.1,side*2.4,n*.52-.9},.36,.16,a);
            }
        }else if(i.variant==3){
            for(int side:new int[]{-1,1}){
                b.beam(side*2.3,-1.4,.3,side*1.5,2.1,.3,.19,a);
                for(int n=0;n<4;n++)b.gem(side*(.4+n*.52),-2-Math.sin(n)*.5,.35,.31,.57,a);
            }
            b.gem(0,2.25,.4,.42,.85,a);
        }
        // Face-mounted pieces present their detailed side to the outside.
        target.append(b.mesh(),x,y,z,scale,scale,z<0?-scale:scale);
    }
    private ExpandedCosmetics(){}
}
