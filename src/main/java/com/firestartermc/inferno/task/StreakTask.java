package com.firestartermc.inferno.task;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import com.firestartermc.inferno.Inferno;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StreakTask implements Runnable {

    private final Inferno inferno;
    private final ItemStack floatingItem;

    public StreakTask(@NotNull Inferno inferno) {
        this.inferno = inferno;
        this.floatingItem = new ItemStack(Material.PAPER, 1);
    }

    @Override
    public void run() {
        try {
            Connection connection = inferno.getStorage().getConnection();
            PreparedStatement statement = connection.prepareStatement("SELECT uuid, last_vote FROM votes;");
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                long time = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - result.getLong(2));

                if (time >= 2) {
                    PreparedStatement statement1 = connection.prepareStatement("DELETE FROM votes WHERE uuid = ?;");
                    statement1.setString(1, result.getString(1));
                    inferno.getStreaks().setStreak(UUID.fromString(result.getString(1)), 0);
                }
            }

            ArrayList<String> leaderboard = new ArrayList<>();
            AtomicInteger index = new AtomicInteger();
            PreparedStatement statement1 = connection.prepareStatement("SELECT uuid, level, votes FROM votes ORDER BY level DESC LIMIT 5;");
            ResultSet result1 = statement1.executeQuery();

            try (connection; statement1; result1) {
                while (result1.next()) {
                    leaderboard.add(ChatColor.translateAlternateColorCodes('&', String.format(
                            inferno.getConfig().getString("hologram.entry", ""), index.incrementAndGet(),
                            Bukkit.getOfflinePlayer(UUID.fromString(result1.getString(1))).getName(), result1.getInt(2)
                    )));
                }
            }

            updateLeaderboard(leaderboard);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLeaderboard(@NotNull ArrayList<String> lines) {
        Bukkit.getScheduler().runTask(inferno, () -> {
            Hologram leaderboard = inferno.getLeaderboard();
            leaderboard.clearLines();
            leaderboard.appendItemLine(floatingItem);
            leaderboard.appendTextLine(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Top Vote Streaks");
            lines.forEach(line -> leaderboard.appendTextLine(ChatColor.translateAlternateColorCodes('&', line)));
            leaderboard.appendTextLine(ChatColor.YELLOW + "/vote");
        });
    }
}
