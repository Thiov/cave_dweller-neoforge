package de.cadentem.cave_dweller.entities;

import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.state.AnimationTest;
import software.bernie.geckolib.animation.object.LoopType;
import software.bernie.geckolib.animation.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;
import de.cadentem.cave_dweller.config.ServerConfig;
import de.cadentem.cave_dweller.entities.goals.*;
import de.cadentem.cave_dweller.network.CaveSoundPayload;
import de.cadentem.cave_dweller.registry.ModSounds;
import de.cadentem.cave_dweller.util.Utils;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class CaveDwellerEntity extends Monster implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private final RawAnimation CHASE = RawAnimation.begin().then("animation.cave_dweller.new_run", LoopType.LOOP);
    private final RawAnimation CHASE_IDLE = RawAnimation.begin().then("animation.cave_dweller.run_idle", LoopType.LOOP);
    private final RawAnimation CROUCH_RUN = RawAnimation.begin().then("animation.cave_dweller.crouch_run_new", LoopType.LOOP);
    private final RawAnimation CROUCH_IDLE = RawAnimation.begin().then("animation.cave_dweller.crouch_idle", LoopType.LOOP);
    private final RawAnimation CALM_RUN = RawAnimation.begin().then("animation.cave_dweller.calm_move", LoopType.LOOP);
    private final RawAnimation CALM_STILL = RawAnimation.begin().then("animation.cave_dweller.calm_idle", LoopType.LOOP);
    private final RawAnimation IS_SPOTTED = RawAnimation.begin().then("animation.cave_dweller.spotted", LoopType.HOLD_ON_LAST_FRAME);
    private final RawAnimation CRAWL = RawAnimation.begin().then("animation.cave_dweller.crawl", LoopType.LOOP);
    private final RawAnimation FLEE = RawAnimation.begin().then("animation.cave_dweller.flee", LoopType.LOOP);

    public static final EntityDataAccessor<Boolean> FLEEING_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CROUCHING_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CRAWLING_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> SPOTTED_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> CLIMBING_ACCESSOR = SynchedEntityData.defineId(CaveDwellerEntity.class, EntityDataSerializers.BOOLEAN);

    public Roll currentRoll = Roll.STROLL;
    public boolean isFleeing;
    public boolean hasSpawned;
    public boolean pleaseStopMoving;
    public boolean targetIsFacingMe;
    private int ticksTillRemove;
    private int chaseSoundClock;
    private boolean alreadyPlayedFleeSound;
    private boolean alreadyPlayedSpottedSound;
    private boolean startedPlayingChaseSound;
    private boolean alreadyPlayedDeathSound;

    public CaveDwellerEntity(EntityType<? extends CaveDwellerEntity> entityType, Level level) {
        super(entityType, level);
        this.refreshDimensions();
        this.ticksTillRemove = Utils.secondsToTicks(ServerConfig.TIME_UNTIL_LEAVE);
        this.setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0F);
    }

    @Override
    public SpawnGroupData finalizeSpawn(@NotNull net.minecraft.world.level.ServerLevelAccessor level,
                                        @NotNull net.minecraft.world.DifficultyInstance difficulty,
                                        @NotNull EntitySpawnReason reason,
                                        SpawnGroupData groupData) {
        setAttribute(getAttribute(Attributes.MAX_HEALTH), ServerConfig.MAX_HEALTH);
        setAttribute(getAttribute(Attributes.ATTACK_DAMAGE), ServerConfig.ATTACK_DAMAGE);
        setAttribute(getAttribute(Attributes.ATTACK_SPEED), ServerConfig.ATTACK_SPEED);
        setAttribute(getAttribute(Attributes.MOVEMENT_SPEED), ServerConfig.MOVEMENT_SPEED);
        setAttribute(getAttribute(Attributes.STEP_HEIGHT), 1.0);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    private void setAttribute(AttributeInstance attribute, double value) {
        if (attribute != null) {
            attribute.setBaseValue(value);
            if (attribute.getAttribute() == Attributes.MAX_HEALTH) this.setHealth((float) value);
            else if (attribute.getAttribute() == Attributes.MOVEMENT_SPEED) this.setSpeed((float) value);
        }
    }

    public static AttributeSupplier createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.ATTACK_SPEED, 0.35)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 100.0)
                .build();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FLEEING_ACCESSOR, false);
        builder.define(CROUCHING_ACCESSOR, false);
        builder.define(CRAWLING_ACCESSOR, false);
        builder.define(SPOTTED_ACCESSOR, false);
        builder.define(CLIMBING_ACCESSOR, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new CaveDwellerChaseGoal(this, true));
        this.goalSelector.addGoal(1, new CaveDwellerFleeGoal(this, 20.0F, 1.0));
        this.goalSelector.addGoal(2, new CaveDwellerBreakInvisGoal(this));
        this.goalSelector.addGoal(2, new CaveDwellerStareGoal(this));
        if (ServerConfig.CAN_BREAK_DOOR) {
            this.goalSelector.addGoal(2, new CaveDwellerBreakDoorGoal(this, d -> true));
        }
        this.goalSelector.addGoal(3, new CaveDwellerStrollGoal(this, 0.35));
        this.targetSelector.addGoal(1, new CaveDwellerTargetTooCloseGoal(this, 12.0F));
        this.targetSelector.addGoal(2, new CaveDwellerTargetSeesMeGoal(this));
    }

    public void disappear() {
        playDisappearSound();
        discard();
    }

    public boolean hasSpawned() { return this.hasSpawned; }

    @Override
    protected boolean canRide(@NotNull Entity vehicle) {
        return ServerConfig.ALLOW_RIDING && super.canRide(vehicle);
    }

    @Override
    public boolean startRiding(@NotNull Entity vehicle, boolean force, boolean dismountOnDeath) {
        return ServerConfig.ALLOW_RIDING && super.startRiding(vehicle, force, dismountOnDeath);
    }

    @Override
    public void tick() {
        this.ticksTillRemove--;
        if (this.ticksTillRemove <= 0) disappear();

        if (this.goalSelector.getAvailableGoals().isEmpty() || this.targetSelector.getAvailableGoals().isEmpty()) {
            registerGoals();
            this.goalSelector.tick();
            this.targetSelector.tick();
        }

        if (getTarget() != null) {
            this.targetIsFacingMe = isLookingAtMe(getTarget(), false);
        }

        if (this.level() instanceof ServerLevel) {
            boolean isAboveSolid = this.level().getBlockState(blockPosition().above()).isSolid();
            boolean isTwoAboveSolid = this.level().getBlockState(blockPosition().above(2)).isSolid();
            boolean isThreeAboveSolid = this.level().getBlockState(blockPosition().above(3)).isSolid();
            Vec3i offset = getDirectionVector();
            boolean isFacingSolid = this.level().getBlockState(blockPosition().relative(getDirection())).isSolid();
            if (isFacingSolid) offset = offset.offset(0, 1, 0);
            boolean isOffsetFacingSolid = this.level().getBlockState(blockPosition().offset(offset)).isSolid();
            boolean isOffsetFacingAboveSolid = this.level().getBlockState(blockPosition().offset(offset).above()).isSolid();
            boolean isOffsetFacingTwoAboveSolid = this.level().getBlockState(blockPosition().offset(offset).above(2)).isSolid();
            boolean shouldCrouch = isTwoAboveSolid
                    || !isOffsetFacingSolid && !isOffsetFacingAboveSolid && (isOffsetFacingTwoAboveSolid || isFacingSolid && isThreeAboveSolid);
            boolean shouldCrawl = isAboveSolid || !isOffsetFacingSolid && isOffsetFacingAboveSolid || isFacingSolid && isTwoAboveSolid;
            if (this.isAggressive() || this.isFleeing) {
                this.entityData.set(SPOTTED_ACCESSOR, false);
            }
            setClimbing(this.horizontalCollision);
            this.entityData.set(CROUCHING_ACCESSOR, shouldCrouch);
            setCrawling(shouldCrawl);
        }

        if (this.entityData.get(SPOTTED_ACCESSOR)) playSpottedSound();

        refreshDimensions();
        getNavigation().setSpeedModifier(getSpeedModifier());
        super.tick();
    }

    public double getSpeedModifier() {
        return isCrawling() ? 0.35 : (isCrouching() ? 0.6 : 0.85);
    }

    @Override
    @NotNull
    public EntityDimensions getDefaultDimensions(@NotNull Pose pose) {
        if (this.entityData.get(CRAWLING_ACCESSOR)) return EntityDimensions.fixed(0.5F, 0.5F);
        if (this.entityData.get(CROUCHING_ACCESSOR)) return EntityDimensions.fixed(0.5F, 1.7F);
        return super.getDefaultDimensions(pose);
    }

    private boolean isMoving() {
        Vec3 velocity = getDeltaMovement();
        float avgVelocity = (float) (Math.abs(velocity.x) + Math.abs(velocity.z)) / 2.0F;
        return avgVelocity > 0.03F;
    }

    public void reRoll() {
        this.currentRoll = Roll.fromValue(new Random().nextInt(3));
    }

    public void pickRoll(@NotNull List<Roll> rolls) {
        this.currentRoll = rolls.get(new Random().nextInt(rolls.size()));
    }

    @Override
    public boolean onClimbable() {
        return isClimbing();
    }

    public boolean isClimbing() {
        if (!ServerConfig.CAN_CLIMB) return false;
        if (getTarget() == null) return false;
        return !isCrawling() && !isCrouching() && this.entityData.get(CLIMBING_ACCESSOR);
    }

    public void setClimbing(boolean isClimbing) {
        this.entityData.set(CLIMBING_ACCESSOR, isClimbing);
    }

    @Override
    @NotNull
    protected PathNavigation createNavigation(@NotNull Level level) {
        WallClimberNavigation navigation = new WallClimberNavigation(this, level);
        navigation.setMaxVisitedNodesMultiplier(4.0F);
        return navigation;
    }

    private PlayState predicate(AnimationTest<CaveDwellerEntity> state) {
        boolean isCurrentAboveSolid = this.level().getBlockState(blockPosition().above()).isSolid();
        boolean unsure = isCrawling() && this.level().getBlockState(blockPosition()).isSolid();
        boolean isCurrentTwoAboveSolid = this.level().getBlockState(blockPosition().above(2)).isSolid();
        if (isCurrentAboveSolid || unsure) return state.setAndContinue(this.CRAWL);
        if (isCurrentTwoAboveSolid) return state.isMoving() ? state.setAndContinue(this.CROUCH_RUN) : state.setAndContinue(this.CROUCH_IDLE);
        if (isAggressive()) return state.isMoving() ? state.setAndContinue(this.CHASE) : state.setAndContinue(this.CHASE_IDLE);
        if (this.entityData.get(FLEEING_ACCESSOR)) return state.isMoving() ? state.setAndContinue(this.FLEE) : state.setAndContinue(this.CHASE_IDLE);
        if (!this.pleaseStopMoving && (!this.entityData.get(SPOTTED_ACCESSOR) || state.isMoving())) {
            return state.isMoving() ? state.setAndContinue(this.CALM_RUN) : state.setAndContinue(this.CALM_STILL);
        }
        return state.setAndContinue(this.IS_SPOTTED);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        registrar.add(new AnimationController<CaveDwellerEntity>("controller", 3, this::predicate));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void playStepSound(@NotNull BlockPos pos, @NotNull BlockState state) {
        super.playStepSound(pos, state);
        playEntitySound(chooseStep());
    }

    private void playEntitySound(SoundEvent soundEvent) { playEntitySound(soundEvent, 1.0F, 1.0F); }
    private void playEntitySound(SoundEvent soundEvent, float volume, float pitch) {
        this.level().playSound(null, this, soundEvent, SoundSource.HOSTILE, volume, pitch);
    }

    private void playBlockPosSound(net.minecraft.resources.Identifier soundResource, float volume, float pitch) {
        if (this.level() instanceof ServerLevel serverLevel) {
            int radius = 60;
            for (ServerPlayer player : serverLevel.getPlayers(p -> p.distanceToSqr(this) <= (double) (radius * radius))) {
                PacketDistributor.sendToPlayer(player, new CaveSoundPayload(soundResource, player.blockPosition(), volume, pitch));
            }
        }
    }

    public void playChaseSound() {
        if (this.startedPlayingChaseSound || isMoving()) {
            if (this.chaseSoundClock <= 0) {
                Random rand = new Random();
                SoundEvent ev = switch (rand.nextInt(4)) {
                    case 1 -> ModSounds.CHASE_2.get();
                    case 2 -> ModSounds.CHASE_3.get();
                    case 3 -> ModSounds.CHASE_4.get();
                    default -> ModSounds.CHASE_1.get();
                };
                playEntitySound(ev, 3.0F, 1.0F);
                this.startedPlayingChaseSound = true;
                resetChaseSoundClock();
            }
            this.chaseSoundClock--;
        }
    }

    public void playDisappearSound() {
        playBlockPosSound(ModSounds.DISAPPEAR.get().location(), 3.0F, 1.0F);
    }

    public void playFleeSound() {
        if (!this.alreadyPlayedFleeSound) {
            Random rand = new Random();
            SoundEvent ev = (rand.nextInt(2) == 0) ? ModSounds.FLEE_1.get() : ModSounds.FLEE_2.get();
            playEntitySound(ev, 3.0F, 1.0F);
            this.alreadyPlayedFleeSound = true;
        }
    }

    private void playSpottedSound() {
        if (!this.alreadyPlayedSpottedSound) {
            playEntitySound(ModSounds.SPOTTED.get(), 3.0F, 1.0F);
            this.alreadyPlayedSpottedSound = true;
        }
    }

    private void resetChaseSoundClock() { this.chaseSoundClock = Utils.secondsToTicks(5); }

    private SoundEvent chooseStep() {
        Random rand = new Random();
        return switch (rand.nextInt(4)) {
            case 1 -> ModSounds.CHASE_STEP_2.get();
            case 2 -> ModSounds.CHASE_STEP_3.get();
            case 3 -> ModSounds.CHASE_STEP_4.get();
            default -> ModSounds.CHASE_STEP_1.get();
        };
    }

    private SoundEvent chooseHurtSound() {
        Random rand = new Random();
        return switch (rand.nextInt(4)) {
            case 1 -> ModSounds.DWELLER_HURT_2.get();
            case 2 -> ModSounds.DWELLER_HURT_3.get();
            case 3 -> ModSounds.DWELLER_HURT_4.get();
            default -> ModSounds.DWELLER_HURT_1.get();
        };
    }

    @Override
    protected void playHurtSound(@NotNull DamageSource source) {
        playEntitySound(chooseHurtSound(), 2.0F, 1.0F);
    }

    public void setCrawling(boolean shouldCrawl) {
        if (shouldCrawl) getEntityData().set(CROUCHING_ACCESSOR, false);
        getEntityData().set(CRAWLING_ACCESSOR, shouldCrawl);
        refreshDimensions();
    }

    public boolean isCrawling() { return this.entityData.get(CRAWLING_ACCESSOR); }

    @Override
    protected void tickDeath() {
        super.tickDeath();
        if (!this.alreadyPlayedDeathSound) {
            playBlockPosSound(ModSounds.DWELLER_DEATH.get().location(), 2.0F, 1.0F);
            this.alreadyPlayedDeathSound = true;
        }
    }

    public boolean isLookingAtMe(Entity target, boolean directlyLooking) {
        if (!Utils.isValidPlayer(target)) return false;
        if (target.getEyePosition(1.0F).distanceTo(getPosition(1.0F)) > ServerConfig.SPOTTING_RANGE) return false;
        Vec3 viewVector = target.getViewVector(1.0F).normalize();
        Vec3 difference = new Vec3(getX() - target.getX(), getEyeY() - target.getEyeY(), getZ() - target.getZ()).normalize();
        double dot = viewVector.dot(difference);
        if (directlyLooking && target instanceof Player player) return dot > 0.99 && player.hasLineOfSight(this);
        return dot > 0.3;
    }

    public boolean teleportToTarget() {
        LivingEntity target = getTarget();
        if (target == null) return false;
        Vec3 dir = new Vec3(getX() - target.getX(), getY(0.5) - target.getEyeY(), getZ() - target.getZ()).normalize();
        double radius = 16.0;
        double d1 = getX() + (getRandom().nextDouble() - 0.5) * (radius / 2.0) - dir.x * radius;
        double d2 = getY() + ((double) getRandom().nextInt((int) radius) - radius / 2.0) - dir.y * radius;
        double d3 = getZ() + (getRandom().nextDouble() - 0.5) * (radius / 2.0) - dir.z * radius;
        BlockPos.MutableBlockPos validPosition = new BlockPos.MutableBlockPos(d1, d2, d3);
        while (validPosition.getY() > this.level().getMinY() && !this.level().getBlockState(validPosition).blocksMotion()) {  // getMinY: from LevelHeightAccessor
            validPosition.move(Direction.DOWN);
        }
        teleportTo(validPosition.getX(), validPosition.getY(), validPosition.getZ());
        return true;
    }

    private Vec3i getDirectionVector() {
        return new Vec3i(getDirection().getStepX(), getDirection().getStepY(), getDirection().getStepZ());
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) { return chooseHurtSound(); }

    @Override
    protected SoundEvent getDeathSound() { return ModSounds.DWELLER_DEATH.get(); }

    @Override
    protected float getSoundVolume() { return 0.4F; }
}
