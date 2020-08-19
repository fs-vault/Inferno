package com.firestartermc.inferno;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import com.firestartermc.inferno.task.StreakTask;
import com.firestartermc.inferno.util.Streaks;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import com.firestartermc.inferno.command.AddStreakCommand;
import com.firestartermc.inferno.command.SendVoteCommand;
import com.firestartermc.inferno.command.StreakCommand;
import com.firestartermc.inferno.listener.VoteListener;
import xyz.nkomarn.kerosene.Kerosene;
import xyz.nkomarn.kerosene.data.db.LocalStorage;

public class Inferno extends JavaPlugin {

    private LocalStorage storage;
    private Streaks streaks;
    private Hologram hologram;

    public void onEnable() {
        saveDefaultConfig();

        storage = new LocalStorage("inferno");
        loadStorage();

        streaks = new Streaks(this);
        streaks.cache();

        getCommand("streak").setExecutor(new StreakCommand(this));
        getCommand("addstreak").setExecutor(new AddStreakCommand(this));
        getCommand("sendvote").setExecutor(new SendVoteCommand(this));

        hologram = HologramsAPI.createHologram(this, new Location(
                getServer().getWorld(getConfig().getString("hologram.world")),
                getConfig().getInt("hologram.x") + 0.5,
                getConfig().getInt("hologram.y") + 2,
                getConfig().getInt("hologram.z")  + 0.5
        ));

        Bukkit.getPluginManager().registerEvents(new VoteListener(this), this);
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this, new StreakTask(this), 0L, 60 * 20);
    }

    public void onDisable() {
        hologram.delete();
    }

    @NotNull
    public String getPrefix() {
        return ChatColor.LIGHT_PURPLE + ChatColor.BOLD.toString() + "Votes: " + ChatColor.GRAY;
    }

    @NotNull
    public LocalStorage getStorage() {
        return storage;
    }

    @NotNull
    public Streaks getStreaks() {
        return streaks;
    }

    @NotNull
    public Hologram getLeaderboard() {
        return hologram;
    }

    private void loadStorage() {
        String query = "CREATE TABLE IF NOT EXISTS votes (uuid CHAR(36) PRIMARY KEY, last_vote INTEGER NOT NULL " +
                "DEFAULT '0', level UNSIGNED INTEGER(10) NOT NULL DEFAULT '0', votes INTEGER(10) NOT NULL DEFAULT '0');";

        Kerosene.getPool().submit(() -> {
            try {
                Connection connection = storage.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);

                try (connection; statement) {
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Decides whether "day" or "days" is the grammatically word for a given number of days.
     * @param number The number of days.
     * @return The correct string for the inputted amount of days.
     */
    public static String getDayString(final int number) {
        return number == 1 ? "day" : "days";
    }
}
