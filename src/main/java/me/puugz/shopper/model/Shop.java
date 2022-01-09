package me.puugz.shopper.model;

import lombok.Getter;
import lombok.Setter;
import me.puugz.shopper.Shopper;
import me.puugz.shopper.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.UUID;

/**
 * @author puugz
 * @since 08/01/2022 19:35
 */
@Getter
@Setter
public class Shop {

    public Shop(
            UUID id, UUID owner,
            Location chestLocation, Location signLocation,
            Material sellingItemType, int sellingAmount,
            Material paymentItemType, int gettingAmount
    ) {
        this.id = id;
        this.owner = owner;
        this.chestLocation = chestLocation;
        this.signLocation = signLocation;
        this.sellItemType = sellingItemType;
        this.sellAmount = sellingAmount;
        this.paymentItemType = paymentItemType;
        this.paymentAmount = gettingAmount;

        final Location itemLocation = this.chestLocation.clone().add(0.5, 0.875, 0.5);

        this.itemDisplay = itemLocation.getWorld().dropItem(itemLocation, new ItemStack(sellingItemType));
        this.itemDisplay.setVelocity(new Vector(0, 0, 0));
        this.itemDisplay.setMetadata("unpickupable", new FixedMetadataValue(Shopper.getInstance(), true));
    }

    private final UUID id;

    private UUID owner;
    private Location chestLocation;
    private Location signLocation;
    private boolean closed;
    private Item itemDisplay;

    /**
     * The item type you sell
     */
    private Material sellItemType;
    private int sellAmount;

    /**
     * The item type the owner will get
     */
    private Material paymentItemType;
    private int paymentAmount;

    public void updateStatus() {
        final Sign sign = getSign();

        if (sign == null) return;

        sign.setLine(3, getStatus());
        sign.update(true);
    }

    public String getStatus() {
        final Chest chest = getChest();

        String status = "§cError.";

        if (chest != null) {
            if (this.closed) {
                status = "§cClosed.";
            } else {
                if (!chest.getBlockInventory().contains(this.sellItemType, this.sellAmount)) {
                    status = "§cOut of stock.";
                } else {
                    status = "§aIn stock.";
                }
                if (!InventoryUtil.canAccept(chest.getBlockInventory(), new ItemStack(this.paymentItemType, this.paymentAmount))) {
                    status = "§cNo space.";
                }
            }
        }

        return status;
    }

    public Chest getChest() {
        if (chestLocation.getBlock().getType() != Material.CHEST) {
            return null;
        }
        return (Chest) chestLocation.getBlock().getState();
    }

    public Sign getSign() {
        if (!signLocation.getBlock().getType().name().contains("WALL_SIGN")) {
            return null;
        }
        return (Sign) signLocation.getBlock().getState();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.owner);
    }
}
