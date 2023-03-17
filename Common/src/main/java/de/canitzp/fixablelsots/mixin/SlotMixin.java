package de.canitzp.fixablelsots.mixin;

import de.canitzp.fixablelsots.SaveHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {

    @Final @Shadow private int slot;
    @Final @Shadow public Container container;

    @Inject(method = "mayPlace", at = @At("TAIL"), cancellable = true)
    private void mayPlace(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir){
        if(this.container instanceof Inventory){
            cir.setReturnValue(SaveHelper.isItemValid(((Inventory) this.container).player, this.slot, itemStack));
            cir.cancel();
        }
    }

}
