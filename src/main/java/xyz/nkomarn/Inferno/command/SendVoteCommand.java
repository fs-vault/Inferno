package xyz.nkomarn.Inferno.command;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SendVoteCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.isOp()) return true;
        if (args.length < 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&d&lVotes: &7Usage: /sendvote <username>"));
            return true;
        }

        if (Bukkit.getPlayer(args[0]) == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&d&lVotes: &7Player is offline."));
        } else {
            Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(
                    new Vote("Test", args[0], "127.0.0.1", String.valueOf(System.currentTimeMillis()))));
        }
        return true;
    }
}
