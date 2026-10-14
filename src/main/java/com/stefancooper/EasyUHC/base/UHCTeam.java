package com.stefancooper.EasyUHC.base;

import com.stefancooper.EasyUHC.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UHCTeam {

    private final NamedTextColor color;
    private final String playerNames;
    private final List<Player> players;
    private final String name;

    public UHCTeam(final String name, final String playerNames, final NamedTextColor color) {
        this.name = name;
        this.playerNames = playerNames;
        this.color = color;
        this.players = new ArrayList<>(getPlayerNames().stream().map(Bukkit::getPlayerExact).filter(Objects::nonNull).toList());
    }

    public boolean teamIsInServer() { return getPlayerNames().size() == players.size(); }

    public String getName() {
        return name;
    }

    public List<String> getPlayerNames() {
        return Arrays.stream(playerNames.split(",")).map(String::trim).toList();
    }

    public List<Player> getPlayers() {
        return players;
    }

    public NamedTextColor getColor() {
        return color;
    }

    public static void createTeam(final UHCTeam uhcTeam, final Config config) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        // if team already exists, redefine it
        if (scoreboard.getTeam(uhcTeam.getName()) != null) {
            scoreboard.getTeam(uhcTeam.getName()).unregister();
        }
        Team team = scoreboard.registerNewTeam(uhcTeam.getName());
        uhcTeam.getPlayerNames().forEach(player -> {
            final Team entryTeam = scoreboard.getEntryTeam(player);
            // if player is already on another team, remove them from that team and put them on this team
            if (entryTeam != null) {
                entryTeam.removeEntry(player);
            }
            team.addEntry(player);
        });
        team.color(uhcTeam.getColor());
        team.prefix(Component.text(String.format("[%s] ", uhcTeam.getName())));
        config.getManagedResources().addTeam(uhcTeam);
    }
}
