package com.stefancooper.EasyUHC.base;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShield;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShieldUpgradeType;
import com.stefancooper.EasyUHC.sharedinventory.SharedInventoryManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.logging.Level;

import static com.stefancooper.EasyUHC.base.ConfigKey.ENABLE_EVOLVING_SHIELDS;
import static com.stefancooper.EasyUHC.base.ConfigKey.SHARED_TEAM_HEALTH;
import static com.stefancooper.EasyUHC.base.Utils.splitStringForLore;
public class UHCTeam {

    private final NamedTextColor color;
    private final String playerNames;
    private final List<Player> players;
    private final String name;
    private boolean sharedHealth;
    private boolean sharedInventory;
    private final Config config;
    @Nullable
    private SharedChoice sharedChoice = null;
    private boolean isDecidingChoice = false;
    private BukkitTask sharedChoiceTimeout = null;

    public UHCTeam(final String name, final String playerNames, final NamedTextColor color, final Config config) {
        this.name = name;
        this.playerNames = playerNames;
        this.color = color;
        this.players = new ArrayList<>(getPlayerNames().stream().map(Bukkit::getPlayerExact).filter(Objects::nonNull).toList());
        this.config = config;
        sharedHealth = false;
        sharedInventory = false;
    }

    public boolean teamIsInServer() { return getPlayerNames().size() == players.size(); }

    public String getName() {
        return name;
    }

    public List<String> getPlayerNames() {
        return Arrays.stream(playerNames.split(",")).map(String::trim).toList();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public boolean isSharedHealth() { return sharedHealth; }

    public boolean isSharedInventory() { return sharedInventory; }

    public void setSharedChoice(final boolean sharedHealth, final boolean sharedInventory) {
        this.sharedHealth = sharedHealth;
        this.sharedInventory = sharedInventory;
        final SharedInventoryManager manager = config.getManagedResources().getSharedInventoryManager();
        final List<Player> invPlayers = new ArrayList<>(getPlayers());
        final boolean enableEvolvingShields = config.getProperty(ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS);

        if (sharedHealth && !isDecidingChoice) {
            final int hp = config.getProperty(SHARED_TEAM_HEALTH, Defaults.SHARED_TEAM_HEALTH);
            players.forEach(p -> {
                p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(hp);
                p.setHealth(hp);
            });
        } else {
            players.forEach(p -> {
                p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
                p.setHealth(20);
            });
        }

        if (sharedInventory && !isDecidingChoice) {
            manager.createGroup(invPlayers.getFirst(), name);
            invPlayers.removeFirst();
            invPlayers.forEach(player -> manager.joinGroup(player, name));
        } else {
            invPlayers.forEach(manager::leaveGroup);
        }

        if (enableEvolvingShields && sharedInventory && !isDecidingChoice) {
            config.getManagedResources().runTaskLater(() -> {
                EvolvingShield.createEvolvingShield(config, this);
            }, 5L);
        } else if (enableEvolvingShields && !isDecidingChoice) {
            config.getManagedResources().runTaskLater(() -> {
                players.forEach(p -> EvolvingShield.createEvolvingShield(config, p));
            }, 5L);
        }
    }

    public void triggerChoice(final int timeout) throws Exception {
        if (getPlayers().isEmpty()) {
            throw new Exception("no players to vote");
        }
        sharedChoice = new SharedChoice(getPlayers(), config);
        isDecidingChoice = true;
        sharedChoiceTimeout = config.getManagedResources().runTaskLater(() -> {
            if (sharedChoice == null) return;
            if (isDecidingChoice) {
                isDecidingChoice = false;
                players.forEach(p -> {
                    if (!sharedChoice.getVotes().containsKey(p.getUniqueId())) p.closeInventory();
                });
                final Choice winner = sharedChoice.determineWinner();
                setSharedChoice(winner == Choice.HEALTH, winner == Choice.INVENTORY);
            }
        }, timeout);
    }

    public boolean isDecidingSharedChoice(final Player player) {
        return sharedChoice != null && isDecidingChoice && !sharedChoice.getVotes().containsKey(player);
    }

    public void reopenSharedChoice(final Player player) {
        if (players.contains(player)) {
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            sharedChoice.openSharedChoice(player);
        }
    }

    public void voteInChoice(final Player player, final Choice choice) {
        if (getPlayers().contains(player) && sharedChoice != null) {
            isDecidingChoice = sharedChoice.vote(player.getUniqueId(), choice);
            if (!isDecidingChoice) {
                final Choice winner = sharedChoice.determineWinner();
                setSharedChoice(winner == Choice.HEALTH, winner == Choice.INVENTORY);
                sharedChoiceTimeout.cancel();
            }
        } else {
            config.getPlugin().getLogger().log(Level.WARNING, "PLayer attempted to vote in wrong team. Something went wrong!");
        }
    }

    public static void createTeam(final UHCTeam uhcTeam, final Config config) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // if team already exists, redefine it
        if (scoreboard.getTeam(uhcTeam.getName()) != null) {
            scoreboard.getTeam(uhcTeam.getName()).unregister();
        }
        final Team team = scoreboard.registerNewTeam(uhcTeam.getName());
        uhcTeam.getPlayerNames().forEach(player -> {
            final Team entryTeam = scoreboard.getEntryTeam(player);
            // if player is already on another team, remove them from that team and put them on this team
            if (entryTeam != null) {
                entryTeam.removeEntry(player);
            }
            team.addEntry(player);
        });

        team.color(uhcTeam.getColor());
        team.prefix(Component.text(String.format("[%s] ", uhcTeam.getName())));
        config.getManagedResources().addTeam(uhcTeam);
    }

