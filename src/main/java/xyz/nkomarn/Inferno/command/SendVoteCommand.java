package xyz.nkomarn.Inferno.command;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.nkomarn.Inferno.util.Config;

public class SendVoteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("inferno.sendvote")) return true;

        if (args.length < 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                    "%sSend a test vote using /sendvote [player].", Config.getPrefix()
            )));
        } else if (Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                    "%s%s is offline.", Config.getPrefix(), args[0]
            )));
        } else {
            Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(new Vote("Test", args[0],
                    "127.0.0.1", String.valueOf(System.currentTimeMillis()))));
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                    "%sThe test vote was sent!", Config.getPrefix()
            )));
        }
        return true;
    }
}
