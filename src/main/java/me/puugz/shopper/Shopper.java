package me.puugz.shopper;

import lombok.Getter;
import me.puugz.shopper.command.DeleteShopsCommand;
import me.puugz.shopper.handler.ShopHandler;
import me.puugz.shopper.listener.ItemListener;
import me.puugz.shopper.listener.ShopListener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Shopper extends JavaPlugin {

    @Getter
    private static Shopper instance;

    private ShopHandler shopHandler;

    @Override
    public void onEnable() {
        instance = this;

        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();

        this.shopHandler = new ShopHandler();
        this.shopHandler.loadFromConfig(this.getConfig());

        this.getCommand("deleteshops").setExecutor(new DeleteShopsCommand());

        this.getServer().getPluginManager().registerEvents(new ShopListener(), this);
        this.getServer().getPluginManager().registerEvents(new ItemListener(), this);
    }

    @Override
    public void onDisable() {
        if (this.shopHandler != null) {
            this.shopHandler.saveToConfig();
        }
    }
}
