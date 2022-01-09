package me.puugz.shopper.util;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author puugz
 * @since 09/01/2022 04:36
 */
@UtilityClass
public class InventoryUtil {

    public static boolean hasItem(Inventory inventory, ItemStack item) {
        boolean hasItem = false;
        int amount = 0;

        for (ItemStack stack : inventory.getContents()) {
            if (stack == null || stack.getType() != item.getType()) continue;
            amount += stack.getAmount();
            if (amount >= item.getAmount()) {
                hasItem = true;
                break;
            }
        }

        return hasItem;
    }

    public static boolean canAccept(Inventory inventory, ItemStack item) {
        boolean canAccept = false;
        int amount = 0;

        if (inventory.firstEmpty() != -1) {
            canAccept = true;
        } else {
            for (ItemStack stack : inventory.getContents()) {
                if (stack == null || stack.getType() != item.getType()) continue;
                amount += stack.getAmount();
                if (amount + item.getAmount() <= stack.getMaxStackSize()) {
                    canAccept = true;
                    break;
                }
            }
        }

        return canAccept;
    }

}
