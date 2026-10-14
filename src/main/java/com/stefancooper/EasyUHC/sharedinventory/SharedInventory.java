package com.stefancooper.EasyUHC.sharedinventory;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SharedInventory {

    private ItemStack[] storage = new ItemStack[36];
    private ItemStack[] armour = new ItemStack[4];
    private ItemStack offhand;

    void capture(PlayerInventory inventory) {
        storage = cloneItems(inventory.getStorageContents());
        armour = cloneItems(inventory.getArmorContents());
        offhand = cloneItem(inventory.getItemInOffHand());
    }

    void apply(PlayerInventory inventory) {
        inventory.setStorageContents(cloneItems(storage));
        inventory.setArmorContents(cloneItems(armour));
        inventory.setItemInOffHand(cloneItem(offhand));
    }

    private static ItemStack[] cloneItems(ItemStack[] items) {
        ItemStack[] copy = new ItemStack[items.length];

        for (int i = 0; i < items.length; i++) {
            copy[i] = cloneItem(items[i]);
        }

        return copy;
    }

    private static ItemStack cloneItem(ItemStack item) {
        return item == null ? null : item.clone();
    }
}