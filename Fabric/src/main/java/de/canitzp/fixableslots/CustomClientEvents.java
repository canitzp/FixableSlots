package de.canitzp.fixableslots;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class CustomClientEvents {

    public static final Event<ScreenMouseClick> SCREEN_MOUSE_CLICK_EVENT = EventFactory.createArrayBacked(ScreenMouseClick.class, callbacks -> (screen, mouseX, mouseY, mouseButton) -> {
        boolean anyCancel = false;
        for (ScreenMouseClick callback : callbacks) {
            anyCancel = callback.mouseClick(screen, mouseX, mouseY, mouseButton) || anyCancel;
        }
        return anyCancel;
    });



    public interface ScreenMouseClick {
        boolean mouseClick(AbstractContainerScreen<?> screen, double mouseX, double mouseY, int mouseButton);
    }

}
