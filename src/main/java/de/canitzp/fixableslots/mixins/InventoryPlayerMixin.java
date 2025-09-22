package de.canitzp.fixableslots.mixins;

import de.canitzp.fixableslots.SaveHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryPlayer.class)
public class InventoryPlayerMixin {
    @Shadow
    private ItemStack itemStack;

    @Inject(method = "storeItemStack", at = @At("RETURN"), cancellable = true)
    private void storeItemStack(ItemStack itemStackIn, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() != -1) return;
        cir.setReturnValue(SaveHelper.findBestEmptySlot((InventoryPlayer) (Object) this, itemStackIn));
    }

    @Inject(method = "getFirstEmptyStack", at = @At("RETURN"), cancellable = true)
    private void getFirstEmptyStack(CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() == -1) return;
        cir.setReturnValue(SaveHelper.findBestEmptySlot((InventoryPlayer) (Object) this, itemStack));
    }
}
