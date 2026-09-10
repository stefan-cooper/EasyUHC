package com.stefancooper.EasyUHC.base;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import com.stefancooper.EasyUHC.base.records.Coordinate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import static com.stefancooper.EasyUHC.base.ConfigKey.SPLIT_WITHIN_TEAMS_SIZE;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_CENTER_X;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_CENTER_Z;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_INITIAL_SIZE;
import static com.stefancooper.EasyUHC.base.ConfigKey.WORLD_BORDER_SPAWN_PADDING;

public class SpreadPlayers {

    final Config config;

    public SpreadPlayers(final Config config) {
        this.config = config;
    }


    public void trigger(final boolean force) {
        final int centerX = config.getProperty(WORLD_BORDER_CENTER_X, Defaults.WORLD_BORDER_CENTER_X);
        final int centerZ = config.getProperty(WORLD_BORDER_CENTER_Z, Defaults.WORLD_BORDER_CENTER_Z);
        final int diameter = config.getProperty(WORLD_BORDER_INITIAL_SIZE, Defaults.WORLD_BORDER_INITIAL_SIZE);
        final int splitPadding = config.getProperty(WORLD_BORDER_SPAWN_PADDING, Defaults.WORLD_BORDER_SPAWN_PADDING);
        final int splitSize = config.getProperty(SPLIT_WITHIN_TEAMS_SIZE, Defaults.SPLIT_WITHIN_TEAMS_SIZE);
        final World overworld = config.getWorlds().getOverworld();
        List<List<Player>> groups = getAllTeams();

        if (splitSize > 0) {
            groups = splitIntoClusters(groups, splitSize);
        }

        // minimum grid size of 3x3 (9)
        int gridSize = Math.max(groups.size(), 9);
        if (gridSize > 9) {
            // if there are more than 9 groups to split, then round up to the nearest square number
            // i.e. 10 groups == grid size of 16
            // this will give us a better chance of preventing 'unideal' starting locations
            int root = (int) Math.ceil(Math.sqrt(gridSize));
            gridSize = root * root;
        }

        List<Coordinate> coordinatesToTeleportTo = splitEvenly(centerX, centerZ, diameter - splitPadding, gridSize);

        final List<Coordinate> loadedCoordinatesToTeleportTo = new ArrayList<>(filterToCoordinatesWithLoadedChunks(
                coordinatesToTeleportTo,
                overworld
        ));

        if (loadedCoordinatesToTeleportTo.size() < groups.size()) {
            if (force) {
                config.getPlugin().getLogger().log(Level.WARNING, "UHC force started. Not all coordinates to be teleported to have been loaded, but 'force' was used, so continuing anyway.");
            } else {
                throw new RuntimeException("Not enough chunks generated to teleport to.");
            }
        } else {
            coordinatesToTeleportTo = loadedCoordinatesToTeleportTo;
        }

        final List<Coordinate> notIdealCoordinates = new ArrayList<>();
        for (final Coordinate startingLocation : coordinatesToTeleportTo) {
            final Block startingBlock = overworld.getHighestBlockAt((int) startingLocation.x(), (int) startingLocation.z());
            if (startingBlock.getType().equals(Material.LAVA) || startingBlock.getType().equals(Material.WATER)) {
                notIdealCoordinates.add(startingLocation);
            }
        }
        config.getPlugin().getLogger().log(Level.FINE, String.format("Unideal coordinates: %s", notIdealCoordinates));

        for (Coordinate coordinateIndex : notIdealCoordinates) {
            if (coordinatesToTeleportTo.size() <= groups.size()) {
                break;
            }
            coordinatesToTeleportTo.remove(coordinateIndex);
        }

        if (coordinatesToTeleportTo.size() < groups.size()) {
            throw new RuntimeException("something went wrong and we do not have enough coordinates to teleport to!");
        }

        Collections.shuffle(coordinatesToTeleportTo);

        for (int i = 0; i < groups.size(); i++) {
            final List<Player> team = groups.get(i);
            final Coordinate startingLocation = coordinatesToTeleportTo.get(i);
            for (final Player player : team) {
                if (player != null) {
                    final Block startingBlock = overworld.getHighestBlockAt((int) startingLocation.x(), (int) startingLocation.z());
                    startingBlock.setType(Material.BEDROCK);
                    player.teleport(startingBlock.getLocation().add(0, 2,0));
                }
            }
        }

    }

    private List<Coordinate> filterToCoordinatesWithLoadedChunks(final List<Coordinate> coordinates, final World world) {
        return coordinates.stream().filter(coordinate -> {
            final Location location = new Location(world, coordinate.x(), 64, coordinate.z());
            final int chunkX = location.getBlockX() >> 4;
            final int chunkZ = location.getBlockZ() >> 4;
            // World.isChunkGenerated() is not supported my MockBukkit, so always return true when testing
            return Utils.testMode() || world.isChunkGenerated(chunkX, chunkZ);
        }).toList();
    }

    private List<List<Player>> getAllTeams() {
        List<List<Player>> teams = new ArrayList<>();
        for (UHCTeam team : config.getManagedResources().getTeams()) {
            List<Player> teamPlayers = new ArrayList<>();
            for (String playerName : team.getPlayers()) {
                teamPlayers.add(Bukkit.getPlayerExact(playerName));
            }
            teams.add(teamPlayers);
        }
        return teams;
    }

    private List<List<Player>> splitIntoClusters(List<List<Player>> allTeams, int splitSize) {
        List<List<Player>> dividedTeams = new ArrayList<>();
        for (List<Player> team : allTeams) {
            for (int i = 0; i < team.size(); i += splitSize) {
                int end = Math.min(i + splitSize, team.size());
                dividedTeams.add(new ArrayList<>(team.subList(i, end)));
            }
        }

        return dividedTeams;
    }

    public static List<Coordinate> splitEvenly(final double centerX, final double centerZ, final double diameter, final int totalGroups) {
        if (diameter < 0) {
            return List.of();
        }
        if (totalGroups < 1) {
            return List.of();
        }
        if (totalGroups == 1) {
            return List.of(new Coordinate(centerX, centerZ));
        }
        final List<Coordinate> coordinates = new ArrayList<>(totalGroups);

        final int cols = (int) Math.ceil(Math.sqrt(totalGroups));
        final int rows = (int) Math.ceil((double) totalGroups / cols);

        final double padding = 0;
        final double usableDiameter = diameter - (2 * padding);

        final double colSpacing = (cols > 1) ? usableDiameter / (cols - 1) : 0;
        final double rowSpacing = (rows > 1) ? usableDiameter / (rows - 1) : 0;
        final double radius = diameter / 2.0;
        final double startX = centerX - radius + padding;
        final double startZ = centerZ - radius + padding;

        int coordinatesAdded = 0;
        for (int row = 0; row < rows && coordinatesAdded < totalGroups; row++) {
            for (int col = 0; col < cols && coordinatesAdded < totalGroups; col++) {
                final double x = startX + (col * colSpacing);
                final double z = startZ + (row * rowSpacing);

                coordinates.add(new Coordinate(x,z));
                coordinatesAdded++;
            }
        }

        return coordinates;
    }
}
