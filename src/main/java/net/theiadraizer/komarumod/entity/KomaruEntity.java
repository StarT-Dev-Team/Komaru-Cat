package net.theiadraizer.komarumod.entity;

import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.theiadraizer.komarumod.entity.goal.KomaruLieGoal;
import net.theiadraizer.komarumod.entity.goal.KomaruRelaxOnOwnerGoal;
import net.theiadraizer.komarumod.entity.goal.KomaruSitOnBlockGoal;
import net.theiadraizer.komarumod.registry.KomaruItems;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class KomaruEntity extends TamableAnimal implements GeoEntity {
    // Entity data
    private static final EntityDataAccessor<Boolean> IS_SITTING = SynchedEntityData.defineId(KomaruEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_LYING = SynchedEntityData.defineId(KomaruEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_SLEEPING = SynchedEntityData.defineId(KomaruEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_RABBIT = SynchedEntityData.defineId(KomaruEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_FOLLOWING = SynchedEntityData.defineId(KomaruEntity.class, EntityDataSerializers.BOOLEAN);

    // Animations
    private static final RawAnimation WALKING_ANIM = RawAnimation.begin().thenPlay("animation.komaru.walk");
    private static final RawAnimation SITTING_ANIM = RawAnimation.begin().thenPlay("animation.komaru.sitting");
    private static final RawAnimation LAYING_ANIM = RawAnimation.begin().thenPlay("animation.komaru.lie_down");
    private static final RawAnimation SPRINTING_ANIM = RawAnimation.begin().thenPlay("animation.komaru.sprint");
    private static final RawAnimation BABY_TRANSFORM_ANIM = RawAnimation.begin().thenPlay("animation.komaru.baby_transform");
    private static final RawAnimation RABBIT_ANIM = RawAnimation.begin().thenPlay("animation.komaru.rabbit");
    private static final RawAnimation SLEEPING_ANIM = RawAnimation.begin().thenPlay("animation.komaru.sleep");
    private static final RawAnimation IDLING_ANIM = RawAnimation.begin().thenPlay("animation.komaru.idle");

    // State
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    FollowOwnerGoal followOwnerGoal = new FollowOwnerGoal(this, 1.0, 15.0F, 5.0F, false);

    public KomaruEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.lookControl = new FoxLookControl();
        this.moveControl = new FoxMoveControl();
    }

    public boolean canMove() {
        return !this.isSleeping() && !this.isOrderedToSit() && !this.isLying();
    }


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::handleAnimations));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public static AttributeSupplier setAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 7.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ATTACK_SPEED, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4000000059604645)
                .build();
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(5, new RabbitGoal());
        this.goalSelector.addGoal(10, new BreedGoal(this, 0.8));
        this.goalSelector.addGoal(2, new FloatGoal(this));
        this.goalSelector.addGoal(3, new SleepGoal());
        this.goalSelector.addGoal(0, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(1, new KomaruRelaxOnOwnerGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, Ingredient.of(KomaruItems.KOMARU_POWDER.get()), false));
        this.goalSelector.addGoal(4, new KomaruLieGoal(this, 1.1, 8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new PanicGoal(this, 1.0));
        this.goalSelector.addGoal(7, new KomaruSitOnBlockGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3F));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
    }

    @Nullable
    protected SoundEvent getAmbientSound() {
        if (!this.isTame())
            return SoundEvents.CAT_STRAY_AMBIENT;

        if (this.isInLove())
            return SoundEvents.CAT_PURR;

        return random.nextInt(4) == 0 ? SoundEvents.CAT_PURREOW : SoundEvents.CAT_AMBIENT;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CAT_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    @Nonnull
    public InteractionResult mobInteract(Player player, @Nonnull InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        var item = stack.getItem();
        var isItemForTaming = KomaruItems.KOMARU_POWDER.get();
        var isItemForFollowing = Items.BOWL;
        var level = level();

        if (!level.isClientSide && item == isItemForTaming) {
            if (isTame() && getHealth() < getMaxHealth()) {
                heal(2.0F);
                return InteractionResult.CONSUME;
            }

            if (!isTame()) {
                if (random.nextInt(3) == 0) {
                    playSound(SoundEvents.CAT_PURR, 1.0F, 1.0F);
                    super.tame(player);
                    setOrderedToSit(true);
                    setFollowing(true);
                    navigation.recomputePath();
                    setTarget(null);
                    level.broadcastEntityEvent(this, (byte)7);
                } else {
                    level.broadcastEntityEvent(this, (byte)6);
                }

                usePlayerItem(player, hand, stack);
                return InteractionResult.CONSUME;
            }
        }

        if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (isTame()) {
                playSound(SoundEvents.CAT_PURR, 1.0F, 1.0F);

                for(int i = 0; i < 5; ++i) {
                    double d0 = this.random.nextGaussian() * 0.02;
                    double d1 = this.random.nextGaussian() * 0.02;
                    double d2 = this.random.nextGaussian() * 0.02;
                    level.addParticle(ParticleTypes.HEART, getRandomX(1.0), getEyeY() + 0.3, getRandomZ(1.0), d0, d1, d2);
                }

                getNavigation().stop();
                tickCount = 0;
                return InteractionResult.SUCCESS;
            }

            this.playSound(SoundEvents.CAT_HISS, 1.0F, 1.0F);
        }

        if (isTame() && !player.isShiftKeyDown()) {
            if (item == isItemForFollowing && hand == InteractionHand.MAIN_HAND) {
                setFollowing(!this.isFollowing());
                if (level.isClientSide) {
                    player.displayClientMessage(Component.literal("Komaru " + (this.isFollowing() ? "follow" : "unfollow") + " owner now"), false);
                }

                return InteractionResult.SUCCESS;
            }

            if (hand == InteractionHand.MAIN_HAND) {
                clearStates();
                setOrderedToSit(!isOrderedToSit());
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    public void readAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setInSittingPose(tag.getBoolean("isSitting"));
        setLying(tag.getBoolean("isLying"));
        setRabbit(tag.getBoolean("isRabbit"));
        setSleeping(tag.getBoolean("isSleeping"));
        setFollowing(tag.getBoolean("isFollowing"));
    }

    public @Nullable AgeableMob getBreedOffspring(@Nonnull ServerLevel level, @Nonnull AgeableMob mob) {
        return null;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IS_SITTING, false);
        entityData.define(IS_LYING, false);
        entityData.define(IS_RABBIT, false);
        entityData.define(IS_SLEEPING, false);
        entityData.define(IS_FOLLOWING, true);
    }

    public void addAdditionalSaveData(@Nonnull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("isFollowing", isFollowing());
    }

    public void setFollowing(boolean isFollowing) {
        entityData.set(IS_FOLLOWING, isFollowing);

        if (isFollowing()) {
            goalSelector.addGoal(2, followOwnerGoal);
        } else {
            goalSelector.removeGoal(followOwnerGoal);
        }
    }

    public boolean isFollowing() {
        return entityData.get(IS_FOLLOWING);
    }

    public void setLying(boolean isLying) {
        entityData.set(IS_LYING, isLying);
    }

    public boolean isLying() {
        return entityData.get(IS_LYING);
    }

    public void setRabbit(boolean isRabbit) {
        entityData.set(IS_RABBIT, isRabbit);
    }

    public boolean isRabbit() {
        return entityData.get(IS_RABBIT);
    }

    public boolean isSleeping() {
        return entityData.get(IS_SLEEPING);
    }

    void setSleeping(boolean isSleeping) {
        entityData.set(IS_SLEEPING, isSleeping);
    }

    private <E extends GeoAnimatable> PlayState handleAnimations(AnimationState<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(WALKING_ANIM);
        } else if (this.isInSittingPose()) {
            event.getController().setAnimation(SITTING_ANIM);
        } else if (this.isLying()) {
            event.getController().setAnimation(LAYING_ANIM);
        } else if (this.isSprinting()) {
            event.getController().setAnimation(SPRINTING_ANIM);
        } else if (this.isBaby()) {
            event.getController().setAnimation(BABY_TRANSFORM_ANIM);
        } else if (this.isRabbit()) {
            event.getController().setAnimation(RABBIT_ANIM);
        } else if (this.isSleeping()) {
            event.getController().setAnimation(SLEEPING_ANIM);
        } else {
            event.getController().setAnimation(IDLING_ANIM);
        }

        return PlayState.CONTINUE;
    }

    public void clearStates() {
        setRabbit(false);
        setLying(false);
        setSleeping(false);
    }

    public class FoxLookControl extends LookControl {
        public FoxLookControl() {
            super(KomaruEntity.this);
        }

        public void tick() {
            if (!KomaruEntity.this.isSleeping()) {
                super.tick();
            }
        }
    }

    class FoxMoveControl extends MoveControl {
        public FoxMoveControl() {
            super(KomaruEntity.this);
        }

        public void tick() {
            if (KomaruEntity.this.canMove()) {
                super.tick();
            }
        }
    }

    class RabbitGoal extends Goal {
        private static final int WAIT_TIME_BEFORE_RABBIT = reducedTickDelay(6000);
        private int countdown;
        private static final int RABBIT_DURATION = reducedTickDelay(60);
        private int rabbitTimer;

        protected boolean hasShelter() {
            final var komaru = KomaruEntity.this;
            BlockPos blockpos = komaru.blockPosition();
            return !komaru.level().canSeeSky(blockpos) && komaru.getWalkTargetValue(blockpos) >= 0.0F;
        }

        public RabbitGoal() {
            this.countdown = KomaruEntity.this.random.nextInt(WAIT_TIME_BEFORE_RABBIT);
            this.rabbitTimer = 0;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
        }

        public boolean canUse() {
            final var komaru = KomaruEntity.this;
            if (komaru.xxa == 0.0F && komaru.yya == 0.0F && komaru.zza == 0.0F) {
                return !komaru.isRabbit() && (canRabbit() || canContinueToUse()) && !komaru.isInWater();
            } else {
                return false;
            }
        }

        public boolean canContinueToUse() {
            return rabbitTimer > 0;
        }

        private boolean canRabbit() {
            if (countdown > 0) {
                --countdown;
                return false;
            } else {
                return !hasShelter();
            }
        }

        public void stop() {
            this.rabbitTimer = 0;
            this.countdown = KomaruEntity.this.random.nextInt(WAIT_TIME_BEFORE_RABBIT);
            this.rabbitTimer = 0;
            KomaruEntity.this.setRabbit(false);
        }

        public void start() {
            final var komaru = KomaruEntity.this;
            komaru.setRabbit(true);
            komaru.getNavigation().stop();
            komaru.getMoveControl().setWantedPosition(komaru.getX(), komaru.getY(), komaru.getZ(), 0.0);
        }

        public void tick() {
            if (this.canRabbit()) {
                ++this.rabbitTimer;
                if (this.rabbitTimer >= RABBIT_DURATION) {
                    KomaruEntity.this.setSleeping(false);
                    this.rabbitTimer = 0;
                }
            } else {
                this.rabbitTimer = 0;
            }
        }
    }

    class SleepGoal extends Goal {
        private static final int WAIT_TIME_BEFORE_SLEEP = reducedTickDelay(1200);
        private static final int SLEEP_DURATION = reducedTickDelay(600);
        private int countdown;
        private int sleepTimer;

        protected boolean hasShelter() {
            final var komaru = KomaruEntity.this;
            BlockPos blockpos = komaru.blockPosition();
            return !komaru.level().canSeeSky(blockpos) && komaru.getWalkTargetValue(blockpos) >= 0.0F;
        }

        public SleepGoal() {
            this.countdown = KomaruEntity.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            this.sleepTimer = 0;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
        }

        public boolean canUse() {
            final var komaru = KomaruEntity.this;
            if (komaru.xxa == 0.0F && komaru.yya == 0.0F && komaru.zza == 0.0F) {
                return !komaru.isSleeping() && (canSleep() || canContinueToUse()) && !komaru.isInWater();
            } else {
                return false;
            }
        }

        public boolean canContinueToUse() {
            return this.sleepTimer > 0;
        }

        private boolean canSleep() {
            if (this.countdown > 0) {
                --this.countdown;
                return false;
            } else {
                return this.hasShelter() && !KomaruEntity.this.isInPowderSnow;
            }
        }

        public void stop() {
            this.countdown = KomaruEntity.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            this.sleepTimer = 0;
            KomaruEntity.this.setSleeping(false);
        }

        public void start() {
            final var komaru = KomaruEntity.this;
            komaru.setSleeping(true);
            komaru.getNavigation().stop();
            komaru.getMoveControl().setWantedPosition(komaru.getX(), komaru.getY(), komaru.getZ(), 0.0);
        }

        public void tick() {
            if (this.canSleep()) {
                ++this.sleepTimer;
                if (this.sleepTimer >= SLEEP_DURATION) {
                    KomaruEntity.this.setSleeping(false);
                    this.sleepTimer = 0;
                }
            } else {
                this.sleepTimer = 0;
            }

        }
    }

}
