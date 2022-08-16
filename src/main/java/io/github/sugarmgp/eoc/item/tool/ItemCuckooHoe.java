package io.github.sugarmgp.eoc.item.tool;

import io.github.sugarmgp.eoc.EOC;
import io.github.sugarmgp.eoc.util.ModItemTier;
import net.minecraft.item.HoeItem;

public class ItemCuckooHoe extends HoeItem implements ICuckooTool {
    public ItemCuckooHoe() {
        super(
                ModItemTier.CUCKOO,
                -3,
                0.0F,
                new Properties().group(EOC.ITEMGROUP)
        );
    }
}