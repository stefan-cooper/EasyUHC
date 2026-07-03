package com.stefancooper.EasyUHC.base;

import com.stefancooper.EasyUHC.Config;
import org.bukkit.NamespacedKey;

import static com.stefancooper.EasyUHC.enchants.Constants.BLASTWAVE_ENCHANTMENT;
import static com.stefancooper.EasyUHC.base.Constants.CRAFTABLE_PLAYER_HEAD;
import static com.stefancooper.EasyUHC.enchants.Constants.EVOLVING_SHIELD_UPGRADE_AVAILABLE_KEY;
import static com.stefancooper.EasyUHC.enchants.Constants.EVOLVING_SHIELD_UPGRADE_TOTAL_KEY;
import static com.stefancooper.EasyUHC.enchants.Constants.EVOLVING_SHIELD_UPGRADE_TYPE_KEY;
import static com.stefancooper.EasyUHC.enchants.Constants.EVOLVING_SHIELD_USER_KEY;
import static com.stefancooper.EasyUHC.enchants.Constants.EVOLVING_SHIELD_XP_KEY;
import static com.stefancooper.EasyUHC.base.Constants.NOTCH_APPLE;
import static com.stefancooper.EasyUHC.base.Constants.PLAYER_HEAD;
import static com.stefancooper.EasyUHC.enchants.Constants.HOE_REAPER_MARK;
import static com.stefancooper.EasyUHC.enchants.Constants.QUICKBOOM_ENCHANTMENT;
import static com.stefancooper.EasyUHC.base.Constants.NAMESPACE;
import static com.stefancooper.EasyUHC.enchants.Constants.SHIELD_FIRE_ENCHANTMENT;
import static com.stefancooper.EasyUHC.enchants.Constants.SHIELD_JUMP_ENCHANTMENT;
import static com.stefancooper.EasyUHC.enchants.Constants.SHIELD_SLOWNESS_ENCHANTMENT;
import static com.stefancooper.EasyUHC.enchants.Constants.SHIELD_STRENGTH_ENCHANTMENT;
import static com.stefancooper.EasyUHC.enchants.Constants.SHIELD_SWIFTNESS_ENCHANTMENT;
import static com.stefancooper.EasyUHC.enchants.Constants.SHIELD_THUNDER_ENCHANTMENT;
import static com.stefancooper.EasyUHC.enchants.Constants.SHIELD_WATER_ENCHANTMENT;
import static com.stefancooper.EasyUHC.enchants.Constants.SHIELD_WEAKNESS_ENCHANTMENT;
import static com.stefancooper.EasyUHC.enchants.Constants.SHIELD_WIND_ENCHANTMENT;

public class NamespaceKeys {

    final private NamespacedKey playerHead;
    final private NamespacedKey craftablePlayerHead;
    final private NamespacedKey notchApple;
    final private NamespacedKey quickboomEnchantment;
    final private NamespacedKey blastwaveEnchantment;

    final private NamespacedKey shieldSwiftnessEnchantment;
    final private NamespacedKey shieldJumpEnchantment;
    final private NamespacedKey shieldSlownessEnchantment;
    final private NamespacedKey shieldWeaknessEnchantment;
    final private NamespacedKey shieldStrengthEnchantment;

    final private NamespacedKey shieldFireEnchantment;
    final private NamespacedKey shieldWindEnchantment;
    final private NamespacedKey shieldThunderEnchantment;
    final private NamespacedKey shieldWaterEnchantment;

    final private NamespacedKey evolvingShieldUserKey;
    final private NamespacedKey evolvingShieldXPKey;
    final private NamespacedKey evolvingShieldUpgradeTypeKey;
    final private NamespacedKey evolvingShieldUpgradeReadyKey;
    final private NamespacedKey evolvingShieldUpgradeStageKey;

    final private NamespacedKey reapersMarkEnchantment;

