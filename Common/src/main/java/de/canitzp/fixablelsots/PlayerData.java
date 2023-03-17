package de.canitzp.fixablelsots;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.player.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    public static final Map<UUID, CompoundTag> DATA = new HashMap<>();

    public static CompoundTag getOrCreatePlayerData(Player player){
        CompoundTag playerDataTag;
        if(DATA.containsKey(player.getUUID())){
            playerDataTag = DATA.get(player.getUUID());
        } else {
            playerDataTag = new CompoundTag();
            DATA.put(player.getUUID(), playerDataTag);
        }
        return playerDataTag;
    }

    public static CompoundTag getOrCreateFixableSlotsData(Player player){
        CompoundTag playerDataTag = PlayerData.getOrCreatePlayerData(player);
        CompoundTag fixableSlotsDataTag;
        if(playerDataTag.contains("FixableSlotsData", 10)){
            fixableSlotsDataTag = playerDataTag.getCompound("FixableSlotsData");
        } else {
            fixableSlotsDataTag = new CompoundTag();
            playerDataTag.put("FixableSlotsData", fixableSlotsDataTag);
        }
        return fixableSlotsDataTag;
    }

    public static void save(File file){
        CompoundTag saveTag = new CompoundTag();
        DATA.forEach((uuid, compoundTag) -> {
            saveTag.put(uuid.toString(), compoundTag);
        });
        try {
            NbtIo.writeCompressed(saveTag, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void read(File file){
        try {
            CompoundTag saveTag = NbtIo.readCompressed(file);
            for (String key : saveTag.getAllKeys()) {
                try {
                    UUID uuid = UUID.fromString(key);
                    DATA.put(uuid, saveTag.getCompound(key));
                } catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
