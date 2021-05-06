package de.canitzp.fixableslots;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;

public class PacketSlotClick {

    public static final ResourceLocation NAME = new ResourceLocation(FixableSlots.MODID, "slot_click");

    public static void register(){
        ServerPlayNetworking.registerGlobalReceiver(NAME, PacketSlotClick::receive);
    }

    public static void send(int slotIndex, ItemStack carried){
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(slotIndex);
        buf.writeItem(carried);

        ClientPlayNetworking.send(NAME, buf);
    }

    public static void receive(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
        int slotIndex = buf.readInt();
        ItemStack carried = buf.readItem();
        server.execute(() -> {
            SlotType slotType = SaveHelper.getSlotType(player, slotIndex);

            SlotType slotTypeNext = slotType;
            do {
                slotTypeNext = slotTypeNext.shouldJumpToNextType(player, slotIndex) ? slotTypeNext.getNextInOrder() : slotTypeNext;
            } while (!slotTypeNext.canBeUsed(player, slotIndex));

            if(carried.isEmpty() || slotTypeNext == SlotType.VANILLA){
                // erase slot data for slot
                SaveHelper.setSlot(player, slotIndex, SlotType.VANILLA.ordinal(), ItemStack.EMPTY);
            } else {
                SaveHelper.setSlot(player, slotIndex, slotTypeNext.ordinal(), carried);
            }
        });
    }
}
