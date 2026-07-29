package awa.Aether_254.damage_control.content;

import awa.Aether_254.damage_control.DamageControl;
import awa.Aether_254.damage_control.DamageControlConfig;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.items.IItemHandler;

@EventBusSubscriber(modid = DamageControl.MOD_ID)
public final class ContainerCapture {
    private ContainerCapture() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreak(BlockEvent.BreakEvent event) {
        if (!event.isCanceled() && event.getLevel() instanceof Level level)
            capture(level, event.getPos(), event.getState());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onExplosion(ExplosionEvent.Detonate event) {
        for (BlockPos pos : List.copyOf(event.getAffectedBlocks()))
            capture(event.getLevel(), pos, event.getLevel().getBlockState(pos));
    }

    private static void capture(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide || isExcluded(state))
            return;
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null)
            return;

        if (blockEntity instanceof Container container) {
            List<ItemStack> stacks = snapshot(container);
            if (!qualifies(stacks))
                return;
            for (int slot = 0; slot < container.getContainerSize(); slot++)
                container.setItem(slot, ItemStack.EMPTY);
            container.setChanged();
            spawn(level, pos, stacks);
            return;
        }

        IItemHandler handler = Capabilities.ItemHandler.BLOCK.getCapability(
            level, pos, state, blockEntity, (Direction) null);
        if (handler == null)
            return;
        List<ItemStack> stacks = snapshot(handler);
        if (!qualifies(stacks))
            return;
        List<ItemStack> extracted = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack present = handler.getStackInSlot(slot);
            if (!present.isEmpty()) {
                ItemStack removed = handler.extractItem(slot, present.getCount(), false);
                if (!removed.isEmpty())
                    extracted.add(removed.copy());
            }
        }
        if (!extracted.isEmpty())
            spawn(level, pos, extracted);
    }

    private static List<ItemStack> snapshot(Container container) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (!stack.isEmpty())
                stacks.add(stack.copy());
        }
        return stacks;
    }

    private static List<ItemStack> snapshot(IItemHandler handler) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty())
                stacks.add(stack.copy());
        }
        return stacks;
    }

    private static boolean qualifies(List<ItemStack> stacks) {
        return stacks.size() > DamageControlConfig.get().minDroppedStacks;
    }

    private static void spawn(Level level, BlockPos pos, List<ItemStack> stacks) {
        DamagedPackageEntity entity = DamageControl.DAMAGED_PACKAGE.get().create(level);
        if (entity == null)
            return;
        entity.setPos(pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5);
        entity.setStoredItems(stacks);
        level.addFreshEntity(entity);
    }

    private static boolean isExcluded(BlockState state) {
        DamageControlConfig.Data config = DamageControlConfig.get();
        if (!config.enableForShulkerBoxes && state.getBlock() instanceof ShulkerBoxBlock)
            return true;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        for (String raw : config.containerBlacklist) {
            if (raw == null || raw.isBlank())
                continue;
            String value = raw.trim();
            if (value.startsWith("#")) {
                ResourceLocation id = ResourceLocation.tryParse(value.substring(1));
                if (id != null && state.is(TagKey.create(Registries.BLOCK, id)))
                    return true;
            } else {
                ResourceLocation id = ResourceLocation.tryParse(value);
                if (id != null && id.equals(blockId))
                    return true;
            }
        }
        return false;
    }
}
