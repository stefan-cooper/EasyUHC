package com.stefancooper.EasyUHC.base;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UHCCommandTabCompleter  {
    public static List<String> onTabComplete(final CommandSender sender, final String label, final String[] args) {
        List<String> suggestions = new ArrayList<>();

        // First argument
        if (args.length == 1) {
            suggestions.addAll(Arrays.asList("set", "view", "start", "resume", "latestart", "cancel", "pvp", "randomise", "unset", "give"));
        }
        // Suggestions for "set" command
        else if (args.length >= 2 && (args[0].equalsIgnoreCase("set"))) {
            List<String> configKeys = Arrays.stream(ConfigKey.values()).toList().stream().map((key) -> key.configName + "=").toList();
            suggestions.addAll(configKeys);
        }
        // Suggestions for "unset" command
        else if (args.length >= 2 && (args[0].equalsIgnoreCase("unset"))) {
            List<String> configKeys = Arrays.stream(ConfigKey.values()).toList().stream().map((key) -> key.configName).toList();
            suggestions.addAll(configKeys);
        }
        // Suggestions for "view" command
        else if (args.length >= 2 && args[0].equalsIgnoreCase("view")) {
            List<String> configKeys = Arrays.stream(ConfigKey.values()).toList().stream().map((key) -> key.configName).toList();
            suggestions.addAll(configKeys);
            suggestions.add("config");
        }
        // Third argument for "set team.<colour>="
        else if (args.length == 3 && args[0].equalsIgnoreCase("set") && args[1].startsWith("team.")) {
            String color = args[1].substring(5); // Extract the color part after "team."

            // Validate color
            if (Arrays.asList("red", "blue", "green", "yellow", "orange", "pink").contains(color)) {
                // Check if the input ends with '=' to suggest player names
                if (args[1].endsWith("=")) {
                    // Suggest all online player names
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        suggestions.add(player.getName());
                    }
                } else if (args[1].contains("=")) {
                    // If there's text after '=', suggest based on the input after it
                    String prefix = args[1].substring(args[1].indexOf('=') + 1); // Get text after '='
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(prefix.toLowerCase())) {
                            suggestions.add(player.getName());
                        }
                    }
                }
            }
        }
        // Suggestions for latestart
        else if (args.length >= 2 && (args[0].equalsIgnoreCase("latestart"))) {
            Bukkit.getOnlinePlayers().forEach(player -> suggestions.add(player.getName()));
        }

        else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
           suggestions.add("shieldxp");
        }

        else if (args.length >= 4 && args[0].equalsIgnoreCase("give") && args[1].equalsIgnoreCase("shieldxp")) {
            Bukkit.getOnlinePlayers().forEach(player -> suggestions.add(player.getName()));
        }

        // Filter suggestions based on the last argument typed
        List<String> filteredSuggestions = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                filteredSuggestions.add(suggestion);
            }
        }

        return filteredSuggestions;
    }
}
