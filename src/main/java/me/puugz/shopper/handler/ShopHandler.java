package me.puugz.shopper.handler;

import lombok.Getter;
import me.puugz.shopper.Shopper;
import me.puugz.shopper.model.Shop;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author puugz
 * @since 08/01/2022 19:36
 */
@Getter
public class ShopHandler {

    private final Set<Shop> shops = new HashSet<>();

    public void loadFromConfig(FileConfiguration config) {
        final ConfigurationSection section = config.getConfigurationSection("shops");

        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            final ConfigurationSection shopSection = section.getConfigurationSection(key);

            try {
                final Shop shop = new Shop(
                        UUID.fromString(key),
                        UUID.fromString(shopSection.getString("owner")),
                        stringToLocation(shopSection.getString("chestLocation")),
                        stringToLocation(shopSection.getString("signLocation")),
                        Material.getMaterial(shopSection.getString("sellItemType")),
                        shopSection.getInt("sellAmount"),
                        Material.getMaterial(shopSection.getString("paymentItemType")),
                        shopSection.getInt("paymentAmount")
                );

                this.shops.add(shop);
            } catch (Exception e) {
                e.printStackTrace();
                Shopper.getInstance().getLogger().warning("Couldn't load shop with id " + key + ".");
            }
        }

        Shopper.getInstance().getLogger().info("Loaded " + shops.size() + " shop" + (shops.size() == 1 ? "" : "s") + " from config.yml!");
    }

    public void saveToConfig() {
        final FileConfiguration config = Shopper.getInstance().getConfig();

        for (Shop shop : shops) {
            final String key = "shops." + shop.getId();
            config.set(key, null);
            shop.getItemDisplay().remove();

            ConfigurationSection section = config.getConfigurationSection(key);

            if (section == null) {
                section = config.createSection(key);
            }

            section.set("owner", shop.getOwner().toString());
            section.set("chestLocation", locationToString(shop.getChestLocation()));
            section.set("signLocation", locationToString(shop.getSignLocation()));
            section.set("sellItemType", shop.getSellItemType().name());
            section.set("sellAmount", shop.getSellAmount());
            section.set("paymentItemType", shop.getPaymentItemType().name());
            section.set("paymentAmount", shop.getPaymentAmount());
        }

        Shopper.getInstance().saveConfig();
        Shopper.getInstance().getLogger().info("Saved " + this.shops.size() + " shops to config.yml!");
    }

    private Location stringToLocation(String string) {
        final String[] split = string.split(", ");
        Location location;

        try {
            final double x = Double.parseDouble(split[0]);
            final double y = Double.parseDouble(split[1]);
            final double z = Double.parseDouble(split[2]);

            location = new Location(Bukkit.getWorld(split[3]), x, y, z);
        } catch (Exception e) {
            return null;
        }

        return location;
    }

    private String locationToString(Location location) {
        return location.getX() + ", " + location.getY() + ", " + location.getZ() + ", " + location.getWorld().getName();
    }

    public Shop getByLocation(Location location) {
        return this.shops.stream()
                .filter(shop -> shop.getChestLocation().equals(location) || shop.getSignLocation().equals(location))
                .findFirst().orElse(null);
    }
}
