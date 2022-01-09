package me.puugz.shopper.command;

import me.puugz.shopper.Shopper;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author puugz
 * @since 09/01/2022 01:00
 */
public class DeleteShopsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        if (!sender.hasPermission("shopper.command.delete")) return true;

        final Player player = (Player) sender;

        Shopper.getInstance().getShopHandler().getShops().clear();
        player.sendMessage("§aRemoved all shops.");

        return true;
    }
}
