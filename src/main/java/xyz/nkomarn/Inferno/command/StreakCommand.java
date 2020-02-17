package xyz.nkomarn.Inferno.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.nkomarn.Inferno.Inferno;
import xyz.nkomarn.Inferno.util.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StreakCommand implements CommandExecutor {
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player)) {
            Inferno.getInferno().getLogger().info("Only a player can have streaks.");
            return true;
        }

        Player player = (Player) sender;
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    final Connection connection = Inferno.getHandler().open();
                    if (connection == null) {
                        Inferno.getInferno().getLogger().warning("Database error.");
                        return;
                    }

                    PreparedStatement statement = connection.prepareStatement("SELECT level " +
                            "FROM inferno WHERE uuid=?");
                    statement.setString(1, player.getUniqueId().toString());

                    final ResultSet result = statement.executeQuery();
                    while (result.next()) {
                        int level = result.getInt(1);
                        String dayString; // "Day" or "days"
                        int nextLevel = level + 1;
                        if (nextLevel == 1) dayString = "day";
                        else dayString = "days";

                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                Config.getString("streak"), Config.getPrefix(), level, dayString
                        )));
                    }
                    statement.close();
                    connection.close();
                } catch (SQLException exception) {
                    exception.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Inferno.getInferno());
        return true;
    }
}
