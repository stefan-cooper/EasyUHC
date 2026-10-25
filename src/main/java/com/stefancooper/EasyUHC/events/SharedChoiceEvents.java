package com.stefancooper.EasyUHC.events;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.base.ConfigKey;
import com.stefancooper.EasyUHC.base.UHCTeam;
import com.stefancooper.EasyUHC.base.Utils;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShield;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.stefancooper.EasyUHC.base.ConfigKey.ENABLE_EVOLVING_SHIELDS;
import static com.stefancooper.EasyUHC.base.ConfigKey.SHARED_TEAM_HEALTH;

public class SharedChoiceEvents implements Listener {

    private final Config config;

    public SharedChoiceEvents(Config config) {
        this.config = config;
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final ItemStack current = event.getCurrentItem();
        if (!(event.getWhoClicked() instanceof final Player player))  return;

        final Optional<UHCTeam> team = Utils.getTeamForPlayer(config, player);
        if (team.isEmpty() || !team.get().isDecidingSharedChoice(player)) return;

        // Did they click one of the shared choices?
        if (current != null && current.getItemMeta() != null && current.getItemMeta().getPersistentDataContainer().has(config.getManagedResources().getKeys().getSharedChoiceTypeKey())) {
            // they did, so cancel the event and count their vote
            event.setCancelled(true);
            final String choice = current.getItemMeta().getPersistentDataContainer().get(config.getManagedResources().getKeys().getSharedChoiceTypeKey(), PersistentDataType.STRING);
            final UHCTeam.Choice chosenChoice = UHCTeam.Choice.fromString(choice);
            if (chosenChoice != null) {
                team.get().voteInChoice(player, chosenChoice);
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;
        if (!(event.getPlayer() instanceof final Player player)) return;

        final Optional<UHCTeam> team = Utils.getTeamForPlayer(config, player);
        if (team.isPresent() && team.get().isDecidingSharedChoice(player)) {
            config.getManagedResources().runTaskLater(() -> team.get().reopenSharedChoice(player),  1L);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(final PlayerJoinEvent event) {
        if (!config.getPlugin().isUHCLive()) return;
        final boolean isEvolvingShieldEnabled = config.getProperty(ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS);
        final Player player = event.getPlayer();
        config.getManagedResources().runTaskLater(() -> {
            final Optional<UHCTeam> team = Utils.getTeamForPlayer(config, player);
            if (team.isEmpty()) return;
            if (team.get().isDecidingSharedChoice(player)) {
                 team.get().reopenSharedChoice(player);
            } else if (isEvolvingShieldEnabled && player.getGameMode() == GameMode.SURVIVAL) {
                final boolean hasEvolvingShield = EvolvingShield.getEvolvingShieldFromPlayer(config, player).isPresent();
                if (hasEvolvingShield) return;
                // if they join the server and dont have a shield, give them one
                if (!team.get().isSharedInventory()) {
                    EvolvingShield.createEvolvingShield(config, player);
                }
            }
        }, 1L);
    }
}
