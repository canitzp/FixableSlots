package de.canitzp.fixableslots;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * @author canitzp
 */
public enum SlotType {

    VANILLA {
        @Override
        public boolean isValid(ItemStack definition, ItemStack stack) {
            return true;
        }
    },
    EXACT { // meta and nbt have to match
        @Override
        public boolean isValid(ItemStack definition, ItemStack stack) {
            return Util.areStacksEqual(definition, stack);
        }
    
        @Override
        public void addText(@Nonnull EntityPlayer player, @Nonnull Slot slot, @Nonnull NonNullList<String> tooltip){
            ItemStack stack = SaveHelper.getStackForSlot(player, slot.getSlotIndex());
            tooltip.add("Exact mode");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- No other item type can go into it");
            tooltip.add("");
            tooltip.add(TextFormatting.GOLD.toString() + "Currently set to: \"" + stack.getItem().getRegistryName() + "/" + stack.getItemDamage() + "\" NBT:{" + (stack.hasTagCompound() ? stack.getTagCompound().getSize() + " entr(y|ies)" : "no entries") + "}");
        }
    },
    FUZY_LAZY_META { // only nbt has to match, meta is ignored
        @Override
        public boolean isValid(ItemStack definition, ItemStack stack) {
            return Util.areStacksEqualExceptMeta(definition, stack);
        }
    
        @Nonnull
        @Override
        public ItemStack getRenderStack(@Nonnull ItemStack origin, @Nonnull EntityPlayer player, @Nonnull Slot slot){
            if(origin.getHasSubtypes()){
                NonNullList<ItemStack> subs = NonNullList.create();
                origin.getItem().getSubItems(CreativeTabs.SEARCH, subs);
                return subs.get(SlotType.getRenderFrameAsInt(player.getEntityWorld().getTotalWorldTime(), subs.size(), WAIT_TIME_BEFORE_RENDER_STACK_CHANGES_IN_TICKS));
            }
            return super.getRenderStack(origin, player, slot);
        }
    
        @Override
        public void addText(@Nonnull EntityPlayer player, @Nonnull Slot slot, @Nonnull NonNullList<String> tooltip){
            ItemStack stack = SaveHelper.getStackForSlot(player, slot.getSlotIndex());
            tooltip.add("Fuzzy mode (Meta)");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- You can put the same item with different meta/durability in it");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- NBT has to be equal");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- Useful for tools (Except energetic ones)");
            tooltip.add("");
            tooltip.add(TextFormatting.GOLD.toString() + "Currently set to: \"" + stack.getItem().getRegistryName() + "\" NBT:{" + (stack.hasTagCompound() ? stack.getTagCompound().getSize() + " entr(y|ies)" : "") + "}");
        }
    },
    FUZY_LAZY_NBT { // only meta has to match, nbt is ignored
        @Override
        public boolean isValid(ItemStack definition, ItemStack stack) {
            return Util.areStacksEqualExceptNBT(definition, stack);
        }
    
        @Nonnull
        @Override
        public ItemStack getRenderStack(@Nonnull ItemStack origin, @Nonnull EntityPlayer player, @Nonnull Slot slot){
            if(origin.hasTagCompound()){
                origin.setTagCompound(new NBTTagCompound());
            }
            return origin;
        }
    
        @Override
        public void addText(@Nonnull EntityPlayer player, @Nonnull Slot slot, @Nonnull NonNullList<String> tooltip){
            ItemStack stack = SaveHelper.getStackForSlot(player, slot.getSlotIndex());
            tooltip.add("Fuzzy mode (NBT)");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- You can put the same item with different NBT in it");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- Meta has to be equal");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- Useful for energetic tools or forestry bees");
            tooltip.add("");
            tooltip.add(TextFormatting.GOLD.toString() + "Currently set to: \"" + stack.getItem().getRegistryName() + "/" + stack.getItemDamage() + "\"");
        }
    },
    FUZY_STRICT { // meta and nbt are ignored
        @Override
        public boolean isValid(ItemStack definition, ItemStack stack) {
            return ItemStack.areItemsEqual(definition, stack);
        }
    
        @Override
        public void addText(@Nonnull EntityPlayer player, @Nonnull Slot slot, @Nonnull NonNullList<String> tooltip){
            tooltip.add("Fuzzy mode (Strict)");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- You can put the same item with different meta/durability and different NBT in it");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- Idk when this is useful. Tell it me and I add it here");
            tooltip.add("");
            tooltip.add(TextFormatting.GOLD.toString() + "Currently set to: \"" + SaveHelper.getStackForSlot(player, slot.getSlotIndex()).getItem().getRegistryName() + "\"");
        }
    },
    ORE_DICT {
        @Override
        public boolean isValid(ItemStack definition, ItemStack stack) {
            int[] idsDef = OreDictionary.getOreIDs(definition);
            int[] idsStack = OreDictionary.getOreIDs(stack);
            return Arrays.stream(idsDef).anyMatch(value -> Arrays.stream(idsStack).anyMatch(value1 -> value == value1));
        }
    
        @Nonnull
        @Override
        public ItemStack getRenderStack(@Nonnull ItemStack origin, @Nonnull EntityPlayer player, @Nonnull Slot slot){
            NonNullList<ItemStack> stacks = NonNullList.create();
            stacks.add(origin);
            Arrays.stream(OreDictionary.getOreIDs(origin))
                  .mapToObj(i -> OreDictionary.getOres(OreDictionary.getOreName(i)).stream())
                  .flatMap(Function.identity())
                  .filter(stack -> !stacks.contains(stack))
                  .forEach(e -> {
                      if(e.getItemDamage() == OreDictionary.WILDCARD_VALUE){
                          NonNullList<ItemStack> subs = NonNullList.create();
                          e.getItem().getSubItems(CreativeTabs.SEARCH, subs);
                          subs.stream().filter(stack -> !stacks.contains(stack)).forEach(stacks::add);
                      } else {
                          stacks.add(e);
                      }
                  });
            return stacks.get(SlotType.getRenderFrameAsInt(player.getEntityWorld().getTotalWorldTime(), stacks.size(), WAIT_TIME_BEFORE_RENDER_STACK_CHANGES_IN_TICKS));
        }
    
        @Override
        public void addText(@Nonnull EntityPlayer player, @Nonnull Slot slot, @Nonnull NonNullList<String> tooltip){
            List<String> oreNames = Arrays.stream(OreDictionary.getOreIDs(SaveHelper.getStackForSlot(player, slot.getSlotIndex())))
                                          .mapToObj(i -> "\"" + OreDictionary.getOreName(i) + "\"")
                                          .collect(Collectors.toList());
            tooltip.add("Ore Dict mode");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- You can put every item with one equal Ore Dictionary key in it");
            tooltip.add(TextFormatting.DARK_GRAY.toString() + "- Useful for ores/ingots");
            tooltip.add("");
            tooltip.add(TextFormatting.GOLD.toString() + "Currently set to: " + String.join(" & ", oreNames));
        }
    };

    public static final long WAIT_TIME_BEFORE_RENDER_STACK_CHANGES_IN_TICKS = 20;
    
    public int getColor(){
        return 0x80E8D990;
    }
    
    public static SlotType getNextInOrder(int current){
        current++;
        if(current >= values().length){
            return VANILLA;
        }
        return values()[current];
    }
    
    public SlotType getNextInOrder(){
        return getNextInOrder(this.ordinal());
    }
    
    public abstract boolean isValid(ItemStack definition, ItemStack stack);
    
    @Nonnull
    public ItemStack getRenderStack(@Nonnull ItemStack origin, @Nonnull EntityPlayer player, @Nonnull Slot slot){
        return origin;
    }
    
    public void addText(@Nonnull EntityPlayer player, @Nonnull Slot slot, @Nonnull NonNullList<String> tooltip){
        tooltip.add(this.name());
    }
    
    private static long getRenderFrame(long currentTime, long frameCount, long timeBetweenFrames){
        if(currentTime >= frameCount && currentTime >= timeBetweenFrames){
            return (currentTime / timeBetweenFrames) % frameCount;
        }
        return 0;
    }
    
    private static int getRenderFrameAsInt(long currentTime, long frameCount, long timeBetweenFrames){
        return Math.toIntExact(getRenderFrame(currentTime, frameCount, timeBetweenFrames));
    }

}
