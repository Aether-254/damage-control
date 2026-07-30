package awa.Aether_254.damage_control.content;

import awa.Aether_254.damage_control.DamageControlConfig;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.box.PackageStyles;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;

public final class DamagedPackageEntity extends PackageEntity implements Leashable {
    private final List<ItemStack> storedItems = new ArrayList<>();
    @Nullable
    private LeashData leashData;

    public DamagedPackageEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        setBox(PackageStyles.getDefaultBox());
        setGlowingTag(DamageControlConfig.get().entity.glowEffect);
    }

    public void setStoredItems(List<ItemStack> items) {
        storedItems.clear();
        for (ItemStack stack : items) {
            if (!stack.isEmpty())
                storedItems.add(stack.copy());
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!isAlive())
            return;
        setGlowingTag(DamageControlConfig.get().entity.glowEffect);
        Leashable.tickLeash(this);
    }

    @Override
    protected void onInsideBlock(BlockState state) {
    }

    @Override
    protected void onBelowWorld() {
        switch (DamageControlConfig.get().voidHandling) {
            case FLOAT -> {
                setPos(getX(), level().getMinBuildHeight() + 1.0, getZ());
                setDeltaMovement(Vec3.ZERO);
                setNoGravity(true);
            }
            case TELEPORT -> {
                BlockPos spawn = level().getSharedSpawnPos();
                BlockPos landing = findSafeLanding(spawn);
                setDeltaMovement(Vec3.ZERO);
                if (landing == null) {
                    setNoGravity(true);
                    setPos(spawn.getX() + 0.5, level().getMinBuildHeight() + 1.0, spawn.getZ() + 0.5);
                } else {
                    setNoGravity(false);
                    setPos(landing.getX() + 0.5, landing.getY(), landing.getZ() + 0.5);
                }
                fallDistance = 0;
            }
            case SCATTER -> {
                setPos(getX(), level().getMinBuildHeight() + 1.0, getZ());
                releaseContents();
            }
            case DESTROY -> discard();
        }
    }

    @Nullable
    private BlockPos findSafeLanding(BlockPos spawn) {
        int minY = level().getMinBuildHeight();
        int maxY = level().getMaxBuildHeight() - 2;
        int startY = Math.max(minY, Math.min(maxY, spawn.getY()));
        for (int y = startY; y <= maxY; y++) {
            BlockPos support = new BlockPos(spawn.getX(), y, spawn.getZ());
            if (isSafeSupport(support))
                return support.above();
        }
        for (int y = startY - 1; y >= minY; y--) {
            BlockPos support = new BlockPos(spawn.getX(), y, spawn.getZ());
            if (isSafeSupport(support))
                return support.above();
        }
        return null;
    }

    private boolean isSafeSupport(BlockPos support) {
        return !level().getBlockState(support).getCollisionShape(level(), support).isEmpty()
            && level().getBlockState(support.above()).isAir()
            && level().getBlockState(support.above(2)).isAir();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player player) || !isAlive())
            return false;
        if (!CommonHooks.onPlayerAttackTarget(player, this) || !player.getAbilities().mayBuild)
            return false;
        if (!level().isClientSide)
            releaseContents();
        return true;
    }

    private void releaseContents() {
        if (!(level() instanceof ServerLevel serverLevel) || !isAlive())
            return;
        serverLevel.sendParticles(ParticleTypes.POOF, getX(), getY() + 0.4, getZ(),
            18, 0.35, 0.3, 0.35, 0.03);
        AllSoundEvents.PACKAGE_POP.playOnServer(level(), blockPosition());
        for (ItemStack stack : storedItems) {
            if (stack.isEmpty())
                continue;
            ItemEntity item = new ItemEntity(level(), getX(), getY(), getZ(), stack.copy(),
                0.0, 0.0, 0.0);
            item.setDeltaMovement(Vec3.ZERO);
            item.setPickUpDelay(10);
            level().addFreshEntity(item);
        }
        storedItems.clear();
        remove(RemovalReason.KILLED);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.LEAD) && canHaveALeashAttachedToIt()) {
            if (!level().isClientSide) {
                setLeashedTo(player, true);
                if (!player.hasInfiniteMaterials())
                    held.shrink(1);
                gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (held.isEmpty() && player.isShiftKeyDown() && getLeashHolder() == player) {
            if (!level().isClientSide) {
                dropLeash(true, !player.hasInfiniteMaterials());
                gameEvent(GameEvent.ENTITY_INTERACT, player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public boolean canBeLeashed() {
        return DamageControlConfig.get().entity.canBeLeashed;
    }

    @Override
    public void leashTooFarBehaviour() {
        Entity holder = getLeashHolder();
        if (holder != null)
            elasticRangeLeashBehaviour(holder, distanceTo(holder));
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isAttackable() {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Nullable
    @Override
    public LeashData getLeashData() {
        return leashData;
    }

    @Override
    public void setLeashData(@Nullable LeashData data) {
        leashData = data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ListTag items = new ListTag();
        for (ItemStack stack : storedItems)
            items.add(stack.save(level().registryAccess()));
        tag.put("StoredItems", items);
        writeLeashData(tag, leashData);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        storedItems.clear();
        ListTag items = tag.getList("StoredItems", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(level().registryAccess(), items.getCompound(i));
            if (!stack.isEmpty())
                storedItems.add(stack);
        }
        leashData = readLeashData(tag);
    }
}