    public enum Choice {
        HEALTH("healthChoice"),
        INVENTORY("inventoryChoice");
        public final String choiceName;

        Choice(final String name) {
            this.choiceName = name;
        }
        @Override
        public String toString() {
            return choiceName;
        }
        @Nullable
        public static Choice fromString(final String name) {
            if (name.equals(Choice.HEALTH.toString())) {
                return Choice.HEALTH;
            } else if (name.equals(Choice.INVENTORY.toString())) {
                return Choice.INVENTORY;
            }
            return null;
        }
    }

    public static class SharedChoice {
        private final List<UUID> players;
        private final Map<UUID, Choice> votes;
        private final Config config;

        SharedChoice(final List<Player> players, final Config config) {
            this.players = players.stream().map(Entity::getUniqueId).toList();
            this.config = config;
            votes = new HashMap<>();
            players.forEach(this::openSharedChoice);
        }

        public Map<UUID, Choice> getVotes() {
            return votes;
        }

        public void openSharedChoice(final Player player) {
            final Inventory inv = Bukkit.createInventory(null, 27, Component.text("Vote for your shared choice!"));

            inv.setItem(12, createSharedItem(Choice.HEALTH, config));
            inv.setItem(14, createSharedItem(Choice.INVENTORY, config));
            player.openInventory(inv);
        }

        private ItemStack createSharedItem(final Choice choice, final Config config) {
            final ItemStack item;
            if (choice == Choice.HEALTH) {
                item = new ItemStack(Material.HEART_OF_THE_SEA);
                final ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(config.getManagedResources().getKeys().getSharedChoiceTypeKey(), PersistentDataType.STRING, Choice.HEALTH.toString());
                meta.setEnchantmentGlintOverride(true);
                meta.setDisplayName("Shared Health");
                final List<Component> lore = splitStringForLore("");
                lore.add(Component.text(""));
                meta.lore(lore);
                item.setItemMeta(meta);
            } else {
                item = new ItemStack(Material.ENDER_CHEST);
                final ItemMeta meta = item.getItemMeta();
                meta.getPersistentDataContainer().set(config.getManagedResources().getKeys().getSharedChoiceTypeKey(), PersistentDataType.STRING, Choice.INVENTORY.toString());
                meta.setEnchantmentGlintOverride(true);
                meta.setDisplayName("Shared Inventory");
                final List<Component> lore = splitStringForLore("");
                lore.add(Component.text(""));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            return item;
        }

        public boolean vote(final UUID playerId, final Choice choice ) {
            votes.put(playerId, choice);
            final Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.closeInventory();
            }
            return !players.stream().allMatch(votes::containsKey);
        }

        public Choice determineWinner() {
            final int healthVotes = votes.values().stream().filter(c -> c == Choice.HEALTH).toList().size();
            final int invVotes = votes.values().stream().filter(c -> c == Choice.INVENTORY).toList().size();

            if (healthVotes > invVotes) {
                return Choice.HEALTH;
            } else if (invVotes > healthVotes) {
                return Choice.INVENTORY;
            }
            return Utils.checkOddsOf(2) ? Choice.HEALTH : Choice.INVENTORY;
        }
    }
}
