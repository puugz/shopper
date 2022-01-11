package me.puugz.shopper.listener;

import me.puugz.shopper.Shopper;
import me.puugz.shopper.model.Shop;
import me.puugz.shopper.util.InventoryUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author puugz
 * @since 08/01/2022 19:27
 */
public class ShopListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final BlockData data = event.getClickedBlock().getBlockData();
        final Player player = event.getPlayer();
        final Block block = event.getClickedBlock();
        final Shop shop = Shopper.getInstance()
                .getShopHandler()
                .getByLocation(block.getLocation());

        if (shop == null) return;

        if (data instanceof Chest) {
            if (!player.getUniqueId().equals(shop.getOwner()) && !player.hasPermission("shopper.bypass")) {
                player.sendMessage("§cYou can't open this chest!");
                event.setCancelled(true);
            }
        }

        if (data instanceof WallSign) {
            if (player.getUniqueId().equals(shop.getOwner())) {
                player.sendMessage("§cYou can't buy from yourself.");
            } else {
                final Material paymentType = shop.getPaymentItemType();
                final String paymentName = toNiceString(paymentType.name());
                final int paymentAmount = shop.getPaymentAmount();
                final String buy = toNiceString(shop.getSellItemType().name());
                final ItemStack paymentItem = new ItemStack(paymentType, paymentAmount);
                final ItemStack soldItem = new ItemStack(shop.getSellItemType(), shop.getSellAmount());
                final org.bukkit.block.Chest chest = shop.getChest();

                if (!InventoryUtil.hasItem(chest.getBlockInventory(), soldItem)) {
                    player.sendMessage("§cThis store is currently out of stock.");
                    return;
                }
                if (!InventoryUtil.hasItem(player.getInventory(), paymentItem)) {
                    player.sendMessage("§cYou must have §e" + paymentAmount + "x " + paymentName + " §cto buy §e" + buy + "§c.");
                    return;
                }
                if (!InventoryUtil.canAccept(player.getInventory(), soldItem)) {
                    player.sendMessage("§cYou don't have enough space in your inventory.");
                    return;
                }
                if (!InventoryUtil.canAccept(chest.getBlockInventory(), paymentItem)) {
                    player.sendMessage("§cThere is no space for the items in this chest.");
                    return;
                }

                final int sellingAmount = shop.getSellAmount();
                final String sellingName = toNiceString(shop.getSellItemType().name());

                chest.getBlockInventory().removeItem(soldItem);
                chest.getBlockInventory().addItem(paymentItem);
                player.getInventory().removeItem(paymentItem);
                player.getInventory().addItem(soldItem);
                player.updateInventory();

                final Player owner = shop.getPlayer();
                shop.updateStatus();

                if (owner != null) {
                    owner.sendMessage("§6[Shop] §c" + player.getDisplayName() + " §7has bought §c" + sellingAmount + "x " + sellingName + " §7for §c" + paymentAmount + "x " + paymentName + "§e.");
                }

                player.sendMessage("§aYou have bought §e" + sellingAmount + "x " + sellingName + " §afor §e" + paymentAmount + "x " + paymentName + "§a.");
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getType() != InventoryType.CHEST) return;

        final Location location = event.getInventory().getLocation();
        final Shop shop = Shopper.getInstance()
                .getShopHandler()
                .getByLocation(location);

        if (shop != null) {
            shop.updateStatus();
        }
    }

    // check for & hopper:
    // final Block block = location.getBlock();
    // final Directional directional = (Directional) block.getBlockData();
    // Arrays.asList(BlockFace.SOUTH, BlockFace.EAST, BlockFace.NORTH,
    //                    BlockFace.WEST, BlockFace.UP, BlockFace.DOWN);

    @EventHandler
    public void onExplosion(BlockExplodeEvent event) {
        final List<Block> toRemove = new ArrayList<>();

        for (Block block : event.blockList()) {
            final BlockData data = block.getBlockData();
            if (!(data instanceof Chest) && !(data instanceof WallSign)) continue;

            final Shop shop = Shopper.getInstance()
                    .getShopHandler()
                    .getByLocation(block.getLocation());

            if (shop != null) {
                toRemove.add(block);
            }
        }

        toRemove.forEach(block -> event.blockList().remove(block));
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event) {
        final List<Block> toRemove = new ArrayList<>();

        for (Block block : event.blockList()) {
            final BlockData data = block.getBlockData();
            if (!(data instanceof Chest) && !(data instanceof WallSign)) continue;

            final Shop shop = Shopper.getInstance()
                    .getShopHandler()
                    .getByLocation(block.getLocation());

            if (shop != null) {
                toRemove.add(block);
            }
        }

        toRemove.forEach(block -> event.blockList().remove(block));
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        final BlockData data = event.getBlock().getBlockData();
        if (!(data instanceof Chest) && !(data instanceof WallSign)) return;

        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final Shop shop = Shopper.getInstance()
                .getShopHandler()
                .getByLocation(block.getLocation());

        if (shop != null) {
            if (!player.getUniqueId().equals(shop.getOwner()) && !player.hasPermission("shopper.bypass")) {
                player.sendMessage("§cYou can't break this block!");
                event.setCancelled(true);
            } else {
                if (!player.isSneaking()) {
                    player.sendMessage("§cSneak-break this block to destroy the shop.");
                    event.setCancelled(true);
                } else {
                    shop.getItemDisplay().remove();
                    player.sendMessage("§eYou have destroyed the shop.");

                    Shopper.getInstance().getShopHandler().getShops().remove(shop);
                }
            }
        }
    }

