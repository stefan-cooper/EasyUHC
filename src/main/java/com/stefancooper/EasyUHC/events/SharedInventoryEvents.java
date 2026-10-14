package com.stefancooper.EasyUHC.events;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.base.UHCTeam;
import com.stefancooper.EasyUHC.base.Utils;
import com.stefancooper.EasyUHC.sharedinventory.SharedInventoryManager;
import io.papermc.paper.event.player.PlayerInventorySlotChangeEvent;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;
import java.util.Optional;

public class SharedInventoryEvents implements Listener {
    private final Config config;
    private final SharedInventoryManager manager;

    public SharedInventoryEvents(Config config) {
        this.config = config;
        this.manager = config.getManagedResources().getSharedInventoryManager();
    }

    private boolean shouldAction(final Player player) {
        if (!config.getPlugin().isUHCLive()) return false;
        if (player.getGameMode() != GameMode.SURVIVAL) return false;
        final Optional<UHCTeam> team = Utils.getTeamForPlayer(config, player);
        return team.isPresent() && team.get().isSharedInventory();
    }

    /** Paper-specific, immediate notification for real slot changes (counts, items and item meta/durability). */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onSlotChange(PlayerInventorySlotChangeEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.onSlotChanged(event.getPlayer());
        }
    }

//    /** Refresh the acting player's slots before vanilla processes a click against them. */
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onInventoryClick(InventoryClickEvent event) {
//        if (event.getWhoClicked() instanceof Player player && shouldAction(player)) {
//            manager.prepareForAction(player);
//        }
//    }
//
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onInventoryDrag(InventoryDragEvent event) {
//        if (event.getWhoClicked() instanceof Player player && shouldAction(player)) {
//            manager.prepareForAction(player);
//        }
//    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player && shouldAction(player)) {
            manager.prepareForAction(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player && shouldAction(player)) {
            manager.markPendingCapture(player);
        }
    }

//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onDrop(PlayerDropItemEvent event) {
//        if (shouldAction(event.getPlayer())) {
//            manager.prepareForAction(event.getPlayer());
//        }
//    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && shouldAction(player)) {
            manager.prepareForAction(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.prepareForAction(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.prepareForAction(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.prepareForAction(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.prepareForAction(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.prepareForAction(event.getPlayer());
        }
    }

    /** Keep current inventory canonical before combat changes weapon or armour durability. */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player victim && shouldAction(victim)) {
            manager.prepareForAction(victim);
        }

        if (event instanceof EntityDamageByEntityEvent byEntity ) {
            Player attacker = playerDamager(byEntity);
            if (attacker != null && attacker != event.getEntity() && shouldAction(attacker)) {
                manager.prepareForAction(attacker);
            }
        }
    }

//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onShootBow(EntityShootBowEvent event) {
//        if (event.getEntity() instanceof Player player && shouldAction(player)) {
//            manager.prepareForAction(player);
//        }
//    }


    /*
     * These MONITOR handlers queue a next-tick snapshot. Bukkit often calls an event before the actual
     * vanilla mutation is applied, so copying the inventory inside these handlers would be too early.
     * The manager also flushes queued snapshots before the next member performs an action.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void afterInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player && shouldAction(player)) {
            manager.markPendingCapture(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player && shouldAction(player)) {
            manager.markPendingCapture(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterDrop(PlayerDropItemEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.markPendingCapture(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player && shouldAction(player)) {
            manager.markPendingCapture(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterSwapHands(PlayerSwapHandItemsEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.markPendingCapture(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterInteract(PlayerInteractEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.markPendingCapture(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterConsume(PlayerItemConsumeEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.markPendingCapture(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterItemDamage(PlayerItemDamageEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.markPendingCapture(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterItemBreak(PlayerItemBreakEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.markPendingCapture(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterItemMend(PlayerItemMendEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.markPendingCapture(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterBlockBreak(BlockBreakEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.markPendingCapture(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterBlockPlace(BlockPlaceEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.markPendingCapture(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player victim && shouldAction(victim)) {
            manager.markPendingCapture(victim);
        }
        if (event instanceof EntityDamageByEntityEvent byEntity) {
            Player attacker = playerDamager(byEntity);
            if (attacker != null && shouldAction(attacker)) {
                manager.markPendingCapture(attacker);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void afterShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player player && shouldAction(player)) {
            manager.markPendingCapture(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!shouldAction(player) || !manager.isShared(player)) {
            return;
        }

        final Optional<UHCTeam> team = Utils.getTeamForPlayer(config, player);
        if (team.isEmpty()) {
            return;
        }
        final List<Player> aliveTeammates = team.get().getPlayers().stream().filter(p -> !p.equals(player) && p.getGameMode() != GameMode.SPECTATOR && !p.isDead()).toList();
        if (!aliveTeammates.isEmpty()) {
            event.getDrops().clear();
        }

        manager.prepareForAction(player);
    }

//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onRespawn(PlayerRespawnEvent event) {
//        manager.applyAfterRespawn(event.getPlayer());
//    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.applyAfterJoin(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        if (shouldAction(event.getPlayer())) {
            manager.handleQuit(event.getPlayer());
        }
    }

    private static Player playerDamager(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }
        return null;
    }
}
