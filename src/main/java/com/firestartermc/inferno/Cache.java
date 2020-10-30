package com.firestartermc.inferno;

import com.firestartermc.inferno.entity.PlayerData;
import com.firestartermc.kerosene.Kerosene;
import com.firestartermc.kerosene.data.db.LocalStorage;
import com.firestartermc.kerosene.util.ConcurrentUtils;
import com.firestartermc.kerosene.util.Constants;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;

public class Cache extends BukkitRunnable implements Listener {

    private final Inferno inferno;
    private final LocalStorage storage;
    private final Map<Player, PlayerData> cache;

    public Cache(Inferno inferno) {
        this.inferno = inferno;
        this.storage = Kerosene.getKerosene().getLocalStorage("inferno");
        this.cache = new ConcurrentHashMap<>();

        runTaskTimer(inferno, 0L, 20L * 300L);
        createTable();
    }

    @Nullable
    public PlayerData getData(Player player) {
        return cache.get(player);
    }

    @NotNull
    public LocalStorage getStorage() {
        return storage;
    }

    private void createTable() {
        var query = "CREATE TABLE IF NOT EXISTS players (" +
                "uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                "votes UNSIGNED INTEGER NOT NULL DEFAULT 0," +
                "streak UNSIGNED INTEGER NOT NULL DEFAULT 0," +
                "last_vote INTEGER NOT NULL DEFAULT 0," +
                "tokens UNSIGNED INTEGER NOT NULL DEFAULT 0" +
                ");";

        ConcurrentUtils.callAsync(() -> {
            var connection = storage.getConnection();
            var statement = connection.prepareStatement(query);

            try (connection; statement) {
                statement.executeUpdate();
            }
            return null;
        });
    }

    public CompletableFuture<PlayerData> cachePlayer(Player player) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var connection = storage.getConnection();
                var statement = connection.prepareStatement("SELECT votes, streak, last_vote, tokens FROM players WHERE uuid = ?;");
                statement.setString(1, player.getUniqueId().toString());
                var result = statement.executeQuery();

                try (connection; statement; result) {
                    if (!result.next()) {
                        var statement1 = connection.prepareStatement("INSERT OR IGNORE INTO players (uuid) VALUES (?);");
                        statement1.setString(1, player.getUniqueId().toString());
                        statement1.executeUpdate();
                        var data = new PlayerData(player, 0, 0, System.nanoTime(), 0);
                        cache.put(player, data);
                        return data;
                    }

                    var data = new PlayerData(player, result.getInt(1), result.getInt(2), result.getTimestamp(3).getTime(), result.getInt(4));
                    cache.put(player, data);
                    return data;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<Void> update(PlayerData data) {
        if (!data.isDirty()) {
            return CompletableFuture.completedFuture(null);
        }

        return ConcurrentUtils.callAsync(() -> {
            try {
                var connection = storage.getConnection();
                var statement = connection.prepareStatement("REPLACE INTO players (uuid, votes, streak, last_vote, tokens) VALUES (?, ?, ?, ?, ?);");

                try (connection; statement) {
                    statement.setString(1, data.getPlayer().getUniqueId().toString());
                    statement.setInt(2, data.getVotes());
                    statement.setInt(3, data.getStreak());
                    statement.setLong(4, data.getLastVote());
                    statement.setInt(5, data.getTokens());
                    statement.executeUpdate();
                    data.setDirty(false);
                    return null;
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public void invalidate(PlayerData data) {
        cache.remove(data.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        cachePlayer(event.getPlayer()).exceptionally(throwable -> {
            event.getPlayer().sendMessage(Constants.FAILED_TO_LOAD_DATA);
            return null;
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        var data = cache.get(player);
        if (data != null) {
            update(data)
                    .thenAccept(a -> cache.remove(player))
                    .exceptionally(throwable -> {
                        throwable.printStackTrace();
                        return null;
                    });
        }
    }

    @Override
    public void run() {
        cache.values().forEach(this::update);
    }
}
