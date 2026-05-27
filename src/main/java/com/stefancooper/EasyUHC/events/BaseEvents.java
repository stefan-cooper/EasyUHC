package com.stefancooper.EasyUHC.events;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.base.DeathAction;
import com.stefancooper.EasyUHC.base.BossBarBorder;
import com.stefancooper.EasyUHC.base.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static com.stefancooper.EasyUHC.base.ConfigKey.*;
import static com.stefancooper.EasyUHC.base.Utils.testMode;

public class BaseEvents implements Listener {

    private final Config config;

    public BaseEvents(Config config) {
        this.config = config;
    }

    // View docs for various events https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/event/package-summary.html

    @Nullable
    private Location getWorldSpawn() {
        Optional<Integer> worldSpawnX = Optional.ofNullable(config.getProperty(WORLD_SPAWN_X));
        Optional<Integer> worldSpawnY = Optional.ofNullable(config.getProperty(WORLD_SPAWN_Y));
        Optional<Integer> worldSpawnZ = Optional.ofNullable(config.getProperty(WORLD_SPAWN_Z));
        if (worldSpawnX.isPresent() && worldSpawnY.isPresent() && worldSpawnZ.isPresent()) {
            int x = worldSpawnX.get();
            int y = worldSpawnY.get();
            int z = worldSpawnZ.get();
            return new Location(config.getWorlds().getOverworld(), x, y, z);
        }
        return null;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        switch (DeathAction.fromString(config.getProperty(ON_DEATH_ACTION, Defaults.ON_DEATH_ACTION))) {
            case SPECTATE:
                event.getEntity().setGameMode(GameMode.SPECTATOR);
                break;
            case KICK:
                event.getEntity().kick(Component.text("GG, you suck"));
                break;
            case null:
            default:
                break;
        }

        if (config.getProperty(PLAYER_HEAD_GOLDEN_APPLE, Defaults.PLAYER_HEAD_GOLDEN_APPLE)) {
            Player player = event.getEntity();
            ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
            SkullMeta headMeta = (SkullMeta) head.getItemMeta();
            assert headMeta != null;
            headMeta.displayName(Component.text(String.format("%s's head", player.getName())));
            headMeta.lore(List.of(Component.text("Put this item in a bench"), Component.text("For a Golden Apple")));
            headMeta.setOwningPlayer(player);
            headMeta.setUnbreakable(true);
            headMeta.setFireResistant(true);
            try {
                headMeta.setRarity(ItemRarity.EPIC);
            } catch (Exception e) {
                // noop
                // for some reason, this function is not implemented in MockBukkit but it is not worth us mocking
            }
            head.setItemMeta(headMeta);
            player.getWorld().dropItemNaturally(player.getLocation(), head);
        }


        if (config.getProperty(WHISPER_TEAMMATE_DEAD_LOCATION, Defaults.WHISPER_TEAMMATE_DEAD_LOCATION)) {
            Player player = event.getEntity();
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Optional<Team> playerTeam = scoreboard.getTeams().stream().filter(team -> team.hasEntry(player.getDisplayName())).findFirst();
            playerTeam.ifPresent(team -> team.getEntries().forEach(teammatePlayer -> {
                Player teammate = Bukkit.getPlayer(teammatePlayer);
                if (teammate != null) {
                    teammate.sendMessage(Component.text(String.format("(Only visible to your team) %s death location: %s, %s, %s",
                            player.getName(),
                            (int) player.getLocation().getX(),
                            (int) player.getLocation().getY(),
                            (int) player.getLocation().getZ()
                    )));
                }
            }));
        }

        // Play death cannon
        if (config.getPlugin().isUHCLive()) {
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1));
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location deathLocation = player.getLastDeathLocation();
        if (deathLocation != null && player.getGameMode().equals(GameMode.SPECTATOR)) {
            event.setRespawnLocation(deathLocation);
        }
        if (!config.getPlugin().isUHCLive()) {
            final Location worldSpawn = getWorldSpawn();
            if (worldSpawn != null) {
                event.setRespawnLocation(worldSpawn);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        /* -- Setting Game mode -- */
        GameMode currentGamemode = event.getPlayer().getGameMode();
        if (currentGamemode == GameMode.SPECTATOR) {
            return;
        } else if (config.getPlugin().isUHCLive()) {
            event.getPlayer().setGameMode(GameMode.SURVIVAL);
        } else {
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
            final Location worldSpawn = getWorldSpawn();
            final int inventorySize = Arrays.stream(event.getPlayer().getInventory().getContents()).filter(item -> item != null && item.getType() != Material.AIR).toList().size();
            // don't teleport them if their inventory has something inside it (this suggests that the uhc has started and maybe the server crashed)
            if (inventorySize == 0 && worldSpawn != null) {
                event.getPlayer().teleport(worldSpawn);
            }
        }

        /* -- Setting boss bar -- */
        if (config.getPlugin().isUHCLive() && config.getProperty(WORLD_BORDER_IN_BOSSBAR, Defaults.WORLD_BORDER_IN_BOSSBAR)) {
            BossBarBorder bossBarBorder = config.getManagedResources().getBossBarBorder();
            bossBarBorder.getBossBar().addPlayer(event.getPlayer());
            bossBarBorder.getBossBar().setVisible(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        final Location getTo = event.getTo();
        final Location getFrom = event.getFrom();
        if (config.getPlugin().isCountingDown() && getTo != null && (getTo.getY() > getFrom.getY() || getTo.getX() != getFrom.getX() || getTo.getZ() != getFrom.getZ())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {

        // Set Happy Ghasts to 60 hearts
        if (event.getEntity().getType().equals(EntityType.HAPPY_GHAST)) {
            final HappyGhast ghast = (HappyGhast) event.getEntity();
            final AttributeInstance maxHealth = ghast.getAttribute(Attribute.MAX_HEALTH);
            if (maxHealth != null) {
                maxHealth.setBaseValue(120);
            }
            ghast.setHealth(120);
        }

        if (!event.getSpawnReason().equals(CreatureSpawnEvent.SpawnReason.NATURAL)) {
            // noop
            return;
        }
        if (config.getProperty(DISABLE_WITCHES, Defaults.DISABLE_WITCHES) && event.getEntity().getType().equals(EntityType.WITCH)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onChatMessage(AsyncPlayerChatEvent event) {
        final Player sender = event.getPlayer();
        final GameMode gameMode = sender.getGameMode();

        if (config.getProperty(ENABLE_DEATH_CHAT, Defaults.ENABLE_DEATHCHAT) && gameMode.equals(GameMode.SPECTATOR)) {
            final List<Player> alivePlayers = (List<Player>) Bukkit.getOnlinePlayers().stream().filter(player -> player.getGameMode().equals(GameMode.SURVIVAL)).toList();
            alivePlayers.forEach(player -> event.getRecipients().remove(player));
            event.setMessage(String.format("(Death Chat) %s", event.getMessage()));
        }
    }

    private static final Map<Material, Material> AUTOSMELT = Map.of(
            Material.COPPER_ORE, Material.COPPER_INGOT,
            Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT,
            Material.IRON_ORE, Material.IRON_INGOT,
            Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT,
            Material.GOLD_ORE, Material.GOLD_INGOT,
            Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT
    );

    private float getFurnaceXp(Material result) {
        ItemStack stack = new ItemStack(result);

        for (Recipe recipe : Bukkit.getRecipesFor(stack)) {
            if (recipe instanceof FurnaceRecipe furnace) {
                return furnace.getExperience();
            }
        }
        return 0f;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (config.getProperty(ALL_TREES_SPAWN_APPLES, Defaults.ALL_TREES_SPAWN_APPLES)) {
            if (isBlockLeaves(event.getBlock())) {
                final ItemStack destroyedWith = event.getPlayer().getInventory().getItemInMainHand();
                final boolean dropApple;
                switch (destroyedWith.getType()) {
                    case Material.NETHERITE_HOE:
                        dropApple = Utils.checkOddsOf(16);
                        break;
                    case Material.DIAMOND_HOE:
                        dropApple = Utils.checkOddsOf(32);
                        break;
                    case Material.IRON_HOE:
                        dropApple = Utils.checkOddsOf(64);
                        break;
                    case Material.COPPER_HOE:
                    case Material.GOLDEN_HOE:
                        dropApple = Utils.checkOddsOf(96);
                        break;
                    case Material.STONE_HOE:
                    case Material.WOODEN_HOE:
                        dropApple = Utils.checkOddsOf(128);
                        break;
                    default:
                        dropApple = Utils.checkOddsOf(200);
                        break;
                }
                if (dropApple) {
                    event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.APPLE));
                }
            }
        }
        if (config.getProperty(ENABLE_AUTOSMELT, Defaults.ENABLE_AUTOSMELT)) {
            final Block block = event.getBlock();
            final Player player = event.getPlayer();

            // get drops from block
            final Collection<ItemStack> drops = block.getDrops(
                    player.getInventory().getItemInMainHand(),
                    player
            );
            // if no drops, ignore - don't break out early in test mode because .drops is always empty in MockBukkit
            if (drops.isEmpty() && !testMode()) return;
            // map the drop to the smelt
            final Material smeltResult = AUTOSMELT.get(block.getType());
            if (smeltResult == null) return;

            // cancel the original drop
            event.setDropItems(false);

            int total = drops.stream()
                    .mapToInt(ItemStack::getAmount)
                    .sum();
            if (testMode()) total = 1;
            else if (total <= 0) return;
            // drop the smelted items
            block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(smeltResult, total));

            // give the xp you would have gotten for smelting
            final float xpPerItem = getFurnaceXp(smeltResult);
            final int totalXp = Math.round(xpPerItem * total);

            if (totalXp > 0) {
                block.getWorld().spawn(block.getLocation(),
                        ExperienceOrb.class,
                        orb -> orb.setExperience(totalXp)
                );
            }
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (config.getProperty(ALL_TREES_SPAWN_APPLES, Defaults.ALL_TREES_SPAWN_APPLES)) {
            // 1/200 chance when leaves decay to spawn an apple
            if (isBlockLeaves(event.getBlock()) && Utils.checkOddsOf(200)) {
                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), new ItemStack(Material.APPLE));
            }
        }
    }

    @EventHandler
    public void onPearlTeleport(EntityDamageEvent event) {
        if (config.getProperty(DISABLE_ENDER_PEARL_DAMAGE, Defaults.DISABLE_ENDER_PEARL_DAMAGE) && event.getDamageSource().getDamageType().equals(DamageType.ENDER_PEARL)) {
            event.setCancelled(true);
        }
    }

    private boolean isBlockLeaves(Block block) {
        return switch (block.getType()) {
            case Material.ACACIA_LEAVES, Material.AZALEA_LEAVES, Material.BIRCH_LEAVES, Material.CHERRY_LEAVES,
                 Material.JUNGLE_LEAVES, Material.MANGROVE_LEAVES, Material.SPRUCE_LEAVES,
                 Material.FLOWERING_AZALEA_LEAVES, Material.PALE_OAK_LEAVES -> true;
            default -> false;
        };
    }
}