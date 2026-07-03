package com.stefancooper.EasyUHC.commands;

import com.stefancooper.EasyUHC.Config;
import com.stefancooper.EasyUHC.base.UHCCommandTabCompleter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;


public class UHCCommand extends Command {

    public static final String COMMAND_KEY = "uhc";
    private final Config config;

    public UHCCommand(final Config config) {
        super(COMMAND_KEY);
        this.config = config;
    }

    /** Used to pass to child commands so that we don't pass the command key to them */
    private String[] getCommandArgs (String[] allArgs) {
        return Arrays.copyOfRange(allArgs, 1, allArgs.length);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String cmd, @NotNull String @NotNull [] args) {
        if (cmd.equals("uhc") && args.length > 0) {
            switch (args[0]) {
                case SetConfigCommand.COMMAND_KEY:
                    new SetConfigCommand(sender, cmd, getCommandArgs(args), config).execute();
                    return true;
                case UnsetConfigCommand.COMMAND_KEY:
                    new UnsetConfigCommand(sender, cmd, getCommandArgs(args), config).execute();
                    return true;
                case ViewConfigCommand.COMMAND_KEY:
                    new ViewConfigCommand(sender, cmd, getCommandArgs(args), config).execute();
                    return true;
                case StartCommand.COMMAND_KEY:
                    new StartCommand(sender, cmd, getCommandArgs(args), config).execute();
                    return true;
                case CancelCommand.COMMAND_KEY:
                    new CancelCommand(sender, cmd, getCommandArgs(args), config).execute();
                    return true;
                case ResumeCommand.COMMAND_KEY:
                    new ResumeCommand(sender, cmd, getCommandArgs(args), config).execute();
                    return true;
                case LateStartCommand.COMMAND_KEY:
                    new LateStartCommand(sender, cmd, getCommandArgs(args), config).execute();
                    return true;
                case RandomiseTeamsCommand.COMMAND_KEY:
                    new RandomiseTeamsCommand(sender, cmd, getCommandArgs(args), config).execute();
                    return true;
                case GiveCommand.COMMAND_KEY:
                    new GiveCommand(sender, cmd, getCommandArgs(args), config).execute();
                    return true;
                default:
                    break;
            }
        }
        return false;
    }

    @Override
    public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return UHCCommandTabCompleter.onTabComplete(sender, alias, args);
    }
}
