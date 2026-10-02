package com.jesz.createdieselgenerators.mixins.client;

import com.jesz.createdieselgenerators.client.CDGPartialModels;
import com.jesz.createdieselgenerators.content.turret.TurretData;
import com.zurrtum.create.client.content.equipment.hats.HatState;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Whoever operates a turret wears the operator hat. Create Fly already draws hats from the render state
 * (train conductors); this runs after its own pick and replaces it for turret operators.
 */
@Mixin(value = LivingEntityRenderer.class, priority = 1100)
public class TurretOperatorHatMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void cdg$turretOperatorHat(T entity, S state, float partialTicks, CallbackInfo ci) {
        if (TurretData.getTurretPos(entity) == null)
            return;
        HatState hatState = (HatState) state;
        hatState.create$setHat(CDGPartialModels.TURRET_OPERATOR_HAT);
        hatState.create$updateHatInfo(entity);
    }
}
