package com.astryx.client.features;

/** Deterministic cloth motion; root vertices never leave their attachment point. */
public final class CosmeticMotion {
    private static float speed(float value){return Float.isNaN(value)||Float.isInfinite(value)?0:Math.max(0,Math.min(1,value));}
    private static float bend(float y,float length){return Math.max(0,Math.min(1,y/length));}
    public static float clothX(float x,float y,float age,float movement,float turning){
        float b=bend(y,16);float turn=Float.isNaN(turning)?0:Math.max(-30,Math.min(30,turning))/30;
        return x+(float)Math.sin(age*.085-y*.35)*b*b*(.35f+speed(movement)*.35f)-turn*b*b*2.8f;
    }
    public static float clothY(float y,float age){return clothY(y,age,0);}
    public static float clothY(float y,float age,float movement){
        float b=bend(y,16);double lift=Math.toRadians(55*speed(movement));
        return y*(float)Math.cos(lift)+(float)Math.sin(age*.11-y*.45)*b*b*.13f;
    }
    public static float clothZ(float z,float x,float y,float age,float movement,boolean backpack){
        float b=bend(y,16),s=speed(movement);
        return z+y*(float)Math.sin(Math.toRadians(55*s))+b*b*1.05f+(float)Math.sin(age*.13-y*.5+x*.09)*b*(.7f+s*.45f)
            +(float)Math.sin(age*.055-y*.23)*b*b*.3f+(backpack?Math.min(1,Math.max(0,y/2.2f))*6.8f:0);
    }
    public static float scarfX(float x,float y,float age,float phase){float b=bend(y,9);return x+(float)Math.sin(age*.115-y*.4+phase)*b*b*.35f;}
    public static float scarfZ(float z,float y,float age,float movement,float phase){float b=bend(y,9);return z-b*b*(.35f+speed(movement)*1.7f)-(float)Math.sin(age*.13-y*.5+phase)*b*.35f;}
    public static float wingYaw(float side,float age,float movement){return -side*(4f+(float)Math.sin(age*.16)*(4f+speed(movement)*2f));}
    public static float wingRoll(float side,float age,float movement){return side*((float)Math.sin(age*.16+.7)*1.8f+speed(movement)*.18f);}
    private CosmeticMotion(){}
}
