package com.astryx.modern;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import net.minecraft.core.registries.BuiltInRegistries;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

enum HudElement {
    FPS("fps","FPS","Frames per second","144 FPS"), PING("ping","Ping","Server latency","28 ms"),
    CPS("cps","Click counter","Left clicks per second","8 CPS"), XYZ("coordinates","Coordinates","Block position","XYZ  16  72  -9"),
    BED("bedwars","BedWars resources","Materials in inventory","Iron 32  Gold 6  Dia 2  Em 1"),
    HELD("held","Held item","Selected stack and count","Wool ×48"),
    DIRECTION("facing","Direction","Compass bearing","North · 180°"),
    SPEED("speed","Speed","Horizontal blocks per second","4.3 m/s"),
    TIME("clock","Clock","Local time","12:45"), DAY("day","World day","Elapsed level days","Day 14"),
    BIOME("biome","Biome","Current biome","Cherry grove"), LIGHT("light","Light level","Local light at your feet","Light 12"),
    ARMOR("armor","Armour","Total armour points","Armour 18"), DURABILITY("durability","Durability","Held tool durability","Durability 98%"),
    ARROWS("arrows","Arrows","Arrows in inventory","Arrows 24"), WOOL("wool","Wool counter","All sixteen wool colours","Wool 48"),
    FIREBALL("fireballs","Fireballs","Fire charges in inventory","Fireballs 2"), APPLES("apples","Golden apples","Golden apples in inventory","Apples 3"),
    HEALTH("health","Health","Current and maximum health","Health 18 / 20"), MEMORY("memory","Memory","Used Java heap","Memory 640 / 2048 MB"),
    SESSION("session","Session timer","Time since starting Minecraft","Session 00:14:32"),
    SERVER("server","Server address","Current multiplayer server","mc.hypixel.net"),
    ONLINE("online","Online players","Players in the tab list","Online 24"),
    SPACE("space","Inventory space","Free main inventory slots","Free slots 18 / 36"),
    HUNGER("hunger","Hunger","Current food points","Food 18 / 20"),
    XP("xp","Experience level","Current experience level","Level 12"),
    KEYS("keys","Keystrokes","Movement key states","W · A · S · D");
    final String key,label,detail,sample;
    HudElement(String key,String label,String detail,String sample){this.key=key;this.label=label;this.detail=detail;this.sample=sample;}
    private static final DateTimeFormatter CLOCK=DateTimeFormatter.ofPattern("HH:mm");
    String value(Minecraft m){
        var p=m.player;
        switch(this){
            case MEMORY:
                Runtime vm=Runtime.getRuntime();return "Memory "+(vm.totalMemory()-vm.freeMemory())/1048576+" / "+vm.maxMemory()/1048576+" MB";
            case SESSION:
                long sec=java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime()/1000;
                return String.format(Locale.ROOT,"Session %02d:%02d:%02d",sec/3600,(sec/60)%60,sec%60);
            case SERVER:return m.getCurrentServer()==null?"Singleplayer":m.getCurrentServer().ip;
            case ONLINE:return m.getConnection()==null?"":"Online "+m.getConnection().getOnlinePlayers().size();
            case SPACE:
                int free=0;for(int slot=0;slot<36;slot++)if(p.getInventory().getItem(slot).isEmpty())free++;
                return "Free slots "+free+" / 36";
            case HUNGER:return "Food "+p.getFoodData().getFoodLevel()+" / 20";
            case XP:return "Level "+p.experienceLevel;
            case FPS:return m.getFps()+" FPS";
            case PING:
                if(m.getConnection()==null||m.hasSingleplayerServer())return "";
                var row=m.getConnection().getPlayerInfo(p.getUUID());return row==null?"":row.getLatency()+" ms";
            case CPS:return ModernClient.cps()+" CPS";
            case XYZ:return "XYZ  "+p.getBlockX()+"  "+p.getBlockY()+"  "+p.getBlockZ();
            case BED:return ModernClient.resources(m);
            case HELD:var item=p.getMainHandItem();return item.isEmpty()?"":item.getHoverName().getString()+" ×"+item.getCount();
            case DIRECTION:return p.getDirection().getSerializedName()+" · "+Math.floorMod(Math.round(p.getYRot()),360)+"°";
            case SPEED:return String.format(Locale.ROOT,"%.1f m/s",Math.hypot(p.getDeltaMovement().x,p.getDeltaMovement().z)*20);
            case TIME:return LocalTime.now().format(CLOCK);
            case DAY:return "Day "+m.level.getGameTime()/24000;
            case BIOME:return m.level.getBiome(p.blockPosition()).unwrapKey().map(k->k.identifier().getPath().replace('_',' ')).orElse("");
            case LIGHT:return "Light "+m.level.getMaxLocalRawBrightness(p.blockPosition());
            case ARMOR:return "Armour "+p.getArmorValue();
            case DURABILITY:var tool=p.getMainHandItem();return !tool.isDamageableItem()?"":"Durability "+Math.round(100.0*(tool.getMaxDamage()-tool.getDamageValue())/tool.getMaxDamage())+"%";
            case HEALTH:return "Health "+(int)Math.ceil(p.getHealth())+" / "+(int)p.getMaxHealth();
            case KEYS:return (m.options.keyUp.isDown()?"[W]":" W ") +(m.options.keyLeft.isDown()?"[A]":" A ")+(m.options.keyDown.isDown()?"[S]":" S ")+(m.options.keyRight.isDown()?"[D]":" D ");
            default:
                int count=0;var inv=p.getInventory();
                for(int i=0;i<inv.getContainerSize();i++){
                    var s=inv.getItem(i);
                    boolean match=this==WOOL?s.is(net.minecraft.tags.ItemTags.WOOL):this==ARROWS?s.is(Items.ARROW)||s.is(Items.TIPPED_ARROW)||s.is(Items.SPECTRAL_ARROW):this==FIREBALL?s.is(Items.FIRE_CHARGE):s.is(Items.GOLDEN_APPLE)||s.is(Items.ENCHANTED_GOLDEN_APPLE);
                    if(match)count+=s.getCount();
                }
                return (this==WOOL?"Wool":this==ARROWS?"Arrows":this==FIREBALL?"Fireballs":"Apples")+" "+count;
        }
    }
}
