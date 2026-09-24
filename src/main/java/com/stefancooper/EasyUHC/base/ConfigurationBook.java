package com.stefancooper.EasyUHC.base;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.base.records.Coordinate;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShield;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Difficulty;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.stefancooper.EasyUHC.base.ConfigKey.*;
import static com.stefancooper.EasyUHC.base.ConfigKey.ALL_TREES_SPAWN_APPLES;
import static com.stefancooper.EasyUHC.base.ConfigKey.CRAFTABLE_NOTCH_APPLE;
import static com.stefancooper.EasyUHC.base.ConfigKey.CRAFTABLE_PLAYER_HEAD;
import static com.stefancooper.EasyUHC.base.ConfigKey.ENABLE_DEATH_CHAT;
import static com.stefancooper.EasyUHC.base.ConfigKey.ENABLE_PERFORMANCE_TRACKING;
import static com.stefancooper.EasyUHC.base.ConfigKey.GRACE_PERIOD_TIMER;
import static com.stefancooper.EasyUHC.base.ConfigKey.LOOT_CHEST_ENABLED;
import static com.stefancooper.EasyUHC.base.ConfigKey.PLAYER_HEAD_GOLDEN_APPLE;
import static com.stefancooper.EasyUHC.base.ConfigKey.RANDOM_FINAL_LOCATION;
import static com.stefancooper.EasyUHC.base.ConfigKey.REVIVE_ENABLED;
import static com.stefancooper.EasyUHC.base.ConfigKey.REVIVE_HP;
import static com.stefancooper.EasyUHC.base.ConfigKey.SPLIT_WITHIN_TEAMS_SIZE;
import static com.stefancooper.EasyUHC.base.ConfigKey.TEAM_BLUE;
import static com.stefancooper.EasyUHC.base.ConfigKey.TEAM_GREEN;
import static com.stefancooper.EasyUHC.base.ConfigKey.TEAM_ORANGE;
import static com.stefancooper.EasyUHC.base.ConfigKey.TEAM_PINK;
import static com.stefancooper.EasyUHC.base.ConfigKey.TEAM_PURPLE;
import static com.stefancooper.EasyUHC.base.ConfigKey.TEAM_RED;
import static com.stefancooper.EasyUHC.base.ConfigKey.TEAM_YELLOW;
import static com.stefancooper.EasyUHC.base.ConfigKey.WHISPER_TEAMMATE_DEAD_LOCATION;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_CENTER_X;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_CENTER_Z;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_FINAL_SIZE;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_FINAL_Y;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_GRACE_PERIOD;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_INITIAL_SIZE;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_IN_BOSSBAR;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_SHRINKING_PERIOD;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_SPAWN_PADDING;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_Y_SHRINKING_PERIOD;

public class ConfigurationBook {

