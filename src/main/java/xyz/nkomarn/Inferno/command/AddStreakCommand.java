package xyz.nkomarn.Inferno.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import xyz.nkomarn.Inferno.Inferno;
import xyz.nkomarn.Inferno.util.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AddStreakCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("inferno.addstreak")) return true;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                    "%sAdd streak levels to a player using /addstreak [player] [levels].", Config.getPrefix()
            )));
        } else if (!args[1].matches(".*\\d.*")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                    "%sLevels must be a number.", Config.getPrefix()
            )));
        } else {
            final int levels = Integer.parseInt(args[1]);
            Bukkit.getScheduler().runTaskAsynchronously(Inferno.getInferno(), () -> {
                try (Connection connection = Inferno.getStorage().getConnection()) {
                    try (PreparedStatement statement = connection.prepareStatement("UPDATE votes SET level = level + ? " +
                            "WHERE uuid = ?;")) {
                        statement.setInt(1, levels);
                        statement.setString(2, Bukkit.getOfflinePlayer(args[0]).getUniqueId().toString());
                        statement.executeUpdate();
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                "%sAdded &d%s &7streak levels!", Config.getPrefix(), levels
                        )));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }
        return true;
    }
}
