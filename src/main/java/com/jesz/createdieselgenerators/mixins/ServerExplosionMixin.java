package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.events.GameEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Stands in for NeoForge's ExplosionEvent.Detonate: flammable fluids near an explosion blow up too. */
@Mixin(ServerExplosion.class)
public class ServerExplosionMixin {
    @Shadow @Final private ServerLevel level;
    @Shadow @Final private Vec3 center;

    @Inject(method = "explode()I", at = @At("HEAD"))
    private void cdg$onExplode(CallbackInfoReturnable<Integer> cir) {
        GameEvents.onExplosion(level, center);
    }
}
