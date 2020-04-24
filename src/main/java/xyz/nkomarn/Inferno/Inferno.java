package xyz.nkomarn.Inferno;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.nkomarn.Inferno.command.AddStreakCommand;
import xyz.nkomarn.Inferno.command.SendVoteCommand;
import xyz.nkomarn.Inferno.command.StreakCommand;
import xyz.nkomarn.Inferno.listener.PlayerJoinListener;
import xyz.nkomarn.Inferno.listener.VoteListener;
import xyz.nkomarn.Inferno.task.StreakTask;
import xyz.nkomarn.Kerosene.data.LocalStorage;

public class Inferno extends JavaPlugin {
    private static Inferno inferno;
    public static Hologram HOLOGRAM;

    public void onEnable() {
        inferno = this;
        saveDefaultConfig();
        loadStorage();

        getCommand("streak").setExecutor(new StreakCommand());
        getCommand("addstreak").setExecutor(new AddStreakCommand());
        getCommand("sendvote").setExecutor(new SendVoteCommand());
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new VoteListener(), this);

        HOLOGRAM = HologramsAPI.createHologram(this, new Location(
                getServer().getWorld(getConfig().getString("hologram.world")),
                getConfig().getInt("hologram.x") + 0.5,
                getConfig().getInt("hologram.y") + 2,
                getConfig().getInt("hologram.z")  + 0.5
        ));

        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this,
                new StreakTask(), 0L, 60 * 20);
    }

    public void onDisable() {
        HOLOGRAM.delete();
    }

    public static Inferno getInferno() {
        return inferno;
    }
    
    private void loadStorage() {
        final String query = "CREATE TABLE IF NOT EXISTS votes (uuid CHAR(36) PRIMARY KEY, last_vote INTEGER NOT " +
                "NULL DEFAULT '0', level UNSIGNED INTEGER(10) NOT NULL DEFAULT '0', votes INTEGER(10) NOT NULL DEFAULT '0');";

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
           try (Connection connection = LocalStorage.getConnection()) {
               try (PreparedStatement statement = connection.prepareStatement(query)) {
                   statement.execute();
               }
           } catch (SQLException e) {
               e.printStackTrace();
           }
        });
    }

    public static void createEntry(final Connection connection, final Player player) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT INTO votes (uuid) VALUES (?)")) {
            statement.setString(1, player.getUniqueId().toString());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void resetStreak(final Connection connection, final Player player) {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE votes SET votes = 0, " +
                "level = 0, last_vote=? WHERE uuid=?")) {
            statement.setLong(1, System.currentTimeMillis());
            statement.setString(2, player.getUniqueId().toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getStreakLevel(final Connection connection, final Player player) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT level FROM votes WHERE uuid=?")) {
            statement.setString(1, player.getUniqueId().toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Decide whether "day" or "days" is the grammatically
     * word for a given number of days.
     * @param number The number of days.
     * @return The correct string for the inputted amount of days.
     */
    public static String getDayString(final int number) {
        if (number == 1) return "day";
        else return "days";
    }
}
