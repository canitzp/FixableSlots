package de.canitzp.fixableslots;

import de.canitzp.fixableslots.mixin.AbstractContainerScreenInvoker;
import de.canitzp.fixableslots.mixin.SlotAccessor;
import de.canitzp.fixableslots.mixin.SlotWrapperAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Util{

    public static int getSlotIndex(Slot slot){
        if(slot instanceof CreativeModeInventoryScreen.SlotWrapper){
            slot = ((SlotWrapperAccessor) slot).accessTarget();
        }
        return ((SlotAccessor) slot).accessSlotIndex();
    }

    public static int getScreenLeft(AbstractContainerScreen<?> screen){
        return ((AbstractContainerScreenInvoker) screen).getLeft();
    }

    public static int getScreenTop(AbstractContainerScreen<?> screen){
        return ((AbstractContainerScreenInvoker) screen).getTop();
    }

    public static boolean areStacksEqual(ItemStack a, ItemStack b){
        return ItemStack.matches(a, b);
    }
    
    public static boolean areStacksEqualExceptMeta(ItemStack a, ItemStack b){
        return a.getItem() == b.getItem() && ItemStack.tagMatches(a, b);
    }
    
    public static boolean areStacksEqualExceptNBT(ItemStack a, ItemStack b){
        return ItemStack.isSame(a, b);
    }

    public static List<FormattedCharSequence> wrapText(List<Component> components, int screenWidth){
        List<FormattedCharSequence> ret = new ArrayList<>();
        for (Component component : components) {
            ret.addAll(Minecraft.getInstance().font.split(component, Math.max(screenWidth / 2, 200)));
        }
        return ret;
    }

    public static List<ResourceLocation> getMatchingTags(Item item){
        List<ResourceLocation> resourceLocations = new ArrayList<>();
        ItemTags.getAllTags().getAllTags().forEach((resourceLocation, itemTag) -> {
            if(item.is(itemTag)){
                resourceLocations.add(resourceLocation);
            }
        });
        return resourceLocations;
    }

}
