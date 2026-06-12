package de.cadentem.cave_dweller.entities.goals;

import de.cadentem.cave_dweller.config.ServerConfig;
import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import de.cadentem.cave_dweller.util.Utils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

public class CaveDwellerChaseGoal extends Goal {
    private final CaveDwellerEntity caveDweller;
    private final int maxSpeedReached;
    private final boolean followTargetEvenIfNotSeen;
    private long lastGameTimeCheck;
    private int ticksUntilLeave;
    private int ticksUntilNextAttack;
    private int speedUp;

    public CaveDwellerChaseGoal(CaveDwellerEntity caveDweller, boolean followTargetEvenIfNotSeen) {
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        this.caveDweller = caveDweller;
        this.followTargetEvenIfNotSeen = followTargetEvenIfNotSeen;
        this.ticksUntilLeave = Utils.secondsToTicks(ServerConfig.TIME_UNTIL_LEAVE_CHASE);
        this.maxSpeedReached = Utils.secondsToTicks(3);
    }

    @Override
    public boolean canUse() {
        if (caveDweller.isInvisible()) return false;
        if (caveDweller.currentRoll != Roll.CHASE) return false;
        if (!caveDweller.targetIsFacingMe) return false;
        long ticks = caveDweller.level().getGameTime();
        if (ticks - lastGameTimeCheck < 20L) return false;
        lastGameTimeCheck = ticks;
        LivingEntity target = caveDweller.getTarget();
        if (!Utils.isValidPlayer(target)) return false;
        Path path = caveDweller.getNavigation().createPath(target, 0);
        if (path != null) return true;
        if (getAttackReachSqr(target) >= caveDweller.distanceToSqr(target)) return true;
        return caveDweller.getNavigation().createPath(target, 0) != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = caveDweller.getTarget();
        if (!Utils.isValidPlayer(target)) {
            caveDweller.disappear();
            return false;
        }
        return !followTargetEvenIfNotSeen
                ? !caveDweller.getNavigation().isDone()
                : caveDweller.isWithinHome(target.blockPosition());
    }

    @Override
    public void start() {
        caveDweller.setAggressive(true);
        ticksUntilNextAttack = 0;
    }

    @Override
    public void stop() {
        LivingEntity target = caveDweller.getTarget();
        if (!Utils.isValidPlayer(target)) caveDweller.setTarget(null);
        speedUp = 0;
        caveDweller.setAggressive(false);
        caveDweller.getEntityData().set(CaveDwellerEntity.CRAWLING_ACCESSOR, false);
        caveDweller.getNavigation().stop();
        caveDweller.refreshDimensions();
    }

    @Override
    public boolean requiresUpdateEveryTick() { return true; }

    @Override
    public void tick() {
        if (ticksUntilLeave <= 0 && !caveDweller.targetIsFacingMe) caveDweller.disappear();

        LivingEntity target = caveDweller.getTarget();
        if (!Utils.isValidPlayer(target)) return;

        Path path = caveDweller.getNavigation().getPath();
        fixPath(path);
        boolean targetMoved = path != null && path.getEndNode() != null
                && path.getEndNode().distanceTo(target.blockPosition()) > 2.0F;
        if (path == null
                || caveDweller.getNavigation().isStuck()
                || targetMoved
                || path.isDone() && !shouldClimb(path)
                || caveDweller.getNavigation().shouldRecomputePath(target.blockPosition()) && caveDweller.tickCount % 20 == 0) {
            path = caveDweller.getNavigation().createPath(target, 0);
            fixPath(path);
        }

        if (path != null && !path.isDone() && caveDweller.hasLineOfSight(target)) {
            caveDweller.playChaseSound();
        }

        caveDweller.getNavigation().moveTo(path, caveDweller.getSpeedModifier());
        if (!caveDweller.isCrawling()) {
            if (caveDweller.isAggressive()) caveDweller.getLookControl().setLookAt(target, 90.0F, 90.0F);
            else caveDweller.getLookControl().setLookAt(target, 180.0F, 1.0F);
        }

        ticksUntilNextAttack = Math.max(ticksUntilNextAttack - 1, 0);
        double distance = caveDweller.distanceToSqr(target);
        checkAndPerformAttack(target, distance);
        ticksUntilLeave--;
        if (speedUp < maxSpeedReached) speedUp++;
    }

    private void fixPath(Path path) {
        LivingEntity target = caveDweller.getTarget();
        if (target == null || !shouldClimb(path)) return;
        if (path.getNode(0).distanceTo(target.blockPosition()) > 0.1) {
            path.replaceNode(0, path.getNode(0).cloneAndMove(
                    target.blockPosition().getX(),
                    target.blockPosition().getY(),
                    target.blockPosition().getZ()));
        }
    }

    private boolean shouldClimb(Path path) {
        if (caveDweller.getTarget() == null) return false;
        if (path == null || path.getNodeCount() != 1) return false;
        float stepHeight = (float) caveDweller.getAttributeValue(Attributes.STEP_HEIGHT);
        return caveDweller.getTarget().blockPosition().getY() > caveDweller.blockPosition().getY() + stepHeight;
    }

    private void checkAndPerformAttack(LivingEntity target, double distanceToTarget) {
        double attackReach = getAttackReachSqr(target);
        if (distanceToTarget <= attackReach && ticksUntilNextAttack <= 0) {
            ticksUntilNextAttack = adjustedTickDelay(20);
            caveDweller.swing(InteractionHand.MAIN_HAND);
            caveDweller.doHurtTarget((net.minecraft.server.level.ServerLevel) caveDweller.level(), target);
        }
    }

    private double getAttackReachSqr(LivingEntity target) {
        return caveDweller.getBbWidth() * 4.0F * caveDweller.getBbWidth() * 4.0F + target.getBbWidth();
    }
}
