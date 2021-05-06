package de.canitzp.fixableslots.mixin;

import de.canitzp.fixableslots.SaveHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Redirect(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "net/minecraft/world/entity/player/Inventory.getFreeSlot()I"))
    private int getFreeSlot(Inventory inventory, int slot, ItemStack stack){
        return this.getFreeSlot(inventory, stack);
    }

    @Redirect(method = "addResource(Lnet/minecraft/world/item/ItemStack;)I", at = @At(value = "INVOKE", target = "net/minecraft/world/entity/player/Inventory.getFreeSlot()I"))
    private int getFreeSlot(Inventory inventory, ItemStack stack){
        for(int i = 0; i < inventory.items.size(); ++i) {
            if (inventory.items.get(i).isEmpty() && SaveHelper.isItemValid(inventory.player, i, stack)) {
                return i;
            }
        }
        return -1;
    }

}
