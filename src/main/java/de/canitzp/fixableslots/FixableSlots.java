package de.canitzp.fixableslots;

import de.canitzp.fixableslots.mixin.AbstractContainerScreenInvoker;
import de.canitzp.fixableslots.mixin.DimensionDataStorageInvoker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.dimension.DimensionType;

import java.io.File;
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

        CustomEvents.PLAYER_LOGGED_IN_EVENT.register((connection, player) -> {
            CompoundTag playerData = PlayerData.getOrCreatePlayerData(player);
            PacketLogin.send(player, playerData);
        });

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.dimension().location().equals(DimensionType.OVERWORLD_EFFECTS)) {
                File fixableSlotsFile = ((DimensionDataStorageInvoker) world.getDataStorage()).invokeGetDataFile("fixableSlots");
                if (fixableSlotsFile.exists()) {
                    PlayerData.read(fixableSlotsFile);
                }
            }
        });
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            if (world.dimension().location().equals(DimensionType.OVERWORLD_EFFECTS)) {
                if (PlayerData.DATA.isEmpty()) {
                    return;
                }
                File fixableSlotsFile = ((DimensionDataStorageInvoker) world.getDataStorage()).invokeGetDataFile("fixableSlots");
                PlayerData.save(fixableSlotsFile);
                PlayerData.DATA.clear(); // clear to delete all uuids and compounds before joining new world
            }
        });

    }

    private void initializeClient(){
        PacketUpdateClientNBT.register();
        PacketLogin.register();

        CustomClientEvents.AFTER_SCREEN_RENDER_EVENT.register((screen, poseStack, mouseX, mouseY, partialTicks) -> {
            if (screen instanceof AbstractContainerScreen) {
                AbstractContainerMenu menu = ((AbstractContainerScreen<?>) screen).getMenu();
                for (Slot slot : menu.slots) {
                    if (!slot.hasItem()) {
                        if (slot.container instanceof Inventory) {
                            Player player = ((Inventory) slot.container).player;
                            SlotType type = SaveHelper.getSlotType(player, Util.getSlotIndex(slot));
                            if (type != SlotType.VANILLA) {
                                int left = Util.getScreenLeft((AbstractContainerScreen<?>) screen);
                                int top = Util.getScreenTop((AbstractContainerScreen<?>) screen);
                                poseStack.pushPose();
                                poseStack.translate(left, top, 0D);
                                Minecraft.getInstance().getItemRenderer().renderGuiItem(SaveHelper.getSlotType(player, Util.getSlotIndex(slot)).getRenderStack(SaveHelper.getStackForSlot(player, Util.getSlotIndex(slot)), player, slot), slot.x + left, slot.y + top);
                                poseStack.translate(0, 0, 200);
                                GuiComponent.fill(poseStack, slot.x, slot.y, slot.x + 16, slot.y + 16, type.getColor());
                                if (((AbstractContainerScreenInvoker) screen).invokeIsHovering(slot, mouseX, mouseY)) {
                                    NonNullList<Component> text = NonNullList.create();
                                    type.addText(player, slot, text);
                                    poseStack.translate(-left, -top, 0);
                                    screen.renderTooltip(poseStack, Util.wrapText(text, screen.width), mouseX, mouseY);
                                }
                                poseStack.popPose();
                            }
                        }
                    }
                }
            }
        });

        final AtomicLong lastClick = new AtomicLong(0); // hacky ftw
        CustomClientEvents.SCREEN_MOUSE_CLICK_EVENT.register((screen, mouseX, mouseY, mouseButton) -> {
            AbstractContainerMenu menu = screen.getMenu();
            for (Slot slot : menu.slots) {
                if (((AbstractContainerScreenInvoker) screen).invokeIsHovering(slot, mouseX, mouseY)) {
                    if (slot.container instanceof Inventory) {
                        if (mouseButton == 2 && lastClick.get() + 200 <= System.currentTimeMillis()) {
                            lastClick.set(System.currentTimeMillis());
                            PacketSlotClick.send(Util.getSlotIndex(slot), ((Inventory) slot.container).getCarried());
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }
}