package com.astryx.modern.mixin;
import com.astryx.modern.DiscordPresence;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
abstract class TickHook {
    @Inject(method={"tick","method_1574"},at=@At("TAIL"),remap=false)
    private void astryxPresence(CallbackInfo ci){DiscordPresence.INSTANCE.tick((Minecraft)(Object)this);}
}
