package de.canitzp.fixableslots;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketUpdateClientNBT implements IMessage, IMessageHandler<PacketUpdateClientNBT, IMessage>{
    
    private EntityPlayer player;
    private int slotIndex;
    private NBTTagCompound slotTag;
    
    public PacketUpdateClientNBT(){}
    
    public PacketUpdateClientNBT(EntityPlayer player, int slotIndex, NBTTagCompound slotTag){
        this.player = player;
        this.slotIndex = slotIndex;
        this.slotTag = slotTag;
    }
    
    @Override
    public void fromBytes(ByteBuf buf){
        World world = DimensionManager.getWorld(buf.readInt());
        if(world != null){
            this.player = world.getPlayerEntityByUUID(new PacketBuffer(buf).readUniqueId());
        }
        this.slotIndex = buf.readInt();
        this.slotTag = ByteBufUtils.readTag(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(this.player.getEntityWorld().provider.getDimension());
        new PacketBuffer(buf).writeUniqueId(this.player.getUniqueID());
        buf.writeInt(this.slotIndex);
        ByteBufUtils.writeTag(buf, this.slotTag);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(PacketUpdateClientNBT message, MessageContext ctx){
        if(message.player != null && message.slotTag != null){
            EntityPlayer client = Minecraft.getMinecraft().player;
            if(message.player.getUniqueID().equals(client.getUniqueID())){
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    NBTTagCompound playerNBT = client.getEntityData();
                    if(playerNBT.hasKey("FixableSlotsData", Constants.NBT.TAG_COMPOUND)){
                        playerNBT.getCompoundTag("FixableSlotsData").setTag("Slot_" + message.slotIndex, message.slotTag);
                    } else {
                        NBTTagCompound data = new NBTTagCompound();
                        data.setTag("Slot_" + message.slotIndex, message.slotTag);
                        playerNBT.setTag("FixableSlotsData", data);
                    }
                });
            }
        }
        return null;
    }
}
