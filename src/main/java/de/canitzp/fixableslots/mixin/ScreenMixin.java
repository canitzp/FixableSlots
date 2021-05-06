package de.canitzp.fixableslots.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.canitzp.fixableslots.CustomClientEvents;
import de.canitzp.fixableslots.CustomEvents;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void afterRendering(PoseStack poseStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci){
        CustomClientEvents.AFTER_SCREEN_RENDER_EVENT.invoker().afterScreenRender((Screen) (Object) this, poseStack, mouseX, mouseY, partialTicks);
    }

}
