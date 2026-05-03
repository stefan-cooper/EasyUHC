package com.stefancooper.EasyUHC.evolvingshield;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.base.PerformanceTrackingEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.STAGE_1;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.STAGE_10;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.STAGE_2;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.STAGE_3;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.STAGE_4;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.STAGE_5;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.STAGE_6;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.STAGE_7;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.STAGE_8;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.STAGE_9;
import static com.stefancooper.EasyUHC.evolvingshield.EvolvingShield.setShieldBanner;

public class EvolvingShieldUpgradeMenu {

    final Config config;
    final Player player;

    public EvolvingShieldUpgradeMenu(final Config config, final Player player) {
        this.config = config;
        this.player = player;
        final Optional<ItemStack> shield = EvolvingShield.getEvolvingShieldFromPlayer(config, player);
        if (shield.isPresent()) {
            final NamespacedKey stageKey = config.getManagedResources().getKeys().getEvolvingShieldUpgradeStageKey();
            final ItemMeta shieldMeta = shield.get().getItemMeta();
            final int stage = shieldMeta.getPersistentDataContainer().get(stageKey, PersistentDataType.INTEGER);
            final Inventory inv = Bukkit.createInventory(null, 27, "§8Select your upgrade!");

            switch (stage) {
                case 0:
                    inv.setItem(10, createUpgradeItem(
                            Material.COOKED_BEEF,
                            "§6Steak",
                            "§7Some food (x32) to get you started",
                            Constants.FOOD,
                            false
                    ));
                    inv.setItem(12, createUpgradeItem(
                            Material.BOOK,
                            "§6Book",
                            "§7Some books (x4) for your future enchantment table ;o",
                            Constants.BOOKS,
                            false
                    ));
                    inv.setItem(14, createUpgradeItem(
                            Material.APPLE,
                            "§6Apples",
                            "§7Don't go punching leaves and take these apples (x8)",
                            Constants.APPLES,
                            false
                    ));
                    inv.setItem(16, createUpgradeItem(
                            Material.IRON_INGOT,
                            "§6Iron Ingots",
                            "§7Enough ingots (x24) for a full set of armour. Use it wisely.",
                            Constants.IRON,
                            false
                    ));
                    break;
                case 1:
                    inv.setItem(12, createUpgradeItem(
                            Material.PISTON,
                            "§6Knockback",
                            "§7Apply Knockback to your shield. Blocked attacks will knock your attacker away.",
                            Constants.KNOCKBACK
                    ));

                    inv.setItem(14, createUpgradeItem(
                            Material.CACTUS,
                            "§6Thorns",
                            "§7Apply Thorns to your shield. Blocked attacks will deal damage to your attacker.",
                            Constants.THORNS
                    ));
                    break;
                case 2:
                    inv.setItem(11, createUpgradeItem(
                            Material.GOLDEN_APPLE,
                            "§6Absorption",
                            "§7Gain permanent Absorption (2 extra hearts)",
                            Constants.ABSORPTION
                    ));
                    inv.setItem(13, createUpgradeItem(
                            Material.POTION,
                            "§6Regenerate 4 hearts",
                            "§7Immediately regenerate 4 hearts.",
                            Constants.REGEN
                    ));
                    inv.setItem(15, createUpgradeItem(
                            Material.PLAYER_HEAD,
                            "§6Player Head",
                            "§7Gain a player head for you to use at your own discretion ;o",
                            Constants.PLAYER_HEAD
                    ));
                    break;
                case 3:
                    inv.setItem(11, createUpgradeItem(
                            Material.ARROW,
                            "§6Arrows",
                            "§7Gain some arrows (x32)",
                            Constants.ARROWS,
                            false
                    ));
                    inv.setItem(13, createUpgradeItem(
                            Material.SPECTRAL_ARROW,
                            "§6Spectral arrows",
                            "§7Gain some spectral arrows (x16)",
                            Constants.ARROWS_SPECTRAL,
                            false
                    ));
                    inv.setItem(15, createUpgradeItem(
                            Material.ARROW,
                            "§6Tipped Arrow",
                            "§7Gain one very powerful arrow",
                            Constants.ARROWS_TIPPED,
                            true
                    ));
                    break;
                case 4:
                    inv.setItem(11, createUpgradeItem(
                            Material.ICE,
                            "§6Swift Defense",
                            "§7Apply Swift Defense to your shield. Chance of gaining swiftness after blocking an attack (1/16)",
                            Constants.SWIFTNESS
                    ));

                    inv.setItem(13, createUpgradeItem(
                            Material.RABBIT_FOOT,
                            "§6Leap Guard",
                            "§7Apply Leap Guard to your shield. Chance of gaining jump boost after blocking an attack (1/8)",
                            Constants.JUMP
                    ));
                    inv.setItem(15, createUpgradeItem(
                            Material.DIAMOND_SWORD,
                            "§6Counterforce",
                            "§7Apply Counterforce to your shield. Chance of gaining strength after blocking an attack (1/24)",
                            Constants.STRENGTH
                    ));
                    break;
                case 5:
                    inv.setItem(12, createUpgradeItem(
                            Material.WOODEN_SWORD,
                            "§6Sapping Guard",
                            "§7Apply Sapping Guard to your shield. Chance of giving weakness after blocking an attack (1/16)",
                            Constants.WEAKNESS
                    ));

                    inv.setItem(14, createUpgradeItem(
                            Material.SOUL_SAND,
                            "§6Snare Guard",
                            "§7Apply Snare Guard to your shield. Chance of giving slowness after blocking an attack (1/8)",
                            Constants.SLOWNESS
                    ));
                    break;
                case 6:
                    inv.setItem(12, createUpgradeItem(
                            Material.PISTON,
                            "§6Knockback",
                            "§7Upgrade or add Knockback to your shield. Blocked attacks will knock your attacker away.",
                            Constants.KNOCKBACK
                    ));

                    inv.setItem(14, createUpgradeItem(
                            Material.CACTUS,
                            "§6Thorns",
                            "§7Upgrade or add Thorns to your shield. Blocked attacks will deal damage to your attacker.",
                            Constants.THORNS
                    ));
                    break;
                case 7:
                    inv.setItem(11, createUpgradeItem(
                            Material.ENDER_PEARL,
                            "§6Reapers Kit",
                            "§7Gain the Reapers kit",
                            Constants.REAPER_KIT,
                            true
                    ));
                    inv.setItem(13, createUpgradeItem(
                            Material.BREWING_STAND,
                            "§6Apothecary Kit",
                            "§7Gain the Apothecary kit",
                            Constants.APOTHECARY_KIT,
                            true
                    ));
                    inv.setItem(15, createUpgradeItem(
                            Material.BOOK,
                            "§6Librarians Kit",
                            "§7Gain the Librarian kit",
                            Constants.LIBRARIAN_KIT,
                            true
                    ));
                    break;
                case 8:
                    inv.setItem(11, createUpgradeItem(
                            Material.TNT,
                            "§6Fast TNT",
                            "§7Gain a fast exploding TNT (Quickboom IV)",
                            Constants.FAST_TNT
                    ));
                    inv.setItem(13, createUpgradeItem(
                            Material.TNT,
                            "§6Powerful TNT",
                            "§7Gain an extremely powerful TNT (Blastwave IV)",
                            Constants.BIG_TNT
                    ));
                    inv.setItem(15, createUpgradeItem(
                            Material.TNT,
                            "§6Mixed Enchanted TNT",
                            "§7Gain an enchanted TNT with a mix of enchantments (Blastwave II, Quickboom II)",
                            Constants.MIXED_TNT
                    ));
                    break;
                case 9:
                    inv.setItem(10, createUpgradeItem(
                            Material.FIRE_CHARGE,
                            "§6Fire Elemental",
                            "§7Apply Fire Elemental to your shield. Blocking whilst sneaking will shoot a fire charge (60 second cooldown)",
                            Constants.FIRE
                    ));

                    inv.setItem(12, createUpgradeItem(
                            Material.LIGHTNING_ROD,
                            "§6Thunder Elemental",
                            "§7Apply Thunder Elemental to your shield. Blocking whilst sneaking will strike a thunderbolt on the block you're looking at. (60 second cooldown)",
                            Constants.THUNDER
                    ));
                    inv.setItem(14, createUpgradeItem(
                            Material.WIND_CHARGE,
                            "§6Air Elemental",
                            "§7Apply Air Elemental to your shield. Blocking whilst sneaking will shoot a wind charge (30 second cooldown)",
                            Constants.WIND
                    ));
                    inv.setItem(16, createUpgradeItem(
                            Material.WATER_BUCKET,
                            "§6Water Elemental",
                            "§7Apply Water Elemental to your shield. Blocking whilst sneaking will shoot a snowball that creates a bucket of water (30 second cooldown)",
                            Constants.WATER
                    ));
                    break;
            }

            player.openInventory(inv);
        }
    }

