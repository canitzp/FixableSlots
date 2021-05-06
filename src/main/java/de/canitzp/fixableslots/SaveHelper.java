package de.canitzp.fixableslots;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @author canitzp
 */
public class SaveHelper {

    public static boolean isItemValid(@NotNull Player player, int slotIndex, @NotNull ItemStack stack){
        return getSlotType(player, slotIndex).isValid(player, slotIndex, getStackForSlot(player, slotIndex), stack);
    }
    
    @NotNull
    public static ItemStack getStackForSlot(@NotNull Player player, int slotIndex){
        CompoundTag nbt = PlayerData.getOrCreateFixableSlotsData(player);
        if(nbt.contains("Slot_" + slotIndex, NbtType.COMPOUND)){
            CompoundTag slotTag = nbt.getCompound("Slot_" + slotIndex);
            if(slotTag.contains("Definition", NbtType.COMPOUND)){
                return ItemStack.of(slotTag.getCompound("Definition"));
            }
        }
        return ItemStack.EMPTY;
    }

    @NotNull
    public static SlotType getSlotType(@NotNull Player player, int slotIndex){
        CompoundTag nbt = PlayerData.getOrCreateFixableSlotsData(player);
        if(nbt.contains("Slot_" + slotIndex, NbtType.COMPOUND)){
            CompoundTag slotTag = nbt.getCompound("Slot_" + slotIndex);
            if(slotTag.contains("Type", NbtType.INT)){
                return SlotType.values()[slotTag.getInt("Type")];
            }
        }
        return SlotType.VANILLA;
    }

    @NotNull
    public static CompoundTag getSlotData(@NotNull Player player, int slotIndex){
        CompoundTag nbt = PlayerData.getOrCreateFixableSlotsData(player);
        if(nbt.contains("Slot_" + slotIndex, NbtType.COMPOUND)){
            CompoundTag slotTag = nbt.getCompound("Slot_" + slotIndex);
            if(slotTag.contains("Data", NbtType.COMPOUND)){
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

    public static void setSlot(@NotNull Player player, int slotIndex, int typeIndex, @NotNull ItemStack definition){
        if(player instanceof ServerPlayer){ // should only be called on server side! Use 'PacketSetSlot' to call this from Client
            CompoundTag fixableSlotsData = PlayerData.getOrCreateFixableSlotsData(player);
            CompoundTag slotTag;
            if(fixableSlotsData.contains("Slot_" + slotIndex, NbtType.COMPOUND)){
                slotTag = fixableSlotsData.getCompound("Slot_" + slotIndex);
            } else {
                slotTag = new CompoundTag();
                fixableSlotsData.put("Slot_" + slotIndex, slotTag);
            }

            if(typeIndex == SlotType.VANILLA.ordinal()){ // Remove all VANILLA slots
                fixableSlotsData.remove("Slot_" + slotIndex);
                PacketUpdateClientNBT.send((ServerPlayer) player, slotIndex, new CompoundTag());
                return;
            }
    
            slotTag.putInt("Type", typeIndex);
            slotTag.put("Definition", definition.save(new CompoundTag()));

            PacketUpdateClientNBT.send((ServerPlayer) player, slotIndex, slotTag);
        }
    }

}
