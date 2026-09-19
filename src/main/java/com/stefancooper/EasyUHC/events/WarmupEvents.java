package com.stefancooper.EasyUHC.events;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.base.Utils;
import com.stefancooper.EasyUHC.warmup.Warmup;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import static com.stefancooper.EasyUHC.warmup.Warmup.isWarmupButton;

public class WarmupEvents  implements Listener {
    private final Config config;
    private final LinkedHashSet<Player> queue = new LinkedHashSet<>();
    private final Set<Location> warmupButtons = new HashSet<>();

    private Player activePlayer1;
    private Player activePlayer2;
    private BukkitTask battleTimer;

    public WarmupEvents (Config config) {
        this.config = config;
    }

    private boolean isPvpPair(final Player attacker, final Player victim) {
        if (activePlayer1 == null || activePlayer2 == null) {
            return false;
        } else {
            return attacker.getUniqueId().equals(activePlayer1.getUniqueId()) && victim.getUniqueId().equals(activePlayer2.getUniqueId()) ||
                    attacker.getUniqueId().equals(activePlayer2.getUniqueId()) && victim.getUniqueId().equals(activePlayer1.getUniqueId());
        }
    }

    private void startTimer() {
        if (battleTimer != null) {
            battleTimer.cancel();
            battleTimer = null;
        }
        battleTimer = config.getManagedResources().runTaskLater(() -> {
            battleTimer = null;
            if (config.getPlugin().isUHCLive()) return;
            final double player1HP = activePlayer1.getHealth();
            final double player2HP = activePlayer2.getHealth();
            final Player winner = player1HP > player2HP ? activePlayer1 : activePlayer2;
            final Player loser = winner.equals(activePlayer1) ? activePlayer2 : activePlayer1;
            warmupFightOverCallback(winner, loser);
        }, 60);
    }

    @Nullable
    private Player warmupFightOverCallback(final Player winner, final Player loser) {
        if (battleTimer != null) {
            battleTimer.cancel();
            battleTimer = null;
        }
        final Location worldSpawn = Utils.getWorldSpawn(config);
        if (worldSpawn != null) {
            loser.setRespawnLocation(worldSpawn);
        }

        config.getManagedResources().runTaskLater(() -> {
            if (loser.isDead()) {
                loser.spigot().respawn();
            }
        }, 1);

        loser.sendMessage(Component.text("Unlucky... Get back in the queue and get your revenge!", Style.style(NamedTextColor.RED, TextDecoration.ITALIC)));

        if (!queue.isEmpty()) {
            final Player nextUp = queue.removeFirst();
            new Warmup(config, winner, nextUp).trigger();
            startTimer();
            winner.sendMessage(Component.text("Winner stays on! Good luck!", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)));
            queue.forEach(player -> player.sendMessage(Component.text("A warmup match just finished, your queue position was updated", Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC))));
            return nextUp;
        } else if (worldSpawn != null) {
            winner.sendMessage(Component.text("Congrats on the dub, teleporting you back but you are still next in queue! Good luck!", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)));
            winner.teleport(worldSpawn);
        }

        return null;
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (config.getPlugin().isUHCLive()) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }
        if (!Tag.BUTTONS.isTagged(clickedBlock.getType())) {
            return;
        }
        if (!warmupButtons.contains(clickedBlock.getLocation())) {
            return;
        }
        final Player player = event.getPlayer();
        if (queue.remove(player)) {
            player.sendMessage(Component.text("You're no longer in the warmup queue.", Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC)));
        } else if (!queue.contains(player)) {
            queue.addLast(player);
            if (queue.size() == 1 && activePlayer1 == null && activePlayer2 == null) {
                player.sendMessage(Component.text("Welcome to the warmup queue. You're the first one in the queue! Waiting for another player...", Style.style(NamedTextColor.GOLD)));
            } else if (queue.size() >= 2 && activePlayer1 == null && activePlayer2 == null) {
                activePlayer1 = queue.removeFirst();
                activePlayer2 = queue.removeFirst();
                new Warmup(config, activePlayer1, activePlayer2).trigger();
                startTimer();
            } else if (!queue.isEmpty() && activePlayer1 == null) {
                activePlayer1 = queue.removeFirst();
                new Warmup(config, activePlayer1, activePlayer2).trigger();
                startTimer();
            } else if (!queue.isEmpty() && activePlayer2 == null) {
                activePlayer2 = queue.removeFirst();
                new Warmup(config, activePlayer1, activePlayer2).trigger();
                startTimer();
            } else {
                player.sendMessage(Component.text(String.format("Welcome to the warmup queue. Your queue position is %s", queue.size()), Style.style(NamedTextColor.GOLD)));
            }
        }
    }


    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (config.getPlugin().isUHCLive()) return;
        final ItemStack item = event.getItemInHand();
        if (!isWarmupButton(config, item)) {
            return;
        }
        final Block block = event.getBlockPlaced();
        if (!Tag.BUTTONS.isTagged(block.getType())) {
            return;
        }

        warmupButtons.add(block.getLocation());
    }

    @EventHandler
    public void onPlayerDamage(final EntityDamageByEntityEvent event) {
        if (config.getPlugin().isUHCLive()) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!isPvpPair(attacker, victim)) {
            event.setCancelled(true); // TODO - this will be annoying to test if i dont figure out a way to turn on pvp outside of uhc start
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        if (config.getPlugin().isUHCLive()) return;
        if (activePlayer1 == null || activePlayer2 == null) return;
        final Player victim = event.getPlayer();
        if (victim.getUniqueId().equals(activePlayer1.getUniqueId())) {
            activePlayer1 = warmupFightOverCallback(activePlayer2, activePlayer1);
        } else if (victim.getUniqueId().equals(activePlayer2.getUniqueId())) {
            activePlayer2 = warmupFightOverCallback(activePlayer1, activePlayer2);
        }
    }
}
