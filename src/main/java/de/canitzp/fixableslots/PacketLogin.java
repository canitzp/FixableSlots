package de.canitzp.fixableslots;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketLogin implements IMessage, IMessageHandler<PacketLogin, IMessage>{
    
    private NBTTagCompound data;
    
    public PacketLogin(){
    }
    
    public PacketLogin(NBTTagCompound data){
        this.data = data;
    }
    
    @Override
    public void fromBytes(ByteBuf buf){
        this.data = ByteBufUtils.readTag(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf){
        ByteBufUtils.writeTag(buf, this.data);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public IMessage onMessage(PacketLogin message, MessageContext ctx){
        if(message.data != null && !message.data.hasNoTags()){
            Minecraft.getMinecraft().addScheduledTask(() -> {
                NBTTagCompound playerNBT = Minecraft.getMinecraft().player.getEntityData();
                playerNBT.setTag("FixableSlotsData", message.data);
            });
        }
        return null;
    }
}
