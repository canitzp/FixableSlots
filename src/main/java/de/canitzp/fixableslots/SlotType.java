package de.canitzp.fixableslots;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author canitzp
 */
public enum SlotType {

    VANILLA {
        @Override
        public boolean isValid(@NotNull Player player, int slotIndex, ItemStack definition, ItemStack stack) {
            return true;
        }
    },
    EXACT { // meta and nbt have to match
        @Override
        public boolean isValid(@NotNull Player player, int slotIndex, ItemStack definition, ItemStack stack) {
            return Util.areStacksEqual(definition, stack);
        }
    
        @Override
        public void addText(@NotNull Player player, @NotNull Slot slot, @NotNull NonNullList<Component> tooltip){
            ItemStack stack = SaveHelper.getStackForSlot(player, Util.getSlotIndex(slot));
            tooltip.add(new TextComponent("Exact mode"));
            tooltip.add(new TextComponent("- No other item type can go into it").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent(""));
            tooltip.add(new TextComponent(String.format("Currently set to: \"%s/%d\" NBT:{%s}", Registry.ITEM.getKey(stack.getItem()), stack.getDamageValue(), (stack.hasTag() ? stack.getTag().size() + " entr(y|ies)" : "no entries"))).setStyle(Style.EMPTY.applyFormat(ChatFormatting.GOLD)));
        }
    },
    FUZY_LAZY_META { // only nbt has to match, meta is ignored
        @Override
        public boolean isValid(@NotNull Player player, int slotIndex, ItemStack definition, ItemStack stack) {
            return Util.areStacksEqualExceptMeta(definition, stack);
        }
    
        @NotNull
        @Override
        public ItemStack getRenderStack(@NotNull ItemStack origin, @NotNull Player player, @NotNull Slot slot){
            NonNullList<ItemStack> subs = NonNullList.create();
            origin.getItem().fillItemCategory(CreativeModeTab.TAB_SEARCH, subs);
            return subs.get(SlotType.getRenderFrameAsInt(player.level.getGameTime(), subs.size(), WAIT_TIME_BEFORE_RENDER_STACK_CHANGES_IN_TICKS));
        }
    
        @Override
        public void addText(@NotNull Player player, @NotNull Slot slot, @NotNull NonNullList<Component> tooltip){
            ItemStack stack = SaveHelper.getStackForSlot(player, Util.getSlotIndex(slot));
            tooltip.add(new TextComponent("Fuzzy mode (Meta)"));
            tooltip.add(new TextComponent("- You can put the same item with different meta/durability in it").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent("- NBT has to be equal").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent("- Useful for tools (Except energetic ones)").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent(""));
            tooltip.add(new TextComponent(String.format("Currently set to: \"%s\" NBT:{%s}", Registry.ITEM.getKey(stack.getItem()), (stack.hasTag() ? stack.getTag().size() + " entr(y|ies)" : ""))).setStyle(Style.EMPTY.applyFormat(ChatFormatting.GOLD)));
        }
    },
    FUZY_LAZY_NBT { // only meta has to match, nbt is ignored
        @Override
        public boolean isValid(@NotNull Player player, int slotIndex, ItemStack definition, ItemStack stack) {
            return Util.areStacksEqualExceptNBT(definition, stack);
        }
    
        @NotNull
        @Override
        public ItemStack getRenderStack(@NotNull ItemStack origin, @NotNull Player player, @NotNull Slot slot){
            if(origin.hasTag()){
                origin.setTag(new CompoundTag());
            }
            return origin;
        }
    
        @Override
        public void addText(@NotNull Player player, @NotNull Slot slot, @NotNull NonNullList<Component> tooltip){
            ItemStack stack = SaveHelper.getStackForSlot(player, Util.getSlotIndex(slot));
            tooltip.add(new TextComponent("Fuzzy mode (NBT)"));
            tooltip.add(new TextComponent("- You can put the same item with different NBT in it").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent("- Meta has to be equal").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent("- Useful for energetic tools or forestry bees").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent(""));
            tooltip.add(new TextComponent(String.format("Currently set to: \"%s/%d\"", Registry.ITEM.getKey(stack.getItem()), stack.getDamageValue())).setStyle(Style.EMPTY.applyFormat(ChatFormatting.GOLD)));
        }
    },
    FUZY_STRICT { // meta and nbt are ignored
        @Override
        public boolean isValid(@NotNull Player player, int slotIndex, ItemStack definition, ItemStack stack) {
            return ItemStack.isSame(definition, stack);
        }
    
        @Override
        public void addText(@NotNull Player player, @NotNull Slot slot, @NotNull NonNullList<Component> tooltip){
            ItemStack stack = SaveHelper.getStackForSlot(player, Util.getSlotIndex(slot));
            tooltip.add(new TextComponent("Fuzzy mode (Strict)"));
            tooltip.add(new TextComponent("- You can put the same item with different meta/durability and different NBT in it").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent("- Idk when this is useful. Tell it me and I add it here").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent(""));
            tooltip.add(new TextComponent(String.format("Currently set to: \"%s\"", Registry.ITEM.getKey(stack.getItem()))).setStyle(Style.EMPTY.applyFormat(ChatFormatting.GOLD)));
        }
    },
    TAG {
        @Override
        public boolean isValid(@NotNull Player player, int slotIndex, ItemStack definition, ItemStack stack) {
            int tagIndex = SaveHelper.getSlotData(player, slotIndex).getInt("TagIndex");
            Collection<ResourceLocation> matchingTagLocations = Util.getMatchingTags(definition.getItem());
            if(matchingTagLocations.size() > tagIndex) {
                ResourceLocation resLoc = new ArrayList<>(matchingTagLocations).get(tagIndex);
                Tag<Item> tag = ItemTags.getAllTags().getTag(resLoc);
                return tag != null && tag.contains(stack.getItem());
            }
            return Util.areStacksEqual(definition, stack);
        }
    
        @NotNull
        @Override
        public ItemStack getRenderStack(@NotNull ItemStack origin, @NotNull Player player, @NotNull Slot slot){
            int tagIndex = SaveHelper.getSlotData(player, Util.getSlotIndex(slot)).getInt("TagIndex");
            Collection<ResourceLocation> matchingTagLocations = Util.getMatchingTags(origin.getItem());
            if(matchingTagLocations.size() > tagIndex){
                ResourceLocation resLoc = new ArrayList<>(matchingTagLocations).get(tagIndex);
                Tag<Item> matchingTag = ItemTags.getAllTags().getTag(resLoc);
                if(matchingTag == null){
                    return origin;
                }

                NonNullList<Item> tagItems = NonNullList.create();
                Registry.ITEM.stream()
                        .filter(matchingTag::contains)
                        .filter(item -> !tagItems.contains(item))
                        .forEach(tagItems::add);
                Item toRenderNow = tagItems.get(SlotType.getRenderFrameAsInt(player.level.getGameTime(), tagItems.size(), WAIT_TIME_BEFORE_RENDER_STACK_CHANGES_IN_TICKS));
                return new ItemStack(toRenderNow);
            }
            return origin;
        }
    
        @Override
        public void addText(@NotNull Player player, @NotNull Slot slot, @NotNull NonNullList<Component> tooltip){
            ItemStack stack = SaveHelper.getStackForSlot(player, Util.getSlotIndex(slot));
            int tagIndex = SaveHelper.getSlotData(player, Util.getSlotIndex(slot)).getInt("TagIndex");
            Collection<ResourceLocation> matchingTagLocations = Util.getMatchingTags(stack.getItem());
            String tagName;
            if(matchingTagLocations.size() > tagIndex){
                ResourceLocation resLoc = new ArrayList<>(matchingTagLocations).get(tagIndex);
                Tag<Item> tag = ItemTags.getAllTags().getTag(resLoc);
                tagName = tag instanceof Tag.Named ? ((Tag.Named<Item>) tag).getName().toString() : resLoc.toString();
            } else {
                tagName = "OOB";
            }

            tooltip.add(new TextComponent("Tag mode"));
            tooltip.add(new TextComponent("- You can put every item with one equal Ore Dictionary key in it").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent("- Useful for ores/ingots").setStyle(Style.EMPTY.applyFormat(ChatFormatting.DARK_GRAY)));
            tooltip.add(new TextComponent(""));
            tooltip.add(new TextComponent("Currently set to: \"" + tagName + "\"").setStyle(Style.EMPTY.applyFormat(ChatFormatting.GOLD)));
        }

        @Override
        public boolean shouldJumpToNextType(@NotNull Player player, int slotIndex) {
            ItemStack stack = SaveHelper.getStackForSlot(player, slotIndex);
            int tagLength = Util.getMatchingTags(stack.getItem()).size();
            CompoundTag slotData = SaveHelper.getSlotData(player, slotIndex);

            int tagIndex = slotData.getInt("TagIndex");
            if(tagIndex >= tagLength - 1){
                // exceeded tags size
                slotData.remove("TagIndex");
                return true;
            } else {
                slotData.putInt("TagIndex", tagIndex + 1);
                return false;
            }
        }

        @Override
        public boolean canBeUsed(@NotNull Player player, int slotIndex) {
            return !Util.getMatchingTags(SaveHelper.getStackForSlot(player, slotIndex).getItem()).isEmpty();
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
    
    public abstract boolean isValid(@NotNull Player player, int slotIndex, ItemStack definition, ItemStack stack);
    
    @NotNull
    public ItemStack getRenderStack(@NotNull ItemStack origin, @NotNull Player player, @NotNull Slot slot){
        return origin;
    }
    
    public void addText(@NotNull Player player, @NotNull Slot slot, @NotNull NonNullList<Component> tooltip){
        tooltip.add(new TextComponent(this.name()));
    }

    public boolean shouldJumpToNextType(@NotNull Player player, int slotIndex){
        return true;
    }

    public boolean canBeUsed(@NotNull Player player, int slotIndex){
        return true;
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
