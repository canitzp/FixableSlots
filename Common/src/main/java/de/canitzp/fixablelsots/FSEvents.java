package de.canitzp.fixablelsots;

import com.mojang.blaze3d.vertex.PoseStack;
import de.canitzp.fixablelsots.mixin.AbstractContainerScreenInvoker;
import de.canitzp.fixablelsots.mixin.DimensionDataStorageInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

import java.io.File;

public class FSEvents {

    public static void onPlayerJoin(Player player){
        CompoundTag playerData = PlayerData.getOrCreatePlayerData(player);
        //PacketLogin.send(player, playerData);
    }

    public static void onWorldLoad(Level level){
        if (level.dimension().location().equals(DimensionType.OVERWORLD_EFFECTS)) {
            File fixableSlotsFile = ((DimensionDataStorageInvoker) level.getLevelData()).invokeGetDataFile("fixableSlots");
            if (fixableSlotsFile.exists()) {
                PlayerData.read(fixableSlotsFile);
            }
        }
    }

    public static void onWorldUnload(Level level){
        if (level.dimension().location().equals(DimensionType.OVERWORLD_EFFECTS)) {
            if (PlayerData.DATA.isEmpty()) {
                return;
            }
            File fixableSlotsFile = ((DimensionDataStorageInvoker) level.getLevelData()).invokeGetDataFile("fixableSlots");
            PlayerData.save(fixableSlotsFile);
            PlayerData.DATA.clear(); // clear to delete all uuids and compounds before joining new world
        }
    }

    public static void afterScreenRender(Screen screen, PoseStack poseStack, int mouseX, int mouseY, float partialTicks){
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
    }

    public static boolean onMouseClickedWithinScreen(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int mouseButton){
        return false;
    }

}
