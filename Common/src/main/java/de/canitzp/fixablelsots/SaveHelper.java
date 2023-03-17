package de.canitzp.fixablelsots;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * @author canitzp
 */
public class SaveHelper {

    public static boolean isItemValid(@Nonnull Player player, int slotIndex, @Nonnull ItemStack stack){
        return getSlotType(player, slotIndex).isValid(player, slotIndex, getStackForSlot(player, slotIndex), stack);
    }
    
    @Nonnull
    public static ItemStack getStackForSlot(@Nonnull Player player, int slotIndex){
        CompoundTag nbt = PlayerData.getOrCreateFixableSlotsData(player);
        if(nbt.contains("Slot_" + slotIndex, Tag.TAG_COMPOUND)){
            CompoundTag slotTag = nbt.getCompound("Slot_" + slotIndex);
            if(slotTag.contains("Definition", Tag.TAG_COMPOUND)){
                return ItemStack.of(slotTag.getCompound("Definition"));
            }
        }
        return ItemStack.EMPTY;
    }

    @Nonnull
    public static SlotType getSlotType(@Nonnull Player player, int slotIndex){
        CompoundTag nbt = PlayerData.getOrCreateFixableSlotsData(player);
        if(nbt.contains("Slot_" + slotIndex, 10)){
            CompoundTag slotTag = nbt.getCompound("Slot_" + slotIndex);
            if(slotTag.contains("Type", Tag.TAG_INT)){
                return SlotType.values()[slotTag.getInt("Type")];
            }
        }
        return SlotType.VANILLA;
    }

    @Nonnull
    public static CompoundTag getSlotData(@Nonnull Player player, int slotIndex){
        CompoundTag nbt = PlayerData.getOrCreateFixableSlotsData(player);
        if(nbt.contains("Slot_" + slotIndex, Tag.TAG_COMPOUND)){
            CompoundTag slotTag = nbt.getCompound("Slot_" + slotIndex);
            if(slotTag.contains("Data", Tag.TAG_COMPOUND)){
                return slotTag.getCompound("Data");
            } else {
                CompoundTag slotDataTag = new CompoundTag();
                slotTag.put("Data", slotDataTag);
                return slotDataTag;
            }
        } else {
            CompoundTag slotTag = new CompoundTag();
            CompoundTag slotDataTag = new CompoundTag();
            slotTag.put("Data", slotDataTag);
            nbt.put("Slot_" + slotIndex, slotTag);
            return slotDataTag;
        }
    }

    public static void setSlot(@Nonnull Player player, int slotIndex, int typeIndex, @Nonnull ItemStack definition){
        if(player instanceof ServerPlayer){ // should only be called on server side! Use 'PacketSetSlot' to call this from Client
            CompoundTag fixableSlotsData = PlayerData.getOrCreateFixableSlotsData(player);
            CompoundTag slotTag;
            if(fixableSlotsData.contains("Slot_" + slotIndex, Tag.TAG_COMPOUND)){
                slotTag = fixableSlotsData.getCompound("Slot_" + slotIndex);
            } else {
                slotTag = new CompoundTag();
                fixableSlotsData.put("Slot_" + slotIndex, slotTag);
            }

            if(typeIndex == SlotType.VANILLA.ordinal()){ // Remove all VANILLA slots
                fixableSlotsData.remove("Slot_" + slotIndex);
                //PacketUpdateClientNBT.send((ServerPlayer) player, slotIndex, new CompoundTag());
                return;
            }
    
            slotTag.putInt("Type", typeIndex);
            slotTag.put("Definition", definition.save(new CompoundTag()));

            //PacketUpdateClientNBT.send((ServerPlayer) player, slotIndex, slotTag);
        }
    }

}
