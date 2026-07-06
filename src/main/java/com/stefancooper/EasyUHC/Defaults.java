package com.stefancooper.EasyUHC;

import com.stefancooper.EasyUHC.base.ConfigKey;
import com.stefancooper.EasyUHC.base.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;
import java.util.Properties;

import static com.stefancooper.EasyUHC.base.ConfigKey.*;
import static com.stefancooper.EasyUHC.base.DeathAction.SPECTATE;
import static com.stefancooper.EasyUHC.base.Constants.MAXIMUM_FINAL_SIZE_FOR_Y_SHRINK;

public class Defaults {

    /* -- Defaults that are written to uhc_config.properties -- */
    public static String HEALTH_OBJECTIVE = "health";
    public static String WORLD_NAME = "world";
    public static String NETHER_WORLD_NAME = "world_nether";
    public static String END_WORLD_NAME = "world_the_end";
    public static int WORLD_BORDER_INITIAL_SIZE = 2000;
    public static int WORLD_BORDER_SPAWN_PADDING = 1;
    public static int WORLD_BORDER_FINAL_SIZE = 150;
    public static int WORLD_BORDER_SHRINKING_PERIOD = 7200;
    public static int WORLD_BORDER_GRACE_PERIOD = 1800;
    public static int WORLD_BORDER_CENTER_X = 0;
    public static int WORLD_BORDER_CENTER_Z = 0;
    public static int GRACE_PERIOD_TIMER = 600;
    public static int MOB_GRACE_PERIOD = 0;
    public static String ON_DEATH_ACTION = SPECTATE.name;
    public static int COUNTDOWN_TIMER_LENGTH = 10;
    public static Difficulty DIFFICULTY = Difficulty.EASY;
    public static boolean ENABLE_TIMESTAMPS = true;
    public static boolean ENABLE_PERFORMANCE_TRACKING = true;
    /* -- End of defaults written to uhc_config.properties -- */

    /* -- Other internal defaults -- */

    // Misc
    public static boolean PLAYER_HEAD_GOLDEN_APPLE = false;
    public static boolean WHISPER_TEAMMATE_DEAD_LOCATION = false;
    public static boolean RANDOM_FINAL_LOCATION = false;
    public static boolean ENABLE_DEATHCHAT = true;
    public static boolean DISABLE_END_GAME_AUTOMATICALLY = false;
    public static boolean DISABLE_WITCHES = false;
    public static boolean ALL_TREES_SPAWN_APPLES = true;
    public static boolean ENABLE_AUTOSMELT = false;
    public static boolean DISABLE_DEBUG_INFO = false;
    public static boolean DISABLE_ENDER_PEARL_DAMAGE = false;
    public static int SPLIT_WITHIN_TEAMS_SIZE = -1;

    // Craftables
    public static boolean CRAFTABLE_NOTCH_APPLE = false;
    public static boolean CRAFTABLE_PLAYER_HEAD = false;

    // World Border
    public static boolean WORLD_BORDER_IN_BOSSBAR = true;
    public static int WORLD_BORDER_FINAL_Y = -256;
    public static int WORLD_BORDER_Y_SHRINKING_PERIOD = 0;

    // Revives
    public static boolean REVIVE_ENABLED = false;
    public static int REVIVE_HP = 4;
    public static int REVIVE_LOSE_MAX_HEALTH = 4;
    public static boolean REVIVE_ANY_HEAD = true;

    // Loot chest
    public static boolean LOOT_CHEST_ENABLED = false;
    public static int LOOT_CHEST_GRACE_PERIOD = 0;
    public static int LOOT_CHEST_FREQUENCY = 300;
    public static int LOOT_CHEST_SPINS_PER_GEN = 5;
    public static int LOOT_CHEST_HIGH_LOOT_ODDS = 8;
    public static int LOOT_CHEST_MID_LOOT_ODDS = 40;

    // Additional Enchants
    public static boolean ADDITIONAL_ENCHANTS_SHIELD = false;
    public static boolean ADDITIONAL_ENCHANTS_TNT = false;

