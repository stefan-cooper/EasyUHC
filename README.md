# EasyUHC

Run a UHC on your [Paper](https://papermc.io/) Server.

Want to contribute? See [Contributing](#contributing)

## Installing

1. Download the plugin - we don't currently have any accessible download links. Contact @stefan-cooper or any other contributor to retrieve a .jar file

2. Add the plugin to your `plugins` folder in your [Paper](https://papermc.io/) server

## Configure your UHC

You can configure your UHC in the server (see [Configuring](#configuring)) or you can create a `uhc_config.properties` file inside your `plugins` folder. One of these will be created anyway after starting the server with this plugin installed.

The following configurations are available for managing your UHC:

### Required Properties (they have defaults)

```properties
# Name of the minecraft overworld
world.name=world
# Name of the minecraft nether world
nether.world.name=world_nether
# enable timestamps of notable events (defaults to true)
enable.timestamps=false|true
# enable timestamps of notable events (defaults to true)
enable.performance.tracker=false|true
# Name of the minecraft end world
end.world.name=world_end
# Difficulty of the game when UHC is live
difficulty=EASY
# Disable debug info from being shown to clients (coordinates, chunk info, hitboxes, etc). Enabling this will show coordinates to players during the UHC in the action bar
disable.debug.info=false
# Countdown to start the game after UHC start command issued
countdown.timer.length=5
# Grace period time (in seconds) before PVP is enabled
grace.period.timer=600
# World border center X coord
world.border.center.x=0
# World border center Z coord
world.border.center.z=0
# On /uhc start, use this value to determine a minimum 'padding' that players will spawn away from the border
world.border.spawn.padding=1
# Final size of the world border at the end of the UHC
world.border.final.size=150
# Grace period time (in seconds) before the border will begin to shrink
world.border.grace.period=3600
# Initial size world border at start of the UHC
world.border.initial.size=2000
# Time (in seconds) to shrink from the initial size to the final size
world.border.shrinking.period=7200
# Action to undertake when a player dies ("spectate" | "kick")
on.death.action=spectate
# Mob grace period - period of time (in seconds) that the game will remain in 'Peaceful' mode (default: 0)
mob.grace.period=0
```

#### Revive configuration

See [Reviving players](./src/main/java/com/stefancooper/EasyUHC/revive/README.md)

#### Loot chest configuration

See [UHC Loot](./src/main/java/com/stefancooper/EasyUHC/uhcloot/README.md)

#### Enchantment configuration

See [enchantments](./src/main/java/com/stefancooper/EasyUHC/enchants/README.md)

#### Evolving shield configuration

See [evolving shields](./src/main/java/com/stefancooper/EasyUHC/evolvingshield/README.md)

### Optional properties:

#### World border

```properties
# (optional) world border y border. Setting this and the world.border.y.shrinking.period means that after 
#            the final xz border is finished shrinking, the y border will begin shrinking from the bottom 
#            of the map up to this coordinate by replacing the layers with bedrock.
#
#            Note: this only works if the `world.border.final.size` value is 300 or less
world.border.final.y=60
# (optional) length of time in seconds to shrink the y border by
world.border.y.shrinking.period=900
```

#### Team configuration

```properties
# Team Blue players (comma seperated e.g team.orange=player1,player2) - Note: This is caps sensitive
team.blue=shurf
# Team Orange players (comma seperated e.g team.orange=player1,player2) - Note: This is caps sensitive
team.orange=JawadAJamil
# Team Red players (comma seperated e.g team.orange=player1,player2) - Note: This is caps sensitive
team.red=badTHREEEK
# Team Green players (comma seperated e.g team.orange=player1,player2) - Note: This is caps sensitive
team.green=chuckle
# Team Yellow players (comma seperated e.g team.orange=player1,player2) - Note: This is caps sensitive
team.yellow=StetoGuy
# Team Pink players (comma seperated e.g team.orange=player1,player2) - Note: This is caps sensitive
team.pink=SimplySquare
# Team Purple players (comma seperated e.g team.orange=player1,player2) - Note: This is caps sensitive
team.purple=Rking42
```

#### Randomise team configuration

```properties
random.teams.pot.1=badTHREEEK,someoneelse
random.teams.pot.2=chuckle_chuckle,someoneelse2
random.teams.pot.3=JawadJ,someoneelse3
```

#### Misc configuration

```properties
# (optional) drop player heads who are killed that can be crafted into golden apples
player.head.golden.apple=false|true
# (optional) show the current progress of the world border in the boss bar
world.border.in.bossbar=false|true
# (optional) generate a random final location within the initial world border
random.final.location=false|true
# (optional) X coordinate for world spawn when a UHC is not active
world.spawn.x=0
# (optional) Y coordinate for world spawn when a UHC is not active
world.spawn.y=64
# (optional) Z coordinate for world spawn when a UHC is not active
world.spawn.z=0
# (optional) disable natural witch spawns
disable.witches=true
# (optional) re-add notch apples
craftable.notch.apples=true
# (optional) add a craftable player head (golden apple surrounded by diamonds)
craftable.player.head=true
# (optional) whisper the location of dead teammates when they die
whisper.teammate.dead.location=true
# (optional) disable the end game automatically (mainly for dev purposes)
disable.end.game.automatically=false
# (optional) active players cannot see messages from spectators
enable.death.chat=true
# (optional) turn off all leaves having chance of dropping apples (by default this is on)
all.trees.spawn.apples=false
# (optional) turn off damage from ender pearls (by default, this is off)
disable.ender.pearl.damage=false
# (optional) enable autosmelt on iron, gold and copper ore to ingots (by default, this is off)
enable.autosmelt=false
# (optional) decide how the players are spread. this can be used to split up players despite their teams. (e.g. setting this to 1 will make all players be separated but retain their teams). Default is -1 (no split)
split.within.teams.size=-1
```

## Commands

The following commands are available in game:

### Configuring

#### Set config value:

`/uhc set world.border.initial.size=500`

#### Set multiple config values:

`/uhc set world.border.initial.size=500 world.border.final.size=250`

#### View full config:

`/uhc view config`

#### View a specific config value:

`/uhc view world.border.initial.size`

#### Unset config value:

`/uhc unset world.border.initial.size`

#### Unset multiple config values:

`/uhc unset world.border.initial.size world.border.final.size`

### Running

#### Start the UHC:

`/uhc start`

#### Start a UHC midway/resume a UHC

`/uhc resume <minutes>`

e.g: `/uhc resume 30`

#### Randomise teams:

`/uhc randomise <teamSize>`

e.g: `/uhc randomise 3`

* Note: This command requires the setting of the following config values: `random.teams.pot.1`, `random.teams.pot.2`, `random.teams.pot.3`. See [here](#randomise-team-configuration)

#### Late start player midway during a UHC

`/uhc latestart <username>`

e.g: `/uhc latestart shurf`

#### End/cancel the UHC:

`/uhc cancel`

### Notes on running your UHC smoothly

We recommend using a plugin like [Chunky](https://www.spigotmc.org/resources/chunky.81534/) to pre-load your world chunks. This will hopefully mean less lag at the beginning of your UHC.

## Contributing

Contact @stefan-cooper for information about contributing. This is an open source project so if you feel like you want to add something, just raise a PR!

### Prereqs

- Java 25
- Maven
- Git

### Getting started

1. Clone the repo

2. Run the following command to build the server dev env

   ```
   REFRESH_BUILD=true ./setup_server.sh
   ```

### Running

1. Run server

   ```
   ./run_server.sh
   ```
   