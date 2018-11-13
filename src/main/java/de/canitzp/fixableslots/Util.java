package de.canitzp.fixableslots;

import net.minecraft.item.ItemStack;

public class Util{
    
    public static boolean areStacksEqual(ItemStack a, ItemStack b){
        return ItemStack.areItemStacksEqual(a, b);
    }
    
    public static boolean areStacksEqualExceptMeta(ItemStack a, ItemStack b){
        return a.getItem() == b.getItem() && ItemStack.areItemStackTagsEqual(a, b);
    }
    
    public static boolean areStacksEqualExceptNBT(ItemStack a, ItemStack b){
        return ItemStack.areItemsEqual(a, b);
    }
    
}
