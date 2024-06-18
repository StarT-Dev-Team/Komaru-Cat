package net.theiadraizer.komarumod.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.theiadraizer.komarumod.entity.KomaruEntity;

import javax.annotation.Nullable;
import java.util.Objects;

public class KomaruRelaxOnOwnerGoal extends Goal {
    public final KomaruEntity entity;
    @Nullable
    private Player ownerPlayer;
    @Nullable
    private BlockPos goalPos;
    private int onBedTicks;

    public KomaruRelaxOnOwnerGoal(KomaruEntity entity) {
        this.entity = entity;
    }

    public boolean canUse() {
        if (!entity.isTame() || entity.isOrderedToSit())
            return false;

        LivingEntity owner = entity.getOwner();
        if (!(owner instanceof Player))
            return false;

        ownerPlayer = (Player) owner;
        if (!owner.isSleeping())
            return false;

        if (entity.distanceToSqr(ownerPlayer) > 100.0)
            return false;

        BlockPos pos = ownerPlayer.blockPosition();
        BlockState state = entity.level().getBlockState(pos);
        if (!state.is(BlockTags.BEDS))
            return false;

        this.goalPos = state.getOptionalValue(BedBlock.FACING)
                .map(direction -> pos.relative(direction.getOpposite()))
                .orElseGet(() -> new BlockPos(pos));

        return !spaceIsOccupied();

    }

    private boolean spaceIsOccupied() {
        for (KomaruEntity other : entity.level().getEntitiesOfClass(KomaruEntity.class, new AABB(goalPos).inflate(2.0))) {
            if (other != entity && other.isLying())
                return true;
        }
        return false;
    }

    public boolean canContinueToUse() {
        return entity.isTame() && !entity.isOrderedToSit() && ownerPlayer != null && ownerPlayer.isSleeping() && goalPos != null && !spaceIsOccupied();
    }

    public void start() {
        if (goalPos != null) {
            entity.setInSittingPose(false);
            entity.getNavigation().moveTo(goalPos.getX(), goalPos.getY(), goalPos.getZ(), 1.100000023841858);
        }
    }

    public void stop() {
        entity.setLying(false);
        float f = entity.level().getTimeOfDay(1.0F);
        if (ownerPlayer.getSleepTimer() >= 100 && (double) f > 0.77 && (double) f < 0.8 && (double) entity.level().getRandom().nextFloat() < 0.7) {
            giveMorningGift();
        }
        onBedTicks = 0;
        entity.getNavigation().stop();
    }

    public void giveMorningGift() {
        RandomSource random = this.entity.getRandom();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        pos.set(this.entity.blockPosition());
        entity.randomTeleport(pos.getX() + random.nextInt(11) - 5, pos.getY() + random.nextInt(5) - 2, pos.getZ() + random.nextInt(11) - 5, false);
        pos.set(this.entity.blockPosition());

        ServerLevel level = (ServerLevel) entity.level();
        LootTable lootTable = Objects.requireNonNull(level.getServer()).getLootData().getLootTable(BuiltInLootTables.CAT_MORNING_GIFT);
        LootParams lootParams = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, this.entity.position())
                .withParameter(LootContextParams.THIS_ENTITY, this.entity)
                .create(LootContextParamSets.GIFT);

        lootTable.getRandomItems(lootParams)
                .forEach(stack -> {
                    var entity = new ItemEntity(level,
                            (double) pos.getX() - (double) Mth.sin(this.entity.yBodyRot * 0.017453292F),
                            pos.getY(),
                            (double) pos.getZ() + (double) Mth.cos(this.entity.yBodyRot * 0.017453292F),
                            stack);
                    entity.level().addFreshEntity(entity);
                });
    }

    public void tick() {
        if (ownerPlayer == null || goalPos == null)
            return;

        entity.setInSittingPose(false);
        entity.getNavigation().moveTo(goalPos.getX(), goalPos.getY(), goalPos.getZ(), 1.100000023841858);

        if (entity.distanceToSqr(ownerPlayer) < 2.5) {
            ++onBedTicks;
            if (onBedTicks > adjustedTickDelay(16)) {
                entity.setLying(true);
            } else {
                entity.lookAt(ownerPlayer, 45.0F, 45.0F);
            }
        } else {
            entity.setLying(false);
        }

    }
}
