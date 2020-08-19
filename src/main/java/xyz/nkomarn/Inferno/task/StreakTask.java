package xyz.nkomarn.Inferno.task;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.nkomarn.Inferno.Inferno;
import xyz.nkomarn.Inferno.util.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class StreakTask implements Runnable {

    private static final ItemStack FLOATING_ITEM = new ItemStack(Material.PAPER, 1);

    @Override
    public void run() {
        try {
            Connection connection = Inferno.getStorage().getConnection();
            PreparedStatement statement = connection.prepareStatement("DELETE FROM votes WHERE ? - last_vote >= 172800000;");
            statement.setLong(1, System.currentTimeMillis());
            statement.execute();

            ArrayList<String> leaderboard = new ArrayList<>();
            AtomicInteger index = new AtomicInteger();
            PreparedStatement statement1 = connection.prepareStatement("SELECT uuid, level, votes FROM votes ORDER BY level DESC LIMIT 5;");
            ResultSet result = statement1.executeQuery();

            try (connection; statement1; result) {
                while (result.next()) {
                    leaderboard.add(ChatColor.translateAlternateColorCodes('&', String.format(
                            Config.getString("hologram.entry"), index.incrementAndGet(),
                            Bukkit.getOfflinePlayer(UUID.fromString(result.getString(1))).getName(), result.getInt(2)
                    )));
                }
            }

            updateLeaderboard(leaderboard);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateLeaderboard(@NotNull ArrayList<String> lines) {
        Bukkit.getScheduler().runTask(Inferno.getInferno(), () -> {
            Hologram leaderboard = Inferno.getLeaderboard();
            leaderboard.clearLines();
            leaderboard.appendItemLine(FLOATING_ITEM);
            leaderboard.appendTextLine(ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Top Vote Streaks");
            lines.forEach(line -> Inferno.getLeaderboard().appendTextLine(ChatColor.translateAlternateColorCodes('&', line)));
            leaderboard.appendTextLine(ChatColor.YELLOW + "/vote");
        });
    }
}
