package com.stefancooper.EasyUHC.base;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import static com.stefancooper.EasyUHC.base.ConfigKey.REVIVE_ENABLED;

public class Utils {

    public static World getWorld (String name) {
        final String worldName = name != null ? name : Defaults.WORLD_NAME;
        if (Bukkit.getWorld(worldName) == null) return Bukkit.createWorld(WorldCreator.name(worldName).environment(World.Environment.NORMAL));
        else return Bukkit.getWorld(worldName);
    }

    public interface WorldBorderCallback {
        void execute(World world);
    }

    public static void setWorldEffects(List<World> worlds, WorldBorderCallback callback) {
        worlds.forEach(callback::execute);
    }

    // Some things are managed in Minecraft ticks. Use this to convert from seconds to ticks
    public static long secondsToTicks (int seconds) {
        return (long) seconds * 20;
    }

    /**
     * Calculation:
     *  Required fields: Initial Size, Final Size, Current Size
     *  -
     *  Shrink Total Distance = Initial - Final
     *  Shrink Progress = Initial - Current
     *  -
     *  Percentage = ( Total - Progress ) / Total
     */
    public static double calculateWorldBorderProgress (int initialSize, int finalSize, int currentSize) {
        int distanceToShrink = initialSize - finalSize;
        int progress = initialSize - currentSize;
        return (double) (distanceToShrink - progress) / distanceToShrink;
    }

    public static boolean testMode() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (element.getClassName().startsWith("org.junit.")) {
                return true;
            }
        }
        return false;
    }

    public static String getResourceLocation(final String fileName) {
        if (testMode()) {
            return String.format("./src/test/java/resources/%s", fileName);
        } else {
            return String.format("./plugins/%s", fileName);
        }
    }

    public static void spawnDustParticle(final World world, final Location location, final int count, final Particle.DustOptions dustOptions) {
        try {
            world.spawnParticle(Particle.DUST, location, count, 0.25, 0.25, 0.25, 0.0, dustOptions, true);
        } catch (Exception e) {
            // noop
            // for some reason, this function is not implemented in MockBukkit but it is not worth us mocking
        }
    }

    public static void spawnParticle(final World world, final Particle particle, final Location location, final int count) {
        try {
            world.spawnParticle(particle, location, count);
        } catch (Exception e) {
            // noop
            // for some reason, this function is not implemented in MockBukkit but it is not worth us mocking
        }
    }

    public static boolean isReviveViaArmorStandEnabled(final Config config) {
        return config.getProperty(REVIVE_ENABLED, Defaults.REVIVE_ENABLED);
    }

    public static boolean checkOddsOf(final int outOf) {
        return ThreadLocalRandom.current().nextInt(outOf) == 0;
    }
}
