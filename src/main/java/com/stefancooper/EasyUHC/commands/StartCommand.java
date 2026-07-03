package com.stefancooper.EasyUHC.commands;

import java.util.List;
import java.util.Optional;

import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.base.ConfigKey;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShield;
import com.stefancooper.EasyUHC.base.BossBarBorder;
import com.stefancooper.EasyUHC.base.RandomFinalLocation;
import com.stefancooper.EasyUHC.uhcloot.UHCLoot;
import com.stefancooper.EasyUHC.base.SpreadPlayers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.base.Utils;
import org.bukkit.scheduler.BukkitTask;

import static com.stefancooper.EasyUHC.base.ConfigKey.COUNTDOWN_TIMER_LENGTH;
import static com.stefancooper.EasyUHC.base.ConfigKey.DIFFICULTY;
import static com.stefancooper.EasyUHC.base.ConfigKey.DISABLE_DEBUG_INFO;
import static com.stefancooper.EasyUHC.base.ConfigKey.ENABLE_EVOLVING_SHIELDS;
import static com.stefancooper.EasyUHC.base.ConfigKey.GRACE_PERIOD_TIMER;
import static com.stefancooper.EasyUHC.base.ConfigKey.LOOT_CHEST_GRACE_PERIOD;
import static com.stefancooper.EasyUHC.base.ConfigKey.MOB_GRACE_PERIOD;
import static com.stefancooper.EasyUHC.base.ConfigKey.RANDOM_FINAL_LOCATION;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_CENTER_X;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_CENTER_Z;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_FINAL_SIZE;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_FINAL_Y;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_GRACE_PERIOD;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_INITIAL_SIZE;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_SHRINKING_PERIOD;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_Y_SHRINKING_PERIOD;
import static com.stefancooper.EasyUHC.base.Constants.MAXIMUM_FINAL_SIZE_FOR_Y_SHRINK;

public class StartCommand extends AbstractCommand {

    public static final String COMMAND_KEY = "start";

    private static int shrinkYBorderBlock;
    private BukkitTask runner;

    public StartCommand(CommandSender sender, String cmd, String[] args, Config config) {
        super(sender, cmd, args, config);
    }