//    @EventHandler
//    public void onPistonExtend(BlockPistonExtendEvent event) {
//        if (event.getBlock().getType() != Material.CHEST) return;
//
//        final Block block = event.getBlock();
//        final Shop shop = Shopper.getInstance().getShopHandler().getByChestLocation(block.getLocation());
//
//        if (shop != null) {
//            event.setCancelled(true);
//        }
//    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (!event.getLine(0).equalsIgnoreCase("[Shop]")) {
            return;
        }

        final Player player = event.getPlayer();
        final Sign sign = (Sign) event.getBlock().getState();

        Directional directional;
        try {
            directional = (Directional) sign.getBlock().getBlockData();
        } catch (ClassCastException e) {
            player.sendMessage("§cYou must place the sign on a chest.");
            return;
        }

        final Block chest = sign.getBlock().getRelative(directional.getFacing().getOppositeFace());

        if (!sign.getType().name().contains("WALL_SIGN") || chest.getType() != Material.CHEST) {
            player.sendMessage("§cYou must place the sign on a chest.");
            return;
        }

        final String[] sellingLine = event.getLine(1).split(" ");
        final String[] gettingLine = event.getLine(2).split(" ");

        int sellingAmount = 0;
        int gettingAmount = 0;

        try {
            sellingAmount = Integer.parseInt(sellingLine[0]);
            gettingAmount = Integer.parseInt(gettingLine[0]);
        } catch (NumberFormatException ignored) {}

        if (sellingAmount < 1 || gettingAmount < 1) {
            player.sendMessage("§cInvalid selling or getting amount entered.");
            player.sendMessage("§6Example:");
            player.sendMessage("§f[Shop]");
            player.sendMessage("§e1 DIAMOND_SWORD §7(The item you sell)");
            player.sendMessage("§25 EMERALD §7(The item you get)");
            return;
        }

        Material sellingItemType;
        Material gettingItemType;

        try {
            sellingItemType = Material.getMaterial(sellingLine[1].toUpperCase());
            gettingItemType = Material.getMaterial(gettingLine[1].toUpperCase());
        } catch (ArrayIndexOutOfBoundsException e) {
            player.sendMessage("§cYou must enter both item materials!");
            return;
        }

        if (sellingItemType == null || gettingItemType == null) {
            player.sendMessage("§cInvalid selling or getting item entered.");
            player.sendMessage("§6Example:");
            player.sendMessage("§f[Shop]");
            player.sendMessage("§e1 DIAMOND_SWORD §7(The item you sell)");
            player.sendMessage("§25 EMERALD §7(The item you get)");
            return;
        }

        if (sellingItemType == gettingItemType) {
            player.sendMessage("§cYou can't use the same items.");
            return;
        }

        final Shop shop = new Shop(
                UUID.randomUUID(),
                player.getUniqueId(),
                chest.getLocation(),
                sign.getLocation(),
                sellingItemType, sellingAmount,
                gettingItemType, gettingAmount
        );

        final String niceSellingItemName = toNiceString(sellingItemType.name());
        final String niceGettingItemName = toNiceString(gettingItemType.name());

        event.setLine(0, "§a[Shop]");
        event.setLine(1, sellingAmount + " " + niceSellingItemName);
        event.setLine(2, gettingAmount + " " + niceGettingItemName);
        event.setLine(3, shop.getStatus());

        Shopper.getInstance().getShopHandler().getShops().add(shop);

        player.sendMessage("§eCreated a new shop that will sell §c" + sellingAmount + "x " + niceSellingItemName + " §efor §c" + gettingAmount + "x " + niceGettingItemName + "§e.");
        player.sendMessage("§eYou can manage your shops with §c/shops§e.");
    }

    private String toNiceString(String input) {
        String output = input.toLowerCase().replace('_', ' ').trim();

        for (String s : output.split(" ")) {
            String c = String.valueOf(s.charAt(0));

            String replace = s.replaceFirst(c, c.toUpperCase());
            output = output.replace(s, replace);
        }

        return output;
    }
}
