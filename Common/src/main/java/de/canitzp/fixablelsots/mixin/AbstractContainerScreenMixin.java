package de.canitzp.fixablelsots.mixin;

import de.canitzp.fixablelsots.FSEvents;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void mouseClick(double mouseX, double mouseY, int mouseButton, CallbackInfoReturnable<Boolean> cir){
        boolean anyCancel = FSEvents.onMouseClickedWithinScreen((AbstractContainerScreen<?>) (Object) this, mouseX, mouseY, mouseButton);
        if(anyCancel){
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

}
