package net.theiadraizer.komarumod.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.theiadraizer.komarumod.entity.KomaruEntity;

public class KomaruLieGoal extends MoveToBlockGoal {
    private final KomaruEntity entity;

    public KomaruLieGoal(KomaruEntity entity, double speedModifier, int searchRange) {
        super(entity, speedModifier, searchRange);
        this.entity = entity;
    }

    public boolean canUse() {
        return !entity.isSleeping() && entity.isTame() && !entity.isOrderedToSit() && super.canUse();
    }

    public void start() {
        super.start();

        entity.clearStates();
    }

    public void stop() {
        super.stop();

        entity.clearStates();
    }

    public void tick() {
        super.tick();

        entity.clearStates();
        if (!isReachedTarget()) {
            entity.clearStates();
        } else if (!entity.isLying()) {
            entity.setLying(true);
        }
    }

    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        if (!level.isEmptyBlock(pos.above()))
            return false;

        var state = level.getBlockState(pos);
        return state.is(BlockTags.BEDS) || state.is(BlockTags.WOOL_CARPETS) || state.is(BlockTags.WOOL);
    }
}
