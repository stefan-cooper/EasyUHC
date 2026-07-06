package com.stefancooper.EasyUHC.evolvingshield;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.base.UHCTeam;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Banner;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

import static com.stefancooper.EasyUHC.base.ConfigKey.EVOLVING_SHIELDS_EXP_THRESHOLD;
import static com.stefancooper.EasyUHC.base.ConfigKey.EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShieldUpgradeMenu.calculateUpgradeAvailable;

public class EvolvingShield {

    public static int STAGE_1 = 30;
    public static int STAGE_2 = 100;
    public static int STAGE_3 = 400;
    public static int STAGE_4 = 700;
    public static int STAGE_5 = 1100;
    public static int STAGE_6 = 1600;
    public static int STAGE_7 = 2100;
    public static int STAGE_8 = 3000;
    public static int STAGE_9 = 3500;
    public static int STAGE_10 = 5000;
    public static int STAGE_11 = 7500;

    public static void createEvolvingShield(final Config config, final Player player) {
        final ItemStack shield = new ItemStack(Material.SHIELD, 1);
        ItemMeta shieldMeta = shield.getItemMeta();
        assert shieldMeta != null;
        shieldMeta.displayName(Component.text(String.format("%s's shield", player.getName())));
        updateLore(config, shieldMeta, 0, false);
        shieldMeta.setEnchantmentGlintOverride(true);
        shieldMeta.setUnbreakable(true);
        shieldMeta.setFireResistant(true);
        shieldMeta.getPersistentDataContainer().set(config.getManagedResources().getKeys().getEvolvingShieldUserKey(), PersistentDataType.STRING, player.getName());
        shieldMeta.getPersistentDataContainer().set(config.getManagedResources().getKeys().getEvolvingShieldXPKey(), PersistentDataType.INTEGER, 0);
        shieldMeta.getPersistentDataContainer().set(config.getManagedResources().getKeys().getEvolvingShieldUpgradeReadyKey(), PersistentDataType.BOOLEAN, false);
        shieldMeta.getPersistentDataContainer().set(config.getManagedResources().getKeys().getEvolvingShieldUpgradeStageKey(), PersistentDataType.INTEGER, 0);

        try {
            shieldMeta.setRarity(ItemRarity.EPIC);
        } catch (Exception e) {
            // noop
            // for some reason, this function is not implemented in MockBukkit but it is not worth us mocking
        }
        shield.setItemMeta(shieldMeta);
        setShieldBanner(config, player, shield, List.of());
        player.getInventory().addItem(shield);
    }

    public enum EvolvingShieldXPType {
        EXPERIENCE,
        DAMAGE_HEARTS,
        LOOT_CHEST,
        REVIVE,
        KILL,
        MANUAL
    }

    private static int calculateXP(final EvolvingShieldXPType type, final int toAdd, final int mcMultiplier) {
        return switch (type) {
            case KILL:
                yield 250;
            case REVIVE:
                yield 500;
            case LOOT_CHEST:
                yield 200;
            case DAMAGE_HEARTS:
                yield toAdd * 100;
            case EXPERIENCE:
                yield toAdd * mcMultiplier;
            case MANUAL:
                yield toAdd;
        };
    }

