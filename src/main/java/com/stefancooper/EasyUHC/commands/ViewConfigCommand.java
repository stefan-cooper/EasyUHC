package com.stefancooper.EasyUHC.commands;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.base.ConfigurationBook;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class ViewConfigCommand extends AbstractCommand {

    public static final String COMMAND_KEY = "view";

    public ViewConfigCommand(CommandSender sender, String cmd, String[] args, Config config) {
        super(sender, cmd, args, config);
    }

    @Override
    public void execute() {
        if (getArgs().length == 1) {
            final String viewConfigKey = getArgs()[0];
            final String prop = getConfig().getProp(viewConfigKey);

            if (prop != null) {
                getSender().sendMessage(viewConfigKey + "=" + prop);
            } else if (viewConfigKey.equals("config") && getSender() instanceof Player player) {
                ConfigurationBook.giveConfigurationBook(player, getConfig());
            } else {
                getSender().sendMessage("Unknown config value requested or not set");
            }
        } else {
            getConfig().getPlugin().getLogger().log(Level.FINE, "Too many arguments provided to view config");
            getSender().sendMessage("Too many arguments provided to view config");
        }
    }
}
