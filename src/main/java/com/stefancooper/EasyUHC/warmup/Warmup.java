package com.stefancooper.EasyUHC.warmup;

import com.stefancooper.EasyUHC.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public class Warmup {



    final int PLAYER_1_X_SPAWN;
    final int PLAYER_1_Y_SPAWN;
    final int PLAYER_1_Z_SPAWN;
    final int PLAYER_2_X_SPAWN;
    final int PLAYER_2_Y_SPAWN;
    final int PLAYER_2_Z_SPAWN;
    final Player player1;
    final Player player2;
    final Config config;


    public Warmup (final Config config, final Player player1, final Player player2) {
        // TODO - get from config
        PLAYER_1_X_SPAWN = 1000;
        PLAYER_1_Y_SPAWN = 100;
        PLAYER_1_Z_SPAWN = 1000;
        PLAYER_2_X_SPAWN = 1005;
        PLAYER_2_Y_SPAWN = 100;
        PLAYER_2_Z_SPAWN = 1000;
        this.player1 = player1;
        this.player2 = player2;
        this.config = config;
    }

    public void trigger() {
        // Clear all items on the floor
        config.getWorlds().getOverworld().getEntities().stream().filter(entity -> entity.getType().equals(EntityType.ITEM)).forEach(Entity::remove);
        // TODO - persist warmupbutton after world save
        // Tell the players who they're about to fight
        player1.sendMessage(String.format("Get ready, you're about to face %s!", player2.getName()));
        player2.sendMessage(String.format("Get ready, you're about to face %s!", player1.getName()));
        // in three seconds, start the battle!
        config.getManagedResources().runTaskLater(() -> {
            player1.getInventory().clear();
            player1.getEquipment().clear();
            player1.setHealth(player1.getAttribute(Attribute.MAX_HEALTH).getDefaultValue());
            player2.getInventory().clear();
            player2.getEquipment().clear();
            player2.setHealth(player2.getAttribute(Attribute.MAX_HEALTH).getDefaultValue());

            player1.teleport(new Location(config.getWorlds().getOverworld(), PLAYER_1_X_SPAWN, PLAYER_1_Y_SPAWN, PLAYER_1_Z_SPAWN));
            player1.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
            player1.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            player1.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            player1.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
            player1.getEquipment().setItemInOffHand(new ItemStack(Material.SHIELD));
            player1.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            player1.give(new ItemStack(Material.BOW));
            player1.give(new ItemStack(Material.IRON_AXE));
            player1.give(new ItemStack(Material.ARROW, 16));

            player2.teleport(new Location(config.getWorlds().getOverworld(), PLAYER_2_X_SPAWN, PLAYER_2_Y_SPAWN, PLAYER_2_Z_SPAWN));
            player2.getEquipment().setBoots(new ItemStack(Material.IRON_BOOTS));
            player2.getEquipment().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
            player2.getEquipment().setChestplate(new ItemStack(Material.IRON_CHESTPLATE));
            player2.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
            player2.getEquipment().setItemInOffHand(new ItemStack(Material.SHIELD));
            player2.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
            player2.give(new ItemStack(Material.BOW));
            player2.give(new ItemStack(Material.IRON_AXE));
            player2.give(new ItemStack(Material.ARROW, 16));
        }, 3);

    }

    public static boolean isWarmupButton(final Config config, final ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        final NamespacedKey key =
                config.getManagedResources().getKeys().getWarmupButtonKey();

        return Boolean.TRUE.equals(
                item.getItemMeta()
                        .getPersistentDataContainer()
                        .get(key, PersistentDataType.BOOLEAN)
        );
    }


    // TODO
    // - Events file
    //      - track clicks of a specific button
    //      - track deaths to start next warmup match
    //          - on death, regen winner and teleport next in queue in 
    // - givecommand file
    //      - give warmup button
    // - this file
    //      - warmup start
    //          - teleport players to correct places
    //          - give sword, axe, shield, bow, arrows
    //
    // INVESTIGATE
    // - pvp methods
    //      - does putting the 'arena' players into their own special team and turning friendly fire in that team mean that they can fight each other?
    //      - or something like this:
}
