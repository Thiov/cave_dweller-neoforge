package de.cadentem.cave_dweller.entities.goals;

import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import de.cadentem.cave_dweller.util.Utils;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class CaveDwellerTargetSeesMeGoal extends NearestAttackableTargetGoal<Player> {
    private final CaveDwellerEntity caveDweller;

    public CaveDwellerTargetSeesMeGoal(CaveDwellerEntity mob) {
        super(mob, Player.class, false);
        this.caveDweller = mob;
    }

    @Override
    public boolean canUse() {
        if (caveDweller.isInvisible()) return false;
        this.target = Utils.getValidTarget(caveDweller);
        if (!Utils.isValidPlayer(this.target)) return false;
        return caveDweller.isLookingAtMe(this.target, true);
    }

    @Override
    public void start() {
        caveDweller.setTarget(this.target);
        caveDweller.getEntityData().set(CaveDwellerEntity.SPOTTED_ACCESSOR, true);
        if (this.target != null) caveDweller.pickRoll(List.of(Roll.CHASE, Roll.STARE, Roll.STARE, Roll.FLEE));
        super.start();
    }

    @Override
    public boolean canContinueToUse() { return Utils.isValidPlayer(this.target); }
}
