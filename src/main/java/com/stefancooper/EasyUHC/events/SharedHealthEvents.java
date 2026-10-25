package com.stefancooper.EasyUHC.events;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.base.UHCTeam;
import com.stefancooper.EasyUHC.base.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.stefancooper.EasyUHC.base.ConfigKey.SHARED_TEAM_HEALTH;

public class SharedHealthEvents implements Listener {

    private final Config config;

    public SharedHealthEvents(Config config) {
        this.config = config;
    }

    @EventHandler
    public void onPotionEffect(final EntityPotionEffectEvent event) {
        if (!config.getPlugin().isUHCLive()) return;
        if (!(event.getEntity() instanceof final Player player)) return;
        if (event.getCause().equals(EntityPotionEffectEvent.Cause.PLUGIN)) return;
        final Optional<UHCTeam> team = Utils.getTeamForPlayer(config, player);
        if (team.isEmpty() || !team.get().isSharedHealth()) return;

        final PotionEffect newEffect = event.getNewEffect();

        if (newEffect != null) {
            final PotionEffectType type = newEffect.getType();
            if (type == PotionEffectType.ABSORPTION || type == PotionEffectType.HEALTH_BOOST || type == PotionEffectType.REGENERATION || type == PotionEffectType.INSTANT_HEALTH) {
                final List<Player> players = team.get().getPlayers().stream().filter(p -> !player.equals(p)).toList();
                players.forEach(teammate -> teammate.addPotionEffect(newEffect));
            }
        }
    }

    @EventHandler
    public void onMilkDrink(PlayerItemConsumeEvent event) {
        if (!config.getPlugin().isUHCLive()) return;
        if (event.getItem().getType() != Material.MILK_BUCKET) return;
        final Player player = event.getPlayer();
        final Optional<UHCTeam> team = Utils.getTeamForPlayer(config, player);
        if (team.isEmpty() || !team.get().isSharedHealth()) return;
        final List<Player> players = team.get().getPlayers().stream().filter(p -> !player.equals(p)).toList();
        players.forEach(LivingEntity::clearActivePotionEffects);
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent event) {
        if (!config.getPlugin().isUHCLive()) return;
        if (event.getCause().equals(EntityDamageEvent.DamageCause.CUSTOM)) return;
        if (!(event.getEntity() instanceof final Player damagee)) return;
        final Optional<UHCTeam> team = Utils.getTeamForPlayer(config, damagee);
        if (team.isEmpty() || !team.get().isSharedHealth()) return;

        final List<Player> players = team.get().getPlayers().stream().filter(player -> !damagee.equals(player)).toList();
        players.forEach(teammate -> teammate.damage(event.getFinalDamage()));

        config.getManagedResources().runTaskLater(() -> {
            final List<Player> onlinePlayers = team.get().getPlayers();

            final List<Player> onlineAlivePlayers = onlinePlayers.stream()
                    .filter(player -> !player.isDead() && player.getGameMode() != GameMode.SPECTATOR)
                    .toList();

            if (onlineAlivePlayers.isEmpty()) return;

            if (onlinePlayers.size() > onlineAlivePlayers.size()) {
                onlineAlivePlayers.forEach(Damageable::kill);
                return;
            }

            final double lowestHealth = onlineAlivePlayers.stream()
                    .mapToDouble(Player::getHealth)
                    .min()
                    .orElse(0.0);

            if (lowestHealth <= 0.0) {
                onlineAlivePlayers.forEach(Damageable::kill);
            } else {
                onlineAlivePlayers.forEach(p -> p.setHealth(lowestHealth));
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onJoin(final PlayerJoinEvent event) {
        if (!config.getPlugin().isUHCLive()) return;
        final Player player = event.getPlayer();
        final Optional<UHCTeam> team = Utils.getTeamForPlayer(config, player);
        if (team.isEmpty() || !team.get().isSharedHealth()) return;
        final int expectedMaxHP = config.getProperty(SHARED_TEAM_HEALTH, Defaults.SHARED_TEAM_HEALTH);
        final List<Player> teammates = team.get().getPlayers().stream().filter(p -> !player.equals(p)).toList();
        final double playerMaxHP = player.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
        if (playerMaxHP < expectedMaxHP) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(expectedMaxHP);
        }

        if (teammates.isEmpty()) {
            // noop, cannot find teammates
        } else if (teammates.getFirst().isDead() || teammates.getFirst().getGameMode().equals(GameMode.SPECTATOR)) {
            player.setGameMode(GameMode.SPECTATOR);
        } else {
            player.setHealth(teammates.getFirst().getHealth());
        }
    }

}
