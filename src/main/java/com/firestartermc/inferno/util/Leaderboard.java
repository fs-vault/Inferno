package com.firestartermc.inferno.util;

import com.firestartermc.inferno.Inferno;
import com.firestartermc.kerosene.util.MessageUtils;
import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Leaderboard extends BukkitRunnable {

    private final Inferno inferno;
    private final Hologram hologram;
    private final ItemStack itemStack;

    public Leaderboard(@NotNull Inferno inferno) {
        this.inferno = inferno;
        this.itemStack = new ItemStack(Material.PAPER, 1);

        var config = inferno.getConfig();
        this.hologram = HologramsAPI.createHologram(inferno, new Location(
                inferno.getServer().getWorld(config.getString("hologram.world")),
                config.getInt("hologram.x") + 0.5,
                config.getInt("hologram.y") + 2,
                config.getInt("hologram.z") + 0.5
        ));

        runTaskTimerAsynchronously(inferno, 0L, 240 * 20L);
    }

    public void removeHologram() {
        hologram.delete();
    }

    @Override
    public void run() {
        try {
            var connection = inferno.getCache().getStorage().getConnection();
            var statement = connection.prepareStatement(("UPDATE players SET streak = 0 WHERE ? - last_vote >= 172800000;"));
            statement.setLong(1, System.currentTimeMillis());
            statement.executeUpdate();

            var index = new AtomicInteger();
            ArrayList<String> leaderboard = new ArrayList<>();
            var statement1 = connection.prepareStatement("SELECT uuid, streak, votes FROM players ORDER BY streak DESC LIMIT 5;");
            var result = statement1.executeQuery();

            try (connection; result) {
                while (result.next()) {
                    leaderboard.add(ChatColor.translateAlternateColorCodes('&', String.format(
                            inferno.getConfig().getString("hologram.entry", ""),
                            index.incrementAndGet(),
                            Bukkit.getOfflinePlayer(UUID.fromString(result.getString(1))).getName(),
                            result.getInt(2)
                    )));
                }
            }

            Bukkit.getScheduler().runTask(inferno, () -> {
                hologram.clearLines();
                hologram.appendItemLine(itemStack);
                hologram.appendTextLine(MessageUtils.formatColors("&d&lTOP VOTE STREAKS", true));
                leaderboard.forEach(line -> hologram.appendTextLine(ChatColor.translateAlternateColorCodes('&', line)));
                hologram.appendTextLine(ChatColor.YELLOW + "/vote");
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