    private static final Component defaultWarning = Component.textOfChildren(
            Component.text(" This is the", Style.style(NamedTextColor.GOLD)),
            Component.text(" default ", Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)),
            Component.text("value.", Style.style(NamedTextColor.GOLD))
    );
    private static final Component unusualWarning = Component.textOfChildren(
            Component.text(" This is an", Style.style(NamedTextColor.RED)),
            Component.text(" unusual ", Style.style(NamedTextColor.RED, TextDecoration.BOLD)),
            Component.text("value.", Style.style(NamedTextColor.RED))
    );
    private static final Component disabledWarning = Component.textOfChildren(
            Component.text(" This value means it is", Style.style(NamedTextColor.DARK_RED)),
            Component.text(" disabled.", Style.style(NamedTextColor.DARK_RED, TextDecoration.BOLD))
    );
    private static final Component conflictWarning = Component.textOfChildren(
            Component.text(" Disabled", Style.style(NamedTextColor.DARK_RED, TextDecoration.BOLD)),
            Component.text(" as a result of another config value.", Style.style(NamedTextColor.DARK_RED))
    );

    private static Component title(final String text) {
        return Component.textOfChildren(
                Component.text(text, Style.style(NamedTextColor.DARK_AQUA, TextDecoration.BOLD)),
                Component.text("\n\n")
        );
    }

    private static Component configValue(final String key, @Nullable final String value, @Nullable final Component warning) {
        final String constructedValue = value != null ? value : "Not Set";
        if (warning == null) {
            return Component.textOfChildren(
                    Component.text(key, Style.style(NamedTextColor.DARK_GRAY)),
                    Component.text(constructedValue, Style.style(NamedTextColor.DARK_GRAY, TextDecoration.ITALIC)),
                    Component.text("\n")
            );
        } else {
            return Component.textOfChildren(
                    Component.text(key, Style.style(NamedTextColor.DARK_GRAY)),
                    Component.text(constructedValue, Style.style(NamedTextColor.DARK_GRAY, TextDecoration.ITALIC)),
                    warning,
                    Component.text("\n")
            );
        }
    }

    public static void giveConfigurationBook(final Player player, final Config config) {
        // Configs ----------------------------------------------------------------------------------------------------------------------------------------
        // General
        final int UHC_COUNTDOWN = config.getProperty(COUNTDOWN_TIMER_LENGTH, Defaults.COUNTDOWN_TIMER_LENGTH);
        final Difficulty GAME_DIFFICULTY = config.getProperty(DIFFICULTY, Defaults.DIFFICULTY);
        @Nullable final Integer WORLD_SPAWN_X = config.getProperty(ConfigKey.WORLD_SPAWN_X);
        @Nullable final Integer WORLD_SPAWN_Y = config.getProperty(ConfigKey.WORLD_SPAWN_Y);
        @Nullable final Integer WORLD_SPAWN_Z = config.getProperty(ConfigKey.WORLD_SPAWN_Z);
        final boolean TIMESTAMPS = config.getProperty(ENABLE_TIMESTAMPS, Defaults.ENABLE_TIMESTAMPS);
        final boolean DEATH_CHAT = config.getProperty(ENABLE_DEATH_CHAT, Defaults.ENABLE_DEATHCHAT);
        final boolean PERFORMANCE_TRACKING = config.getProperty(ENABLE_PERFORMANCE_TRACKING, Defaults.ENABLE_PERFORMANCE_TRACKING);
        final int PVP_GRACE_PERIOD = config.getProperty(GRACE_PERIOD_TIMER, Defaults.GRACE_PERIOD_TIMER);
        final int MOB_GRACE_PERIOD = config.getProperty(ConfigKey.MOB_GRACE_PERIOD, Defaults.MOB_GRACE_PERIOD);

        // UHC Settings
        final String ON_DEATH_ACTION = config.getProperty(ConfigKey.ON_DEATH_ACTION, Defaults.ON_DEATH_ACTION);
        final boolean ENABLE_PLAYER_HEAD_GAPPLES = config.getProperty(PLAYER_HEAD_GOLDEN_APPLE, Defaults.PLAYER_HEAD_GOLDEN_APPLE);
        final boolean USE_RANDOM_FINAL_LOCATION = config.getProperty(RANDOM_FINAL_LOCATION, Defaults.RANDOM_FINAL_LOCATION);
        final boolean ENABLE_CRAFTABLE_NOTCH_APPLES = config.getProperty(CRAFTABLE_NOTCH_APPLE, Defaults.CRAFTABLE_NOTCH_APPLE);
        final boolean ENABLE_CRAFTABLE_PLAYER_HEADS = config.getProperty(CRAFTABLE_PLAYER_HEAD, Defaults.CRAFTABLE_PLAYER_HEAD);
        final boolean ENABLE_WHISPER_DEAD_TEAMMATE_LOC = config.getProperty(WHISPER_TEAMMATE_DEAD_LOCATION, Defaults.WHISPER_TEAMMATE_DEAD_LOCATION);
        final boolean ENABLE_ALL_TREES_SPAWN_APPLES = config.getProperty(ALL_TREES_SPAWN_APPLES, Defaults.ALL_TREES_SPAWN_APPLES);
        final boolean ENABLE_AUTOSMELT = config.getProperty(ConfigKey.ENABLE_AUTOSMELT, Defaults.ENABLE_AUTOSMELT);
        final boolean DISABLE_DEBUG_INFO = config.getProperty(ConfigKey.DISABLE_DEBUG_INFO, Defaults.DISABLE_DEBUG_INFO);
        final boolean DISABLE_ENDER_PEARL_DAMAGE = config.getProperty(ConfigKey.DISABLE_ENDER_PEARL_DAMAGE, Defaults.DISABLE_ENDER_PEARL_DAMAGE);
        final boolean DISABLE_WITCHES = config.getProperty(ConfigKey.DISABLE_WITCHES, Defaults.DISABLE_WITCHES);

        // World Border
        final int CENTER_X = config.getProperty(WORLD_BORDER_CENTER_X, Defaults.WORLD_BORDER_CENTER_X);
        final int CENTER_Z = config.getProperty(WORLD_BORDER_CENTER_Z, Defaults.WORLD_BORDER_CENTER_Z);
        final int FINAL_Y = config.getProperty(WORLD_BORDER_FINAL_Y, Defaults.WORLD_BORDER_FINAL_Y);
        final int INITIAL_BORDER_SIZE = config.getProperty(WORLD_BORDER_INITIAL_SIZE, Defaults.WORLD_BORDER_INITIAL_SIZE);
        final int FINAL_BORDER_SIZE = config.getProperty(WORLD_BORDER_FINAL_SIZE, Defaults.WORLD_BORDER_FINAL_SIZE);
        final int BORDER_SHRINKING_TIME = config.getProperty(WORLD_BORDER_SHRINKING_PERIOD, Defaults.WORLD_BORDER_SHRINKING_PERIOD);
        final int BORDER_GRACE_PERIOD = config.getProperty(WORLD_BORDER_GRACE_PERIOD, Defaults.WORLD_BORDER_GRACE_PERIOD);
        final int Y_SHRINKING_TIME = config.getProperty(WORLD_BORDER_Y_SHRINKING_PERIOD, Defaults.WORLD_BORDER_Y_SHRINKING_PERIOD);
        final int BORDER_SPAWN_PADDING = config.getProperty(WORLD_BORDER_SPAWN_PADDING, Defaults.WORLD_BORDER_SPAWN_PADDING);
        final boolean ENABLE_WORLD_BORDER_BOSS_BAR = config.getProperty(WORLD_BORDER_IN_BOSSBAR, Defaults.WORLD_BORDER_IN_BOSSBAR);

        // Teams
        final Integer SPLIT_WITHIN_TEAM_SIZE = config.getProperty(SPLIT_WITHIN_TEAMS_SIZE, Defaults.SPLIT_WITHIN_TEAMS_SIZE);
        @Nullable final String RED = config.getProperty(TEAM_RED);
        @Nullable final String BLUE = config.getProperty(TEAM_BLUE);
        @Nullable final String GREEN = config.getProperty(TEAM_GREEN);
        @Nullable final String ORANGE = config.getProperty(TEAM_ORANGE);
        @Nullable final String PINK = config.getProperty(TEAM_PINK);
        @Nullable final String PURPLE = config.getProperty(TEAM_PURPLE);
        @Nullable final String YELLOW = config.getProperty(TEAM_YELLOW);

        // Revive
        final boolean ENABLE_REVIVE = config.getProperty(REVIVE_ENABLED, Defaults.REVIVE_ENABLED);
        final int REVIVE_SPAWN_HP = config.getProperty(REVIVE_HP, Defaults.REVIVE_HP);
        final boolean REVIVE_ANY_HEAD = config.getProperty(ConfigKey.REVIVE_ANY_HEAD, Defaults.REVIVE_ANY_HEAD);
        final int REVIVE_LOSE_MAX_HEALTH = config.getProperty(ConfigKey.REVIVE_LOSE_MAX_HEALTH, Defaults.REVIVE_LOSE_MAX_HEALTH);

        // UHC Loot
        final boolean ENABLE_UHC_LOOT = config.getProperty(LOOT_CHEST_ENABLED, Defaults.LOOT_CHEST_ENABLED);
        final int LOOT_CHEST_GRACE_PERIOD = config.getProperty(ConfigKey.LOOT_CHEST_GRACE_PERIOD, Defaults.LOOT_CHEST_GRACE_PERIOD);
        final String LOOT_CHEST_X_RANGE = config.getProperty(ConfigKey.LOOT_CHEST_X_RANGE, Defaults.LOOT_CHEST_X_RANGE);
        final String LOOT_CHEST_Z_RANGE = config.getProperty(ConfigKey.LOOT_CHEST_Z_RANGE, Defaults.LOOT_CHEST_Z_RANGE);
        final int LOOT_CHEST_FREQUENCY = config.getProperty(ConfigKey.LOOT_CHEST_FREQUENCY, Defaults.LOOT_CHEST_FREQUENCY);
        final int LOOT_CHEST_HIGH_LOOT_ODDS = config.getProperty(ConfigKey.LOOT_CHEST_HIGH_LOOT_ODDS, Defaults.LOOT_CHEST_HIGH_LOOT_ODDS);
        final int LOOT_CHEST_MID_LOOT_ODDS = config.getProperty(ConfigKey.LOOT_CHEST_MID_LOOT_ODDS, Defaults.LOOT_CHEST_MID_LOOT_ODDS);
        final int LOOT_CHEST_SPINS_PER_GEN = config.getProperty(ConfigKey.LOOT_CHEST_SPINS_PER_GEN, Defaults.LOOT_CHEST_SPINS_PER_GEN);

        // Enchantments
        final boolean ADDITIONAL_ENCHANTS_SHIELD = config.getProperty(ConfigKey.ADDITIONAL_ENCHANTS_SHIELD, Defaults.ADDITIONAL_ENCHANTS_SHIELD);
        final boolean ADDITIONAL_ENCHANTS_TNT = config.getProperty(ConfigKey.ADDITIONAL_ENCHANTS_TNT, Defaults.ADDITIONAL_ENCHANTS_TNT);

        // Evolving Shields
        final boolean ENABLE_EVOLVING_SHIELDS = config.getProperty(ConfigKey.ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS);
        final int EVOLVING_SHIELDS_EXP_THRESHOLD = config.getProperty(ConfigKey.EVOLVING_SHIELDS_EXP_THRESHOLD, Defaults.EVOLVING_SHIELDS_EXP_THRESHOLD);
        final int EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER = config.getProperty(ConfigKey.EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER, Defaults.EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER);

        // Projected Coordinates
        boolean errorProjectingPlayers = false;
        @Nullable List<Coordinate> coordinates = null;
        try {
            final SpreadPlayers spread = new SpreadPlayers(config, false);
            coordinates = spread.getCalculatedCoordinates();
        } catch (final RuntimeException e) {
            errorProjectingPlayers = true;
        }

        // WARNINGS ----------------------------------------------------------------------------------------------------------------------------------------
        final Component countdownWarning = UHC_COUNTDOWN == Defaults.COUNTDOWN_TIMER_LENGTH ? defaultWarning : UHC_COUNTDOWN < 10 || UHC_COUNTDOWN > 60 ? unusualWarning : null;
        final Component difficultyWarning = GAME_DIFFICULTY == Difficulty.PEACEFUL ? unusualWarning : null;
        final Component worldSpawnWarning = WORLD_SPAWN_X == null || WORLD_SPAWN_Y == null || WORLD_SPAWN_Z == null ? disabledWarning : (WORLD_SPAWN_X < 2000 && WORLD_SPAWN_X > -2000) || (WORLD_SPAWN_Z < 2000 && WORLD_SPAWN_Z > -2000) ? unusualWarning : null;
        final Component timestampWarning = !TIMESTAMPS ? disabledWarning : null;
        final Component deathChatWarning = !DEATH_CHAT ? disabledWarning : null;
        final Component performanceTrackingWarning = !PERFORMANCE_TRACKING ? disabledWarning : null;
        final Component gracePeriodWarning = PVP_GRACE_PERIOD == Defaults.GRACE_PERIOD_TIMER ? defaultWarning : PVP_GRACE_PERIOD <= 0 || PVP_GRACE_PERIOD > 600 ? unusualWarning : null;
        final Component mobGracePeriodWarning = MOB_GRACE_PERIOD == 0 ? disabledWarning : MOB_GRACE_PERIOD > 300 ? unusualWarning : null;

        // UHC Settings
        final Component playerHeadGappleWarning = !ENABLE_PLAYER_HEAD_GAPPLES ? disabledWarning : null;
        final Component onDeathActionWarning = !ON_DEATH_ACTION.equalsIgnoreCase(Defaults.ON_DEATH_ACTION) ? unusualWarning : null;
        final Component randomFinalLocationWarning = !USE_RANDOM_FINAL_LOCATION ? disabledWarning : null;
        final Component craftableNotchAppleWarning = !ENABLE_CRAFTABLE_NOTCH_APPLES ? disabledWarning : null;
        final Component craftablePlayerHeadWarning = !ENABLE_CRAFTABLE_PLAYER_HEADS ? disabledWarning : null;
        final Component whisperDeadLocWarning = !ENABLE_WHISPER_DEAD_TEAMMATE_LOC ? disabledWarning : null;
        final Component allTreesApplesWarning = !ENABLE_ALL_TREES_SPAWN_APPLES ? disabledWarning : null;
        final Component debugInfoWarning = !DISABLE_DEBUG_INFO ? disabledWarning : null;
        final Component enderPearlDamWarning = !DISABLE_ENDER_PEARL_DAMAGE ? disabledWarning : null;
        final Component autosmeltWarning = !ENABLE_AUTOSMELT ? disabledWarning : null;
        final Component witchesWarning = !DISABLE_WITCHES ? disabledWarning : null;

        // Teams
        final Component splitWithinTeamSizeWarning = SPLIT_WITHIN_TEAM_SIZE == Defaults.SPLIT_WITHIN_TEAMS_SIZE ? disabledWarning : SPLIT_WITHIN_TEAM_SIZE > 2 ? unusualWarning : null;

        // Revive
        final Component reviveEnabledWarning = !ENABLE_REVIVE ? disabledWarning : null;
        final Component reviveSpawnHpWarning = !ENABLE_REVIVE ? conflictWarning : REVIVE_SPAWN_HP == Defaults.REVIVE_HP ? defaultWarning : REVIVE_SPAWN_HP > 8 ? unusualWarning : null;
        final Component reviveLoseMaxHPWarning = !ENABLE_REVIVE ? conflictWarning : REVIVE_LOSE_MAX_HEALTH == Defaults.REVIVE_LOSE_MAX_HEALTH ? defaultWarning : REVIVE_LOSE_MAX_HEALTH > 4 ? unusualWarning : null;
        final Component reviveAnyHeadWarning = !ENABLE_REVIVE ? conflictWarning : !REVIVE_ANY_HEAD ? disabledWarning : null;

        // UHC Loot
        final Component uhcLootEnabledWarning = !ENABLE_UHC_LOOT ? disabledWarning : null;
        final Component lootChestGracePeriodWarning = !ENABLE_UHC_LOOT ? conflictWarning : LOOT_CHEST_GRACE_PERIOD == Defaults.LOOT_CHEST_GRACE_PERIOD ? defaultWarning : LOOT_CHEST_GRACE_PERIOD > 300 ? unusualWarning : null;
        final Component lootChestFrequencyWarning = !ENABLE_UHC_LOOT ? conflictWarning : LOOT_CHEST_FREQUENCY == Defaults.LOOT_CHEST_FREQUENCY ? defaultWarning : LOOT_CHEST_GRACE_PERIOD < 300 ? unusualWarning : null;
        final Component lootChestHighTierLootOddsWarning = !ENABLE_UHC_LOOT ? conflictWarning : LOOT_CHEST_HIGH_LOOT_ODDS == Defaults.LOOT_CHEST_HIGH_LOOT_ODDS ? defaultWarning : LOOT_CHEST_HIGH_LOOT_ODDS > 10 || LOOT_CHEST_HIGH_LOOT_ODDS < 5 ? unusualWarning : null;
        final Component lootChestMidTierLootOddsWarning = !ENABLE_UHC_LOOT ? conflictWarning : LOOT_CHEST_MID_LOOT_ODDS == Defaults.LOOT_CHEST_MID_LOOT_ODDS ? defaultWarning : LOOT_CHEST_MID_LOOT_ODDS > 50 || LOOT_CHEST_MID_LOOT_ODDS < 25 ? unusualWarning : null;
        final Component lootChestSpinsPerGenWarning = !ENABLE_UHC_LOOT ? conflictWarning : LOOT_CHEST_SPINS_PER_GEN == Defaults.LOOT_CHEST_SPINS_PER_GEN ? defaultWarning : LOOT_CHEST_SPINS_PER_GEN > 8 || LOOT_CHEST_SPINS_PER_GEN < 5 ? unusualWarning : null;
        final Component lootChestRangeWarning = !ENABLE_UHC_LOOT ? conflictWarning : !LOOT_CHEST_X_RANGE.equalsIgnoreCase(Defaults.LOOT_CHEST_X_RANGE) || !LOOT_CHEST_Z_RANGE.equalsIgnoreCase(Defaults.LOOT_CHEST_Z_RANGE) ? unusualWarning : null;

        // Evolving Shields
        final Component evolvingShieldsEnabledWarning = !ENABLE_EVOLVING_SHIELDS ? disabledWarning : null;
        final Component evolvingShieldsExpThresholdWarning = !ENABLE_EVOLVING_SHIELDS ? conflictWarning : EVOLVING_SHIELDS_EXP_THRESHOLD == Defaults.EVOLVING_SHIELDS_EXP_THRESHOLD ? disabledWarning : EVOLVING_SHIELDS_EXP_THRESHOLD > EvolvingShield.STAGE_4 || EVOLVING_SHIELDS_EXP_THRESHOLD <= EvolvingShield.STAGE_1 ? unusualWarning : null;
        final Component evolvingShieldsMCEXPMultiplierWarning = !ENABLE_EVOLVING_SHIELDS ? conflictWarning : EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER != Defaults.EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER ? unusualWarning : null;

        // Enchantments
        final Component customShieldEnchantsWarning = ENABLE_EVOLVING_SHIELDS ? conflictWarning : !ADDITIONAL_ENCHANTS_SHIELD ? disabledWarning : null;
        final Component customTNTEnchantsWarning = !ADDITIONAL_ENCHANTS_TNT ? disabledWarning : null;

        // World border
        final Component centerWarning = CENTER_X != Defaults.WORLD_BORDER_CENTER_X || CENTER_Z != Defaults.WORLD_BORDER_CENTER_Z ? unusualWarning : null;
        final Component initialSizeWarning = INITIAL_BORDER_SIZE == Defaults.WORLD_BORDER_INITIAL_SIZE ? defaultWarning : INITIAL_BORDER_SIZE > 2000 || INITIAL_BORDER_SIZE < 1000 ? unusualWarning : null;
        final Component finalSizeWarning = FINAL_BORDER_SIZE == Defaults.WORLD_BORDER_FINAL_SIZE ? defaultWarning : FINAL_BORDER_SIZE > 200 || FINAL_BORDER_SIZE < 75 ? unusualWarning : null;
        final Component shrinkingTimeWarning = BORDER_SHRINKING_TIME == Defaults.WORLD_BORDER_SHRINKING_PERIOD ? defaultWarning : BORDER_SHRINKING_TIME > 7200 || BORDER_SHRINKING_TIME < 1800 ? unusualWarning : null;
        final Component borderGracePeriodWarning = BORDER_GRACE_PERIOD == Defaults.WORLD_BORDER_GRACE_PERIOD ? defaultWarning : BORDER_GRACE_PERIOD > 1800 || BORDER_GRACE_PERIOD < 600 ? unusualWarning : null;
        final Component yBorderWarning = FINAL_Y == Defaults.WORLD_BORDER_FINAL_Y ? disabledWarning : FINAL_Y > 32 || FINAL_Y < 0 ? unusualWarning : null;
        final Component yShrinkingTimeWarning = Y_SHRINKING_TIME == Defaults.WORLD_BORDER_Y_SHRINKING_PERIOD ? disabledWarning : Y_SHRINKING_TIME > 900 || Y_SHRINKING_TIME < 300 ? unusualWarning : null;
        final Component borderPaddingWarning = BORDER_SPAWN_PADDING == Defaults.WORLD_BORDER_Y_SHRINKING_PERIOD ? disabledWarning : BORDER_SPAWN_PADDING > 50 || BORDER_SPAWN_PADDING < 0 ? unusualWarning : null;
        final Component worldBorderInBossBarWarning = !ENABLE_WORLD_BORDER_BOSS_BAR ? disabledWarning : null;

        // Projected coordinates
        final Component projectionWarning = errorProjectingPlayers ? Component.text("Error projecting players. Likely due to chunks not being loaded.", Style.style(NamedTextColor.DARK_RED, TextDecoration.BOLD)) : null;

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);

        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setTitle("Configuration");
        meta.setAuthor("EasyUHC");
        meta.addPages(
                // General Settings
                Component.textOfChildren(
                        title("General Settings:"),
                        configValue("Countdown Time: ", String.valueOf(UHC_COUNTDOWN), countdownWarning),
                        configValue("Difficulty: ", GAME_DIFFICULTY.name(), difficultyWarning),
                        configValue("World spawn: ", String.format("%s,%s,%s", WORLD_SPAWN_X, WORLD_SPAWN_Y, WORLD_SPAWN_Z), worldSpawnWarning)
                ),
                Component.textOfChildren(
                        title("General Settings (cont):"),
                        configValue("Timestamps Enabled: ", String.valueOf(TIMESTAMPS), timestampWarning),
                        configValue("Death Chat Enabled: ", String.valueOf(DEATH_CHAT), deathChatWarning),
                        configValue("Performance Tracking Enabled: ", String.valueOf(PERFORMANCE_TRACKING), performanceTrackingWarning)
                ),
                Component.textOfChildren(
                        title("General Settings (cont):"),
                        configValue("PVP Grace Period: ", String.valueOf(PVP_GRACE_PERIOD), gracePeriodWarning),
                        configValue("Mob Grace Period: ", String.valueOf(MOB_GRACE_PERIOD), mobGracePeriodWarning)
                ),
                // World Border settings
                Component.textOfChildren(
                        title("World Border Settings:"),
                        configValue("Center: ", String.format("%s,%s", CENTER_X, CENTER_Z), centerWarning),
                        configValue("Initial Size: ", String.valueOf(INITIAL_BORDER_SIZE), initialSizeWarning),
                        configValue("Final Size: ", String.valueOf(FINAL_BORDER_SIZE), finalSizeWarning)
                ),
                Component.textOfChildren(
                        title("World Border Settings (cont):"),
                        configValue("Shrinking period: ", String.format("%s minutes", BORDER_SHRINKING_TIME / 60), shrinkingTimeWarning),
                        configValue("Time before shrink: ", String.format("%s minutes", BORDER_GRACE_PERIOD / 60), borderGracePeriodWarning),
                        configValue("Final Y border: ", String.valueOf(FINAL_Y), yBorderWarning)
                ),
                Component.textOfChildren(
                        title("World Border Settings (cont):"),
                        configValue("Y Shrinking Period: ", String.format("%s minutes", Y_SHRINKING_TIME / 60), yShrinkingTimeWarning),
                        configValue("Spawn Padding: ", String.valueOf(BORDER_SPAWN_PADDING), borderPaddingWarning),
                        configValue("Show Border Progress: ", String.valueOf(ENABLE_WORLD_BORDER_BOSS_BAR), worldBorderInBossBarWarning)
                ),
                // UHC Settings
                Component.textOfChildren(
                        title("UHC Settings:"),
                        configValue("Dead Player Heads Enabled: ", String.valueOf(ENABLE_PLAYER_HEAD_GAPPLES), playerHeadGappleWarning),
                        configValue("Craftable Notch Apples Enabled: ", String.valueOf(ENABLE_CRAFTABLE_NOTCH_APPLES), craftableNotchAppleWarning),
                        configValue("Craftable Player Heads Enabled: ", String.valueOf(ENABLE_CRAFTABLE_PLAYER_HEADS), craftablePlayerHeadWarning)
                ),
                Component.textOfChildren(
                        title("UHC Settings (cont):"),
                        configValue("All Trees Drop Apples Enabled: ", String.valueOf(ENABLE_ALL_TREES_SPAWN_APPLES), allTreesApplesWarning),
                        configValue("Whisper Dead Teammate Location Enabled: ", String.valueOf(ENABLE_WHISPER_DEAD_TEAMMATE_LOC), whisperDeadLocWarning),
                        configValue("Autosmelt Enabled: ", String.valueOf(ENABLE_AUTOSMELT), autosmeltWarning)
                ),
                Component.textOfChildren(
                        title("UHC Settings (cont):"),
                        configValue("Block F3 Debug: ", String.valueOf(DISABLE_DEBUG_INFO), debugInfoWarning),
                        configValue("No Ender Pearl Damage: ", String.valueOf(DISABLE_ENDER_PEARL_DAMAGE), enderPearlDamWarning),
                        configValue("No Witches: ", String.valueOf(DISABLE_WITCHES), witchesWarning)
                ),
                Component.textOfChildren(
                        title("UHC Settings (cont):"),
                        configValue("Enable Random Final Location: ", String.valueOf(USE_RANDOM_FINAL_LOCATION), randomFinalLocationWarning),
                        configValue("On Death Action: ", ON_DEATH_ACTION, onDeathActionWarning)
                ),
                // Teams
                Component.textOfChildren(
                        title("Teams:"),
                        configValue("Team Split Size: ", String.valueOf(SPLIT_WITHIN_TEAM_SIZE), splitWithinTeamSizeWarning),
                        configValue("Team Red: ", RED, null),
                        configValue("Team Blue: ", BLUE, null)
                ),
                Component.textOfChildren(
                        title("Teams (cont):"),
                        configValue("Team Yellow: ", YELLOW, null),
                        configValue("Team Orange: ", ORANGE, null),
                        configValue("Team Pink: ", PINK, null)
                ),
                Component.textOfChildren(
                        title("Teams (cont):"),
                        configValue("Team Green: ", GREEN, null),
                        configValue("Team Purple: ", PURPLE, null)
                ),
                // Revive settings
                Component.textOfChildren(
                        title("Revive:"),
                        configValue("Revive Enabled: ", String.valueOf(ENABLE_REVIVE), reviveEnabledWarning),
                        configValue("Revive With Any Head: ", String.valueOf(REVIVE_ANY_HEAD), reviveAnyHeadWarning)
                ),
                Component.textOfChildren(
                        title("Revive (cont):"),
                        configValue("Revive Lose Max HP: ", String.valueOf(REVIVE_LOSE_MAX_HEALTH), reviveLoseMaxHPWarning),
                        configValue("Revive Spawn HP: ", String.valueOf(REVIVE_SPAWN_HP), reviveSpawnHpWarning)
                ),
                // UHC Loot settings
                Component.textOfChildren(
                        title("UHC Loot:"),
                        configValue("UHC Loot Enabled: ", String.valueOf(ENABLE_UHC_LOOT), uhcLootEnabledWarning),
                        configValue("Grace Period: ", String.valueOf(LOOT_CHEST_GRACE_PERIOD), lootChestGracePeriodWarning),
                        configValue("Frequency: ", String.valueOf(LOOT_CHEST_FREQUENCY), lootChestFrequencyWarning)
                ),
                Component.textOfChildren(
                        title("UHC Loot (cont):"),
                        configValue("Spins Per Spawn: ", String.valueOf(LOOT_CHEST_SPINS_PER_GEN), lootChestSpinsPerGenWarning),
                        configValue("High Tier Odds (per spin): ", String.format("%s%%", LOOT_CHEST_HIGH_LOOT_ODDS), lootChestHighTierLootOddsWarning),
                        configValue("Mid Tier Odds (per spin): ", String.format("%s%%", LOOT_CHEST_MID_LOOT_ODDS), lootChestMidTierLootOddsWarning)
                ),
                Component.textOfChildren(
                        title("UHC Loot (cont):"),
                        configValue("Spawn Range: ", String.format("X: %s, Z: %s", LOOT_CHEST_X_RANGE, LOOT_CHEST_Z_RANGE), lootChestRangeWarning)
                ),
                // Evolving shields settings
                Component.textOfChildren(
                        title("Evolving Shields:"),
                        configValue("Evolving Shields Enabled: ", String.valueOf(ENABLE_EVOLVING_SHIELDS), evolvingShieldsEnabledWarning),
                        configValue("EXP Threshold: ", String.valueOf(EVOLVING_SHIELDS_EXP_THRESHOLD), evolvingShieldsExpThresholdWarning),
                        configValue("EXP Multiplier: ", String.valueOf(EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER), evolvingShieldsMCEXPMultiplierWarning)
                ),
                // Enchantment settings
                Component.textOfChildren(
                        title("Enchantments:"),
                        configValue("Shield Enchantments: ", String.valueOf(ADDITIONAL_ENCHANTS_SHIELD), customShieldEnchantsWarning),
                        configValue("TNT Enchantments: ", String.valueOf(ADDITIONAL_ENCHANTS_TNT), customTNTEnchantsWarning)
                ),
                // Projected spawn locations
                Component.textOfChildren(
                        title("Projected Spawn Locations:"),
                        configValue("Projected Coordinates: ", coordinates != null ? coordinates.toString() : "ERROR: ", projectionWarning)
                        // plotted/mapped spawn locations for each team
                        // have all of the chunks been loaded?
                )
        );
        book.setItemMeta(meta);
        player.getInventory().addItem(book);
    }
}
