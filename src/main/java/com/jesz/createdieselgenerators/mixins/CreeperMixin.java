package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.CDGFluids;
import com.jesz.createdieselgenerators.CDGItems;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster {
    protected CreeperMixin(EntityType<? extends Monster> type, Level level) { super(type, level); }

    @Shadow public abstract void ignite();

    @Inject(method = "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at = @At("TAIL"))
    public void cdg$mobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir){
        ItemStack stackInHand = player.getItemInHand(hand);
        if(!CDGItems.LIGHTER.isIn(stackInHand))
            return;
        FluidStack fluid = CDGItems.LIGHTER.get().readFluid(stackInHand);
        if (fluid.isEmpty())
            return;
        fluid.setAmount(Math.max(0, fluid.getAmount() - CDGFluids.MB));
        CDGItems.LIGHTER.get().writeFluid(stackInHand, fluid);
        ignite();
        this.level().playSound(player, this.getX(), this.getY(), this.getZ(), SoundEvents.FLINTANDSTEEL_USE, this.getSoundSource(), 1.0F, this.random.nextFloat() * 0.4F + 0.8F);


    }
}
