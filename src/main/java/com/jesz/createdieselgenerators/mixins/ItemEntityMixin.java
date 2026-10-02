package com.jesz.createdieselgenerators.mixins;

import com.jesz.createdieselgenerators.content.tools.lighter.LighterItem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Stands in for NeoForge's Item#onEntityItemUpdate: a dropped ignited lighter sets off flammable fluids. */
@Mixin(ItemEntity.class)
public class ItemEntityMixin {
    @Inject(method = "tick()V", at = @At("HEAD"))
    private void cdg$tick(CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        ItemStack stack = self.getItem();
        if (stack.getItem() instanceof LighterItem lighter)
            lighter.onEntityItemUpdate(stack, self);
    }
}