    @Override
    public void execute() {
        shrinkYBorderBlock = -64;

        // Worlds
        final World world = getConfig().getWorlds().getOverworld();
        final World nether = getConfig().getWorlds().getNether();
        final World end = getConfig().getWorlds().getEnd();

        // Config Values
        final int centerX = getConfig().getProperty(WORLD_BORDER_CENTER_X, Defaults.WORLD_BORDER_CENTER_X);
        final int centerZ = getConfig().getProperty(WORLD_BORDER_CENTER_Z, Defaults.WORLD_BORDER_CENTER_Z);

        // World and Countdown timer are both configs that will always be set
        final int countdownTimer = getConfig().getProperty(COUNTDOWN_TIMER_LENGTH, Defaults.COUNTDOWN_TIMER_LENGTH);

        // Final Center Location
        final Location finalLocation;
        if (getConfig().getProperty(RANDOM_FINAL_LOCATION, Defaults.RANDOM_FINAL_LOCATION)) {
            final int initialWorldBorderSize = getConfig().getProperty(WORLD_BORDER_INITIAL_SIZE, Defaults.WORLD_BORDER_INITIAL_SIZE);
            final RandomFinalLocation location = new RandomFinalLocation(world, centerX, centerZ, initialWorldBorderSize);
            finalLocation = location.getLocation();
        } else {
            finalLocation = new Location(world, centerX, 64, centerZ);
        }

        // Wipe existing achievements
        getSender().getServer().dispatchCommand(getSender(), "advancement revoke @a everything");

        // Actions on the world
        Bukkit.setDefaultGameMode(GameMode.SURVIVAL);
        Utils.setWorldEffects(List.of(world, nether, end), (cbWorld) -> {
            world.getWorldBorder().setSize(getConfig().getProperty(WORLD_BORDER_INITIAL_SIZE, Defaults.WORLD_BORDER_INITIAL_SIZE));
            world.getWorldBorder().setCenter(finalLocation.getX(), finalLocation.getZ());
            world.setTime(1000);
            world.setDifficulty(Difficulty.PEACEFUL);
            world.getEntities().stream().filter(entity -> entity.getType().equals(EntityType.ITEM)).forEach(Entity::remove);
            world.setGameRule(GameRules.FALL_DAMAGE, true);
            world.setGameRule(GameRules.REDUCED_DEBUG_INFO, getConfig().getProperty(DISABLE_DEBUG_INFO, Defaults.DISABLE_DEBUG_INFO));
        });

        // Actions on the player
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.resetMaxHealth();
            double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getDefaultValue();
            player.setHealth(maxHealth);
            player.setSaturation(20);
            player.setFoodLevel(20);
            player.getInventory().clear();
            if (getConfig().getProperty(ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS)) {
                EvolvingShield.createEvolvingShield(getConfig(), player);
            }
            player.setExp(0);
            player.setLevel(0);
            player.setGameMode(GameMode.SURVIVAL);
            player.clearActivePotionEffects();
            player.addPotionEffect(PotionEffectType.MINING_FATIGUE.createEffect((int) Utils.secondsToTicks(countdownTimer), 3));
            player.addPotionEffect(PotionEffectType.REGENERATION.createEffect((int) Utils.secondsToTicks(countdownTimer) + (int) Utils.secondsToTicks(30), 3));
            player.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect((int) Utils.secondsToTicks(countdownTimer) + (int) Utils.secondsToTicks(30), 3));
            if (getConfig().getProperty(RANDOM_FINAL_LOCATION, Defaults.RANDOM_FINAL_LOCATION)) {
                player.getInventory().addItem(RandomFinalLocation.generateWorldCenterCompass());
                player.setCompassTarget(world.getWorldBorder().getCenter());
            }
        });

        // Spread players
        final SpreadPlayers spread = new SpreadPlayers(getConfig());
        spread.trigger();

        Bukkit.getServer().broadcast(Component.text("UHC: Countdown starting now. Don't forget to record your POV if you can. GLHF!", Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC)));

        // Timed actions
        int gracePeriod = getConfig().getProperty(GRACE_PERIOD_TIMER, Defaults.GRACE_PERIOD_TIMER);
        int worldBorderGracePeriod = getConfig().getProperty(WORLD_BORDER_GRACE_PERIOD, Defaults.WORLD_BORDER_GRACE_PERIOD);

        // World border grace period
        if (worldBorderGracePeriod > 0) getConfig().getManagedResources().runTaskLater(endWorldBorderGracePeriod(), worldBorderGracePeriod + countdownTimer);
        // PVP Grace period
        if (gracePeriod > 0) getConfig().getManagedResources().runTaskLater(endGracePeriod(), gracePeriod + countdownTimer);

        // Countdown timer
        for (int curr = 0; curr <= countdownTimer; curr++) {
            if (curr == 0) {
                getConfig().getPlugin().setCountingDown(true);
                getConfig().getManagedResources().runTaskLater(countdown(curr, world), 0);
            } else {
                getConfig().getManagedResources().runTaskLater(countdown(curr, world), curr);
            }
        }

        // World border boss bar
        if (getConfig().getProperty(ConfigKey.WORLD_BORDER_IN_BOSSBAR, Defaults.WORLD_BORDER_IN_BOSSBAR)) {
            BossBarBorder bossBarBorder = getConfig().getManagedResources().getBossBarBorder();
            Bukkit.getOnlinePlayers().forEach(player -> bossBarBorder.getBossBar().addPlayer(player));
            bossBarBorder.getBossBar().setVisible(true);
            getConfig().getManagedResources().runRepeatingTask(bossBarBorder.updateProgress(), 1);
        }

        // Timestamps
        if (getConfig().getProperty(ConfigKey.ENABLE_TIMESTAMPS, Defaults.ENABLE_TIMESTAMPS)) {
            getConfig().getManagedResources().addTimestamp("[Meta] UHC Started", false);
        }

        // Performance Tracking
        if (getConfig().getProperty(ConfigKey.ENABLE_PERFORMANCE_TRACKING, Defaults.ENABLE_PERFORMANCE_TRACKING)) {
            getConfig().getManagedResources().runTaskLater(() -> {
                // don't start performance tracking until after the countdown has finished
                getConfig().getManagedResources().runRepeatingTask(getConfig().getManagedResources().updatePerformanceStatistics(), 60);
            }, countdownTimer);
        }

        // UHC Loot
        if (UHCLoot.isConfigured(getConfig())) {
            final int uhcLootGracePeriod = getConfig().getProperty(LOOT_CHEST_GRACE_PERIOD, Defaults.LOOT_CHEST_GRACE_PERIOD);
            getConfig().getManagedResources().runTaskLater(() -> new UHCLoot(getConfig()), countdownTimer + uhcLootGracePeriod);
        }

        if (getConfig().getProperty(DISABLE_DEBUG_INFO, Defaults.DISABLE_DEBUG_INFO)) {
            getConfig().getManagedResources().runRepeatingTask(updateActionBarLocation(), 1L);
        }

        final int endMobGracePeriod = getConfig().getProperty(MOB_GRACE_PERIOD, Defaults.MOB_GRACE_PERIOD);
        if (endMobGracePeriod > 0) {
            getConfig().getManagedResources().runTaskLater(endMobGracePeriod(List.of(world, nether, end)), endMobGracePeriod + countdownTimer);
        }

        getConfig().getPlugin().setUHCLive(true);
    }

    protected Runnable countdown(final int remaining, final World world) {
        final int countdownTimer = getConfig().getProperty(COUNTDOWN_TIMER_LENGTH, Defaults.COUNTDOWN_TIMER_LENGTH);
        final boolean mobGracePeriodIsZero = getConfig().getProperty(MOB_GRACE_PERIOD, Defaults.MOB_GRACE_PERIOD) == 0;
        return () -> {
            int timeLeft = countdownTimer - remaining;
            if (timeLeft == 2) {
                Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(Title.title(Component.text(Integer.toString(timeLeft)), Component.text("Ready"), 10, 70, 20)));
            } else if (timeLeft == 1) {
                Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(Title.title(Component.text(Integer.toString(timeLeft)), Component.text("Set"), 10, 70, 20)));
            } else if (timeLeft == 0) {
                // Countdown over!
                Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(Title.title(Component.text(Integer.toString(timeLeft)), Component.text("Go!"), 10, 70, 20)));
                if (mobGracePeriodIsZero) {
                    world.setDifficulty(getConfig().getProperty(DIFFICULTY, Defaults.DIFFICULTY));
                }
                getConfig().getPlugin().setCountingDown(false);
            } else {
                Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(Title.title(Component.text(Integer.toString(timeLeft)), Component.text("Starting soon..."), 10, 70, 20)));
            }
        };
    }

    protected Runnable endGracePeriod () {
        return () -> {
            Bukkit.getServer().broadcast(Component.text("UHC: PVP grace period is now over.", Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC)));
            getConfig().getManagedResources().addTimestamp("[Meta] PVP grace period is now over.");
            Utils.setWorldEffects(List.of(getConfig().getWorlds().getOverworld(), getConfig().getWorlds().getNether(), getConfig().getWorlds().getEnd()), (cbWorld) -> cbWorld.setGameRule(GameRules.PVP, true));
            Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(Title.title(Component.text("Grace period over"), Component.text("\uD83D\uDC40 Watch your back \uD83D\uDC40"), 10, 70, 20)));
        };
    }

    protected Runnable endWorldBorderGracePeriod () {
        return () -> {

            int finalWorldBorderSize = getConfig().getProperty(WORLD_BORDER_FINAL_SIZE, Defaults.WORLD_BORDER_FINAL_SIZE);
            int shrinkingTime = getConfig().getProperty(WORLD_BORDER_SHRINKING_PERIOD, Defaults.WORLD_BORDER_SHRINKING_PERIOD);

            Utils.setWorldEffects(List.of(getConfig().getWorlds().getOverworld(), getConfig().getWorlds().getNether(), getConfig().getWorlds().getEnd()), (cbWorld) -> {
                WorldBorder wb = cbWorld.getWorldBorder();
                wb.setDamageBuffer(5);
                wb.setDamageAmount(0.2);
                wb.changeSize(finalWorldBorderSize, Utils.secondsToTicks(shrinkingTime));
            });

            if (Optional.ofNullable(getConfig().getProperty(WORLD_BORDER_Y_SHRINKING_PERIOD)).isPresent() &&
                    Optional.ofNullable(getConfig().getProperty(WORLD_BORDER_FINAL_Y)).isPresent() &&
                        finalWorldBorderSize <= MAXIMUM_FINAL_SIZE_FOR_Y_SHRINK) {
                getConfig().getManagedResources().runTaskLater(shrinkYBorderOverTime(), shrinkingTime);
            }

            Bukkit.getServer().broadcast(Component.text("UHC: World Border shrink grace period is now over.", Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC)));
            getConfig().getManagedResources().addTimestamp("[Meta] World Border shrink grace period is now over.");
            Bukkit.getOnlinePlayers().forEach(player -> player.showTitle(Title.title(Component.text("World border shrinking"), Component.text("Don't get caught..."), 10, 70, 20)));
        };
    }

    protected Runnable shrinkYBorderOverTime() {
        return () -> {
            final int centerX = getConfig().getProperty(WORLD_BORDER_CENTER_X, Defaults.WORLD_BORDER_CENTER_X);
            final int centerZ = getConfig().getProperty(WORLD_BORDER_CENTER_Z, Defaults.WORLD_BORDER_CENTER_Z);
            final int finalSize = getConfig().getProperty(WORLD_BORDER_FINAL_SIZE, Defaults.WORLD_BORDER_FINAL_SIZE);
            final int finalY = getConfig().getProperty(WORLD_BORDER_FINAL_Y, Defaults.WORLD_BORDER_FINAL_Y);
            final int shrinkTime = getConfig().getProperty(WORLD_BORDER_Y_SHRINKING_PERIOD, Defaults.WORLD_BORDER_Y_SHRINKING_PERIOD);
            final int interval = shrinkTime / (finalY + 64);

            int eitherSide = finalSize / 2;
            int corner1X = centerX + eitherSide;
            int corner2X = centerX - eitherSide;
            int corner1Z = centerZ + eitherSide;
            int corner2Z = centerZ - eitherSide;

            if (shrinkTime > 0) {
                Bukkit.getServer().broadcast(Component.text("UHC: Y Border shrink grace period over.", Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC)));
                getConfig().getManagedResources().addTimestamp("[Meta] Y Border shrink grace period over.");
                runner = getConfig().getManagedResources().runRepeatingTask(() -> {
                    shrinkYBorderBlock++;
                    final String fillCommand = String.format("fill %s %s %s %s %s %s minecraft:bedrock", corner1X, shrinkYBorderBlock, corner1Z, corner2X, shrinkYBorderBlock, corner2Z);
                    getSender().getServer().dispatchCommand(Bukkit.getConsoleSender(), fillCommand);
                    if (shrinkYBorderBlock >= finalY) {
                        runner.cancel();
                    }
                }, interval);
            }
        };
    }

    protected Runnable updateActionBarLocation() {
        return () -> Bukkit.getOnlinePlayers().forEach(player -> player.sendActionBar(Component.text("X: " + player.getLocation().getBlockX() + " Y: " + player.getLocation().getBlockY() + " Z: " + player.getLocation().getBlockZ(), NamedTextColor.AQUA)));
    }

    protected Runnable endMobGracePeriod(final List<World> worlds) {
        return () -> {
            Bukkit.getServer().broadcast(Component.text("UHC: Mob grace period is over", Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC)));
            getConfig().getManagedResources().addTimestamp("[Meta] Mob grace period is over.");
            Utils.setWorldEffects(worlds, (cb) -> cb.setDifficulty(getConfig().getProperty(DIFFICULTY, Defaults.DIFFICULTY)));
        };
    }
}
