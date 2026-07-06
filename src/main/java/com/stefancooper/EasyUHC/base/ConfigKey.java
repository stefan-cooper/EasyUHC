package com.stefancooper.EasyUHC.base;

public enum ConfigKey {

    // ----- Required config -----
    WORLD_NAME("world.name"), // Name of the minecraft world
    WORLD_NAME_NETHER("nether.world.name"), // Name of the nether world
    WORLD_NAME_END("end.world.name"), // Name of the end world
    GRACE_PERIOD_TIMER("grace.period.timer"), // Grace period time (in seconds) before PVP is enabled
    COUNTDOWN_TIMER_LENGTH("countdown.timer.length"), // Countdown to start the game after UHC start command issued
    DIFFICULTY("difficulty"), // Difficulty during the UHC
    WORLD_BORDER_INITIAL_SIZE("world.border.initial.size"), // Initial size world border at start of the UHC
    WORLD_BORDER_FINAL_SIZE("world.border.final.size"), // Final size of the world border at the end of the UHC
    WORLD_BORDER_SHRINKING_PERIOD("world.border.shrinking.period"), // Time (in seconds) to shrink from the initial size to the final size
    WORLD_BORDER_GRACE_PERIOD("world.border.grace.period"), // Grace period time (in seconds) before the border will begin to shrink
    WORLD_BORDER_CENTER_X("world.border.center.x"), // World border center X coord
    WORLD_BORDER_CENTER_Z("world.border.center.z"), // World border center Z coord
    WORLD_BORDER_FINAL_Y("world.border.final.y"), // World border final Y coord
    WORLD_BORDER_Y_SHRINKING_PERIOD("world.border.y.shrinking.period"), // World border time to shrink to the final y coord
    ON_DEATH_ACTION("on.death.action"), // Action to undertake when a player dies ("spectate" | "kick")
    MOB_GRACE_PERIOD("mob.grace.period"), // period of time that the game remains in peaceful mode before enabling the difficulty
    // ----- End required config -----

    // Team Enums
    TEAM_RED("team.red"), // Team red players
    TEAM_YELLOW("team.yellow"), // Team yellow players
    TEAM_GREEN("team.green"), // Team green players
    TEAM_BLUE("team.blue"), // Team blue players
    TEAM_ORANGE("team.orange"), // Team orange players
    TEAM_PINK("team.pink"), // Team pink players
    TEAM_PURPLE("team.purple"), // Team purple players

    // Misc Enums
    PLAYER_HEAD_GOLDEN_APPLE("player.head.golden.apple"), // (optional) drop player heads who are killed that can be crafted into golden apples
    ENABLE_TIMESTAMPS("enable.timestamps"), // Get timestamps in txt file of notable events
    ENABLE_DEATH_CHAT("enable.death.chat"), // active players cannot see messages from spectators
    DISABLE_END_GAME_AUTOMATICALLY("disable.end.game.automatically"), // disable the end game automatically (mainly for dev purposes)
    RANDOM_FINAL_LOCATION("random.final.location"), // Use a random final location
    WORLD_BORDER_IN_BOSSBAR("world.border.in.bossbar"), // Add the world border into the bossbar
    WORLD_SPAWN_X("world.spawn.x"), // X coordinate for world spawn when a UHC is not active
    WORLD_SPAWN_Y("world.spawn.y"), // Y coordinate for world spawn when a UHC is not active
    WORLD_SPAWN_Z("world.spawn.z"), // Z coordinate for world spawn when a UHC is not active
    DISABLE_WITCHES("disable.witches"), // disable witch spawns
    CRAFTABLE_NOTCH_APPLE("craftable.notch.apple"), // re-add craftable notch apple
    CRAFTABLE_PLAYER_HEAD("craftable.player.head"), // add craftable player head
    WHISPER_TEAMMATE_DEAD_LOCATION("whisper.teammate.dead.location"), // whisper the location of dead teammates when they die
    ALL_TREES_SPAWN_APPLES("all.trees.spawn.apples"), // make it so that all trees spawn apples
    ENABLE_PERFORMANCE_TRACKING("enable.performance.tracking"), // Generate a json file that tracks the performance of each player
    DISABLE_DEBUG_INFO("disable.debug.info"), // disable all debug info from f3 (including F3 + B for cheat-y hitboxes). Enabling this will mean that players will get a compass with their current location on it
    DISABLE_ENDER_PEARL_DAMAGE("disable.ender.pearl.damage"), // disable ender pearl damage
    ENABLE_AUTOSMELT("enable.autosmelt"), // enable autosmelt for iron and copper
    SPLIT_WITHIN_TEAMS_SIZE("split.within.teams.size"), // when we spread the players at the beginning of a UHC, should the team be split up? if so, this is the size of the split (default = -1 (no split))
    WORLD_BORDER_SPAWN_PADDING("world.border.spawn.padding"), // How many blocks you would like the players to have spawn away from the border by at least on uhc start

    // Revive config
    REVIVE_ENABLED("revive.enabled"), // Enable revive
    REVIVE_HP("revive.hp"), // Revivee starting hp
    REVIVE_LOSE_MAX_HEALTH("revive.lose.max.health"), // Revivee max hp loss
    REVIVE_ANY_HEAD("revive.any.head"), // Revive-able with any player head

    // Random teams
    RANDOM_TEAMS_POT_ONE("random.teams.pot.1"),
    RANDOM_TEAMS_POT_TWO("random.teams.pot.2"),
    RANDOM_TEAMS_POT_THREE("random.teams.pot.3"),

    // UHC Loot
    LOOT_CHEST_ENABLED("loot.chest.enabled"), // Enable loot chest
    LOOT_CHEST_GRACE_PERIOD("loot.chest.grace.period"), // time before loot chest starts spawning in (default = 0)
    LOOT_CHEST_X_RANGE("loot.chest.x.range"), // Chest x range (e.g loot.chest.x.range=x1,x2)
    LOOT_CHEST_Z_RANGE("loot.chest.z.range"), // Chest z range (e.g loot.chest.z.range=x1,x2)
    LOOT_CHEST_FREQUENCY("loot.chest.frequency"), // Frequency of loot gen (in seconds)
    LOOT_CHEST_HIGH_LOOT_ODDS("loot.chest.high.loot.odds"), // % odds of a high loot item spawning (per spin)
    LOOT_CHEST_MID_LOOT_ODDS("loot.chest.mid.loot.odds"), // % odds of a mid loot item spawning (per spin)
    LOOT_CHEST_SPINS_PER_GEN("loot.chest.spins.per.gen"), // how many items are spawned per gen

    // Additional Enchants
    ADDITIONAL_ENCHANTS_SHIELD("additional.enchants.shield"), // Additional enchants for shield
    ADDITIONAL_ENCHANTS_TNT("additional.enchants.tnt"), // Additional enchants for tnt

    // Evolving Shields
    ENABLE_EVOLVING_SHIELDS("enable.evolving.shields"), // enable evolving shields
    EVOLVING_SHIELDS_EXP_THRESHOLD("evolving.shields.exp.threshold"), // threshold in which you will require to do player damage to continue upgrading your shield
    EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER("evolving.shields.minecraft.exp.multiplier") // multiplier for regular minecraft exp

    ;

    public final String configName;

    ConfigKey(String name) {
        this.configName = name;
    }

    public static ConfigKey fromString(String configName) {
        for (ConfigKey key : ConfigKey.values()) {
            if (key.configName.equalsIgnoreCase(configName)) {
                return key;
            }
        }
        return null;
    }
}
