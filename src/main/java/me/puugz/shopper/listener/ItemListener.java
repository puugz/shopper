package me.puugz.shopper.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;

/**
 * @author puugz
 * @since 09/01/2022 05:50
 */
public class ItemListener implements Listener {

    @EventHandler
    public void onInventoryPickupItem(InventoryPickupItemEvent event) {
        if (event.getItem().hasMetadata("unpickupable")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getItem().hasMetadata("unpickupable")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDespawn(ItemDespawnEvent event) {
        if (event.getEntity().hasMetadata("unpickupable")) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemMerge(ItemMergeEvent event) {
        if (event.getEntity().hasMetadata("unpickupable")) {
            event.setCancelled(true);
        }
    }
}
