package de.canitzp.fixablelsots.mixin;

import de.canitzp.fixablelsots.SaveHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Inventory.class)
public class InventoryMixin {
    
    @Shadow
    @Final
    public Player player;

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
    
    /*@Inject(method = "addResource(ILnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void addResource(int i, ItemStack itemStack, CallbackInfoReturnable<Integer> cir){
        System.out.println("Put item in slot '" + i + "' '" + itemStack + "'");
        if(!SaveHelper.isItemValid(this.player, i, itemStack)){
            System.out.println("cancel");
            cir.setReturnValue(itemStack.getCount());
            cir.cancel();
        }
    }*/

}
