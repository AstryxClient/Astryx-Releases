package com.astryx.modern.mixin;

import com.astryx.modern.DiscordPresence;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
abstract class SystemChatHook {
    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = true, remap = false)
    private void astryxLocation(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isSameThread() && !packet.overlay() && DiscordPresence.INSTANCE.consumeLocation(packet.content().getString(), mc)) {
            ci.cancel();
        }
    }
}
