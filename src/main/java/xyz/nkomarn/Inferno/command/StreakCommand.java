package xyz.nkomarn.Inferno.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import xyz.nkomarn.Inferno.Inferno;
import xyz.nkomarn.Inferno.util.Config;

import java.sql.Connection;
import java.sql.SQLException;

public class StreakCommand implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) return true;

        final Player player = (Player) sender;
        Bukkit.getScheduler().runTaskAsynchronously(Inferno.getInferno(), () -> {
            try (Connection connection = Inferno.getStorage().getConnection()) {
                final int streakLevel = Inferno.getStreakLevel(connection, player);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                        "%sYour voting streak is &d%s&7 %s long.",
                        Config.getPrefix(), streakLevel, Inferno.getDayString(streakLevel)
                )));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
        return true;
    }
}
