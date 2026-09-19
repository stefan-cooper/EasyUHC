package com.stefancooper.EasyUHC.commands;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.base.ConfigKey;
import com.stefancooper.EasyUHC.evolvingshield.EvolvingShield;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class GiveCommand extends AbstractCommand {

    public static final String COMMAND_KEY = "give";
    public static final String SHIELD_XP = "shieldxp";

    private enum Giveable {
        SHIELD_XP("shieldxp"),
        SHIELD("shield"),
        WARMUP_BUTTON("warmupbutton");

        public final String giveable;

        Giveable(String name) {
            this.giveable = name;
        }

        public static Giveable fromString(String giveable) {
            for (Giveable key : Giveable.values()) {
                if (key.giveable.equalsIgnoreCase(giveable)) {
                    return key;
                }
            }
            return null;
        }

        }


    public GiveCommand(CommandSender sender, String cmd, String[] args, Config config) {
        super(sender, cmd, args, config);
    }

    @Override
    public void execute() {

        final Giveable giveable = Giveable.fromString(getArgs()[0]);
        if (giveable == null) {
            getConfig().getPlugin().getLogger().log(Level.WARNING, "Invalid giveable: " + getArgs()[0]);
            getSender().sendMessage("Invalid giveable: " + getArgs()[0]);
        }
        try {
            switch (giveable) {
                case SHIELD_XP -> {
                    if (getArgs().length < 2) {
                        getConfig().getPlugin().getLogger().log(Level.WARNING, "Invalid shield XP give. No XP provided");
                        getSender().sendMessage("Invalid shield XP give. No XP provided");
                    }
                    final int total = Integer.parseInt(getArgs()[1]);
                    if (getArgs().length == 2) {
                        final Player sender = Bukkit.getPlayer(getSender().getName());
                        final Optional<ItemStack> getShield = EvolvingShield.getEvolvingShieldFromPlayer(getConfig(), sender);
                        getShield.ifPresent(shield -> EvolvingShield.updateXP(
                                getConfig(),
                                shield,
                                sender,
                                total,
                                EvolvingShield.EvolvingShieldXPType.MANUAL
                        ));
                    } else {
                        for (int i = 2; i < getArgs().length; i++) {
                            final Player player = Bukkit.getPlayer(getArgs()[i]);
                            final Optional<ItemStack> getShield = EvolvingShield.getEvolvingShieldFromPlayer(getConfig(), player);
                            getShield.ifPresent(shield -> EvolvingShield.updateXP(
                                    getConfig(),
                                    shield,
                                    player,
                                    total,
                                    EvolvingShield.EvolvingShieldXPType.MANUAL
                            ));
                        }
                    }
                }
                case WARMUP_BUTTON -> {
                    final Player sender = Bukkit.getPlayer(getSender().getName());
                    final ItemStack warmupButton = new ItemStack(Material.POLISHED_BLACKSTONE_BUTTON);
                    final ItemMeta btnMeta = warmupButton.getItemMeta();
                    btnMeta.displayName(Component.text("Warmup Button"));
                    btnMeta.getPersistentDataContainer().set(getConfig().getManagedResources().getKeys().getWarmupButtonKey(), PersistentDataType.BOOLEAN, true);
                    btnMeta.setEnchantmentGlintOverride(true);
                    warmupButton.setItemMeta(btnMeta);
                    sender.give(warmupButton);
                }
                case SHIELD -> {
                    if (getArgs().length == 1) {
                        final Player sender = Bukkit.getPlayer(getSender().getName());
                        final Optional<ItemStack> getShield = EvolvingShield.getEvolvingShieldFromPlayer(getConfig(), sender);
                        if (getShield.isEmpty()) {
                            EvolvingShield.createEvolvingShield(getConfig(), sender);
                        } else {
                            getConfig().getPlugin().getLogger().log(Level.WARNING, "Not giving " + sender.getName() + " an evolving shield, as they already have one");
                            getSender().sendMessage("Not giving " + sender.getName() + " an evolving shield, as they already have one");
                        }
                    } else {
                        for (int i = 1; i < getArgs().length; i++) {
                            final Player player = Bukkit.getPlayer(getArgs()[i]);
                            final Optional<ItemStack> getShield = EvolvingShield.getEvolvingShieldFromPlayer(getConfig(), player);
                            if (getShield.isEmpty()) {
                                EvolvingShield.createEvolvingShield(getConfig(), player);
                            } else {
                                getConfig().getPlugin().getLogger().log(Level.WARNING, "Not giving " + player.getName() + " an evolving shield, as they already have one");
                                getSender().sendMessage("Not giving " + player.getName() + " an evolving shield, as they already have one");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            getConfig().getPlugin().getLogger().log(Level.WARNING, "Something went wrong giving: " + giveable + "\n" + e);
            getSender().sendMessage("Something went wrong giving: " + giveable);
        }
    }
}