    public NamespaceKeys(final Config config) {
        // Misc
        this.playerHead = new NamespacedKey(config.getPlugin(), PLAYER_HEAD);
        this.craftablePlayerHead = new NamespacedKey(config.getPlugin(), CRAFTABLE_PLAYER_HEAD);
        this.notchApple = new NamespacedKey(config.getPlugin(), NOTCH_APPLE);

        // Enchantments
        this.quickboomEnchantment = new NamespacedKey(NAMESPACE, QUICKBOOM_ENCHANTMENT);
        this.blastwaveEnchantment = new NamespacedKey(NAMESPACE, BLASTWAVE_ENCHANTMENT);

        this.shieldStrengthEnchantment = new NamespacedKey(NAMESPACE, SHIELD_STRENGTH_ENCHANTMENT);
        this.shieldJumpEnchantment = new NamespacedKey(NAMESPACE, SHIELD_JUMP_ENCHANTMENT);
        this.shieldSlownessEnchantment = new NamespacedKey(NAMESPACE, SHIELD_SLOWNESS_ENCHANTMENT);
        this.shieldSwiftnessEnchantment = new NamespacedKey(NAMESPACE, SHIELD_SWIFTNESS_ENCHANTMENT);
        this.shieldWeaknessEnchantment = new NamespacedKey(NAMESPACE, SHIELD_WEAKNESS_ENCHANTMENT);
        this.shieldFireEnchantment = new NamespacedKey(NAMESPACE, SHIELD_FIRE_ENCHANTMENT);
        this.shieldWaterEnchantment = new NamespacedKey(NAMESPACE, SHIELD_WATER_ENCHANTMENT);
        this.shieldThunderEnchantment = new NamespacedKey(NAMESPACE, SHIELD_THUNDER_ENCHANTMENT);
        this.shieldWindEnchantment = new NamespacedKey(NAMESPACE, SHIELD_WIND_ENCHANTMENT);

        this.reapersMarkEnchantment = new NamespacedKey(NAMESPACE, HOE_REAPER_MARK);

        // Evolving shield
        this.evolvingShieldUserKey = new NamespacedKey(NAMESPACE, EVOLVING_SHIELD_USER_KEY);
        this.evolvingShieldXPKey = new NamespacedKey(NAMESPACE, EVOLVING_SHIELD_XP_KEY);
        this.evolvingShieldUpgradeTypeKey = new NamespacedKey(NAMESPACE, EVOLVING_SHIELD_UPGRADE_TYPE_KEY);
        this.evolvingShieldUpgradeReadyKey = new NamespacedKey(NAMESPACE, EVOLVING_SHIELD_UPGRADE_AVAILABLE_KEY);
        this.evolvingShieldUpgradeStageKey = new NamespacedKey(NAMESPACE, EVOLVING_SHIELD_UPGRADE_TOTAL_KEY);
    }

    public NamespacedKey getPlayerHeadKey() {
        return playerHead;
    }

    public NamespacedKey getCraftablePlayerHeadKey() {
        return craftablePlayerHead;
    }

    public NamespacedKey getNotchAppleKey() {
        return notchApple;
    }

    public NamespacedKey getQuickboomEnchantment() { return quickboomEnchantment; }

    public NamespacedKey getBlastwaveEnchantment() { return blastwaveEnchantment; }

    public NamespacedKey getShieldStrengthEnchantment() { return shieldStrengthEnchantment; }

    public NamespacedKey getShieldWeaknessEnchantment() { return shieldWeaknessEnchantment; }

    public NamespacedKey getShieldSlownessEnchantment() { return shieldSlownessEnchantment; }

    public NamespacedKey getShieldJumpEnchantment() { return shieldJumpEnchantment; }

    public NamespacedKey getShieldSwiftnessEnchantment() { return shieldSwiftnessEnchantment; }

    public NamespacedKey getShieldFireEnchantment() { return shieldFireEnchantment; }

    public NamespacedKey getShieldWindEnchantment() { return shieldWindEnchantment; }

    public NamespacedKey getShieldWaterEnchantment() { return shieldWaterEnchantment; }

    public NamespacedKey getShieldThunderEnchantment() { return shieldThunderEnchantment; }

    public NamespacedKey getReapersMarkEnchantment() { return reapersMarkEnchantment; }

    public NamespacedKey getEvolvingShieldUserKey() {
        return evolvingShieldUserKey;
    }

    public NamespacedKey getEvolvingShieldXPKey() {
        return evolvingShieldXPKey;
    }

    public NamespacedKey getEvolvingShieldUpgradeTypeKey() {
        return evolvingShieldUpgradeTypeKey;
    }

    public NamespacedKey getEvolvingShieldUpgradeReadyKey() {
        return evolvingShieldUpgradeReadyKey;
    }

    public NamespacedKey getEvolvingShieldUpgradeStageKey() {
        return evolvingShieldUpgradeStageKey;
    }

}
