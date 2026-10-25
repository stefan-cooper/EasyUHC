package com.stefancooper.EasyUHC.sharedinventory;

import com.stefancooper.EasyUHC.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public final class SharedInventoryManager {

    private final Config config;
    private final Map<String, SharedInventoryState> groups = new LinkedHashMap<>();
    private final Map<UUID, String> playerGroups = new HashMap<>();
    private final Set<UUID> applyingToPlayer = new HashSet<>();
    private final Set<UUID> inventoryInitialized = new HashSet<>();
    private final Set<String> pendingSyncs = new HashSet<>();
    private final Set<UUID> pendingCaptures = new LinkedHashSet<>();
    private boolean pendingCaptureTaskScheduled;
    private static final Pattern VALID_NAME = Pattern.compile("[a-zA-Z0-9_-]{1,32}");

    public SharedInventoryManager(final Config config) {
        this.config = config;
    }

    public boolean isShared(Player player) {
        return playerGroups.containsKey(player.getUniqueId());
    }

    public void resetToDefaults() {
        groups.clear();
        playerGroups.clear();
        applyingToPlayer.clear();
        inventoryInitialized.clear();
        pendingCaptures.clear();
        pendingSyncs.clear();
    }

    public String groupName(Player player) {
        return playerGroups.get(player.getUniqueId());
    }

    private boolean isApplying(Player player) {
        return applyingToPlayer.contains(player.getUniqueId());
    }

    public Set<String> groupNames() {
        return Collections.unmodifiableSet(groups.keySet());
    }

    /** Creates a new group from the sender's current slots. */
    public boolean createGroup(Player player, String requestedName) {
        requirePrimaryThread();
        flushPendingCaptures();
        String name = normalizeName(requestedName);
        if (name == null || groups.containsKey(name) || isShared(player)) {
            return false;
        }

        SharedInventoryState state = new SharedInventoryState(name);
        state.capture(player.getInventory());
        transferCraftingMatrixToGroup(player, state);
        state.members.add(player.getUniqueId());
        groups.put(name, state);
        playerGroups.put(player.getUniqueId(), name);

        ItemStack cursor = player.getItemOnCursor();
        player.setItemOnCursor(null);
        dropRemainder(player.getLocation(), state.insert(cursor));
        applyToPlayer(player, state);
        scheduleSync(name);
        inventoryInitialized.add(player.getUniqueId());
        return true;
    }

//    public UUID getGroupId(Player player) {
//        return playerGroups.get(player.getUniqueId());
//    }
//
//    public void createGroup(final List<Player> groupPlayers) {
//        final List<Player> players = new ArrayList<>(groupPlayers); // clone so we dont affect the original array
//        if (players.isEmpty()) {
//            throw new IllegalArgumentException("Not enough players");
//        }
//        createGroup(players.getFirst(), UUID.randomUUID().toString());
//        final UUID groupId = getGroupId(players.getFirst());
//        players.removeFirst();
//        players.forEach(player -> addPlayer(player, groupId));
//    }

    /** Adds an ungrouped player, merging their old items into the shared pool without duplicating them. */
    public boolean joinGroup(Player player, String requestedName) {
        requirePrimaryThread();
        flushPendingCaptures();
        String name = normalizeName(requestedName);
        SharedInventoryState state = name == null ? null : groups.get(name);
        if (state == null || isShared(player)) {
            return false;
        }

        // An existing, ungrouped inventory is contributed to the group's pool.
        ItemStack[] incomingStorage = SharedInventoryState.cloneItems(player.getInventory().getStorageContents());
        ItemStack[] incomingArmour = SharedInventoryState.cloneItems(player.getInventory().getArmorContents());
        ItemStack incomingOffhand = SharedInventoryState.cloneItem(player.getInventory().getItemInOffHand());
        ItemStack cursor = SharedInventoryState.cloneItem(player.getItemOnCursor());
        ItemStack[] craftingItems = captureAndClearCraftingMatrix(player);

        for (ItemStack item : incomingStorage) {
            if (!isEmpty(item)) {
                dropRemainder(player.getLocation(), state.insert(item));
            }
        }

        ItemStack[] targetArmour = state.armourCopy();
        for (int i = 0; i < incomingArmour.length; i++) {
            ItemStack item = incomingArmour[i];
            if (isEmpty(item)) {
                continue;
            }
            if (isEmpty(targetArmour[i])) {
                targetArmour[i] = item.clone();
            } else {
                dropRemainder(player.getLocation(), state.insert(item));
            }
        }
        // Store merged armour back through a short-lived inventory-independent copy operation.
        state.setArmour(targetArmour);

        if (!isEmpty(incomingOffhand)) {
            if (isEmpty(state.offhandCopy())) {
                state.setOffhand(incomingOffhand);
            } else {
                dropRemainder(player.getLocation(), state.insert(incomingOffhand));
            }
        }
        dropRemainder(player.getLocation(), state.insert(cursor));
        player.setItemOnCursor(null);
        for (ItemStack item : craftingItems) {
            dropRemainder(player.getLocation(), state.insert(item));
        }

        state.members.add(player.getUniqueId());
        playerGroups.put(player.getUniqueId(), name);
        applyToPlayer(player, state);
        scheduleSync(name);
        inventoryInitialized.add(player.getUniqueId());
        return true;
    }

    /** Leaves the player with an empty inventory so shared items cannot be duplicated into a private inventory. */
    public boolean leaveGroup(Player player) {
        requirePrimaryThread();
        for (String changedGroup : flushPendingCaptures()) {
            scheduleSync(changedGroup);
        }
        String name = playerGroups.remove(player.getUniqueId());
        if (name == null) {
            return false;
        }

        SharedInventoryState state = groups.get(name);
        ItemStack cursor = SharedInventoryState.cloneItem(player.getItemOnCursor());
        ItemStack[] craftingItems = state == null ? new ItemStack[0] : captureAndClearCraftingMatrix(player);
        player.setItemOnCursor(null);
        if (state != null) {
            dropRemainder(player.getLocation(), state.insert(cursor));
            for (ItemStack item : craftingItems) {
                dropRemainder(player.getLocation(), state.insert(item));
            }
            state.members.remove(player.getUniqueId());
            clearPlayerSharedSlots(player);
            scheduleSync(name);
        } else {
            clearPlayerSharedSlots(player);
        }
        inventoryInitialized.remove(player.getUniqueId());
        return true;
    }

    /** Before a vanilla action begins, restore this player's shared slots to the canonical state. */
    public void prepareForAction(Player player) {
        requirePrimaryThread();
        for (String changedGroup : flushPendingCaptures()) {
            scheduleSync(changedGroup);
        }
        String name = playerGroups.get(player.getUniqueId());
        SharedInventoryState state = name == null ? null : groups.get(name);
        if (state != null && !isApplying(player)) {
            applyToPlayer(player, state);
        }
    }

    /** Called by Paper's slot-change event. Capture immediately so a second member's action sees the new state. */
    public void onSlotChanged(Player player) {
        requirePrimaryThread();
        if (isApplying(player) || !inventoryInitialized.contains(player.getUniqueId())) {
            // Ignore vanilla inventory loading and slot changes until this player has received the
            // canonical state. Otherwise a reconnecting player can overwrite newer group contents.
            return;
        }
        String name = playerGroups.get(player.getUniqueId());
        SharedInventoryState state = name == null ? null : groups.get(name);
        if (state == null) {
            return;
        }

        state.capture(player.getInventory());
        scheduleSync(name);
    }

    /** Return a carried cursor stack to the group on disconnect; it is not a shared inventory slot. */
    public void handleQuit(Player player) {
        requirePrimaryThread();
        UUID playerId = player.getUniqueId();

        // A pending full-inventory snapshot from the quitter must never overwrite the group's latest
        // canonical state. Slot changes are captured immediately; other mutations should already have
        // been flushed by their post-event task before a normal disconnect.
        inventoryInitialized.remove(playerId);
        pendingCaptures.remove(playerId);

        Set<String> changedGroups = flushPendingCaptures();
        for (String changedGroup : changedGroups) {
            syncGroupNow(changedGroup);
        }

        String name = playerGroups.get(playerId);
        SharedInventoryState state = name == null ? null : groups.get(name);
        if (state == null) {
            return;
        }

        // Cursor/crafting slots are not part of the shared player inventory; return them to the group.
        ItemStack cursor = SharedInventoryState.cloneItem(player.getItemOnCursor());
        ItemStack[] craftingItems = captureAndClearCraftingMatrix(player);
        player.setItemOnCursor(null);
        dropRemainder(player.getLocation(), state.insert(cursor));
        for (ItemStack item : craftingItems) {
            dropRemainder(player.getLocation(), state.insert(item));
        }
        scheduleSync(name);
    }

    public void applyAfterJoin(Player player) {
        applyCanonicalAfterJoinOrRespawn(player);
    }

    public void applyAfterRespawn(Player player) {
        applyCanonicalAfterJoinOrRespawn(player);
    }

    private void applyCanonicalAfterJoinOrRespawn(Player player) {
        UUID playerId = player.getUniqueId();

        // Ignore slot-change and post-event snapshots while the player's vanilla inventory is loading.
        inventoryInitialized.remove(playerId);
        pendingCaptures.remove(playerId);

        config.getManagedResources().runTask(() -> {
            if (!player.isOnline()) {
                return;
            }

            // Flush other ready members' legitimate pending actions, never this player's stale snapshot.
            pendingCaptures.remove(playerId);
            Set<String> changedGroups = flushPendingCaptures();
            for (String changedGroup : changedGroups) {
                syncGroupNow(changedGroup);
            }

            String name = playerGroups.get(playerId);
            SharedInventoryState state = name == null ? null : groups.get(name);
            if (state != null) {
                applyToPlayer(player, state);
            }

            // Only now may slot-change/post-event events from this player update canonical state.
            inventoryInitialized.add(playerId);
        });
    }

    public void captureAndScheduleSync(Player player) {
        onSlotChanged(player);
    }

    /**
     * Queue a post-event inventory snapshot. Many Bukkit events fire before vanilla applies the mutation,
     * so the snapshot must be captured on the next tick, or flushed before another member's action.
     */
    public void markPendingCapture(Player player) {
        requirePrimaryThread();
        if (!isShared(player) || !inventoryInitialized.contains(player.getUniqueId())) {
            return;
        }
        pendingCaptures.add(player.getUniqueId());
        if (pendingCaptureTaskScheduled) {
            return;
        }
        pendingCaptureTaskScheduled = true;
        config.getManagedResources().runTask(() -> {
            pendingCaptureTaskScheduled = false;
            Set<String> changedGroups = flushPendingCaptures();
            for (String groupName : changedGroups) {
                syncGroupNow(groupName);
            }
            // A future event may have queued more captures while this task was pending.
            if (!pendingCaptures.isEmpty()) {
                schedulePendingCaptureTask();
            }
        });
    }

    private void schedulePendingCaptureTask() {
        if (pendingCaptureTaskScheduled) {
            return;
        }
        pendingCaptureTaskScheduled = true;
        config.getManagedResources().runTask(() -> {
            pendingCaptureTaskScheduled = false;
            Set<String> changedGroups = flushPendingCaptures();
            for (String groupName : changedGroups) {
                syncGroupNow(groupName);
            }
        });
    }

    private Set<String> flushPendingCaptures() {
        Set<String> changedGroups = new LinkedHashSet<>();
        if (pendingCaptures.isEmpty()) {
            return changedGroups;
        }
        List<UUID> pending = new ArrayList<>(pendingCaptures);
        pendingCaptures.clear();
        for (UUID id : pending) {
            Player player = Bukkit.getPlayer(id);
            if (player == null || !player.isOnline() || isApplying(player)) {
                continue;
            }
            String name = playerGroups.get(id);
            SharedInventoryState state = name == null ? null : groups.get(name);
            if (state != null) {
                state.capture(player.getInventory());
                changedGroups.add(name);
            }
        }
        return changedGroups;
    }

    private void transferCraftingMatrixToGroup(Player player, SharedInventoryState state) {
        for (ItemStack item : captureAndClearCraftingMatrix(player)) {
            dropRemainder(player.getLocation(), state.insert(item));
        }
    }

    /** The personal 2x2 crafting matrix is transient UI state, so move its inputs back into the group. */
    private ItemStack[] captureAndClearCraftingMatrix(Player player) {
        if (!(player.getOpenInventory().getTopInventory() instanceof CraftingInventory crafting)) {
            return new ItemStack[0];
        }
        ItemStack[] matrix = SharedInventoryState.cloneItems(crafting.getMatrix());
        crafting.setMatrix(new ItemStack[4]);
        return matrix;
    }

    private void applyToPlayer(Player player, SharedInventoryState state) {
        UUID id = player.getUniqueId();
        applyingToPlayer.add(id);
        try {
            state.apply(player.getInventory());
        } finally {
            applyingToPlayer.remove(id);
        }
    }

    private void clearPlayerSharedSlots(Player player) {
        UUID id = player.getUniqueId();
        applyingToPlayer.add(id);
        try {
            player.getInventory().setStorageContents(new ItemStack[36]);
            player.getInventory().setArmorContents(new ItemStack[4]);
            player.getInventory().setItemInOffHand(null);
        } finally {
            applyingToPlayer.remove(id);
        }
    }

    private void scheduleSync(String name) {
        if (!pendingSyncs.add(name)) {
            return;
        }
        config.getManagedResources().runTask(() -> {
            pendingSyncs.remove(name);
            Set<String> changedGroups = flushPendingCaptures();
            changedGroups.add(name);
            for (String groupName : changedGroups) {
                syncGroupNow(groupName);
            }
        });
    }

    private void syncGroupNow(String name) {
        SharedInventoryState state = groups.get(name);
        if (state == null) {
            return;
        }
        for (UUID memberId : List.copyOf(state.members)) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline() && inventoryInitialized.contains(memberId)) {
                applyToPlayer(member, state);
            }
        }
    }

    private void dropRemainder(Location location, ItemStack remainder) {
        if (!isEmpty(remainder) && location.getWorld() != null) {
            location.getWorld().dropItemNaturally(location, remainder);
        }
    }

    private static boolean isEmpty(ItemStack item) {
        return item == null || item.getType().isAir() || item.getAmount() <= 0;
    }

    private static String normalizeName(String requestedName) {
        if (requestedName == null || !VALID_NAME.matcher(requestedName).matches()) {
            return null;
        }
        return requestedName.toLowerCase(Locale.ROOT);
    }

    private static void requirePrimaryThread() {
        if (!Bukkit.isPrimaryThread()) {
            throw new IllegalStateException("Shared inventory state must only be accessed on the server thread.");
        }
    }

}