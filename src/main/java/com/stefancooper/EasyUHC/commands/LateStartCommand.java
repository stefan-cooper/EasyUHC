package com.stefancooper.EasyUHC.commands;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.base.*;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShield;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static com.stefancooper.EasyUHC.base.ConfigKey.ENABLE_EVOLVING_SHIELDS;
import static com.stefancooper.EasyUHC.base.ConfigKey.RANDOM_FINAL_LOCATION;

public class LateStartCommand extends StartCommand {

    public static final String COMMAND_KEY = "latestart";

    public LateStartCommand(CommandSender sender, String cmd, String[] args, Config config) {
        super(sender, cmd, args, config);
    }

    @Override
    public void execute() {
        final String playerName = getArgs()[0];
        final Player player;
        if (getArgs().length == 0) {
            return;
        } else {
            player = Bukkit.getPlayerExact(playerName);
        }

        if (player == null) {
            getConfig().getPlugin().getLogger().log(Level.WARNING, "UHC latestart command cancelled. The player was not found in the server.");
            getSender().sendMessage(Component.text("UHC latestart command cancelled. The player was not found in the server.", Style.style(NamedTextColor.RED)));
            return;
        }

        final Optional<UHCTeam> findTeam = getConfig().getManagedResources().getTeams().stream().filter(t -> t.getPlayers().contains(player)).findFirst();
        if (findTeam.isEmpty()) {
            getConfig().getPlugin().getLogger().log(Level.WARNING, "UHC latestart command cancelled. We could not find the players team.");
            getSender().sendMessage(Component.text("UHC latestart command cancelled. We could not find the players team.", Style.style(NamedTextColor.RED)));
            return;
        }
        final UHCTeam team = findTeam.get();
        final Optional<Player> findTeammate = team.getPlayers().stream().filter(tm -> !tm.isDead() && !tm.equals(player)).findFirst();
        if (findTeammate.isEmpty()) {
            getConfig().getPlugin().getLogger().log(Level.WARNING, "UHC latestart command cancelled. We could not find an alive teammate to teleport to.");
            getSender().sendMessage(Component.text("UHC latestart command cancelled. We could not find alive teammate to teleport to.", Style.style(NamedTextColor.RED)));
            return;
        }
        final Player teammate = findTeammate.get();

        if (player.isDead()) {
            player.spigot().respawn();
        }

        // ---- CHECKS COMPLETE - LETS START THEM -----

        // Worlds
        final World world = getConfig().getWorlds().getOverworld();

        // Config values
        final boolean useRandomFinalLocation = getConfig().getProperty(RANDOM_FINAL_LOCATION, Defaults.RANDOM_FINAL_LOCATION);
        final boolean bossBarBorderEnabled = getConfig().getProperty(ConfigKey.WORLD_BORDER_IN_BOSSBAR, Defaults.WORLD_BORDER_IN_BOSSBAR);
        final boolean evolvingShieldEnabled = getConfig().getProperty(ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS);

        // Wipe existing achievements
        getSender().getServer().dispatchCommand(getSender(), String.format("advancement revoke %s everything", playerName));

        // Set starting stats
        player.setGameMode(GameMode.SURVIVAL);
        player.resetMaxHealth();
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getDefaultValue();
        player.setHealth(maxHealth);
        player.setSaturation(20);
        player.setFoodLevel(20);
        player.getInventory().clear();
        player.setExp(0);
        player.setLevel(0);
        if (evolvingShieldEnabled) {
            EvolvingShield.createEvolvingShield(getConfig(), player);
        }
        player.clearActivePotionEffects();
        if (useRandomFinalLocation) {
            player.getInventory().addItem(RandomFinalLocation.generateWorldCenterCompass());
            player.setCompassTarget(world.getWorldBorder().getCenter());
        }

        // set bossbar
        if (bossBarBorderEnabled) {
            final BossBarBorder bossBarBorder = getConfig().getManagedResources().getBossBarBorder();
            bossBarBorder.getBossBar().addPlayer(player);
            bossBarBorder.getBossBar().setVisible(true);
        }

        // teleport to teammate
        player.teleport(teammate);
    }
}
