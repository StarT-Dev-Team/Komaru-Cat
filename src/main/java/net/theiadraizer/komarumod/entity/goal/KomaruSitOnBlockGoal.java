package net.theiadraizer.komarumod.entity.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.theiadraizer.komarumod.entity.KomaruEntity;

public class KomaruSitOnBlockGoal extends MoveToBlockGoal {
    private final KomaruEntity entity;

    public KomaruSitOnBlockGoal(KomaruEntity entity, double speedModifier) {
        super(entity, speedModifier, 8);
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

        entity.setInSittingPose(false);
    }

    public void tick() {
        super.tick();

        entity.setInSittingPose(isReachedTarget());
    }

    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        if (!level.isEmptyBlock(pos.above()))
            return false;

        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.CHEST))
            return ChestBlockEntity.getOpenCount(level, pos) < 1;

        return state.is(Blocks.FURNACE) && state.getValue(FurnaceBlock.LIT) || state.is(BlockTags.BEDS, (otherState) ->
                otherState.getOptionalValue(BedBlock.PART).map((part) -> part != BedPart.HEAD).orElse(true));
    }
}
