package de.cadentem.cave_dweller.mixin;

import de.cadentem.cave_dweller.entities.CaveDwellerEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {
    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void cave_dweller$pleaseStopMoving(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof CaveDwellerEntity caveDweller && caveDweller.pleaseStopMoving) {
            ci.cancel();
        }
    }

    // MC 26.2 reworked knockback: the old knockback(DDD) was replaced by
    // knockback(DDD, DamageSource, float, boolean) (the 5-arg overload delegates to it),
    // so target the 6-arg overload explicitly.
    @Inject(method = "knockback(DDDLnet/minecraft/world/damagesource/DamageSource;FZ)V", at = @At("HEAD"), cancellable = true, require = 0)
    public void cave_dweller$noKnockbackWhileClimbing(double strength, double x, double z, DamageSource source, float magnitude, boolean alwaysApply, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof CaveDwellerEntity caveDweller && caveDweller.isClimbing()) {
            ci.cancel();
        }
    }
}
