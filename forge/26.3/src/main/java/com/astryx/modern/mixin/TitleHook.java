package com.astryx.modern.mixin;
import com.astryx.modern.*;
import java.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
abstract class TitleHook extends Screen {
    @Unique private final List<Glass> astryxButtons=new ArrayList<>();
    protected TitleHook(Component t){super(t);}
    @Unique private Glass astryxAction(int x,int y,int w,int h,String label,Runnable action){
        Glass button=Glass.action(x,y,w,h,label,action);
        addRenderableWidget(button);astryxButtons.add(button);return button;
    }
    @Unique private int astryxMenuTop(){return Math.max(60, Math.round(height * .43f - 25));}
    @Inject(method="init",at=@At("TAIL"),remap=false)
    private void astryxInit(CallbackInfo ci){
        clearWidgets();astryxButtons.clear();
        int x=width/2-100,y=astryxMenuTop();
        astryxAction(x,y,200,22,"Singleplayer",()->GameApi.open(minecraft, new AstryxMenus.Worlds(this)));
        astryxAction(x,y+26,200,22,"Multiplayer",()->GameApi.open(minecraft, new AstryxMenus.Servers(this))).active=minecraft.allowsMultiplayer();
        astryxAction(x,y+52,98,21,"Modules",()->GameApi.open(minecraft, new SettingsScreen(this)));
        astryxAction(x+102,y+52,98,21,"Options",()->GameApi.open(minecraft, GameApi.options(minecraft, this)));
        astryxAction(x,y+77,200,20,"Quit game",()->minecraft.stop());
    }
    @Inject(method="extractRenderState",at=@At("HEAD"),remap=false,cancellable=true)
    private void astryxTitle(GuiGraphicsExtractor c,int mx,int my,float delta,CallbackInfo ci){
        Glass.sky(c,width,height);
        int y=astryxMenuTop();
        Glass.heading(c,"ASTRYX",width/2-(int)(font.width("ASTRYX")*1.4f),y-46,2.8f);
        c.centeredText(font,"Your game, your way.",width/2,y-17,Glass.LILAC);
        c.fill(width/2-14,y-5,width/2+14,y-4,0xff8061d8);
        for(Glass b:astryxButtons)b.extractRenderState(c,mx,my,delta);
        ci.cancel();
    }
}
