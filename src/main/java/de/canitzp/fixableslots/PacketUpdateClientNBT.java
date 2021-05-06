package de.canitzp.fixableslots;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class PacketUpdateClientNBT {

    public static final ResourceLocation NAME = new ResourceLocation(FixableSlots.MODID, "update_client_nbt");

    @Environment(EnvType.CLIENT)
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(NAME, PacketUpdateClientNBT::receive);
    }

    public static void send(ServerPlayer player, int slotIndex, CompoundTag slotTag){
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(slotIndex);
        buf.writeNbt(slotTag);

        ServerPlayNetworking.send(player, NAME, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        int slotIndex = buf.readInt();
        CompoundTag slotTag = buf.readAnySizeNbt();
        client.execute(new ClientRun(slotIndex, slotTag, client));
    }

    @Environment(EnvType.CLIENT)
    private static class ClientRun implements Runnable {
        private int slotIndex;
        private CompoundTag slotTag;
        private Minecraft client;

        public ClientRun(int slotIndex, CompoundTag slotTag, Minecraft client) {
            this.slotIndex = slotIndex;
            this.slotTag = slotTag;
            this.client = client;
        }

        @Override
        public void run() {
            CompoundTag fixableSlotsData = PlayerData.getOrCreateFixableSlotsData(client.player);
            if (slotTag.isEmpty()) {
                fixableSlotsData.remove("Slot_" + slotIndex);
            } else {
                fixableSlotsData.put("Slot_" + slotIndex, slotTag);
            }
        }
    }
}
