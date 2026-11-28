package com.astryx.modern.mixin;
import com.astryx.modern.*;
import net.minecraft.client.*;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHook {
    @Inject(method={"keyPress"},at=@At("HEAD"),remap=false,cancellable=true)
    private void astryxKey(long window,int action,net.minecraft.client.input.KeyEvent input,CallbackInfo ci){int key=input.key();
        Minecraft mc=Minecraft.getInstance();
        if(key==InputConstants.KEY_RSHIFT && action==InputConstants.PRESS && window==mc.getWindow().handle()
            && (mc.gui.screen()==null || mc.gui.screen() instanceof SettingsScreen)){
            if(mc.gui.screen()==null)mc.gui.setScreen(new SettingsScreen());else mc.gui.screen().onClose();
            ci.cancel();
        }
    }
}
@Mixin(MouseHandler.class)
abstract class MouseHook {
    @Inject(method={"onButton"},at=@At("HEAD"),remap=false)
    private void astryxClick(long window,net.minecraft.client.input.MouseButtonInfo input,int action,CallbackInfo ci){int button=input.button();
        Minecraft mc=Minecraft.getInstance();
        if(button==com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT && action==1 && mc.gui.screen()==null && mc.player!=null && window==mc.getWindow().handle())ModernClient.click();
    }
}
