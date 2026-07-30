package awa.Aether_254.damage_control.client;

import awa.Aether_254.damage_control.content.DamagedPackageEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;

public final class DamagedPackageRenderer extends EntityRenderer<DamagedPackageEntity> {
    private final ItemRenderer itemRenderer;

    public DamagedPackageRenderer(EntityRendererProvider.Context context) {
        super(context);
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0.5f;
    }

    @Override
    public void render(DamagedPackageEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0, 0.35, 0);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.scale(1.5f, 1.5f, 1.5f);
        itemRenderer.renderStatic(Items.BARREL.getDefaultInstance(), ItemDisplayContext.GROUND,
            light, OverlayTexture.NO_OVERLAY, pose, buffers, entity.level(), entity.getId());
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(DamagedPackageEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
