package net.theiadraizer.komarumod.entity.goal;

import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.theiadraizer.komarumod.entity.KomaruEntity;

public class KomaruFollowOwnerGoal extends FollowOwnerGoal {

    private final KomaruEntity entity;

    public KomaruFollowOwnerGoal(
            TamableAnimal tamable,
            double speedModifier,
            float startDistance,
            float stopDistance,
            boolean canFly,
            KomaruEntity entity) {
        super(tamable, speedModifier, startDistance, stopDistance, canFly);
        this.entity = entity;
    }

    @Override
    public boolean canUse() {
        return entity.isFollowing() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return entity.isFollowing() && super.canContinueToUse();
    }
}
