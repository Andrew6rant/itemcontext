package io.github.andrew6rant.itemcontext.api;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

/**
 * Implement this on an Item to change its behavior with this mod's usage ticker.
 * Based on <a href="https://github.com/VazkiiMods/Quark/blob/master/src/main/java/vazkii/quark/api/IUsageTickerOverride.java">...</a>
 */
public interface IItemContextOverride {

    default int getUsageTickerCountForItem(ItemStack stack, Predicate<ItemStack> target) {
        return 0;
    }

    default boolean shouldUsageTickerCheckMatchSize(ItemStack stack) {
        return false;
    }

    ItemStack getUsageTickerItem(ItemStack stack);

}