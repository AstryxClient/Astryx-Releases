package com.astryx.modern.mixin;

import com.astryx.modern.WingLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
abstract class AvatarHook {
    @Inject(method="<init>", at=@At("RETURN"), remap=false)
    private void astryxAttach(CallbackInfo ci) {
        ((LayerAccess)this).astryxAddLayer(new WingLayer((AvatarRenderer<?>)(Object)this));
    }
}