    public static void setShieldBanner(final Config config, final Player player, final ItemStack shield, final List<PatternType> patterns) {
        final BlockStateMeta meta = (BlockStateMeta) shield.getItemMeta();
        final Banner banner = (Banner) meta.getBlockState();

        final List<UHCTeam> teams = config.getManagedResources().getTeams();
        final Optional<UHCTeam> playersTeam = teams.stream().filter(team -> team.getPlayers().contains(player.getName())).findFirst();
        if (playersTeam.isEmpty()) {
            return;
        }
        DyeColor patternColor = DyeColor.BLACK;
        if (playersTeam.get().getColor() == NamedTextColor.GOLD)
            patternColor = DyeColor.ORANGE;
        else if (playersTeam.get().getColor() == NamedTextColor.RED)
            patternColor = DyeColor.RED;
        else if (playersTeam.get().getColor() == NamedTextColor.AQUA)
            patternColor = DyeColor.LIGHT_BLUE;
        else if (playersTeam.get().getColor() == NamedTextColor.GREEN)
            patternColor = DyeColor.GREEN;
        else if (playersTeam.get().getColor() == NamedTextColor.YELLOW)
            patternColor = DyeColor.YELLOW;
        else if (playersTeam.get().getColor() == NamedTextColor.LIGHT_PURPLE)
            patternColor = DyeColor.PINK;
        else if (playersTeam.get().getColor() == NamedTextColor.DARK_PURPLE)
            patternColor = DyeColor.PURPLE;

        banner.setBaseColor(DyeColor.WHITE);

        // Base color
        final List<Pattern> allPatterns = new ArrayList<>(banner.getPatterns());
        for (final PatternType patternType : patterns) {
            allPatterns.add(new Pattern(patternColor, patternType));
        }
        banner.setPatterns(allPatterns);
        meta.setBlockState(banner);
        // only in 26.2, uncomment this later
        // meta.addItemFlags(ItemFlag.HIDE_BANNER_PATTERNS);
        shield.setItemMeta(meta);
    }

    public static void updateLore(final Config config, final ItemMeta shieldMeta, final boolean isUpgradeAvailable) {
        final NamespacedKey xpKey = config.getManagedResources().getKeys().getEvolvingShieldXPKey();
        final int xp = shieldMeta.getPersistentDataContainer().get(xpKey, PersistentDataType.INTEGER);
        updateLore(config, shieldMeta, xp, isUpgradeAvailable);
    }

    public static ItemMeta updateLore(final Config config, final ItemMeta shieldMeta, final int xp, final boolean isUpgradeAvailable) {
        final int expLimit = config.getProperty(EVOLVING_SHIELDS_EXP_THRESHOLD, Defaults.EVOLVING_SHIELDS_EXP_THRESHOLD);

        final List<Component> lore = new ArrayList<>(List.of(
                Component.text(""),
                Component.text("Level this shield up by gaining EXP,"),
                Component.text("dealing damage to players and getting kills!"),
                Component.text(""))
        );
        if (expLimit > 0 && xp >= expLimit) {
            lore.addAll(List.of(
                    Component.text("Minecraft EXP limit reached: deal damage and kill", NamedTextColor.DARK_RED),
                    Component.text("players to continue levelling up this shield!", NamedTextColor.DARK_RED),
                    Component.text("")
            ));
        }
        if (isUpgradeAvailable) {
            lore.addAll(List.of(
                Component.text("SHIELD UPGRADE AVAILABLE!", NamedTextColor.GOLD),
                Component.text("Shift-click the shield to upgrade", NamedTextColor.GOLD),
                Component.text("")
            ));
        }
        lore.add(Component.text(String.format("Current XP: %s", xp)));

        shieldMeta.lore(lore);
        return shieldMeta;
    }

    public static void setUpgradeAvailable(final Config config, final ItemMeta shieldMeta, final boolean set) {
        final NamespacedKey upgradeAvailable = config.getManagedResources().getKeys().getEvolvingShieldUpgradeReadyKey();
        shieldMeta.getPersistentDataContainer().set(upgradeAvailable, PersistentDataType.BOOLEAN, set);
    }

    public static int incrementUpgradeStage(final Config config, final ItemMeta shieldMeta) {
        final NamespacedKey upgradeStage = config.getManagedResources().getKeys().getEvolvingShieldUpgradeStageKey();
        final int newStage = shieldMeta.getPersistentDataContainer().get(upgradeStage, PersistentDataType.INTEGER) + 1;
        shieldMeta.getPersistentDataContainer().set(upgradeStage, PersistentDataType.INTEGER, newStage);
        return newStage;
    }

