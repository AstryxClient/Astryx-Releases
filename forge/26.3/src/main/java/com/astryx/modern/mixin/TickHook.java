package com.astryx.modern.mixin;
import com.astryx.modern.DiscordPresence;
import com.astryx.modern.ModernClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Minecraft.class)
abstract class TickHook {
    @Inject(method="tick",at=@At("TAIL"),remap=false)
    private void astryxPresence(CallbackInfo ci){
        Minecraft mc = (Minecraft)(Object)this;
        ModernClient.rememberServer(mc);
        DiscordPresence.INSTANCE.tick(mc);
    }
}
