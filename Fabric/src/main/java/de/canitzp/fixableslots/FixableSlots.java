package de.canitzp.fixableslots;

import de.canitzp.fixablelsots.FSEvents;
import de.canitzp.fixablelsots.PlayerData;
import de.canitzp.fixablelsots.Util;
import de.canitzp.fixablelsots.mixin.AbstractContainerScreenInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author canitzp
 */
public class FixableSlots implements ModInitializer {

    public static final String MODID = "fixableslots";

    @Override
    public void onInitialize() {
        EnvType environmentType = FabricLoader.getInstance().getEnvironmentType();
        if (environmentType == EnvType.CLIENT) {
            this.initializeClient();
        }
        PacketSlotClick.register();

        ServerWorldEvents.LOAD.register((server, world) -> {
            FSEvents.onWorldLoad(world);
        });
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            FSEvents.onWorldUnload(world);
        });
    }

    private void initializeClient(){
        PacketUpdateClientNBT.register();
        PacketLogin.register();

        final AtomicLong lastClick = new AtomicLong(0); // hacky ftw
        CustomClientEvents.SCREEN_MOUSE_CLICK_EVENT.register((screen, mouseX, mouseY, mouseButton) -> {
            AbstractContainerMenu menu = screen.getMenu();
            for (Slot slot : menu.slots) {
                if (((AbstractContainerScreenInvoker) screen).invokeIsHovering(slot, mouseX, mouseY)) {
                    if (slot.container instanceof Inventory) {
                        if (mouseButton == 2 && lastClick.get() + 200 <= System.currentTimeMillis()) {
                            lastClick.set(System.currentTimeMillis());
                            PacketSlotClick.send(Util.getSlotIndex(slot), menu.getCarried());
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }
}