    public static Optional<ItemStack> getEvolvingShieldFromPlayer(final Config config, final Player player) {
        final NamespacedKey xpKey = config.getManagedResources().getKeys().getEvolvingShieldXPKey();
        final NamespacedKey userKey = config.getManagedResources().getKeys().getEvolvingShieldUserKey();
        final List<ItemStack> items = new ArrayList<>(Arrays.stream(player.getInventory().getStorageContents()).filter(Objects::nonNull).toList());
        items.add(player.getInventory().getItemInOffHand());
        final List<ItemStack> shields = items.stream().filter(Objects::nonNull).filter(item -> item.getType().equals(Material.SHIELD)).toList();
        final List<ItemStack> evolvingShields = shields.stream().filter(item ->
                item.getItemMeta().getPersistentDataContainer().get(xpKey, PersistentDataType.INTEGER) != null &&
                        item.getItemMeta().getPersistentDataContainer().get(userKey, PersistentDataType.STRING) != null &&
                        Objects.equals(item.getItemMeta().getPersistentDataContainer().get(userKey, PersistentDataType.STRING), player.getName())
        ).toList();

        if (evolvingShields.isEmpty()) {
            config.getPlugin().getLogger().log(Level.FINE, String.format("Could not find player %s shield to add XP to", player.getName()));
            return Optional.empty();
        }
        if (evolvingShields.size() > 1) {
            config.getPlugin().getLogger().log(Level.FINE, String.format("Player %s has too many evolving shields", player.getName()));
            return Optional.empty();
        }
        return Optional.ofNullable(evolvingShields.getFirst());
    }

    public static boolean isUpgradeAvailable(final Config config, final ItemStack shield) {
        final NamespacedKey upgradeAvailable = config.getManagedResources().getKeys().getEvolvingShieldUpgradeReadyKey();
        final ItemMeta shieldMeta = shield.getItemMeta();
        return Boolean.TRUE.equals(shieldMeta.getPersistentDataContainer().get(upgradeAvailable, PersistentDataType.BOOLEAN));
    }

    public static void updateXP(final Config config, final ItemStack shield, final Player player, final int xpBeforeCalculation, final EvolvingShieldXPType type) {
        final NamespacedKey xpKey = config.getManagedResources().getKeys().getEvolvingShieldXPKey();
        final NamespacedKey currentUpgradeStageKey = config.getManagedResources().getKeys().getEvolvingShieldUpgradeStageKey();
        final ItemMeta shieldMeta = shield.getItemMeta();
        final int mcMultiplier = config.getProperty(EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER, Defaults.EVOLVING_SHIELDS_MINECRAFT_EXP_MULTIPLIER);
        final int currentXP = shieldMeta.getPersistentDataContainer().get(xpKey, PersistentDataType.INTEGER);
        final int xpToAdd = calculateXP(type, xpBeforeCalculation, mcMultiplier);
        final int expLimit = config.getProperty(EVOLVING_SHIELDS_EXP_THRESHOLD, Defaults.EVOLVING_SHIELDS_EXP_THRESHOLD);

        final int updatedXP;
        if (expLimit > 0) {
            if (currentXP + xpToAdd > expLimit && type.equals(EvolvingShieldXPType.EXPERIENCE)) {
                updatedXP = currentXP;
            } else {
                updatedXP = currentXP + xpToAdd;
            }
        } else {
            updatedXP = currentXP + xpToAdd;
        }

        final int currentUpgradeStage = shieldMeta.getPersistentDataContainer().get(currentUpgradeStageKey, PersistentDataType.INTEGER);
        shieldMeta.getPersistentDataContainer().set(xpKey, PersistentDataType.INTEGER, updatedXP);

        if (calculateUpgradeAvailable(updatedXP, currentUpgradeStage)) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 1);
            player.sendMessage(Component.text("Shield upgrade available. Open your inventory and shift click your shield to upgrade!", Style.style(NamedTextColor.GOLD, TextDecoration.ITALIC)));
            setUpgradeAvailable(config, shieldMeta, true);
            updateLore(config, shieldMeta, updatedXP, true);
        } else {
            updateLore(config, shieldMeta, updatedXP, false);
        }
        shield.setItemMeta(shieldMeta);
    }

    public static void updateXP(final Config config, final ItemStack shield, final Player player, final EvolvingShieldXPType type) {
        updateXP(config, shield, player, 0, type);
    }

    public static boolean isEvolvingShield(final Config config, final ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();

        return meta.getPersistentDataContainer().has(config.getManagedResources().getKeys().getEvolvingShieldUserKey());
    }
}
