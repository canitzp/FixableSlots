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

public class PacketLogin {

    public static final ResourceLocation NAME = new ResourceLocation(FixableSlots.MODID, "login");

    @Environment(EnvType.CLIENT)
    public static void register(){
        ClientPlayNetworking.registerGlobalReceiver(NAME, PacketLogin::receive);
    }

    public static void send(ServerPlayer player, CompoundTag playerData){
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeNbt(playerData);
        ServerPlayNetworking.send(player, NAME, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void receive(Minecraft client, ClientPacketListener handler, FriendlyByteBuf buf, PacketSender responseSender) {
        CompoundTag playerData = buf.readAnySizeNbt();
        client.execute(() -> {
            PlayerData.DATA.put(client.player.getUUID(), playerData);
        });
    }
}
