package com.astryx.modern.mixin;
import com.astryx.modern.*;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Camera.class)
abstract class ZoomHook {
    @Inject(method={"calculateFov"},at=@At("RETURN"),remap=false,cancellable=true)
    private void astryxZoom(CallbackInfoReturnable<Float> result){
        if(ModernClient.zooming())result.setReturnValue(result.getReturnValue()/4);
    }
}
