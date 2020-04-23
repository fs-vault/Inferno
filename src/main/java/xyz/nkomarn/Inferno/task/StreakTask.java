package xyz.nkomarn.Inferno.task;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import xyz.nkomarn.Inferno.Inferno;
import xyz.nkomarn.Inferno.util.Config;
import xyz.nkomarn.Kerosene.data.LocalStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StreakTask implements Runnable {
    @Override
    public void run() {
        try (Connection connection = LocalStorage.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("SELECT uuid, last_vote FROM votes;")) {
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        if (TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() -
                                result.getLong(2)) >= 2) {
                            try (PreparedStatement statement1 = connection.prepareStatement("UPDATE votes SET level=0, " +
                                    "votes=0, last_vote=? WHERE uuid=?")) {
                                statement1.setLong(1, System.currentTimeMillis());
                                statement1.setString(2, result.getString(1));
                                statement1.executeUpdate();
                            }
                        }
                    }
                }
            }

            final ArrayList<String> lines = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT uuid, level, votes FROM votes " +
                    "ORDER BY level DESC LIMIT 5;")) {
                try (ResultSet result = statement.executeQuery()) {
                    int i = 1;
                    while (result.next()) {
                        lines.add(ChatColor.translateAlternateColorCodes('&', String.format(
                                Config.getString("hologram.entry"), i++, Bukkit.getOfflinePlayer(UUID
                                        .fromString(result.getString(1))).getName(), result.getInt(2)
                        )));
                    }
                }
            }

            Bukkit.getScheduler().runTask(Inferno.getInferno(), () -> {
                Inferno.HOLOGRAM.clearLines();
                Inferno.HOLOGRAM.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&d&lTop Vote Streaks"));
                lines.forEach(line -> Inferno.HOLOGRAM.appendTextLine(ChatColor.translateAlternateColorCodes('&', line)));
                Inferno.HOLOGRAM.appendTextLine(ChatColor.translateAlternateColorCodes('&', "&e/vote"));
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
