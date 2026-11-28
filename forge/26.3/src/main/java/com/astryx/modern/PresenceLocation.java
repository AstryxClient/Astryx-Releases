package com.astryx.modern;

import com.google.gson.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.*;

/** Read server-supplied game and map names without including player names or chat. */
final class PresenceLocation {
    private Object world;
    private long nextQuery,waitingUntil,lastQuery;
    private int attempts;
    private String game="",map="",lobby="";
    static String serverName(String address){
        String host=address.split(":",2)[0];
        String[][] networks={{"hypixel.net","Hypixel"},{"minemen.club","Minemen"},{"cubecraft.net","CubeCraft"},{"pika-network.net","PikaNetwork"},{"jartexnetwork.com","JartexNetwork"},{"gommehd.net","GommeHD"},{"blocksmc.com","BlocksMC"}};
        for(String[] n:networks)if(host.equals(n[0])||host.endsWith("."+n[0]))return n[1];
        return host;
    }
    void tick(Minecraft mc,long now,boolean hypixel){
        if(world!=mc.level){world=mc.level;game=map=lobby="";attempts=0;waitingUntil=0;nextQuery=now+1800;}
        if(waitingUntil>0&&now>waitingUntil)waitingUntil=0;
        if(!hypixel||mc.player==null||attempts>=2||now<nextQuery||now-lastQuery<4000)return;
        attempts++;lastQuery=now;waitingUntil=now+6500;nextQuery=now+10000;
        mc.player.connection.sendCommand("locraw");
    }
    boolean response(String text,Object currentWorld,long now){
        if(currentWorld==null||world!=currentWorld||waitingUntil==0||now>waitingUntil||text.length()>1024||!text.startsWith("{"))return false;
        try{
            JsonObject json=new JsonParser().parse(text).getAsJsonObject();
            if(!field(json,"server").matches("[A-Za-z0-9_-]{1,64}"))return false;
            game=pretty(field(json,"gametype"));map=field(json,"map");lobby=field(json,"lobby");
            if(lobby.isEmpty()&&"LOBBY".equalsIgnoreCase(field(json,"mode")))lobby="Lobby";
            if(game.isEmpty()&&lobby.isEmpty()&&"limbo".equalsIgnoreCase(field(json,"server")))game="Limbo";
            waitingUntil=0;
            if(!map.contains("?"))attempts=2;
            else {map="";nextQuery=now+4000;}
            return true;
        }catch(RuntimeException ignored){return false;}
    }
    private static String field(JsonObject json,String key){
        JsonElement e=json.get(key);return e!=null&&e.isJsonPrimitive()&&e.getAsJsonPrimitive().isString()?clean(e.getAsString()):"";
    }
    private static String clean(String raw){return raw==null?"":raw.replaceAll("§.","").replaceAll("[\\p{C}]","").trim();}
    private static String pretty(String raw){
        String value=clean(raw).replace('_',' ');
        switch(value.toLowerCase(Locale.ROOT).replace(" ","")){
            case "bedwars":return "Bedwars";case "skywars":return "SkyWars";case "skyblock":return "SkyBlock";
            case "murdermystery":return "Murder Mystery";case "buildbattle":return "Build Battle";
            case "uhc":return "UHC";case "nodebuff":return "NoDebuff";case "megawalls":case "walls3":return "Mega Walls";
            case "main":return "Main";case "prototype":return "Prototype";
        }
        if(!value.equals(value.toUpperCase(Locale.ROOT)))return value;
        StringBuilder result=new StringBuilder();for(String word:value.toLowerCase(Locale.ROOT).split(" +"))if(!word.isEmpty()){if(result.length()>0)result.append(' ');result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));}return result.toString();
    }
    String detail(Minecraft mc, boolean hypixel) {
        Scoreboard board = mc.level.getScoreboard();
        Objective objective = board.getDisplayObjective(DisplaySlot.SIDEBAR);
        String title = objective == null ? "" : objective.getDisplayName().getString();
        List<String> lines = new ArrayList<>();
        if (objective != null && !hypixel) {
            for (PlayerScoreEntry score : board.listPlayerScores(objective)) {
                if (score.owner().startsWith("#")) continue;
                Component display = score.display();
                lines.add(display == null ? PlayerTeam.formatNameForTeam(
                    board.getPlayersTeam(score.owner()), Component.literal(score.owner())).getString() : display.getString());
            }
        }
        if(hypixel)return hypixelDetail(game,map,lobby,title,attempts>=2&&waitingUntil==0);
        return describe(game,map,lobby,title,lines);
    }
    static String hypixelDetail(String game,String map,String lobby,String title,boolean resolved){
        if(lobby.isEmpty()&&!map.isEmpty()&&!map.contains("?"))return describe(game,map,"","",Collections.emptyList());
        if(!resolved&&lobby.isEmpty())return "";
        String kind=game.isEmpty()?describe("","","",title,Collections.emptyList()):game;
        if(kind.isEmpty()||kind.equalsIgnoreCase("Lobby"))return "In a lobby";
        if(kind.equals("Limbo"))return "In Limbo";
        if(kind.equalsIgnoreCase("Main"))return "In the main lobby";
        String article="aeiou".contains(kind.substring(0,1).toLowerCase(Locale.ROOT))&&!kind.equals("UHC")?"an ":"a ";
        return "In "+article+kind.toLowerCase(Locale.ROOT)+" lobby";
    }
    static String describe(String game,String map,String lobby,String title,List<String> lines){
        String kit="",mode="",arena="";
        for(String line:lines){
            String[] pair=clean(line).split("\\s*[:»]\\s*",2);if(pair.length!=2||pair[1].isEmpty()||pair[1].length()>50)continue;
            switch(pair[0].toLowerCase(Locale.ROOT)){
                case "map":case "arena":arena=pair[1];break;
                case "game":case "gamemode":case "game mode":case "mode":mode=pretty(pair[1]);break;
                case "kit":case "ladder":case "queue":kit=pretty(pair[1]);break;
            }
        }
        if(!lobby.isEmpty())return game.isEmpty()?"Lobby":game+" Lobby";
        String heading=game;
        if(heading.isEmpty())for(String known:new String[]{"Bedwars","SkyWars","Duels","SkyBlock","Murder Mystery","Build Battle","Practice","Boxing","UHC","SkyPvP","Factions","Survival","Arcade","Lobby"}){
            if(clean(title).toLowerCase(Locale.ROOT).replace(" ","").contains(known.toLowerCase(Locale.ROOT).replace(" ",""))){heading=known;break;}
        }
        if(heading.isEmpty()||heading.equals("Practice"))heading=!kit.isEmpty()?kit:!mode.isEmpty()?mode:heading;
        String place=map.isEmpty()?arena:map;
        if(heading.isEmpty())return place.isEmpty()?"":"Map · "+place;
        if(!place.isEmpty())return heading+" · "+place;
        return !mode.isEmpty()&&!mode.equalsIgnoreCase(heading)?heading+" · "+mode:heading;
    }
}
