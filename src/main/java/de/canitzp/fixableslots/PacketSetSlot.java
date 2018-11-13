package de.canitzp.fixableslots;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetSlot implements IMessage, IMessageHandler<PacketSetSlot, IMessage>{
    
    private EntityPlayer player;
    private int slotIndex, typeIndex;
    private ItemStack stack = ItemStack.EMPTY;
    
    public PacketSetSlot(){}
    
    public PacketSetSlot(EntityPlayer player, int slotIndex, int typeIndex, ItemStack stack){
        this.player = player;
        this.slotIndex = slotIndex;
        this.typeIndex = typeIndex;
        this.stack = stack;
    }
    
    @Override
    public void fromBytes(ByteBuf buf){
        World world = DimensionManager.getWorld(buf.readInt());
        if(world != null){
            this.player = world.getPlayerEntityByUUID(new PacketBuffer(buf).readUniqueId());
        }
        this.slotIndex = buf.readInt();
        this.typeIndex = buf.readInt();
        this.stack = ByteBufUtils.readItemStack(buf);
    }
    
    @Override
    public void toBytes(ByteBuf buf){
        buf.writeInt(this.player.getEntityWorld().provider.getDimension());
        new PacketBuffer(buf).writeUniqueId(this.player.getUniqueID());
        buf.writeInt(this.slotIndex);
        buf.writeInt(this.typeIndex);
        ByteBufUtils.writeItemStack(buf, this.stack);
    }
    
    @Override
    public IMessage onMessage(PacketSetSlot message, MessageContext ctx){
        if(message.player != null){
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> SaveHelper.setSlot(message.player, message.slotIndex, message.typeIndex, message.stack));
        }
        return null;
    }
}
