package com.stefancooper.EasyUHC.sharedinventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class SharedInventoryState {
    static final int STORAGE_SIZE = 36;
    static final int ARMOUR_SIZE = 4;

    final String name;
    final Set<UUID> members = new LinkedHashSet<>();

    private ItemStack[] storage = new ItemStack[STORAGE_SIZE];
    private ItemStack[] armour = new ItemStack[ARMOUR_SIZE]; // boots, leggings, chestplate, helmet
    private ItemStack offhand;

    SharedInventoryState(String name) {
        this.name = name;
    }

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

    void clear() {
        storage = new ItemStack[STORAGE_SIZE];
        armour = new ItemStack[ARMOUR_SIZE];
        offhand = null;
    }

    ItemStack[] storageCopy() {
        return cloneItems(storage);
    }

    ItemStack[] armourCopy() {
        return cloneItems(armour);
    }

    ItemStack offhandCopy() {
        return cloneItem(offhand);
    }

    void setStorageSlot(int slot, ItemStack item) {
        if (slot >= 0 && slot < storage.length) {
            storage[slot] = cloneItem(item);
        }
    }

    void setArmourSlot(int slot, ItemStack item) {
        if (slot >= 0 && slot < armour.length) {
            armour[slot] = cloneItem(item);
        }
    }

    void setArmour(ItemStack[] items) {
        armour = cloneItems(items);
    }

    void setOffhand(ItemStack item) {
        offhand = cloneItem(item);
    }

    /** Inserts an item into shared storage, merging matching stacks first. Returns any remainder. */
    ItemStack insert(ItemStack input) {
        ItemStack remainder = cloneItem(input);
        if (isEmpty(remainder)) {
            return null;
        }

        int max = Math.max(1, remainder.getMaxStackSize());

        for (int i = 0; i < storage.length && !isEmpty(remainder); i++) {
            ItemStack existing = storage[i];
            if (isEmpty(existing) || !existing.isSimilar(remainder)) {
                continue;
            }

            int room = Math.max(0, Math.min(max, existing.getMaxStackSize()) - existing.getAmount());
            int moved = Math.min(room, remainder.getAmount());
            if (moved > 0) {
                existing.setAmount(existing.getAmount() + moved);
                remainder.setAmount(remainder.getAmount() - moved);
            }
        }

        for (int i = 0; i < storage.length && !isEmpty(remainder); i++) {
            if (!isEmpty(storage[i])) {
                continue;
            }

            int moved = Math.min(max, remainder.getAmount());
            ItemStack placed = remainder.clone();
            placed.setAmount(moved);
            storage[i] = placed;
            remainder.setAmount(remainder.getAmount() - moved);
        }

        return isEmpty(remainder) ? null : remainder;
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir() || item.getAmount() <= 0;
    }

    static ItemStack cloneItem(ItemStack item) {
        return isEmpty(item) ? null : item.clone();
    }

    static ItemStack[] cloneItems(ItemStack[] items) {
        if (items == null) {
            return new ItemStack[0];
        }
        return Arrays.stream(items).map(SharedInventoryState::cloneItem)
                .toArray(ItemStack[]::new);
    }
}
