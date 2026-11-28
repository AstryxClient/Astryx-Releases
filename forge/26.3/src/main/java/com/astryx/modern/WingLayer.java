package com.astryx.modern;

import com.astryx.client.features.CosmeticCatalog;
import com.astryx.client.features.CosmeticModels;
import com.astryx.client.features.CosmeticMotion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;

public final class WingLayer extends RenderLayer<AvatarRenderState,PlayerModel> {
    public WingLayer(RenderLayerParent<AvatarRenderState,PlayerModel> parent) { super(parent); }

    @Override public void submit(PoseStack pose, SubmitNodeCollector nodes, int light,
            AvatarRenderState avatar, float yaw, float pitch) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.getId() != avatar.id || avatar.isInvisible || avatar.isSpectator) return;
        var item = CosmeticCatalog.find(ModernClient.settings.getProperty("wardrobe.wings",""));
        if (item == null || item.slot != CosmeticCatalog.Slot.WINGS) return;
        var model = CosmeticModels.ready(item);
        if (model == null) return;
        getParentModel().setupAnim(avatar);
        for (var part : model.parts) {
            pose.pushPose();
            getParentModel().body.translateAndRotate(pose);
            pose.scale(1f/16,1f/16,1f/16);
            pose.translate(part.x, part.y, part.z);
            pose.mulPose(new org.joml.Matrix4f().rotate(Axis.YP.rotationDegrees(CosmeticMotion.wingYaw(part.phase, avatar.ageInTicks, avatar.walkAnimationSpeed))));
            pose.mulPose(new org.joml.Matrix4f().rotate(Axis.ZP.rotationDegrees(CosmeticMotion.wingRoll(part.phase, avatar.ageInTicks, avatar.walkAnimationSpeed))));
            var mesh = part.mesh;
            nodes.submitCustomGeometry(pose, RenderTypes.entityCutout(item.wingMaterial), (transform, out) -> {
                for (int t = 0; t < mesh.triangles(); t++) {
                    float[] v = mesh.vertices;
                    int a = t*9;
                    float ux=v[a+3]-v[a], uy=v[a+4]-v[a+1], uz=v[a+5]-v[a+2];
                    float vx=v[a+6]-v[a], vy=v[a+7]-v[a+1], vz=v[a+8]-v[a+2];
                    float nx=uy*vz-uz*vy, ny=uz*vx-ux*vz, nz=ux*vy-uy*vx;
                    float length=(float)Math.sqrt(nx*nx+ny*ny+nz*nz);
                    if (length < 0.00001f) continue;
                    // The entity pipeline takes quads. Repeat the last vertex for a triangle.
                    for (int corner=0; corner<4; corner++) {
                        int index=Math.min(2,corner), pos=a+index*3, uv=t*6+index*2;
                        out.addVertex(transform,v[pos],v[pos+1],v[pos+2]).setColor(mesh.colors[t])
                            .setUv(mesh.surfaceUV[uv]/16f+.5f,mesh.surfaceUV[uv+1]/16f+item.variant*.17f)
                            .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                            .setNormal(transform,nx/length,ny/length,nz/length);
                    }
                }
            });
            pose.popPose();
        }
    }
}
