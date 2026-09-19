package com.stefancooper.EasyUHC.base;

import com.stefancooper.EasyUHC.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import java.util.Arrays;
import java.util.List;

public class UHCTeam {

    private final NamedTextColor color;
    private final String players;
    private final String name;

    public UHCTeam(String name, String players, NamedTextColor color) {
        this.name = name;
        this.players = players;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public List<String> getPlayers() {
        return Arrays.stream(players.split(",")).map(String::trim).toList();
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
        uhcTeam.getPlayers().forEach(player -> {
            // if player is already on another team, remove them from that team and put them on this team
            if (scoreboard.getEntryTeam(player) != null) {
                scoreboard.getEntryTeam(player).removeEntry(player);
            }
            team.addEntry(player);
        });
        team.color(uhcTeam.getColor());
//        team.setAllowFriendlyFire(false);
        team.prefix(Component.text(String.format("[%s] ", uhcTeam.getName())));
        config.getManagedResources().addTeam(uhcTeam);
    }
}
