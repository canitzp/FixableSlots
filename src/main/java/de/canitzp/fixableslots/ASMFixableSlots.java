package de.canitzp.fixableslots;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * @author canitzp
 */
@IFMLLoadingPlugin.Name(FixableSlots.MODID + " coremod")
public class ASMFixableSlots implements IFMLLoadingPlugin, IClassTransformer{
    
    private String methodName = "isItemValid";
    
    @Override
    public String[] getASMTransformerClass() {
        return new String[]{this.getClass().getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        methodName = ((boolean) data.get("runtimeDeobfuscationEnabled")) ? "func_75214_a" : "isItemValid";
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if("net.minecraft.inventory.Slot".equals(transformedName)){
            ClassReader cr = new ClassReader(basicClass);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            cn = this.transformSlot(cn);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            cn.accept(cw);
            return write(cw.toByteArray(), "Slot");
        }
        return basicClass;
    }

    private byte[] write(byte[] data, @Nonnull String name){
        if(methodName.equals("isItemValid")){
            try {
                FileUtils.writeByteArrayToFile(new File(".", name + ".class"), data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    /*
    ALOAD 0
    ALOAD 1
    INVOKESTATIC de/canitzp/fixableslots/ASMFixableSlots.isItemValidForSlot (Lnet/minecraft/inventory/Slot;Lnet/minecraft/item/ItemStack;)Z
    IRETURN
     */
    private ClassNode transformSlot(ClassNode cn){
        for(MethodNode method : cn.methods){
            if(methodName.equals(method.name)){
                InsnList insnList = new InsnList();
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "de/canitzp/fixableslots/ASMFixableSlots", "isItemValidForSlot", "(Lnet/minecraft/inventory/Slot;Lnet/minecraft/item/ItemStack;)Z", false));
                insnList.add(new InsnNode(Opcodes.IRETURN));
                method.instructions.clear();
                method.instructions.add(insnList);
            }
        }
        return cn;
    }

    public static boolean isItemValidForSlot(Slot slot, ItemStack stack){
        if(slot.inventory instanceof InventoryPlayer){
            return SaveHelper.isItemValid(((InventoryPlayer) slot.inventory).player, slot.getSlotIndex(), stack);
        }
        return true;
    }

}
