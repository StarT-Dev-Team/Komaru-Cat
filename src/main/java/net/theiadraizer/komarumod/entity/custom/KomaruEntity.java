package net.theiadraizer.komarumod.entity.custom;

import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Objects;
import net.minecraft.client.Minecraft;
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
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.theiadraizer.komarumod.item.ModItems;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

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

    public KomaruEntity(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.lookControl = new FoxLookControl();
        this.moveControl = new FoxMoveControl();
    }

    boolean canMove() {
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
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.0, Ingredient.of(ModItems.KOMARU_POWDER.get()), false));
        this.goalSelector.addGoal(4, new KomaruEntityLieGoal(this, 1.1, 8));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new PanicGoal(this, 1.0));
        this.goalSelector.addGoal(7, new KomaruSitOnBlockGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3F));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 0.8, 1.0000001E-5F));
    }

    protected @Nullable SoundEvent getAmbientSound() {
        if (this.isTame()) {
            if (this.isInLove()) {
                return SoundEvents.CAT_PURR;
            } else {
                return this.random.nextInt(4) == 0 ? SoundEvents.CAT_PURREOW : SoundEvents.CAT_AMBIENT;
            }
        } else {
            return SoundEvents.CAT_STRAY_AMBIENT;
        }
    }

    public int getAmbientSoundInterval() {
        return 120;
    }

    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.CAT_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.CAT_DEATH;
    }

    public InteractionResult mobInteract(Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        Item item = itemstack.getItem();
        Item itemForTaming = ModItems.KOMARU_POWDER.get();
        Item itemForFollowing = Items.BOWL;
        if (!this.level().isClientSide && item == itemForTaming) {
            if (this.isTame() && this.getHealth() < this.getMaxHealth()) {
                this.heal(2.0F);
                return InteractionResult.CONSUME;
            }

            if (!this.isTame()) {
                if (this.random.nextInt(3) == 0) {
                    this.playSound(SoundEvents.CAT_PURR, 1.0F, 1.0F);
                    super.tame(player);
                    this.setOrderedToSit(true);
                    this.setFollowing(true);
                    this.navigation.recomputePath();
                    this.setTarget(null);
                    this.level().broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte)6);
                }

                this.usePlayerItem(player, hand, itemstack);
                return InteractionResult.CONSUME;
            }
        }

        if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (this.isTame()) {
                this.playSound(SoundEvents.CAT_PURR, 1.0F, 1.0F);

                for(int i = 0; i < 5; ++i) {
                    double d0 = this.random.nextGaussian() * 0.02;
                    double d1 = this.random.nextGaussian() * 0.02;
                    double d2 = this.random.nextGaussian() * 0.02;
                    this.level().addParticle(ParticleTypes.HEART, this.getRandomX(1.0), this.getEyeY() + 0.3, this.getRandomZ(1.0), d0, d1, d2);
                }

                this.getNavigation().stop();
                this.tickCount = 0;
                return InteractionResult.SUCCESS;
            }

            this.playSound(SoundEvents.CAT_HISS, 1.0F, 1.0F);
        }

        if (this.isTame() && !player.isShiftKeyDown()) {
            if (item == itemForFollowing && hand == InteractionHand.MAIN_HAND) {
                this.setFollowing(!this.isFollowing());
                if (this.level().isClientSide) {
                    assert Minecraft.getInstance().player != null;

                    Minecraft.getInstance().player.sendSystemMessage(Component.literal("Komaru " + (this.isFollowing() ? "follow" : "unfollow") + " owner now"));
                }

                return InteractionResult.SUCCESS;
            }

            if (hand == InteractionHand.MAIN_HAND) {
                this.clearStates();
                this.setOrderedToSit(!this.isOrderedToSit());
                return InteractionResult.SUCCESS;
            }
        }

        return super.mobInteract(player, hand);
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setInSittingPose(tag.getBoolean("isSitting"));
        this.setLying(tag.getBoolean("isLying"));
        this.setRabbit(tag.getBoolean("isRabbit"));
        this.setSleeping(tag.getBoolean("isSleeping"));
        this.setFollowing(tag.getBoolean("isFollowing"));
    }

    public @Nullable AgeableMob getBreedOffspring(@NotNull ServerLevel p_146743_, @NotNull AgeableMob p_146744_) {
        return null;
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_SITTING, false);
        this.entityData.define(IS_LYING, false);
        this.entityData.define(IS_RABBIT, false);
        this.entityData.define(IS_SLEEPING, false);
        this.entityData.define(IS_FOLLOWING, true);
    }

    public void addAdditionalSaveData(@NotNull CompoundTag p_28156_) {
        super.addAdditionalSaveData(p_28156_);
        p_28156_.putBoolean("isFollowing", this.isFollowing());
    }

    public void setFollowing(boolean p_28182_) {
        this.entityData.set(IS_FOLLOWING, p_28182_);
        if (this.isFollowing()) {
            this.goalSelector.addGoal(2, this.followOwnerGoal);
        } else {
            this.goalSelector.removeGoal(this.followOwnerGoal);
        }

    }

    public boolean isFollowing() {
        return this.entityData.get(IS_FOLLOWING);
    }

    public void setLying(boolean p_28182_) {
        this.entityData.set(IS_LYING, p_28182_);
    }

    public boolean isLying() {
        return this.entityData.get(IS_LYING);
    }

    public void setRabbit(boolean p_28182_) {
        this.entityData.set(IS_RABBIT, p_28182_);
    }

    public boolean isRabbit() {
        return this.entityData.get(IS_RABBIT);
    }

    public boolean isSleeping() {
        return this.entityData.get(IS_SLEEPING);
    }

    void setSleeping(boolean p_28627_) {
        this.entityData.set(IS_SLEEPING, p_28627_);
    }

    private <E extends GeoAnimatable> PlayState handleAnimations(AnimationState<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(WALKING_ANIM);
            return PlayState.CONTINUE;
        } else if (this.isInSittingPose()) {
            event.getController().setAnimation(SITTING_ANIM);
            return PlayState.CONTINUE;
        } else if (this.isLying()) {
            event.getController().setAnimation(LAYING_ANIM);
            return PlayState.CONTINUE;
        } else if (this.isSprinting()) {
            event.getController().setAnimation(SPRINTING_ANIM);
            return PlayState.CONTINUE;
        } else if (this.isBaby()) {
            event.getController().setAnimation(BABY_TRANSFORM_ANIM);
            return PlayState.CONTINUE;
        } else if (this.isRabbit()) {
            event.getController().setAnimation(RABBIT_ANIM);
            return PlayState.CONTINUE;
        } else if (this.isSleeping()) {
            event.getController().setAnimation(SLEEPING_ANIM);
            return PlayState.CONTINUE;
        } else {
            event.getController().setAnimation(IDLING_ANIM);
            return PlayState.CONTINUE;
        }
    }

    void clearStates() {
        this.setRabbit(false);
        this.setLying(false);
        this.setSleeping(false);
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
            BlockPos blockpos = KomaruEntity.this.blockPosition();// new BlockPos((int) KomaruEntity.this.getX(), KomaruEntity.this.getBoundingBox().maxY, KomaruEntity.this.getZ());
            return !KomaruEntity.this.level().canSeeSky(blockpos) && KomaruEntity.this.getWalkTargetValue(blockpos) >= 0.0F;
        }

        public RabbitGoal() {
            this.countdown = KomaruEntity.this.random.nextInt(WAIT_TIME_BEFORE_RABBIT);
            this.rabbitTimer = 0;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
        }

        public boolean canUse() {
            if (KomaruEntity.this.xxa == 0.0F && KomaruEntity.this.yya == 0.0F && KomaruEntity.this.zza == 0.0F) {
                return !KomaruEntity.this.isRabbit() && (this.canRabbit() || this.canContinueToUse()) && !KomaruEntity.this.isInWater();
            } else {
                return false;
            }
        }

        public boolean canContinueToUse() {
            return this.rabbitTimer > 0;
        }

        private boolean canRabbit() {
            if (this.countdown > 0) {
                --this.countdown;
                return false;
            } else {
                return !this.hasShelter();
            }
        }

        public void stop() {
            this.rabbitTimer = 0;
            this.countdown = KomaruEntity.this.random.nextInt(WAIT_TIME_BEFORE_RABBIT);
            this.rabbitTimer = 0;
            KomaruEntity.this.setRabbit(false);
        }

        public void start() {
            KomaruEntity.this.setRabbit(true);
            KomaruEntity.this.getNavigation().stop();
            KomaruEntity.this.getMoveControl().setWantedPosition(KomaruEntity.this.getX(), KomaruEntity.this.getY(), KomaruEntity.this.getZ(), 0.0);
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
//            BlockPos blockpos = new BlockPos(KomaruEntity.this.getX(), KomaruEntity.this.getBoundingBox().maxY, KomaruEntity.this.getZ());
            BlockPos blockpos = KomaruEntity.this.blockPosition();
            return !KomaruEntity.this.level().canSeeSky(blockpos) && KomaruEntity.this.getWalkTargetValue(blockpos) >= 0.0F;
        }

        public SleepGoal() {
            this.countdown = KomaruEntity.this.random.nextInt(WAIT_TIME_BEFORE_SLEEP);
            this.sleepTimer = 0;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP, Flag.TARGET));
        }

        public boolean canUse() {
            if (KomaruEntity.this.xxa == 0.0F && KomaruEntity.this.yya == 0.0F && KomaruEntity.this.zza == 0.0F) {
                return !KomaruEntity.this.isSleeping() && (this.canSleep() || this.canContinueToUse()) && !KomaruEntity.this.isInWater();
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
            KomaruEntity.this.setSleeping(true);
            KomaruEntity.this.getNavigation().stop();
            KomaruEntity.this.getMoveControl().setWantedPosition(KomaruEntity.this.getX(), KomaruEntity.this.getY(), KomaruEntity.this.getZ(), 0.0);
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

    public static class KomaruRelaxOnOwnerGoal extends Goal {
        public final KomaruEntity komaru;
        @javax.annotation.Nullable
        private Player ownerPlayer;
        @javax.annotation.Nullable
        private BlockPos goalPos;
        private int onBedTicks;

        public KomaruRelaxOnOwnerGoal(KomaruEntity p_28203_) {
            this.komaru = p_28203_;
        }

        public boolean canUse() {
            if (!this.komaru.isTame()) {
                return false;
            } else if (this.komaru.isOrderedToSit()) {
                return false;
            } else {
                LivingEntity livingentity = this.komaru.getOwner();
                if (livingentity instanceof Player) {
                    this.ownerPlayer = (Player)livingentity;
                    if (!livingentity.isSleeping()) {
                        return false;
                    }

                    if (this.komaru.distanceToSqr(this.ownerPlayer) > 100.0) {
                        return false;
                    }

                    BlockPos blockpos = this.ownerPlayer.blockPosition();
                    BlockState blockstate = this.komaru.level().getBlockState(blockpos);
                    if (blockstate.is(BlockTags.BEDS)) {
                        this.goalPos = blockstate.getOptionalValue(BedBlock.FACING).map((p_28209_) -> {
                            return blockpos.relative(p_28209_.getOpposite());
                        }).orElseGet(() -> {
                            return new BlockPos(blockpos);
                        });
                        return !this.spaceIsOccupied();
                    }
                }

                return false;
            }
        }

        private boolean spaceIsOccupied() {
            Iterator<KomaruEntity> it = this.komaru.level().getEntitiesOfClass(KomaruEntity.class, (new AABB(this.goalPos)).inflate(2.0)).iterator();

            KomaruEntity komaru;
            do {
                if (!it.hasNext()) {
                    return false;
                }

                komaru = it.next();
            } while(komaru == this.komaru || !komaru.isLying());

            return true;
        }

        public boolean canContinueToUse() {
            return this.komaru.isTame() && !this.komaru.isOrderedToSit() && this.ownerPlayer != null && this.ownerPlayer.isSleeping() && this.goalPos != null && !this.spaceIsOccupied();
        }

        public void start() {
            if (this.goalPos != null) {
                this.komaru.setInSittingPose(false);
                this.komaru.getNavigation().moveTo(this.goalPos.getX(), this.goalPos.getY(), this.goalPos.getZ(), 1.100000023841858);
            }

        }

        public void stop() {
            this.komaru.setLying(false);
            float f = this.komaru.level().getTimeOfDay(1.0F);
            if (this.ownerPlayer.getSleepTimer() >= 100 && (double)f > 0.77 && (double)f < 0.8 && (double)this.komaru.level().getRandom().nextFloat() < 0.7) {
                this.giveMorningGift();
            }

            this.onBedTicks = 0;
            this.komaru.getNavigation().stop();
        }

        public void giveMorningGift() {
            RandomSource randomsource = this.komaru.getRandom();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
            blockpos$mutableblockpos.set(this.komaru.blockPosition());
            this.komaru.randomTeleport(blockpos$mutableblockpos.getX() + randomsource.nextInt(11) - 5, blockpos$mutableblockpos.getY() + randomsource.nextInt(5) - 2, blockpos$mutableblockpos.getZ() + randomsource.nextInt(11) - 5, false);
            blockpos$mutableblockpos.set(this.komaru.blockPosition());
            LootTable lootTable = Objects.requireNonNull(komaru.level().getServer()).getLootData().getLootTable(BuiltInLootTables.CAT_MORNING_GIFT);
            LootParams lootParams = (new LootParams.Builder((ServerLevel)this.komaru.level())).withParameter(LootContextParams.ORIGIN, this.komaru.position()).withParameter(LootContextParams.THIS_ENTITY, this.komaru).create(LootContextParamSets.GIFT);
            ObjectListIterator<ItemStack> it = lootTable.getRandomItems(lootParams).iterator();

            while(it.hasNext()) {
                ItemStack itemstack = it.next();
                this.komaru.level().addFreshEntity(new ItemEntity(this.komaru.level(), (double)blockpos$mutableblockpos.getX() - (double)Mth.sin(this.komaru.yBodyRot * 0.017453292F), blockpos$mutableblockpos.getY(), (double)blockpos$mutableblockpos.getZ() + (double)Mth.cos(this.komaru.yBodyRot * 0.017453292F), itemstack));
            }

        }

        public void tick() {
            if (this.ownerPlayer != null && this.goalPos != null) {
                this.komaru.setInSittingPose(false);
                this.komaru.getNavigation().moveTo(this.goalPos.getX(), this.goalPos.getY(), this.goalPos.getZ(), 1.100000023841858);
                if (this.komaru.distanceToSqr(this.ownerPlayer) < 2.5) {
                    ++this.onBedTicks;
                    if (this.onBedTicks > this.adjustedTickDelay(16)) {
                        this.komaru.setLying(true);
                    } else {
                        this.komaru.lookAt(this.ownerPlayer, 45.0F, 45.0F);
                    }
                } else {
                    this.komaru.setLying(false);
                }
            }

        }
    }

    public static class KomaruEntityLieGoal extends MoveToBlockGoal {
        private final KomaruEntity komaruEntity;

        public KomaruEntityLieGoal(KomaruEntity p_25135_, double p_25136_, int p_25137_) {
            super(p_25135_, p_25136_, p_25137_);
            this.komaruEntity = p_25135_;
        }

        public boolean canUse() {
            return !this.komaruEntity.isSleeping() && this.komaruEntity.isTame() && !this.komaruEntity.isOrderedToSit() && super.canUse();
        }

        public void start() {
            super.start();
            this.komaruEntity.clearStates();
        }

        public void stop() {
            super.stop();
            this.komaruEntity.clearStates();
        }

        public void tick() {
            super.tick();
            this.komaruEntity.clearStates();
            if (!this.isReachedTarget()) {
                this.komaruEntity.clearStates();
            } else if (!this.komaruEntity.isLying()) {
                this.komaruEntity.setLying(true);
            }

        }

        protected boolean isValidTarget(LevelReader p_25142_, BlockPos p_25143_) {
            return p_25142_.isEmptyBlock(p_25143_.above()) && p_25142_.getBlockState(p_25143_).is(BlockTags.BEDS) || p_25142_.isEmptyBlock(p_25143_.above()) && p_25142_.getBlockState(p_25143_).is(BlockTags.WOOL_CARPETS) || p_25142_.isEmptyBlock(p_25143_.above()) && p_25142_.getBlockState(p_25143_).is(BlockTags.WOOL);
        }
    }

    public static class KomaruSitOnBlockGoal extends MoveToBlockGoal {
        private final KomaruEntity komaruEntity;

        public KomaruSitOnBlockGoal(KomaruEntity p_25149_, double p_25150_) {
            super(p_25149_, p_25150_, 8);
            this.komaruEntity = p_25149_;
        }

        public boolean canUse() {
            return !this.komaruEntity.isSleeping() && this.komaruEntity.isTame() && !this.komaruEntity.isOrderedToSit() && super.canUse();
        }

        public void start() {
            super.start();
            this.komaruEntity.clearStates();
        }

        public void stop() {
            super.stop();
            this.komaruEntity.setInSittingPose(false);
        }

        public void tick() {
            super.tick();
            this.komaruEntity.setInSittingPose(this.isReachedTarget());
        }

        protected boolean isValidTarget(LevelReader p_25153_, BlockPos p_25154_) {
            if (!p_25153_.isEmptyBlock(p_25154_.above())) {
                return false;
            } else {
                BlockState blockstate = p_25153_.getBlockState(p_25154_);
                if (blockstate.is(Blocks.CHEST)) {
                    return ChestBlockEntity.getOpenCount(p_25153_, p_25154_) < 1;
                } else {
                    return blockstate.is(Blocks.FURNACE) && blockstate.getValue(FurnaceBlock.LIT) || blockstate.is(BlockTags.BEDS, (p_25156_) -> {
                        return p_25156_.getOptionalValue(BedBlock.PART).map((p_148084_) -> {
                            return p_148084_ != BedPart.HEAD;
                        }).orElse(true);
                    });
                }
            }
        }
    }
}
