package de.canitzp.fixableslots;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.GuiContainerEvent;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;

import java.util.List;

/**
 * @author canitzp
 */
@Mod.EventBusSubscriber(modid = FixableSlots.MODID)
@Mod(modid = FixableSlots.MODID, name = FixableSlots.MODNAME, version = FixableSlots.MODVERSION)
public class FixableSlots {

    public static final String MODID = "fixableslots";
    public static final String MODNAME = "Fixable Slots";
    public static final String MODVERSION = "@VERSION@";

    public static SimpleNetworkWrapper NET = new SimpleNetworkWrapper(MODID);
    
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event){
        NET.registerMessage(PacketUpdateClientNBT.class, PacketUpdateClientNBT.class, 0, Side.CLIENT);
        NET.registerMessage(PacketSetSlot.class, PacketSetSlot.class, 1, Side.SERVER);
        NET.registerMessage(PacketLogin.class, PacketLogin.class, 2, Side.CLIENT);
    }
    
    @SubscribeEvent
    public static void onPlayerJoins(PlayerEvent.PlayerLoggedInEvent event){
        if(event.player instanceof EntityPlayerMP){
            NBTTagCompound playerNBT = event.player.getEntityData();
            if(playerNBT.hasKey("FixableSlotsData", Constants.NBT.TAG_COMPOUND)){
                NET.sendTo(new PacketLogin(playerNBT.getCompoundTag("FixableSlotsData")), (EntityPlayerMP) event.player);
            }
        }
    }
    
    private static long lastClick = 0; // hacky ftw
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void drawGuiContainer(GuiContainerEvent.DrawForeground event){
        GuiContainer gui = event.getGuiContainer();
        for(Slot slot : gui.inventorySlots.inventorySlots){
            if(slot.inventory instanceof InventoryPlayer){
                EntityPlayer player = ((InventoryPlayer) slot.inventory).player;
                if(!slot.getHasStack()){
                    SlotType type = SaveHelper.getSlotType(player, slot.getSlotIndex());
                    if(type != SlotType.VANILLA){
                        GlStateManager.pushMatrix();
                        Minecraft.getMinecraft().getRenderItem().zLevel = -90.0F;
                        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(SaveHelper.getSlotType(player, slot.getSlotIndex()).getRenderStack(SaveHelper.getStackForSlot(player, slot.getSlotIndex()), player, slot), slot.xPos, slot.yPos);
                        GlStateManager.translate(0, 0, 20);
                        Gui.drawRect(slot.xPos, slot.yPos, slot.xPos + 16, slot.yPos + 16, type.getColor());
                        GlStateManager.popMatrix();
                    }
                }
            }
        }
    
        Slot slot = gui.getSlotUnderMouse();
        if(slot != null && slot.inventory instanceof InventoryPlayer){
            EntityPlayer player = ((InventoryPlayer) slot.inventory).player;
            SlotType type = SaveHelper.getSlotType(player, slot.getSlotIndex());
            if(type != SlotType.VANILLA){
                NonNullList<String> text = NonNullList.create();
                type.addText(player, slot, text);
                GlStateManager.pushMatrix();
                GuiUtils.drawHoveringText(ItemStack.EMPTY, text, event.getMouseX() - gui.getGuiLeft(), event.getMouseY() - gui.getGuiTop(), gui.width - gui.getGuiLeft(), gui.height - gui.getGuiTop(), -1, Minecraft.getMinecraft().fontRenderer);
                RenderHelper.enableGUIStandardItemLighting(); // cause hover texts aren't allowed this early
                GlStateManager.popMatrix();
            }
            
            if(Mouse.isButtonDown(2) && lastClick + 200 <= System.currentTimeMillis()){
                lastClick = System.currentTimeMillis();
                if(player.inventory.getItemStack().isEmpty() || type.getNextInOrder() == SlotType.VANILLA){
                    NET.sendToServer(new PacketSetSlot(player, slot.getSlotIndex(), SlotType.VANILLA.ordinal(), ItemStack.EMPTY));
                }else{
                    NET.sendToServer(new PacketSetSlot(player, slot.getSlotIndex(), type.getNextInOrder().ordinal(), player.inventory.getItemStack()));
                }
            }
        }
    }

}
