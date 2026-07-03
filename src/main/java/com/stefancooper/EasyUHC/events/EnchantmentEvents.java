package com.stefancooper.EasyUHC.events;

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent;
import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.base.Utils;
import com.stefancooper.EasyUHC.enchants.Constants;
import com.stefancooper.EasyUHC.enchants.EnchantShield;
import com.stefancooper.EasyUHC.enchants.EnchantTNT;
import com.stefancooper.EasyUHC.enchants.PrepareShieldEnchant;
import com.stefancooper.EasyUHC.enchants.PrepareTNTEnchant;
import com.stefancooper.EasyUHC.base.ConfigKey;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import java.util.*;

public class EnchantmentEvents implements Listener {

    private final Config config;

    public EnchantmentEvents(final Config config) {
        this.config = config;
    }

    @EventHandler
    public void onEnchantItem(final EnchantItemEvent event) throws Exception {
        final ItemStack item = event.getItem();

        // Shield enchants
        if (item.getType() == Material.SHIELD &&
                config.getProperty(ConfigKey.ADDITIONAL_ENCHANTS_SHIELD, Defaults.ADDITIONAL_ENCHANTS_SHIELD) &&
                !config.getProperty(ConfigKey.ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS)
        ) {
            final EnchantShield shieldEnchants = new EnchantShield(item, Map.of(event.getEnchantmentHint(), event.getLevelHint()));
            // Note - In the future, if base minecraft adds their own enchants for shields, we may want to remove this
            event.getEnchantsToAdd().clear();
            event.getEnchantsToAdd().putAll(shieldEnchants.getEnchantsToAdd());
        }

        // TNT enchants
        if (item.getType() == Material.TNT &&
                config.getProperty(ConfigKey.ADDITIONAL_ENCHANTS_TNT, Defaults.ADDITIONAL_ENCHANTS_TNT)) {
            final EnchantTNT tntEnchants = new EnchantTNT(item, Map.of(event.getEnchantmentHint(), event.getLevelHint()), config.getManagedResources(), event.getEnchantmentHint(), event.getLevelHint());
            // Note - In the future, if base minecraft adds their own enchants for tnts, we may want to remove this
            event.getEnchantsToAdd().clear();
            event.getEnchantsToAdd().putAll(tntEnchants.getEnchantsToAdd());
        }
    }

    @EventHandler
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        ItemStack item = event.getItem();

        if (item.getType() == Material.SHIELD &&
                config.getProperty(ConfigKey.ADDITIONAL_ENCHANTS_SHIELD, Defaults.ADDITIONAL_ENCHANTS_SHIELD) &&
                !config.getProperty(ConfigKey.ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS)
        ) {

            final PrepareShieldEnchant prepareShieldEnchant = new PrepareShieldEnchant(item, event.getView().getEnchantmentSeed(), event.getEnchantmentBonus());

            if (prepareShieldEnchant.getOffers().length != 3 || prepareShieldEnchant.getOffers()[0] == null) return;

            event.getOffers()[0] = prepareShieldEnchant.getOffers()[0];
            event.getOffers()[1] = prepareShieldEnchant.getOffers()[1];
            event.getOffers()[2] = prepareShieldEnchant.getOffers()[2];

            event.setCancelled(false); // Allow GUI to appear for shields

            // force client to refresh inventory to show new offers
            config.getManagedResources().runTaskLater(() -> event.getEnchanter().updateInventory(), 1L);
            return;
        }

