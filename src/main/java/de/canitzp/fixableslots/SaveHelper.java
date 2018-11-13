package de.canitzp.fixableslots;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

/**
 * @author canitzp
 */
public class SaveHelper {

    public static boolean isItemValid(@Nonnull EntityPlayer player, int slotIndex, @Nonnull ItemStack stack){
        return getSlotType(player, slotIndex).isValid(getStackForSlot(player, slotIndex), stack);
    }
    
    @Nonnull
    public static ItemStack getStackForSlot(@Nonnull EntityPlayer player, int slotIndex){
        NBTTagCompound nbt = player.getEntityData();
        if(nbt.hasKey("FixableSlotsData", Constants.NBT.TAG_COMPOUND)){
            NBTTagCompound data = nbt.getCompoundTag("FixableSlotsData");
            if(data.hasKey("Slot_" + slotIndex, Constants.NBT.TAG_COMPOUND)){
                return new ItemStack(data.getCompoundTag("Slot_" + slotIndex).getCompoundTag("Definition"));
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public static SlotType getSlotType(@Nonnull EntityPlayer player, int slotIndex){
        NBTTagCompound nbt = player.getEntityData();
        if(nbt.hasKey("FixableSlotsData", Constants.NBT.TAG_COMPOUND)) {
            NBTTagCompound data = nbt.getCompoundTag("FixableSlotsData");
            return SlotType.values()[data.getCompoundTag("Slot_" + slotIndex).getInteger("Type")];
        }
        return SlotType.VANILLA;
    }

    public static void setSlot(@Nonnull EntityPlayer player, int slotIndex, int typeIndex, @Nonnull ItemStack definition){
        if(player instanceof EntityPlayerMP){ // should only be called on server side! Use 'PacketSetSlot' to call this from Client
            NBTTagCompound data, slotTag;
            if(player.getEntityData().hasKey("FixableSlotsData", Constants.NBT.TAG_COMPOUND)){
                data = player.getEntityData().getCompoundTag("FixableSlotsData");
                if(data.hasKey("Slot_" + slotIndex, Constants.NBT.TAG_COMPOUND)){
                    slotTag = data.getCompoundTag("Slot_" + slotIndex);
                    if(typeIndex == SlotType.VANILLA.ordinal()){ // Remove all VANILLA slots
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
        }
    }

}
