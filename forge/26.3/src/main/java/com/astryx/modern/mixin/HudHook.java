package com.astryx.modern.mixin;
import com.astryx.modern.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Hud.class)
abstract class HudHook {
    @Inject(method={"extractRenderState","method_1753"},at=@At("TAIL"),remap=false)
    private void astryxHud(GuiGraphicsExtractor ctx,net.minecraft.client.DeltaTracker tick,CallbackInfo ci){ ModernClient.hud(ctx); }
}
