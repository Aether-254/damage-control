package awa.Aether_254.damage_control.client;

import awa.Aether_254.damage_control.content.DamagedPackageEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class DamagedPackageRenderer extends EntityRenderer<DamagedPackageEntity> {
    public DamagedPackageRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.5f;
    }

    @Override
    public void render(DamagedPackageEntity entity, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        if (!VisualizationManager.supportsVisualization(entity.level())) {
            ItemStack box = entity.getBox();
            if (box.isEmpty() || !PackageItem.isPackage(box))
                box = AllBlocks.CARDBOARD_BLOCK.asStack();
            PartialModel model = AllPartialModels.PACKAGES.get(BuiltInRegistries.ITEM.getKey(box.getItem()));
            PackageRenderer.renderBox(entity, yaw, pose, buffers, light, model);
        }
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(DamagedPackageEntity entity) {
        return null;
    }
}
