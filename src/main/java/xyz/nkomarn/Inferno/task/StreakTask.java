package xyz.nkomarn.Inferno.task;

import org.bukkit.Bukkit;
import xyz.nkomarn.Inferno.Inferno;
import xyz.nkomarn.Inferno.util.Streak;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StreakTask implements Runnable {
    @Override
    public void run() {
        try {
            final Connection connection = Inferno.getHandler().open();
            if (connection == null) {
                Inferno.getInferno().getLogger().warning("Database error- can't update streaks.");
                return;
            }

            PreparedStatement statement = connection.prepareStatement("SELECT uuid, last_vote FROM inferno;");
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - result.getLong(2));
                if (days >= 2) {
                    statement = connection.prepareStatement("UPDATE inferno SET level=0, votes=0, last_vote=? WHERE uuid=?");
                    statement.setLong(2, System.currentTimeMillis());
                    statement.setString(1, result.getString(1));
                    statement.execute();
                }
            }

            Inferno.LEADERBOARD.clear();
            statement = connection.prepareStatement("SELECT uuid, level, votes FROM inferno ORDER BY level DESC LIMIT 5;");
            result = statement.executeQuery();
            while (result.next()) {
                Inferno.LEADERBOARD.add(new Streak(result.getInt(3), result.getInt(2),
                        Bukkit.getOfflinePlayer(UUID.fromString(result.getString(1))).getName()));
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
