package de.canitzp.fixablelsots.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenInvoker {

    @Accessor("leftPos")
    public int getLeft();

    @Accessor("topPos")
    public int getTop();

    @Invoker
    public boolean invokeIsHovering(Slot slot, double d, double e);

}
