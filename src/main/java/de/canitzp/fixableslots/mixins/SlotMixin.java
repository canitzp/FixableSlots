package de.canitzp.fixableslots.mixins;

import de.canitzp.fixableslots.SaveHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {
    @Inject(method = "isItemValid", at = @At("HEAD"), cancellable = true)
    private void isItemValid(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        Slot slot = (Slot) (Object) this;
        if (slot.inventory instanceof InventoryPlayer) {
            boolean isValid = SaveHelper.isItemValidForSlot(((InventoryPlayer) slot.inventory).player, slot.getSlotIndex(), stack);
            if (!isValid) cir.setReturnValue(false);
        }
    }
}