    private static List<Component> splitStringForLore(String lore) {
        final List<Component> result = new ArrayList<>();

        while (lore.length() > 24) {
            final int splitIndex = lore.indexOf(" ", 24);
            if (splitIndex == -1) {
                result.add(Component.text(lore, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
                return result;
            }
            result.add(Component.text(lore.substring(0, splitIndex), NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
            lore = lore.substring(splitIndex + 1);
        }

        if (!lore.isEmpty()) {
            result.add(Component.text(lore, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        }

        return result;
    }

    private ItemStack createUpgradeItem(final Material mat, final String name, final String desc, final String key, final boolean glint) {
        final ItemStack item = new ItemStack(mat);
        final ItemMeta meta = item.getItemMeta();

        meta.setEnchantmentGlintOverride(glint);
        meta.getPersistentDataContainer().set(config.getManagedResources().getKeys().getEvolvingShieldUpgradeTypeKey(), PersistentDataType.STRING, key);
        meta.setDisplayName(name);

        final List<Component> lore = splitStringForLore(desc);
        lore.add(Component.text(""));
        meta.lore(lore);

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createUpgradeItem(final Material mat, final String name, final String desc, final String key) {
        return createUpgradeItem(mat, name, desc, key, true);
    }

    public static void applyUpgrade(final Config config, final Player player, final ItemStack selectedUpgrade) {
        final String type = selectedUpgrade.getItemMeta().getPersistentDataContainer().get(config.getManagedResources().getKeys().getEvolvingShieldUpgradeTypeKey(), PersistentDataType.STRING);
        final NamespacedKey xpKey = config.getManagedResources().getKeys().getEvolvingShieldXPKey();
        final Optional<ItemStack> getShield = EvolvingShield.getEvolvingShieldFromPlayer(config, player);
        if (getShield.isPresent()) {
            final ItemStack shield = getShield.get();

            switch (type) {
                case Constants.THORNS:
                    if (shield.getEnchantmentLevel(Enchantment.THORNS) > 0) {
                        shield.addUnsafeEnchantment(Enchantment.THORNS, 2);
                    } else {
                        shield.addUnsafeEnchantment(Enchantment.THORNS, 1);
                    }
                    setShieldBanner(config, player, shield, List.of(
                            PatternType.TRIANGLES_BOTTOM,
                            PatternType.TRIANGLES_TOP
                    ));
                    break;
                case Constants.KNOCKBACK:
                    if (shield.getEnchantmentLevel(Enchantment.KNOCKBACK) > 0) {
                        shield.addUnsafeEnchantment(Enchantment.KNOCKBACK, 2);
                    } else {
                        shield.addUnsafeEnchantment(Enchantment.KNOCKBACK, 1);
                    }
                    setShieldBanner(config, player, shield, List.of(
                            PatternType.BORDER
                    ));
                    break;
                case Constants.SWIFTNESS:
                    shield.addUnsafeEnchantment(config.getManagedResources().getShieldSwiftnessEnchantment(), 1);
                    break;
                case Constants.STRENGTH:
                    shield.addUnsafeEnchantment(config.getManagedResources().getShieldStrengthEnchantment(), 1);
                    break;
                case Constants.JUMP:
                    shield.addUnsafeEnchantment(config.getManagedResources().getShieldJumpEnchantment(), 1);
                    break;
                case Constants.SLOWNESS:
                    shield.addUnsafeEnchantment(config.getManagedResources().getShieldSlownessEnchantment(), 1);
                    break;
                case Constants.WEAKNESS:
                    shield.addUnsafeEnchantment(config.getManagedResources().getShieldWeaknessEnchantment(), 1);
                    break;
                case Constants.FIRE:
                    shield.addUnsafeEnchantment(config.getManagedResources().getShieldFireEnchantment(), 1);
                    setShieldBanner(config, player, shield, List.of(
                            PatternType.FLOWER
                    ));
                    break;
                case Constants.WIND:
                    shield.addUnsafeEnchantment(config.getManagedResources().getShieldWindEnchantment(), 1);
                    setShieldBanner(config, player, shield, List.of(
                            PatternType.GUSTER
                    ));
                    break;
                case Constants.WATER:
                    shield.addUnsafeEnchantment(config.getManagedResources().getShieldWaterEnchantment(), 1);
                    setShieldBanner(config, player, shield, List.of(
                            PatternType.GLOBE
                    ));
                    break;
                case Constants.THUNDER:
                    shield.addUnsafeEnchantment(config.getManagedResources().getShieldThunderEnchantment(), 1);
                    setShieldBanner(config, player, shield, List.of(
                            PatternType.SKULL
                    ));
                    break;
                case Constants.FOOD:
                    player.give(new ItemStack(Material.COOKED_BEEF, 32));
                    break;
                case Constants.BOOKS:
                    player.give(new ItemStack(Material.BOOK, 4));
                    break;
                case Constants.APPLES:
                    player.give(new ItemStack(Material.APPLE, 8));
                    break;
                case Constants.IRON:
                    player.give(new ItemStack(Material.IRON_INGOT, 24));
                    break;
                case Constants.REAPER_KIT:
                    player.give(new ItemStack(Material.ENDER_PEARL, 4));
                    player.give(new ItemStack(Material.CHORUS_FRUIT, 32));
                    final ItemStack reaperArrows = new ItemStack(Material.TIPPED_ARROW, 4);
                    final PotionMeta reaperArrowsMeta = (PotionMeta) reaperArrows.getItemMeta();
                    reaperArrowsMeta.setBasePotionType(PotionType.LONG_WEAKNESS);
                    player.give(reaperArrows);
                    break;
                case Constants.APOTHECARY_KIT:
                    final ItemStack waterBottle1 = new ItemStack(Material.POTION, 1);
                    final PotionMeta waterBottle1Meta = (PotionMeta) waterBottle1.getItemMeta();
                    waterBottle1Meta.setBasePotionType(PotionType.WATER);
                    waterBottle1.setItemMeta(waterBottle1Meta);
                    final ItemStack waterBottle2 = new ItemStack(Material.POTION, 1);
                    final PotionMeta waterBottle2Meta = (PotionMeta) waterBottle2.getItemMeta();
                    waterBottle2Meta.setBasePotionType(PotionType.WATER);
                    waterBottle2.setItemMeta(waterBottle2Meta);
                    final ItemStack waterBottle3 = new ItemStack(Material.POTION, 1);
                    final PotionMeta waterBottle3Meta = (PotionMeta) waterBottle3.getItemMeta();
                    waterBottle3Meta.setBasePotionType(PotionType.WATER);
                    waterBottle3.setItemMeta(waterBottle3Meta);

                    player.give(new ItemStack(Material.BREWING_STAND, 1));
                    player.give(new ItemStack(Material.NETHER_WART, 3));
                    player.give(new ItemStack(Material.BLAZE_POWDER, 1));
                    player.give(waterBottle1);
                    player.give(waterBottle2);
                    player.give(waterBottle3);
                    break;
                case Constants.LIBRARIAN_KIT:
                    final ItemStack knockbackBook = new ItemStack(Material.ENCHANTED_BOOK);
                    final EnchantmentStorageMeta knockbackBookMeta = (EnchantmentStorageMeta) knockbackBook.getItemMeta();
                    knockbackBookMeta.addStoredEnchant(Enchantment.KNOCKBACK, 2, true);
                    knockbackBook.setItemMeta(knockbackBookMeta);

                    final ItemStack sharpnessBook = new ItemStack(Material.ENCHANTED_BOOK);
                    final EnchantmentStorageMeta sharpnessBookMeta = (EnchantmentStorageMeta) sharpnessBook.getItemMeta();
                    sharpnessBookMeta.addStoredEnchant(Enchantment.SHARPNESS, 2, true);
                    sharpnessBook.setItemMeta(sharpnessBookMeta);

                    final ItemStack protBook = new ItemStack(Material.ENCHANTED_BOOK);
                    final EnchantmentStorageMeta protBookMeta = (EnchantmentStorageMeta) protBook.getItemMeta();
                    protBookMeta.addStoredEnchant(Enchantment.PROTECTION, 2, true);
                    protBook.setItemMeta(protBookMeta);

                    player.give(new ItemStack(Material.BOOKSHELF, 4));
                    player.give(new ItemStack(Material.EXPERIENCE_BOTTLE, 32));
                    player.give(knockbackBook);
                    player.give(sharpnessBook);
                    player.give(protBook);
                    break;
                case Constants.ARROWS:
                    player.give(new ItemStack(Material.ARROW, 32));
                    break;
                case Constants.ARROWS_SPECTRAL:
                    player.give(new ItemStack(Material.SPECTRAL_ARROW, 16));
                    break;
                case Constants.ARROWS_TIPPED:
                    final ItemStack tippedArrow = new ItemStack(Material.TIPPED_ARROW, 1);
                    final PotionMeta tippedArrowMeta = (PotionMeta) tippedArrow.getItemMeta();
                    tippedArrowMeta.setBasePotionType(PotionType.STRONG_HARMING);
                    tippedArrow.setItemMeta(tippedArrowMeta);
                    player.give(tippedArrow);
                    break;
                case Constants.PLAYER_HEAD:
                    player.give(new ItemStack(Material.PLAYER_HEAD, 1));
                    break;
                case Constants.REGEN:
                    player.setHealth(Math.min(player.getHealth() + 8.0, player.getAttribute(Attribute.MAX_HEALTH).getValue()));
                    break;
                case Constants.ABSORPTION:
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.ABSORPTION,
                            PotionEffect.INFINITE_DURATION,
                            0,
                            false,
                            false,
                            false
                    ));
                    break;
                case Constants.FAST_TNT:
                    final ItemStack fastTnt = new ItemStack(Material.TNT);
                    fastTnt.addUnsafeEnchantment(config.getManagedResources().getQuickboomEnchantment(), 4);
                    player.give(fastTnt);
                    break;
                case Constants.BIG_TNT:
                    final ItemStack bigTnt = new ItemStack(Material.TNT);
                    bigTnt.addUnsafeEnchantment(config.getManagedResources().getBlastwaveEnchantment(), 4);
                    player.give(bigTnt);
                    break;
                case Constants.MIXED_TNT:
                    final ItemStack mixedTnt = new ItemStack(Material.TNT);
                    mixedTnt.addUnsafeEnchantment(config.getManagedResources().getBlastwaveEnchantment(), 2);
                    mixedTnt.addUnsafeEnchantment(config.getManagedResources().getQuickboomEnchantment(), 2);
                    player.give(mixedTnt);
                    break;
                case null:
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }
            // upgrade applied so upgrade no longer available
            final ItemMeta shieldMeta = shield.getItemMeta();
            final int currentXP = shieldMeta.getPersistentDataContainer().get(xpKey, PersistentDataType.INTEGER);
            final int newStage = EvolvingShield.incrementUpgradeStage(config, shieldMeta);
            final boolean isUpgradeAvailable = calculateUpgradeAvailable(currentXP, newStage);
            EvolvingShield.setUpgradeAvailable(config, shieldMeta, isUpgradeAvailable);
            EvolvingShield.updateLore(config, shieldMeta, isUpgradeAvailable);
            shield.setItemMeta(shieldMeta);
            player.playSound(player.getLocation(), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1, 1);
            config.getManagedResources().addTimestamp(String.format("[Evolving shield] %s selected the %s upgrade. Latest XP: %s", player.getName(), type, currentXP));
            config.getManagedResources().addPerformanceTrackingEvent(PerformanceTrackingEvent.EVOLVING_SHIELD_UPGRADES, player.getName(), newStage);
        } else {
            config.getPlugin().getLogger().log(Level.WARNING, "Could not find shield to evolve");
        }
    }

    public static boolean calculateUpgradeAvailable(final int xp, final int currentStage) {
        if (xp >= STAGE_1 && xp < STAGE_2 && currentStage <= 0) {
            return true;
        } else if (xp >= STAGE_2 && xp < STAGE_3 && currentStage <= 1) {
            return true;
        } else if (xp >= STAGE_3 && xp < STAGE_4 && currentStage <= 2) {
            return true;
        } else if (xp >= STAGE_4 && xp < STAGE_5 && currentStage <= 3) {
            return true;
        } else if (xp >= STAGE_5 && xp < STAGE_6 && currentStage <= 4) {
            return true;
        } else if (xp >= STAGE_6 && xp < STAGE_7 && currentStage <= 5) {
            return true;
        } else if (xp >= STAGE_7 && xp < STAGE_8 && currentStage <= 6) {
            return true;
        } else if (xp >= STAGE_8 && xp < STAGE_9 && currentStage <= 7) {
            return true;
        } else if (xp >= STAGE_9 && xp < STAGE_10 && currentStage <= 8) {
            return true;
        } else if (xp >= STAGE_10 && currentStage <= 9) {
            return true;
        }else {
            return false;
        }
    }
}
