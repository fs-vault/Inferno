package xyz.nkomarn.Inferno.listener;

import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.nkomarn.Inferno.Inferno;
import xyz.nkomarn.Inferno.util.Config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class VoteListener implements Listener {
    @EventHandler
    public void onVote(VotifierEvent event) {
        final String username = event.getVote().getUsername();
        new BukkitRunnable() {
            public void run() {
                try {
                    final Connection connection = Inferno.getHandler().open();
                    if (connection == null) {
                        Inferno.getInferno().getLogger().warning(String.format(
                                "Database error- can't add %s's vote to the database.", username
                        ));
                        return;
                    }

                    Player player = Bukkit.getServer().getPlayer(username);
                    if (player == null) return;

                    PreparedStatement statement = connection.prepareStatement("SELECT last_vote, level, votes " +
                            "FROM inferno WHERE uuid=?");
                    statement.setString(1, player.getUniqueId().toString());

                    final ResultSet result = statement.executeQuery();
                    if (result.next()) {
                        long lastVoted = result.getLong(1);
                        int level = result.getInt(2);
                        int votes = result.getInt(3);
                        result.close();
                        statement.close();

                        long days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastVoted);

                        if (days >= 2) {
                            votes = 0;
                            statement = connection.prepareStatement("UPDATE inferno SET last_vote=?, level='0', votes='0' WHERE uuid=?");
                            statement.setLong(1, System.currentTimeMillis());
                            statement.setString(2, player.getUniqueId().toString());
                            statement.executeUpdate();
                            statement.close();
                        }

                        // Amount to reward the player for voting
                        int money = 10 + (getLevel(player) * 5);
                        player.sendTitle(ChatColor.translateAlternateColorCodes('&', Config.getString("title.top")),
                                ChatColor.translateAlternateColorCodes('&', String.format(
                                        Config.getString("title.bottom"), money, (votes + 1)
                                )));
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);

                        if (votes + 1 == 5) {
                            statement = connection.prepareStatement("UPDATE inferno SET last_vote=?, level=?, votes='0' WHERE uuid=?");
                            statement.setLong(1, System.currentTimeMillis());
                            statement.setInt(2, level + 1);
                            statement.setString(3, player.getUniqueId().toString());
                            statement.executeUpdate();
                            statement.close();

                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                            Bukkit.getScheduler().runTask(Inferno.getInferno(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                    Config.getString("reward").replace("[player]", player.getName())));

                            // "Day" or "days"
                            String dayString;
                            int nextLevel = level + 1;
                            if (nextLevel == 1) dayString = "day";
                            else dayString = "days";

                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                    Config.getString("chat.message"), Config.getPrefix(), (level + 1), dayString
                            )));
                        } else {
                            statement = connection.prepareStatement("UPDATE inferno SET votes=? WHERE uuid=?");
                            statement.setInt(1, votes + 1);
                            statement.setString(2, player.getUniqueId().toString());
                            statement.executeUpdate();
                            statement.close();
                        }
                        Inferno.getEconomy().depositPlayer(player, money);
                    } else {
                        Inferno.getInferno().getLogger().info(String.format("Can't find %s in streaks database.", username));
                    }
                    connection.close();
                } catch (SQLException e) {
                    Inferno.getInferno().getLogger().warning("Error while connecting to database.");
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Inferno.getInferno());
    }

    /**
     * Returns a player's current streak level
     *
     * @param player Player object to check level for
     */
    private int getLevel(Player player) {
        try {
            Connection connection = Inferno.getHandler().open();
            PreparedStatement statement = connection.prepareStatement("SELECT level FROM inferno WHERE uuid=?");
            statement.setString(1, player.getUniqueId().toString());

            final ResultSet result = statement.executeQuery();
            if (result.next()) {
                final int level = result.getInt(1);
                result.close();
                connection.close();
                return level;
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
