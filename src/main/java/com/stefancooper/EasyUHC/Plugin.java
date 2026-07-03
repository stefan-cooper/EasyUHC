package com.stefancooper.EasyUHC;

import com.stefancooper.EasyUHC.base.Utils;
import com.stefancooper.EasyUHC.commands.UHCCommand;
import com.stefancooper.EasyUHC.events.EvolvingShieldEvents;
import com.stefancooper.EasyUHC.events.BaseEvents;
import com.stefancooper.EasyUHC.events.EnchantmentEvents;
import com.stefancooper.EasyUHC.events.PerformanceEvents;
import com.stefancooper.EasyUHC.events.ReviveEvents;
import com.stefancooper.EasyUHC.events.TimestampEvents;
import com.stefancooper.EasyUHC.events.UHCLootEvents;
import com.stefancooper.EasyUHC.base.Constants;
import com.stefancooper.EasyUHC.events.WinEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.EnchantmentKeys;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Plugin extends JavaPlugin implements Listener {

    private Config config;
    private boolean isLive;
    private boolean countingDown;

    @Override
    // This is called when the plugin is loaded into the server.
    public void onEnable() {
        config = new Config(this);
        Defaults.setDefaultGameRules(this.config);
        Bukkit.getPluginManager().registerEvents(new BaseEvents(config), this);
        Bukkit.getPluginManager().registerEvents(new ReviveEvents(config), this);
        Bukkit.getPluginManager().registerEvents(new TimestampEvents(config), this);
        Bukkit.getPluginManager().registerEvents(new WinEvents(config), this);
        Bukkit.getPluginManager().registerEvents(new UHCLootEvents(config), this);
        Bukkit.getPluginManager().registerEvents(new EnchantmentEvents(config), this);
        Bukkit.getPluginManager().registerEvents(new PerformanceEvents(config), this);
        Bukkit.getPluginManager().registerEvents(new EvolvingShieldEvents(config), this);
        isLive = false;

        this.getLogger().log(Level.INFO, "UHC Plugin enabled");
        getServer().getCommandMap().register("uhc", new UHCCommand(config));

        assertCustomEnchantmentsExist();
    }

    private void assertCustomEnchantmentsExist() {
        final Registry<Enchantment> enchantRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
        final Enchantment quickBoom = enchantRegistry.get(EnchantmentKeys.create(Key.key(String.format("%s:%s", Constants.NAMESPACE, com.stefancooper.EasyUHC.enchants.Constants.QUICKBOOM_ENCHANTMENT))));

        if (quickBoom == null) getLogger().warning("QuickBoom enchantment not found in registry!");
        else getLogger().fine("QuickBoom enchantment loaded successfully.");
    }

    public Config getUHCConfig() {
        return config;
    }

    // This is called when the plugin is unloaded from the server.
    public void onDisable() {}

    public void setUHCLive(final boolean isLive) {
        this.isLive = isLive;
    }

    // Has the UHC started?
    public boolean isUHCLive() { return isLive; }

    public boolean isCountingDown() { return countingDown; }

    public void setCountingDown(boolean countingDown) { this.countingDown = countingDown; }
}

