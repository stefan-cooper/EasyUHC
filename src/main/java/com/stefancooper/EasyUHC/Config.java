package com.stefancooper.EasyUHC;

import com.stefancooper.EasyUHC.base.ConfigKey;
import com.stefancooper.EasyUHC.base.records.Configurable;
import com.stefancooper.EasyUHC.base.Worlds;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.stream.Stream;

import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_NAME;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_NAME_END;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_NAME_NETHER;
import static com.stefancooper.EasyUHC.base.ConfigKey.fromString;
import static com.stefancooper.EasyUHC.base.Constants.CONFIG_LOCATION;

public class Config {

    private Properties config;
    private final Properties defaultConfig;
    private final ConfigParser parser;
    private final Plugin plugin;
    private final ManagedResources managedResources;
    private final Worlds worlds;

    public Config(Plugin plugin) {
        this.plugin = plugin;
        this.config = new Properties();
        this.parser = new ConfigParser(this);
        this.defaultConfig = Defaults.createDefaultConfig();
        this.config = loadInitialProperties();
        this.managedResources = new ManagedResources(this);
        this.worlds = new Worlds(this);
        this.setDefaults();
        parser.executeConfigurables(this.config.entrySet().stream().map(prop -> parser.propertyToConfigurable((String) prop.getKey(), (String) prop.getValue())).toList());
    }

    private Properties loadInitialProperties() {
        final Properties props = new Properties();
        try {
            boolean file = new File(CONFIG_LOCATION).createNewFile();
            // if file had to be created, go and set the defaults
            if (file) setDefaults();
            else {
                final FileInputStream in = new FileInputStream(CONFIG_LOCATION);
                props.load(in);
                in.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return props;
    }

    public ManagedResources getManagedResources() {
        return managedResources;
    }

    public Worlds getWorlds() { return worlds; }

    public Plugin getPlugin() {
        return plugin;
    }

    public String getProp(String key) {
        return (String) config.get(key);
    }

    /** improved version of getProp, this will use the parsing that we've already done */
    public <T> T getProperty(ConfigKey key, T defaultValue) {
        final String value = getProp(key.configName);
        if (value != null) {
            return Optional.ofNullable((T) parser.propertyToConfigurable(key.configName, value).value()).orElse(defaultValue);
        }
        return defaultValue;
    }

    /** improved version of getProp, this will use the parsing that we've already done */
    @Nullable
    public <T> T getProperty(ConfigKey key) {
        final String value = getProp(key.configName);
        if (value != null) {
            return (T) parser.propertyToConfigurable(key.configName, value).value();
        }
        return null;
    }

    public String getProps() {
        Stream<String> mapped = this.config.entrySet().stream().sorted(Comparator.comparing(i1 -> (String) i1.getKey())).map(prop -> prop.getKey() + "=" + prop.getValue() + "\n");

        return mapped.reduce("", String::concat);
    }

    public void setProp(String key, String value) {
        Configurable<?> configurable = parser.propertyToConfigurable(key, value);
        if (configurable != null) {
            config.setProperty(key, value);
            parser.executeConfigurable(configurable);
            try {
                File file = new File(CONFIG_LOCATION);
                FileOutputStream fos = new FileOutputStream(file);
                config.store(fos, "saving new value");
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            this.getPlugin().getLogger().log(Level.WARNING, "Invalid config value attempted to be set: " + key + "=" + value);
        }
    }

    public void unsetProp(String key) {
        if (fromString(key) != null) {
            config.remove(key);
            try {
                File file = new File(CONFIG_LOCATION);
                FileOutputStream fos = new FileOutputStream(file);
                config.store(fos, "saving new value");
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            this.getPlugin().getLogger().log(Level.WARNING, "Unknown config key. Not unsetting.");
        }
    }

    private void setDefaults() {
        defaultConfig.forEach((key, value) -> {
            if (getProp((String) key) == null) {
                setProp((String) key, (String) value);
            }
        });
    }

    public void resetToDefaults() {
        config.clear();
        setProp(WORLD_NAME.configName, (String) defaultConfig.get(WORLD_NAME.configName));
        setProp(WORLD_NAME_NETHER.configName, (String) defaultConfig.get(WORLD_NAME_NETHER.configName));
        setProp(WORLD_NAME_END.configName, (String) defaultConfig.get(WORLD_NAME_END.configName));
        defaultConfig.forEach((key, value) -> setProp((String) key, (String) value));
        Defaults.setDefaultGameRules(this);
    }

    public void trigger() {
        setProp(WORLD_NAME.configName, (String) defaultConfig.get(WORLD_NAME.configName));
        config.forEach((key, value) -> setProp((String) key, (String) value));
        Defaults.setDefaultGameRules(this);
    }

}
