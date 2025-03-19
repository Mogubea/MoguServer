package me.mogubea.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

public class CustomSkulls {

    public static final ItemStack PLAYTIME_GIFT = getSkullWithCustomSkin("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZlMTJmZGFlMWZjZWJhNjg3OWY2NTk3OTYxMzJhN2ZmYTA4Y2Q5MmEyNmNiN2ExMDY3ZDQ5NzAzZDdiMWI0YiJ9fX0=");

    public static ItemStack getSkullWithCustomSkin(String base64) {
        ItemStack i = new ItemStack(Material.PLAYER_HEAD,1);
        SkullMeta meta = (SkullMeta) i.getItemMeta();
        PlayerProfile ack = Bukkit.createProfile(UUID.randomUUID());
        ack.getProperties().add(new ProfileProperty("textures", base64));
        meta.setPlayerProfile(ack);
        i.setItemMeta(meta);
        return i;
    }

}
