package com.stefancooper.EasyUHC.revive;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.base.UHCTeam;
import com.stefancooper.EasyUHC.base.Utils;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShield;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

import static com.stefancooper.EasyUHC.base.ConfigKey.ENABLE_EVOLVING_SHIELDS;
import static com.stefancooper.EasyUHC.base.ConfigKey.REVIVE_HP;
import static com.stefancooper.EasyUHC.base.ConfigKey.REVIVE_LOSE_MAX_HEALTH;


public class InstantRevive {

    public interface ReviveCallback {
        void callback();
    }
    public final Config config;
    public final Player reviver;
    public final Player revivee;
    public final ArmorStand armorStand;

    private final int reviveHealth;
    private final int reviveLoseMaxHealth;

    public InstantRevive(final Config config, final Player reviver, final String revivee, final boolean playSound, final ArmorStand armorStand) {
        this.config = config;
        this.reviver = reviver;
        this.armorStand = armorStand;
        this.revivee = Bukkit.getPlayer(revivee);
        this.reviveHealth = config.getProperty(REVIVE_HP, Defaults.REVIVE_HP);
        this.reviveLoseMaxHealth = config.getProperty(REVIVE_LOSE_MAX_HEALTH, Defaults.REVIVE_LOSE_MAX_HEALTH);

        if (this.revivee == null) {
            reviver.sendMessage(String.format("%s is offline, so cannot be revived", revivee));
            return;
        }

        if (playSound) {
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.BLOCK_END_PORTAL_SPAWN, 1, 1));
        }

        revivePlayer();

    }

    void revivePlayer () {
        // double check that the reviver still has the player head
        Bukkit.getServer().broadcast(Component.text(String.format("%s has been revived!", revivee.getName()), Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)));
        config.getManagedResources().addTimestamp(String.format("[Revive] %s has been revived", revivee.getName()));

        // Revivee effects
        final Optional<UHCTeam> team = Utils.getTeamForPlayer(config, revivee);
        if (team.isPresent() && team.get().isSharedInventory()) {
            config.getManagedResources().getSharedInventoryManager().applyAfterJoin(revivee);
        } else {
            revivee.getInventory().clear();
            if (config.getProperty(ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS)) {
                EvolvingShield.createEvolvingShield(config, revivee);
            }
        }
        revivee.spigot().respawn();
        revivee.teleport(armorStand.getLocation());
        revivee.setGameMode(GameMode.SURVIVAL);
        if (revivee.getMaxHealth() < reviveHealth) {
            revivee.setHealth(revivee.getMaxHealth());
        } else {
            revivee.setHealth(reviveHealth);
        }
        revivee.setFoodLevel(20);
        revivee.setExp(0);
        revivee.setLevel(0);

        if (revivee.getMaxHealth() > reviveLoseMaxHealth) {
            revivee.setMaxHealth(revivee.getMaxHealth() - reviveLoseMaxHealth);
        } else {
            revivee.setMaxHealth(1);
        }

        revivee.spawnParticle(Particle.POOF, revivee.getLocation(), 1000);

        armorStand.setVisible(false);
        armorStand.setHealth(0);

        if (config.getProperty(ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS)) {
            final Optional<ItemStack> getShield = EvolvingShield.getEvolvingShieldFromPlayer(config, reviver);
            if (getShield.isPresent()) {
                final ItemStack shield = getShield.get();
                EvolvingShield.updateXP(
                        config,
                        shield,
                        reviver,
                        EvolvingShield.EvolvingShieldXPType.REVIVE
                );
            }
        }
    }


}
