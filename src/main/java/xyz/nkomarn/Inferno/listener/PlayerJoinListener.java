package xyz.nkomarn.Inferno.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.nkomarn.Inferno.Inferno;
import xyz.nkomarn.Kerosene.data.LocalStorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(Inferno.getInferno(), () -> {
            final Player player = event.getPlayer();

            try (Connection connection = LocalStorage.getConnection()) {
                try (PreparedStatement statement = connection.prepareStatement("SELECT last_vote, level FROM votes WHERE uuid=?")) {
                    statement.setString(1, player.getUniqueId().toString());
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            final long daysSinceLastVote = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() -
                                    result.getLong(1));
                            if (daysSinceLastVote >= 2) Inferno.resetStreak(connection, player);
                        } else Inferno.createEntry(connection, player);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
