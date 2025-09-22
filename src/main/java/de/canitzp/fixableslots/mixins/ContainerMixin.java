package de.canitzp.fixableslots.mixins;

import de.canitzp.fixableslots.SaveHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Container.class)
public class ContainerMixin {
    @Inject(method = "mergeItemStack", at = @At("HEAD"), cancellable = true)
    private void mergeItemStack(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection, CallbackInfoReturnable<Boolean> cir) {
        if (!reverseDirection) return;

        Container container = (Container) (Object) this;

        boolean flag = false;
        int i = endIndex - 1;

        if (stack.isStackable()) {
            while (!stack.isEmpty()) {
                if (i < startIndex) {
                    break;
                }
                Slot slot = container.inventorySlots.get(i);
                ItemStack itemstack = slot.getStack();

                if (slot.inventory instanceof InventoryPlayer && SaveHelper.isItemValidForSlot(((InventoryPlayer) slot.inventory).player, slot.getSlotIndex(), stack, true) && !itemstack.isEmpty() && itemstack.getItem() == stack.getItem() && (!stack.getHasSubtypes() || stack.getMetadata() == itemstack.getMetadata()) && ItemStack.areItemStackTagsEqual(stack, itemstack)) {
                    int j = itemstack.getCount() + stack.getCount();
                    int maxSize = Math.min(slot.getSlotStackLimit(), stack.getMaxStackSize());

                    if (j <= maxSize) {
                        stack.setCount(0);
                        itemstack.setCount(j);
                        slot.onSlotChanged();
                        flag = true;
                    } else if (itemstack.getCount() < maxSize) {
                        stack.shrink(maxSize - itemstack.getCount());
                        itemstack.setCount(maxSize);
                        slot.onSlotChanged();
                        flag = true;
                    }
                }

                --i;
            }
        }

        if (!stack.isEmpty()) {
            i = endIndex - 1;
            while (true) {
                if (i < startIndex) {
                    break;
                }

                Slot slot1 = container.inventorySlots.get(i);
                ItemStack itemstack1 = slot1.getStack();

                if (slot1.inventory instanceof InventoryPlayer && SaveHelper.isItemValidForSlot(((InventoryPlayer) slot1.inventory).player, slot1.getSlotIndex(), stack,true) && itemstack1.isEmpty() && slot1.isItemValid(stack)) {
                    if (stack.getCount() > slot1.getSlotStackLimit()) {
                        slot1.putStack(stack.splitStack(slot1.getSlotStackLimit()));
                    } else {
                        slot1.putStack(stack.splitStack(stack.getCount()));
                    }

                    slot1.onSlotChanged();
                    flag = true;
                    break;
                }

                --i;
            }
        }

        if (flag)
            cir.setReturnValue(flag);
    }
}