        if (item.getType() == Material.TNT &&
                config.getProperty(ConfigKey.ADDITIONAL_ENCHANTS_TNT, Defaults.ADDITIONAL_ENCHANTS_TNT)) {

            final PrepareTNTEnchant prepareTNTEnchant = new PrepareTNTEnchant(config, item, event.getView().getEnchantmentSeed(), event.getEnchantmentBonus());

            if (prepareTNTEnchant.getOffers().length != 3 || prepareTNTEnchant.getOffers()[0] == null) return;

            event.getOffers()[0] = prepareTNTEnchant.getOffers()[0];
            event.getOffers()[1] = prepareTNTEnchant.getOffers()[1];
            event.getOffers()[2] = prepareTNTEnchant.getOffers()[2];

            event.setCancelled(false); // Allow GUI to appear for TNT

            // force client to refresh inventory to show new offers
            config.getManagedResources().runTaskLater(() -> event.getEnchanter().updateInventory(), 1L);
        }
    }

    /* ----- Shield events ----- */

    @EventHandler
    public void onShieldBlock(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof final Player defender)) return;
        if (!(event.getDamager() instanceof final LivingEntity attacker)) return;
        if (!defender.isBlocking()) return;

        // Check both hands for a shield
        final ItemStack shield;
        final ItemStack offHand = defender.getInventory().getItemInOffHand();
        final ItemStack mainHand = defender.getInventory().getItemInMainHand();

        // If shield is in both off hand + main hand, use the off hand one
        if ((offHand.getType() == Material.SHIELD && mainHand.getType() == Material.SHIELD) || mainHand.getType() == Material.SHIELD) {
            shield = mainHand;
        } else if (offHand.getType() == Material.SHIELD) {
            shield = offHand;
        } else {
            // if not using a shield, get outta here
            return;
        }

        final int knockbackLevel = shield.getEnchantmentLevel(Enchantment.KNOCKBACK);
        final int thornsLevel = shield.getEnchantmentLevel(Enchantment.THORNS);

        // Apply Thorns damage
        if (thornsLevel > 0) {
            double damage = 1.0 + 0.5 * (thornsLevel - 1);
            attacker.damage(damage, defender);
            attacker.getWorld().spawnParticle(
                    Particle.DAMAGE_INDICATOR,
                    attacker.getLocation().add(0, 1, 0),
                    5 + 2 * thornsLevel
            );
        }

        // Apply Knockback with delay (to avoid Bukkit override)
        if (knockbackLevel > 0) {

            // Check if attacker and defender are facing each other
            final Vector attackerDir = attacker.getLocation().getDirection().normalize();
            final Vector defenderDir = defender.getLocation().getDirection().normalize();
            final Vector attackerToDefender = defender.getLocation().toVector().subtract(attacker.getLocation().toVector()).normalize();
            final Vector defenderToAttacker = attacker.getLocation().toVector().subtract(defender.getLocation().toVector()).normalize();
            final double attackerFacingVictim = attackerDir.dot(attackerToDefender);
            final double defenderFacingAttacker = defenderDir.dot(defenderToAttacker);

            final double threshold = 0.7; // adjust how strict the facing requirement is (1 = perfectly aligned)

            if (attackerFacingVictim > threshold && defenderFacingAttacker > threshold) {
                config.getManagedResources().runTaskLater(() -> {
                    Vector direction = attacker.getLocation().toVector()
                            .subtract(defender.getLocation().toVector())
                            .normalize()
                            .multiply(1);

                    attacker.setVelocity(direction.multiply(0.6 + 0.4 * knockbackLevel));
                }, 1L);
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        // if damage is not thorns, ignore
        if (event.getDamageSource().getDamageType() != DamageType.THORNS) return;
        // if damage is not from a player, ignore
        if (!(event.getDamageSource().getCausingEntity() instanceof final Player damager)) return;
        // if damager has thorns armor equipped, fair enough, ignore
        if (Arrays.stream(damager.getInventory().getArmorContents()).anyMatch(armor -> armor != null && armor.getEnchantments().containsKey(Enchantment.THORNS))) return;
        // cancel event because minecraft is trying to apply thorns damage even though the damager is not blocking
        if (!damager.isBlocking()) event.setCancelled(true);
    }

    // any tool/item with Knockback enchant will cause knockback when hitting
    // turn that off for the shield
    @EventHandler
    public void onEntityKnockbackEvent(EntityKnockbackByEntityEvent event) {
        // check that it is a player attacking
        if (!(event.getHitBy() instanceof final Player attacker)) return;
        final Entity defender = event.getEntity();

        // if the damage was caused by entity attack and by a shield, cancel the event
        if (event.getCause() == EntityKnockbackEvent.Cause.ENTITY_ATTACK &&
                attacker.getInventory().getItemInMainHand().getType() == Material.SHIELD &&
                attacker.getInventory().getItemInMainHand().getEnchantments().containsKey(Enchantment.KNOCKBACK)) {

            event.setKnockback(new Vector(0, 0, 0));
            config.getManagedResources().runTaskLater(() -> {
                final float yaw = attacker.getLocation().getYaw();
                final double x = -Math.sin(Math.toRadians(yaw)) * 0.2; // default knockback
                final double z =  Math.cos(Math.toRadians(yaw)) * 0.2;
                defender.setVelocity(new Vector(x, 0.3, z));
            }, 1L);

        }
    }

    /* ----- End of shield events ----- */

    /* ----- TNT Events ----- */

//    private final Map<String, UUID> tntOwners = new HashMap<>();

    // check if item has quickboom enchant
    private boolean hasQuickboomEnchantment(final ItemStack item) {
        return item.containsEnchantment(config.getManagedResources().getQuickboomEnchantment());
    }

    private boolean hasBlastwaveEnchantment(final ItemStack item) {
        return item.containsEnchantment(config.getManagedResources().getBlastwaveEnchantment());
    }

    private void storeTNTMetadata(final ItemStack item, final Block tntBlock) {
        if (hasQuickboomEnchantment(item) || hasBlastwaveEnchantment(item)) {
            if (hasQuickboomEnchantment(item)) {
                tntBlock.setMetadata(com.stefancooper.EasyUHC.enchants.Constants.QUICKBOOM_ENCHANTMENT,
                        new FixedMetadataValue(config.getPlugin(),
                                item.getEnchantmentLevel(config.getManagedResources().getQuickboomEnchantment())
                        )
                );
            }
            if (hasBlastwaveEnchantment(item)) {
                tntBlock.setMetadata(com.stefancooper.EasyUHC.enchants.Constants.BLASTWAVE_ENCHANTMENT,
                        new FixedMetadataValue(config.getPlugin(),
                                item.getEnchantmentLevel(config.getManagedResources().getBlastwaveEnchantment())
                        )
                );
            }
        }
    }

    private void doTNTExplosion(final Block block) {
        final List<MetadataValue> quickboomMetadata = block.getMetadata(com.stefancooper.EasyUHC.enchants.Constants.QUICKBOOM_ENCHANTMENT);
        final List<MetadataValue> blastwaveMetadata = block.getMetadata(com.stefancooper.EasyUHC.enchants.Constants.BLASTWAVE_ENCHANTMENT);
        if (!quickboomMetadata.isEmpty() || !blastwaveMetadata.isEmpty()) {
            Bukkit.getScheduler().runTask(config.getPlugin(), () -> block.getWorld().getNearbyEntities(block.getLocation().add(0.5, 0.5, 0.5), 1, 1, 1)
                    .stream()
                    .filter(ent -> ent instanceof TNTPrimed)
                    .map(ent -> (TNTPrimed) ent)
                    .forEach(tnt -> {
                        if (!quickboomMetadata.isEmpty() || !blastwaveMetadata.isEmpty())
                            handleTNTEnchantments(tnt, quickboomMetadata, blastwaveMetadata);
                        block.removeMetadata(com.stefancooper.EasyUHC.enchants.Constants.QUICKBOOM_ENCHANTMENT, config.getPlugin());
                        block.removeMetadata(com.stefancooper.EasyUHC.enchants.Constants.BLASTWAVE_ENCHANTMENT, config.getPlugin());
                    }));
        }
    }

    private void handleTNTEnchantments(final TNTPrimed tnt, final List<MetadataValue> quickboomMetadata, final List<MetadataValue> blastwaveMetadata) {
        final int quickboomLevel = quickboomMetadata.isEmpty() ? 0 : quickboomMetadata.getFirst().asInt();
        final int blastwaveLevel = blastwaveMetadata.isEmpty() ? 0 : blastwaveMetadata.getFirst().asInt();
        final float yield = switch (blastwaveLevel) {
            case 4 -> 20f;
            case 3 -> 16f;
            case 2 -> 12f;
            case 1 -> 8f;
            default -> 4f;
        };
        final int fuseTime = switch (quickboomLevel) {
            case 4 -> 10;
            case 3 -> 20;
            case 2 -> 30;
            case 1 -> 40;
            default -> 80;
        };
        // blastwave
        tnt.setYield(yield);
        // quickboom
        tnt.setFuseTicks(fuseTime);
        if (blastwaveLevel > 0) config.getManagedResources().runTaskLater(() -> tnt.getWorld().spawnParticle(Particle.EXPLOSION, tnt.getLocation(), 1000), (long) fuseTime);
    }

    @EventHandler
    public void onTNTBlockBreak(final BlockDropItemEvent event) {
        final List<Item> tnts = event.getItems().stream().filter(item -> item.getItemStack().getType().equals(Material.TNT)).toList();
        final Block destroyedBlock = event.getBlockState().getBlock();

        final List<MetadataValue> quickboomMetadata = destroyedBlock.getMetadata(com.stefancooper.EasyUHC.enchants.Constants.QUICKBOOM_ENCHANTMENT);
        final List<MetadataValue> blastwaveMetadata = destroyedBlock.getMetadata(Constants.BLASTWAVE_ENCHANTMENT);

        if (!quickboomMetadata.isEmpty()) tnts.forEach(tnt -> tnt.getItemStack().addUnsafeEnchantments(Map.of(config.getManagedResources().getQuickboomEnchantment(), quickboomMetadata.getFirst().asInt())));
        if (!blastwaveMetadata.isEmpty()) tnts.forEach(tnt -> tnt.getItemStack().addUnsafeEnchantments(Map.of(config.getManagedResources().getBlastwaveEnchantment(), blastwaveMetadata.getFirst().asInt())));
    }

    // store metadata when tnts are placed so we can track them later
    @EventHandler
    public void onTNTPlace(final BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.TNT) return;
        storeTNTMetadata(event.getItemInHand(), event.getBlockPlaced());
    }

    @EventHandler
    public void onTNTDispenseEvent(final BlockDispenseEvent event) {
        if (!event.getItem().getType().equals(Material.TNT)) return;
        final ItemStack item = event.getItem();
        if (hasQuickboomEnchantment(item) || hasBlastwaveEnchantment(item)) {
            final Block dispenser = event.getBlock();
            final BlockFace facing = ((Directional) dispenser.getBlockData()).getFacing();
            final Location spawnLoc = dispenser.getRelative(facing).getLocation().add(0.5, 0.5, 0.5);
            final Block eventualTntBlock = event.getBlock().getWorld().getBlockAt(spawnLoc);
            storeTNTMetadata(item, eventualTntBlock);
        }
    }



    // when a tnt is primed via dispenser
    @EventHandler
    public void onPrimeTNTSpawnEvent(final EntitySpawnEvent event) {
        if (event.getEntity() instanceof TNTPrimed primed) {
            doTNTExplosion(primed.getLocation().getBlock());
        }
    }

    // when a tnt is primed, check if it has any enchantments we care about and apply any effects
    @EventHandler
    public void onTNTPrimeEvent(final TNTPrimeEvent event) {
        doTNTExplosion(event.getBlock());
    }

    /* ----- End of TNT events ----- */

    /* ----- Hoe Events ----- */

    private boolean hasReapersMarkEnchantment(final ItemStack item) {
        return item.containsEnchantment(config.getManagedResources().getHoeReapersMarkEnchantment());
    }

    @EventHandler
    public void witherEffect(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (event.getDamageSource().getDamageType() != DamageType.PLAYER_ATTACK) return;
        if (attacker.isBlocking()) return;

        final ItemStack item = attacker.getInventory().getItemInMainHand();

        if (!hasReapersMarkEnchantment(item)) return;
        if (event.getFinalDamage() <= 0) return;

        if (Utils.checkOddsOf(1)) {
            victim.addPotionEffect(
                    new PotionEffect(PotionEffectType.WITHER, (int) Utils.secondsToTicks(9), 0)
            );
        }

    }

}
