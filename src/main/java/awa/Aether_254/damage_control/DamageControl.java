package awa.Aether_254.damage_control;

import awa.Aether_254.damage_control.client.DamagedPackageRenderer;
import awa.Aether_254.damage_control.client.DamageControlConfigScreen;
import awa.Aether_254.damage_control.content.DamagedPackageEntity;
import com.simibubi.create.content.logistics.box.PackageEntity;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(DamageControl.MOD_ID)
public final class DamageControl {
    public static final String MOD_ID = "damage_control";
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
        DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
    public static final Supplier<EntityType<DamagedPackageEntity>> DAMAGED_PACKAGE = ENTITY_TYPES.register(
        "damaged_package",
        () -> EntityType.Builder.<DamagedPackageEntity>of(DamagedPackageEntity::new, MobCategory.MISC)
            .sized(1.0f, 1.0f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .build(MOD_ID + ":damaged_package"));

    public DamageControl(IEventBus modBus, ModContainer container) {
        ENTITY_TYPES.register(modBus);
        modBus.addListener(DamageControl::registerAttributes);
        DamageControlConfig.load();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            DamageControlConfigScreen.register(container);
            modBus.addListener(DamageControl::registerRenderers);
        }
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(DAMAGED_PACKAGE.get(), PackageEntity.createPackageAttributes().build());
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(DAMAGED_PACKAGE.get(), DamagedPackageRenderer::new);
    }
}