    // Evolving shields
    public static boolean ENABLE_EVOLVING_SHIELDS = false;
    public static int EVOLVING_SHIELDS_EXP_THRESHOLD = -1;
    public static int EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER = 4;

    /* -- End of defaults -- */

    public static Properties createDefaultConfig() {
        final Properties defaults = new Properties();
        defaults.setProperty(ConfigKey.WORLD_NAME.configName, WORLD_NAME);
        defaults.setProperty(WORLD_NAME_NETHER.configName, NETHER_WORLD_NAME);
        defaults.setProperty(WORLD_NAME_END.configName, END_WORLD_NAME);
        defaults.setProperty(ConfigKey.WORLD_BORDER_INITIAL_SIZE.configName, Integer.toString(WORLD_BORDER_INITIAL_SIZE));
        defaults.setProperty(ConfigKey.WORLD_BORDER_FINAL_SIZE.configName, Integer.toString(WORLD_BORDER_FINAL_SIZE));
        defaults.setProperty(ConfigKey.WORLD_BORDER_SHRINKING_PERIOD.configName, Integer.toString(WORLD_BORDER_SHRINKING_PERIOD));
        defaults.setProperty(ConfigKey.WORLD_BORDER_GRACE_PERIOD.configName, Integer.toString(WORLD_BORDER_GRACE_PERIOD));
        defaults.setProperty(ConfigKey.WORLD_BORDER_CENTER_X.configName, Integer.toString(WORLD_BORDER_CENTER_X));
        defaults.setProperty(ConfigKey.WORLD_BORDER_CENTER_Z.configName, Integer.toString(WORLD_BORDER_CENTER_Z));
        defaults.setProperty(ConfigKey.GRACE_PERIOD_TIMER.configName, Integer.toString(GRACE_PERIOD_TIMER));
        defaults.setProperty(ConfigKey.ON_DEATH_ACTION.configName, ON_DEATH_ACTION);
        defaults.setProperty(ConfigKey.COUNTDOWN_TIMER_LENGTH.configName, Integer.toString(COUNTDOWN_TIMER_LENGTH));
        defaults.setProperty(ConfigKey.DIFFICULTY.configName, DIFFICULTY.name());
        defaults.setProperty(ConfigKey.DISABLE_DEBUG_INFO.configName, Boolean.toString(DISABLE_DEBUG_INFO));
        defaults.setProperty(ConfigKey.ENABLE_TIMESTAMPS.configName, Boolean.toString(ENABLE_TIMESTAMPS));
        defaults.setProperty(ConfigKey.ENABLE_PERFORMANCE_TRACKING.configName, Boolean.toString(ENABLE_PERFORMANCE_TRACKING));
        return defaults;
    }

    public static void setDefaultGameRules(Config config) {
        Utils.setWorldEffects(List.of(config.getWorlds().getOverworld(), config.getWorlds().getNether(), config.getWorlds().getEnd()), (world) -> {
            world.setGameRule(GameRules.NATURAL_HEALTH_REGENERATION, false);
            world.setGameRule(GameRules.SPAWN_PHANTOMS, false);
            world.setGameRule(GameRules.LOCATOR_BAR, false);
            world.setGameRule(GameRules.MAX_BLOCK_MODIFICATIONS, MAXIMUM_FINAL_SIZE_FOR_Y_SHRINK * MAXIMUM_FINAL_SIZE_FOR_Y_SHRINK); // square the maximum final size to be used in y shrink
            // set pvp to false, will be enabled when /uhc start is ran
            world.setGameRule(GameRules.PVP, false);
            world.setGameRule(GameRules.FALL_DAMAGE, false);
        });
        config.getPlugin().setCountingDown(false);
        Bukkit.getOnlinePlayers().forEach(player -> player.setGameMode(GameMode.ADVENTURE));
        final Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        final Objective healthObjective;
        if (board.getObjective(HEALTH_OBJECTIVE) == null) {
            healthObjective = board.registerNewObjective(HEALTH_OBJECTIVE, Criteria.HEALTH, Component.text("Health"));
        } else {
            healthObjective = board.getObjective(HEALTH_OBJECTIVE);
        }
        healthObjective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
        healthObjective.setRenderType(RenderType.HEARTS);
    }
}
