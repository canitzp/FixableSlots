package de.canitzp.fixableslots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author canitzp
 */
public class SaveHelper {

    public static int findBestEmptySlot(InventoryPlayer inventory, ItemStack stack) {
        for (int i = 0; i < inventory.mainInventory.size(); ++i) {
            ItemStack slotStack = inventory.mainInventory.get(i);
            if (!slotStack.isEmpty() || slotStack.getCount() >= slotStack.getMaxStackSize())
                continue;
            if (isItemValidForSlot(inventory.player, i, stack, true))
                return i;
        }
        for (int i = 0; i < inventory.mainInventory.size(); ++i) {
            ItemStack slotStack = inventory.mainInventory.get(i);
            if (!slotStack.isEmpty() || slotStack.getCount() >= slotStack.getMaxStackSize())
                continue;
            if (isItemValidForSlot(inventory.player, i, stack))
                return i;
        }
        return -1;
    }

    public static boolean isItemValidForSlot(@Nonnull EntityPlayer player, int slotIndex, @Nonnull ItemStack stack) {
        return isItemValidForSlot(player, slotIndex, stack, false);
    }

    public static boolean isItemValidForSlot(@Nonnull EntityPlayer player, int slotIndex, @Nonnull ItemStack stack, boolean explicit) {
        if (explicit) {
            NBTTagCompound nbt = player.getEntityData();
            if (nbt.hasKey("FixableSlotsData")) {
                NBTTagCompound data = nbt.getCompoundTag("FixableSlotsData");
                if (!data.hasKey("Slot_" + slotIndex)) return false;
                ItemStack definition = new ItemStack(data.getCompoundTag("Slot_" + slotIndex).getCompoundTag("Definition"));
                if (definition.isEmpty()) return false;
            }
        }
        return getSlotType(player, slotIndex).isValid(getStackForSlot(player, slotIndex), stack);
    }

    @Nonnull
    public static ItemStack getStackForSlot(@Nonnull EntityPlayer player, int slotIndex) {
        NBTTagCompound nbt = player.getEntityData();
        if (nbt.hasKey("FixableSlotsData", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound data = nbt.getCompoundTag("FixableSlotsData");
            if (data.hasKey("Slot_" + slotIndex, Constants.NBT.TAG_COMPOUND)) {
                return new ItemStack(data.getCompoundTag("Slot_" + slotIndex).getCompoundTag("Definition"));
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public static SlotType getSlotType(@Nonnull EntityPlayer player, int slotIndex) {
        NBTTagCompound nbt = player.getEntityData();
        if (nbt.hasKey("FixableSlotsData", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound data = nbt.getCompoundTag("FixableSlotsData");
            return SlotType.values()[data.getCompoundTag("Slot_" + slotIndex).getInteger("Type")];
        }
        return SlotType.VANILLA;
    }

    // For server side use only!
    public static final HashMap<UUID, NBTTagCompound> lastPlayerData = new HashMap<>();

    public static void setSlot(@Nonnull EntityPlayer player, int slotIndex, int typeIndex, @Nonnull ItemStack definition) {
        if (player instanceof EntityPlayerMP) { // should only be called on server side! Use 'PacketSetSlot' to call this from Client
            NBTTagCompound data, slotTag;
            if (player.getEntityData().hasKey("FixableSlotsData", Constants.NBT.TAG_COMPOUND)) {
                data = player.getEntityData().getCompoundTag("FixableSlotsData");
                if (data.hasKey("Slot_" + slotIndex, Constants.NBT.TAG_COMPOUND)) {
                    slotTag = data.getCompoundTag("Slot_" + slotIndex);
                    if (typeIndex == SlotType.VANILLA.ordinal()) { // Remove all VANILLA slots
                        data.removeTag("Slot_" + slotIndex);
                        player.getEntityData().setTag("FixableSlotsData", data);
                        FixableSlots.NET.sendTo(new PacketUpdateClientNBT(player, slotIndex, new NBTTagCompound()), (EntityPlayerMP) player);
                        return;
                    }
                } else {
                    slotTag = new NBTTagCompound();
                }
            } else {
                data = new NBTTagCompound();
                slotTag = new NBTTagCompound();
            }

            slotTag.setInteger("Type", typeIndex);
            NBTTagCompound def = new NBTTagCompound();
            definition.writeToNBT(def);
            slotTag.setTag("Definition", def);

            FixableSlots.NET.sendTo(new PacketUpdateClientNBT(player, slotIndex, slotTag), (EntityPlayerMP) player);

            data.setTag("Slot_" + slotIndex, slotTag);
            player.getEntityData().setTag("FixableSlotsData", data);
            synchronized (lastPlayerData) {
                lastPlayerData.put(player.getUniqueID(), data);
            }
        }
    }

}
