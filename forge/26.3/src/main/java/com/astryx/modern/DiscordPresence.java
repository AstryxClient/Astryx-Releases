package com.astryx.modern;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Locale;
import net.minecraft.client.multiplayer.ServerData;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.client.Minecraft;

/** Windows Discord desktop IPC. Session credentials never enter an activity payload. */
public final class DiscordPresence {
    private final ExecutorService io=Executors.newSingleThreadExecutor(r->{
        Thread t=new Thread(r,"Astryx Discord presence");t.setDaemon(true);return t;
    });
    static final long START_SECONDS=ManagementFactory.getRuntimeMXBean().getStartTime()/1000L;
    public static final DiscordPresence INSTANCE=new DiscordPresence();
    private final PresenceLocation location = new PresenceLocation();
    public boolean consumeLocation(String message, Minecraft mc) {
        return location.response(message, mc.level, System.currentTimeMillis());
    }
    private RandomAccessFile pipe;
    private String sent="";
    private volatile boolean busy;
    private long due,retryArtwork;
    public void tick(Minecraft mc){
        long now=System.currentTimeMillis();
        if(!System.getProperty("os.name","").startsWith("Windows")||busy||now<due)return;
        due=now+2500;
        String id="1575945976609382561";
        ServerData server=mc.getCurrentServer();
        boolean multiplayer=mc.level!=null&&!GameApi.singleplayer(mc)&&server!=null;
        SocketAddress peer=multiplayer&&mc.getConnection()!=null?mc.getConnection().getConnection().getRemoteAddress():null;
        String ip=multiplayer&&publicPeer(peer)?publicAddress(server.ip):"";
        String host = ip.split(":")[0];
        boolean hypixel = host.equals("hypixel.net") || host.endsWith(".hypixel.net");
        location.tick(mc, now, hypixel);
        String details=mc.level==null?(GameApi.screen(mc) instanceof net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen?"On the multiplayer menu":GameApi.screen(mc) instanceof net.minecraft.client.gui.screens.worldselection.SelectWorldScreen?"On the singleplayer menu":GameApi.screen(mc) instanceof net.minecraft.client.gui.screens.TitleScreen?"On the main menu":"In the menus"):GameApi.singleplayer(mc)?"In a singleplayer world":"Playing multiplayer";
        if (!ip.isEmpty()) details = "Playing " + PresenceLocation.serverName(ip);
        String state = ip.isEmpty() ? "" : location.detail(mc, hypixel);
        JsonObject activity=activity(details,state,ip);
        String signature=activity.toString();
        busy=true;
        io.execute(()->{try{
            if(pipe==null)connect(id);
            if(pipe!=null&&(!signature.equals(sent)||retryArtwork>0&&now>=retryArtwork)){
                if(update(activity))retryArtwork=0;
                else{
                    activity.remove("assets");
                    if(!update(activity))throw new IOException("Discord rejected activity");
                    retryArtwork=now+300000;
                }
                sent=signature;
            }else if(pipe!=null){JsonObject ping=new JsonObject();send(3,ping);receive();}
        }catch(Exception ignored){disconnect();}finally{busy=false;}});
    }
    private boolean update(JsonObject activity)throws IOException{
        JsonObject args=new JsonObject();
        args.addProperty("pid",Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]));
        args.add("activity",activity);
        JsonObject cmd=new JsonObject();cmd.addProperty("cmd","SET_ACTIVITY");
        cmd.addProperty("nonce",UUID.randomUUID().toString());cmd.add("args",args);
        send(1,cmd);JsonObject reply=receive();
        return !reply.has("evt")||reply.get("evt").isJsonNull()||!"ERROR".equals(reply.get("evt").getAsString());
    }
    static boolean publicPeer(SocketAddress peer){
        if(!(peer instanceof InetSocketAddress))return false;
        InetAddress ip=((InetSocketAddress)peer).getAddress();
        if(ip==null||ip.isLoopbackAddress()||ip.isSiteLocalAddress()||ip.isLinkLocalAddress()||ip.isAnyLocalAddress()||ip.isMulticastAddress())return false;
        byte[] bytes=ip.getAddress();
        return bytes.length==16?(bytes[0]&0xfe)!=0xfc:!(bytes[0]==100&&(bytes[1]&0xc0)==0x40);
    }
    static String publicAddress(String input){
        if(input==null)return "";
        String value=input.trim().toLowerCase(Locale.ROOT);
        if(value.length()>253||!value.matches("(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9-]{1,63}(?::[0-9]{1,5})?"))return "";
        String[] parts=value.split(":");String host=parts[0];
        if(host.endsWith(".local")||host.endsWith(".localhost")||host.endsWith(".internal")||host.endsWith(".lan")||host.endsWith(".home.arpa"))return "";
        if(parts.length==2){int port=Integer.parseInt(parts[1]);if(port==0||port>65535)return "";if(port==25565)return host;}
        return value;
    }
    static JsonObject activity(String details,String state,String ip){
        String host=ip.split(":",2)[0];
        boolean hypixel=host.equals("hypixel.net")||host.endsWith(".hypixel.net");
        boolean serverArt=!ip.isEmpty();
        JsonObject art=new JsonObject();
        art.addProperty("large_image",serverArt?"https://api.mcstatus.io/v2/icon/"+(hypixel?"mc.hypixel.net":ip):"astryx");
        art.addProperty("large_text",serverArt?(hypixel?"Hypixel":ip):"Astryx Client");
        if(serverArt){art.addProperty("small_image","astryx");art.addProperty("small_text","Astryx Client");}
        JsonObject activity=new JsonObject();activity.addProperty("details",details.substring(0,Math.min(128,details.length())));
        if(!state.isEmpty())activity.addProperty("state",state.substring(0,Math.min(128,state.length())));
        JsonObject time=new JsonObject();time.addProperty("start",START_SECONDS);activity.add("timestamps",time);activity.add("assets",art);
        JsonObject link=new JsonObject();link.addProperty("label","Visit Astryx's GitHub");link.addProperty("url","https://github.com/AstryxClient/Astryx-Releases");
        com.google.gson.JsonArray buttons=new com.google.gson.JsonArray();buttons.add(link);activity.add("buttons",buttons);activity.addProperty("details_url",link.get("url").getAsString());art.addProperty("large_url",link.get("url").getAsString());return activity;
    }
    private void connect(String id)throws IOException{
        for(int index=0;index<10;index++){
            try{pipe=new RandomAccessFile("\\\\.\\pipe\\discord-ipc-"+index,"rw");break;}
            catch(IOException absent){/* Discord selects the first available pipe. */}
        }
        if(pipe==null)return;
        JsonObject hello=new JsonObject();hello.addProperty("v",1);hello.addProperty("client_id",id);send(0,hello);
        JsonObject ready=receive();if(!ready.has("evt")||!"READY".equals(ready.get("evt").getAsString()))throw new IOException("Discord handshake failed");
    }
    private void send(int opcode,JsonObject payload)throws IOException{
        byte[] data=payload.toString().getBytes(StandardCharsets.UTF_8);
        pipe.writeInt(Integer.reverseBytes(opcode));pipe.writeInt(Integer.reverseBytes(data.length));pipe.write(data);
    }
    private JsonObject receive()throws IOException{
        for(;;){
            int op=Integer.reverseBytes(pipe.readInt()),size=Integer.reverseBytes(pipe.readInt());
            if(op==2||size<0||size>65536)throw new IOException("Discord disconnected");
            byte[] data=new byte[size];pipe.readFully(data);
            JsonObject message=new JsonParser().parse(new String(data,StandardCharsets.UTF_8)).getAsJsonObject();
            if(op!=3)return message;
            send(4,message);
        }
    }
    private void disconnect(){if(pipe!=null)try{pipe.close();}catch(IOException ignored){}pipe=null;sent="";}
}
