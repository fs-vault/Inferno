package xyz.nkomarn.Inferno.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.nkomarn.Inferno.Inferno;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                try {
                    final Connection connection = Inferno.getInferno().getHandler().open();
                    if (connection == null) {
                        //Inferno.getInferno().sendMsg((CommandSender)pPlayer, "error.DATABASE-ERROR");
                        return;
                    }

                    PreparedStatement statement = connection.prepareStatement("SELECT last_vote, level FROM inferno WHERE uuid=?");
                    statement.setString(1, player.getUniqueId().toString());

                    final ResultSet result = statement.executeQuery();
                    if (result.next()) {
                        long lastVoted = result.getLong(1);
                        result.close();
                        statement.close();

                        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastVoted);
                        if (days >= 2) {
                            statement = connection.prepareStatement("UPDATE inferno SET votes=0, level=0, last_vote=? WHERE uuid=?");
                            statement.setLong(1, System.currentTimeMillis());
                            statement.setString(2, player.getUniqueId().toString());
                            statement.executeUpdate();
                            statement.close();
                        }
                    } else {
                        result.close();
                        statement.close();
                        statement = connection.prepareStatement("INSERT INTO inferno (uuid) VALUES (?)");
                        statement.setString(1, player.getUniqueId().toString());
                        statement.executeUpdate();
                        statement.close();
                    }
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //Inferno.getPlugin().sendMsg((CommandSender)pPlayer, "error.DATABASE-ERROR");
                }
            }
        }.runTaskAsynchronously(Inferno.getInferno());
    }
}
