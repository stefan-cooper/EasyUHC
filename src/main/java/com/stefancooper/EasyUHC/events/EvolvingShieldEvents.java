package com.stefancooper.EasyUHC.events;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShield;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShieldUpgradeMenu;
import com.stefancooper.EasyUHC.base.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static com.stefancooper.EasyUHC.base.ConfigKey.ENABLE_EVOLVING_SHIELDS;

public class EvolvingShieldEvents implements Listener {

    private final Config config;

    public EvolvingShieldEvents(final Config config) {
        this.config = config;
    }

    // ----- Interacting with Shield Events -----

    @EventHandler
    // Cancel events that drop the evolving shield
    public void onDrop(final PlayerDropItemEvent event) {
        final ItemStack item = event.getItemDrop().getItemStack();

        if (EvolvingShield.isEvolvingShield(config, item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    // Cancel events that attempt to put the evolving shield in chests
    public void onInventoryClick(final InventoryClickEvent event) {
        final Inventory top = event.getView().getTopInventory();
        final ItemStack current = event.getCurrentItem();

        if (event.getWhoClicked() instanceof final Player player) {
            // Upgrade menu selection
            if (current != null && current.getItemMeta() != null && current.getItemMeta().getPersistentDataContainer().has(config.getManagedResources().getKeys().getEvolvingShieldUpgradeTypeKey())) {
                event.setCancelled(true);
                EvolvingShieldUpgradeMenu.applyUpgrade(config, player, current);
                player.closeInventory();
            }

            // Prevent placing into container with cursor ---
            if (event.getClickedInventory() == top) {
                final ItemStack cursor = event.getCursor();

                if (EvolvingShield.isEvolvingShield(config, cursor)) {
                    event.setCancelled(true);
                    return;
                }
            }

            if (event.isShiftClick()) {

                // Handling of upgrading a shield
                if (EvolvingShield.isEvolvingShield(config, current) && EvolvingShield.isUpgradeAvailable(config, current)) {
                    event.setCancelled(true);
                    new EvolvingShieldUpgradeMenu(config, player);
                    return;
                }

                // Prevent shift-click from player inventory into a container
                if (top.getType() != InventoryType.CRAFTING && event.getClickedInventory() == event.getView().getBottomInventory()) {
                    if (EvolvingShield.isEvolvingShield(config, current)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }

            // Prevent number key swaps into container ---
            if (event.getClick() == ClickType.NUMBER_KEY) {
                ItemStack hotbarItem = event.getWhoClicked()
                        .getInventory()
                        .getItem(event.getHotbarButton());

                if (EvolvingShield.isEvolvingShield(config, hotbarItem)
                        && event.getClickedInventory() == top) {
                    event.setCancelled(true);
                }
            }
        }


    }

    @EventHandler
    // Delete evolving shields on death
    public void onDeath(final PlayerDeathEvent event) {
        event.getDrops().removeIf(drop -> EvolvingShield.isEvolvingShield(config, drop));
    }

    @EventHandler
    // Cancel events that attempt to put the shield in an item frame
    public void onInteract(final PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof ItemFrame)) return;
        if (EvolvingShield.isEvolvingShield(config, event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    // ----- XP EVENTS -----
    @EventHandler
    public void onXPPickup(final PlayerPickupExperienceEvent event) {
        if (config.getProperty(ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS)) {
            final Player player = event.getPlayer();
            final Optional<ItemStack> getShield = EvolvingShield.getEvolvingShieldFromPlayer(config, player);
            if (getShield.isPresent()) {
                final ItemStack shield = getShield.get();
                EvolvingShield.updateXP(
                        config,
                        shield,
                        player,
                        event.getExperienceOrb().getExperience(),
                        EvolvingShield.EvolvingShieldXPType.EXPERIENCE
                );
            } else {
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(final EntityDamageByEntityEvent event) {
        // if damage is thorns, ignore
        if (event.getDamageSource().getDamageType() == DamageType.THORNS) {
            return;
        }
        if (config.getProperty(ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS) && event.getEntity() instanceof Player) {
            if (event.getDamager() instanceof final Player attacker) {
                // Melee
                final Optional<ItemStack> getShield = EvolvingShield.getEvolvingShieldFromPlayer(config, attacker);
                if (getShield.isPresent()) {
                    final ItemStack shield = getShield.get();
                    EvolvingShield.updateXP(
                            config,
                            shield,
                            attacker,
                            (int) Math.round(event.getFinalDamage()),
                            EvolvingShield.EvolvingShieldXPType.DAMAGE_HEARTS
                    );
                }
            } else if (event.getDamager() instanceof final Projectile projectile &&
                    (projectile instanceof Arrow || projectile instanceof Trident || projectile instanceof Fireball || projectile instanceof LightningStrike)
                    && projectile.getShooter() instanceof final Player attacker) {
                // Bow or Crossbow
                final Optional<ItemStack> getShield = EvolvingShield.getEvolvingShieldFromPlayer(config, attacker);
                if (getShield.isPresent()) {
                    final ItemStack shield = getShield.get();
                    EvolvingShield.updateXP(
                            config,
                            shield,
                            attacker,
                            (int) Math.round(event.getFinalDamage()),
                            EvolvingShield.EvolvingShieldXPType.DAMAGE_HEARTS
                    );
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        if (event.getEntity().getLastDamageCause() != null &&
                event.getEntity().getLastDamageCause().getDamageSource().getDirectEntity() != null &&
                event.getEntity().getLastDamageCause().getDamageSource().getDirectEntity().getType() == EntityType.PLAYER
        ) {
            final Player killer = (Player) event.getEntity().getLastDamageCause().getDamageSource().getDirectEntity();
            final Optional<ItemStack> getShield = EvolvingShield.getEvolvingShieldFromPlayer(config, killer);
            if (getShield.isPresent()) {
                final ItemStack shield = getShield.get();
                EvolvingShield.updateXP(
                        config,
                        shield,
                        killer,
                        EvolvingShield.EvolvingShieldXPType.KILL
                );
            }
        }
    }

    // --- Custom Shield Enchants ---
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

        if (config.getProperty(ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS)) {
            final int jumpLevel = shield.getEnchantmentLevel(config.getManagedResources().getShieldJumpEnchantment());
            final int swiftnessLevel = shield.getEnchantmentLevel(config.getManagedResources().getShieldSwiftnessEnchantment());
            final int weaknessLevel = shield.getEnchantmentLevel(config.getManagedResources().getShieldWeaknessEnchantment());
            final int strengthLevel = shield.getEnchantmentLevel(config.getManagedResources().getShieldStrengthEnchantment());
            final int slownessLevel = shield.getEnchantmentLevel(config.getManagedResources().getShieldSlownessEnchantment());

            // -- Buffs --
            if (jumpLevel > 0 && Utils.checkOddsOf(8)) {
                defender.addPotionEffect(PotionEffectType.JUMP_BOOST.createEffect((int) Utils.secondsToTicks(10), 5));
            }

            if (swiftnessLevel > 0 && Utils.checkOddsOf(16)) {
                defender.addPotionEffect(PotionEffectType.SPEED.createEffect((int) Utils.secondsToTicks(10), 2));
            }

            if (strengthLevel > 0 && Utils.checkOddsOf(24)) {
                defender.addPotionEffect(PotionEffectType.STRENGTH.createEffect((int) Utils.secondsToTicks(10), 0));
            }

            // -- Debuffs --
            if (slownessLevel > 0 && Utils.checkOddsOf(8)) {
                attacker.addPotionEffect(PotionEffectType.SLOWNESS.createEffect((int) Utils.secondsToTicks(10), 2));
            }

            if (weaknessLevel > 0 && Utils.checkOddsOf(16)) {
                attacker.addPotionEffect(PotionEffectType.WEAKNESS.createEffect((int) Utils.secondsToTicks(10), 0));
            }
        }

        // If you're looking for Thorns/Knockback, see EnchantmentEvents
    }

    final Map<UUID, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onShieldUse(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        // Ignore event if it is not a right click, or if the player is not sneaking
        // elemental/custom enchants here should only be triggered when player is sneaking
        if (!event.getAction().isRightClick() || !player.isSneaking()) return;

        // Check both hands for a shield
        final ItemStack shield;
        final ItemStack offHand = player.getInventory().getItemInOffHand();
        final ItemStack mainHand = player.getInventory().getItemInMainHand();

        // If shield is in both off hand + main hand, use the off hand one
        if (event.getHand() == EquipmentSlot.HAND && (offHand.getType() == Material.SHIELD && mainHand.getType() == Material.SHIELD) || mainHand.getType() == Material.SHIELD) {
            shield = mainHand;
        } else if (event.getHand() == EquipmentSlot.OFF_HAND && offHand.getType() == Material.SHIELD) {
            shield = offHand;
        } else {
            // if not using a shield, get outta here
            return;
        }

        if (config.getProperty(ENABLE_EVOLVING_SHIELDS, Defaults.ENABLE_EVOLVING_SHIELDS)) {

            final int windLevel = shield.getEnchantmentLevel(config.getManagedResources().getShieldWindEnchantment());
            final int fireLevel = shield.getEnchantmentLevel(config.getManagedResources().getShieldFireEnchantment());
            final int thunderLevel = shield.getEnchantmentLevel(config.getManagedResources().getShieldThunderEnchantment());
            final int waterLevel = shield.getEnchantmentLevel(config.getManagedResources().getShieldWaterEnchantment());

            if (windLevel > 0) {
                long now = System.currentTimeMillis();
                long last = cooldown.getOrDefault(player.getUniqueId(), 0L);

                if (!player.getGameMode().equals(GameMode.CREATIVE) && now - last < 30000) return;
                cooldown.put(player.getUniqueId(), now);

                config.getManagedResources().runTaskLater(() -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1, 1);
                    player.sendMessage(Component.text("Elemental shield cooldown refreshed!", Style.style(NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC)));
                }, Utils.secondsToTicks(30));

                final WindCharge charge = player.launchProjectile(WindCharge.class);
                charge.setVelocity(player.getLocation().getDirection().multiply(1.5));
            } else if (fireLevel > 0) {
                long now = System.currentTimeMillis();
                long last = cooldown.getOrDefault(player.getUniqueId(), 0L);

                if (!player.getGameMode().equals(GameMode.CREATIVE) && now - last < 60000) return;
                cooldown.put(player.getUniqueId(), now);

                config.getManagedResources().runTaskLater(() -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1, 1);
                    player.sendMessage(Component.text("Elemental shield cooldown refreshed!", Style.style(NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC)));
                }, Utils.secondsToTicks(60));

                final Fireball charge = player.launchProjectile(Fireball.class);
                charge.setVelocity(player.getLocation().getDirection().multiply(1.5));
            } else if (thunderLevel > 0) {
                long now = System.currentTimeMillis();
                long last = cooldown.getOrDefault(player.getUniqueId(), 0L);

                // Get target location (where player is looking)
                final Block target = player.getTargetBlockExact(20); // 20 block range
                if (target == null) return;

                if (!player.getGameMode().equals(GameMode.CREATIVE) && now - last < 120000) return;
                cooldown.put(player.getUniqueId(), now);

                config.getManagedResources().runTaskLater(() -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1, 1);
                    player.sendMessage(Component.text("Elemental shield cooldown refreshed!", Style.style(NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC)));
                }, Utils.secondsToTicks(120));

                player.getWorld().strikeLightning(target.getLocation());
            } else if (waterLevel > 0) {
                long now = System.currentTimeMillis();
                long last = cooldown.getOrDefault(player.getUniqueId(), 0L);

                if (!player.getGameMode().equals(GameMode.CREATIVE) && now - last < 10000) return;
                cooldown.put(player.getUniqueId(), now);

                config.getManagedResources().runTaskLater(() -> {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BANJO, 1, 1);
                    player.sendMessage(Component.text("Elemental shield cooldown refreshed!", Style.style(NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC)));
                }, Utils.secondsToTicks(10));

                final Snowball snowball = player.launchProjectile(Snowball.class);
                snowball.setVelocity(player.getLocation().getDirection().multiply(1.2));
                snowball.getPersistentDataContainer().set(
                        config.getManagedResources().getKeys().getShieldWaterEnchantment(),
                        PersistentDataType.BOOLEAN,
                        true
                );
            }
        }
    }

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof final Snowball snowball)) return;
        if (!snowball.getPersistentDataContainer().has(config.getManagedResources().getKeys().getShieldWaterEnchantment(), PersistentDataType.BOOLEAN)) return;
        final Location loc = snowball.getLocation();
        final Block block = loc.getBlock();
        if (!block.getType().isAir()) return;
        block.setType(Material.WATER);
    }

}
