package com.stefancooper.EasyUHC.events;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.Defaults;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Scoreboard;
import com.stefancooper.EasyUHC.base.UHCTeam;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static com.stefancooper.EasyUHC.base.ConfigKey.*;

public class WinEvents implements Listener {

    private final Config config;

    public WinEvents(Config config) {
        this.config = config;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String playerName = player.getName();

        Bukkit.getLogger().log(Level.FINE, "Player Death Event triggered for: " + playerName);

        List<UHCTeam> teamsWithSurvivors = getTeamsWithSurvivors();

        final boolean doNotEndGameAutomatically = config.getProperty(DISABLE_END_GAME_AUTOMATICALLY, Defaults.DISABLE_END_GAME_AUTOMATICALLY);

        if (!doNotEndGameAutomatically && config.getPlugin().isUHCLive() && teamsWithSurvivors.size() == 1) {
            UHCTeam winningTeam = teamsWithSurvivors.getFirst();
            String winningTeamName = winningTeam.getName();
            List<String> winningTeamMembers = new ArrayList<>(Bukkit.getScoreboardManager().getMainScoreboard().getTeam(winningTeamName).getEntries());

            String formattedWinningMembers;
            if (winningTeamMembers.size() > 1) {
                formattedWinningMembers = String.join(", ", winningTeamMembers.subList(0, winningTeamMembers.size() - 1))
                        + " and " + winningTeamMembers.getLast();
            } else {
                formattedWinningMembers = winningTeamMembers.getFirst();
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showTitle(Title.title(Component.text("Congratulations to Team " + winningTeamName + "!", NamedTextColor.GOLD), Component.text("GG " + formattedWinningMembers + "!"),  10, 100, 10));
            }

            // cancel timers that might have been running
            config.getManagedResources().cancelTimer();
            config.getPlugin().setUHCLive(false);

            config.getManagedResources().runTaskLater(() -> {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    onlinePlayer.showTitle(Title.title(Component.text("Thank you all for playing!", NamedTextColor.GOLD), Component.text("Hope it was fun!"), 10, 40, 10));
                }
            }, 5);
        }

        Bukkit.getLogger().log(Level.FINE,"Updated teams with survivors after " + playerName + "'s death.");
    }

    private List<UHCTeam> getTeamsWithSurvivors() {
        List<UHCTeam> teamsWithSurvivors = new ArrayList<>();
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        for (Team team : scoreboard.getTeams()) {
            boolean hasSurvivor = false;
            List<String> playerNames = new ArrayList<>();

            for (String playerName : team.getEntries()) {
                Player player = Bukkit.getPlayer(playerName);
                if (player != null && player.getGameMode() == GameMode.SURVIVAL) {
                    hasSurvivor = true;
                }
                playerNames.add(playerName);
            }

            if (hasSurvivor) {
                String playersAsString = String.join(", ", playerNames);
                UHCTeam uhcTeam = new UHCTeam(team.getName(), playersAsString, NamedTextColor.namedColor(team.color().value()));
                teamsWithSurvivors.add(uhcTeam);
            }
        }

        return teamsWithSurvivors;
    }
}
