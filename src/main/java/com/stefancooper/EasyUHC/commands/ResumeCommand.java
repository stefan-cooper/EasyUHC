package com.stefancooper.EasyUHC.commands;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.uhcloot.UHCLoot;
import com.stefancooper.EasyUHC.base.Utils;
import com.stefancooper.EasyUHC.base.ConfigKey;
import com.stefancooper.EasyUHC.base.BossBarBorder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.command.CommandSender;
import java.util.List;
import java.util.Optional;

import static com.stefancooper.EasyUHC.base.ConfigKey.COUNTDOWN_TIMER_LENGTH;
import static com.stefancooper.EasyUHC.base.ConfigKey.DIFFICULTY;
import static com.stefancooper.EasyUHC.base.ConfigKey.GRACE_PERIOD_TIMER;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_FINAL_SIZE;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_FINAL_Y;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_GRACE_PERIOD;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_INITIAL_SIZE;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_SHRINKING_PERIOD;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_Y_SHRINKING_PERIOD;
import static com.stefancooper.EasyUHC.base.Constants.MAXIMUM_FINAL_SIZE_FOR_Y_SHRINK;

public class ResumeCommand extends StartCommand {

    public static final String COMMAND_KEY = "resume";

    public ResumeCommand(CommandSender sender, String cmd, String[] args, Config config) {
        super(sender, cmd, args, config);
    }

    @Override
    public void execute() {
        final World world = getConfig().getWorlds().getOverworld();
        final World nether = getConfig().getWorlds().getNether();
        final World end = getConfig().getWorlds().getEnd();
        getConfig().getManagedResources().cancelTimer();

        final int countdownTimer = getConfig().getProperty(COUNTDOWN_TIMER_LENGTH, Defaults.COUNTDOWN_TIMER_LENGTH);

        int minutesProgressed;
        if (getArgs().length > 0) {
            minutesProgressed = Integer.parseInt(getArgs()[0]);
        } else {
            minutesProgressed = 0;
        }

        // Actions on the player
        Bukkit.getOnlinePlayers().forEach(player -> player.setGameMode(GameMode.SURVIVAL));

        Bukkit.setDefaultGameMode(GameMode.SURVIVAL);

        // Actions on the world
        Utils.setWorldEffects(List.of(world, nether, end), (cbWorld) -> cbWorld.getWorldBorder().setSize(getConfig().getProperty(WORLD_BORDER_INITIAL_SIZE, Defaults.WORLD_BORDER_INITIAL_SIZE)));
        Utils.setWorldEffects(List.of(getConfig().getWorlds().getOverworld(), getConfig().getWorlds().getNether(), getConfig().getWorlds().getEnd()), (cbWorld) -> cbWorld.setDifficulty(getConfig().getProperty(DIFFICULTY, Defaults.DIFFICULTY)));
        Utils.setWorldEffects(List.of(world, nether, end), (cbWorld) -> cbWorld.setGameRule(GameRules.FALL_DAMAGE, true));

        if (getConfig().getProperty(ConfigKey.WORLD_BORDER_IN_BOSSBAR, Defaults.WORLD_BORDER_IN_BOSSBAR)) {
            BossBarBorder bossBarBorder = getConfig().getManagedResources().getBossBarBorder();
            Bukkit.getOnlinePlayers().forEach(player -> bossBarBorder.getBossBar().addPlayer(player));
            bossBarBorder.getBossBar().setVisible(true);
            getConfig().getManagedResources().runRepeatingTask(bossBarBorder.updateProgress(), 1);
        }

        Integer gracePeriod = getConfig().getProperty(GRACE_PERIOD_TIMER, Defaults.GRACE_PERIOD_TIMER);
        Integer worldBorderGracePeriod = getConfig().getProperty(WORLD_BORDER_GRACE_PERIOD, Defaults.WORLD_BORDER_GRACE_PERIOD);

        // PVP Grace Period
        if (gracePeriod > 0) {
            int secondsProgressed = minutesProgressed * 60;
            if (secondsProgressed > gracePeriod) {
                endGracePeriod().run();
            } else {
                getConfig().getManagedResources().runTaskLater(endGracePeriod(), gracePeriod + countdownTimer - secondsProgressed);
            }
        }

        // World Border Grace Period
        if (worldBorderGracePeriod > 0) {
            int secondsProgressed = minutesProgressed * 60;
            if (secondsProgressed > worldBorderGracePeriod) {
                endWorldBorderGracePeriod(secondsProgressed - worldBorderGracePeriod);
            } else {
                getConfig().getManagedResources().runTaskLater(endWorldBorderGracePeriod(), worldBorderGracePeriod + countdownTimer - (minutesProgressed * 60));
            }
        }

        // UHC Loot Chest
        if (UHCLoot.isConfigured(getConfig())) {
            new UHCLoot(getConfig());
        }

        // Timestamps
        if (getConfig().getProperty(ConfigKey.ENABLE_TIMESTAMPS, Defaults.ENABLE_TIMESTAMPS)) {
            getConfig().getManagedResources().addTimestamp(String.format("[Meta] UHC Resumed after %s minutes", minutesProgressed), false);
        }

        getConfig().getPlugin().setUHCLive(true);
    }

    protected void endWorldBorderGracePeriod (int progressedSeconds) {
        int finalWorldBorderSize = getConfig().getProperty(WORLD_BORDER_FINAL_SIZE, Defaults.WORLD_BORDER_FINAL_SIZE);
        int shrinkingTime = getConfig().getProperty(WORLD_BORDER_SHRINKING_PERIOD, Defaults.WORLD_BORDER_SHRINKING_PERIOD);
        Utils.setWorldEffects(List.of(getConfig().getWorlds().getOverworld(), getConfig().getWorlds().getNether(), getConfig().getWorlds().getEnd()), (cbWorld) -> {
            WorldBorder wb = cbWorld.getWorldBorder();
            wb.setDamageBuffer(5);
            wb.setDamageAmount(0.2);
            wb.changeSize(finalWorldBorderSize, Utils.secondsToTicks(shrinkingTime - progressedSeconds));
        });

        if (Optional.ofNullable(getConfig().getProperty(WORLD_BORDER_Y_SHRINKING_PERIOD)).isPresent() &&
                Optional.ofNullable(getConfig().getProperty(WORLD_BORDER_FINAL_Y)).isPresent() &&
                finalWorldBorderSize <= MAXIMUM_FINAL_SIZE_FOR_Y_SHRINK) {
            getConfig().getManagedResources().runTaskLater(shrinkYBorderOverTime(), shrinkingTime);
        }

        Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(Title.title(Component.text("World border shrinking"), Component.text("Don't get caught..."), 10, 70, 20)));
    }
}
