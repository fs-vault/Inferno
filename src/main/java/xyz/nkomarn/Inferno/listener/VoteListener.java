package xyz.nkomarn.Inferno.listener;

import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import xyz.nkomarn.Inferno.Inferno;
import xyz.nkomarn.Inferno.util.Config;
import xyz.nkomarn.kerosene.util.Economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class VoteListener implements Listener {
    @EventHandler
    public void onVote(VotifierEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(Inferno.getInferno(), () -> {
            try (Connection connection = Inferno.STORAGE.getConnection()) {
                Inferno.getInferno().getLogger().info(String.format("Received a vote from %s.", event.getVote().getUsername()));
                final Player player = Bukkit.getServer().getPlayer(event.getVote().getUsername());
                if (player == null) return;

                try (PreparedStatement statement = connection.prepareStatement("SELECT last_vote, level, votes " +
                        "FROM votes WHERE uuid=?")) {
                    statement.setString(1, player.getUniqueId().toString());
                    try (ResultSet result = statement.executeQuery()) {
                        if (result.next()) {
                            final long lastVoteTime = result.getLong(1);
                            final int streakLevel = result.getInt(2);
                            final int totalVotes = result.getInt(3);

                            long daysSinceLastVote = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() -
                                    lastVoteTime);
                            if (daysSinceLastVote >= 2) Inferno.resetStreak(connection, player);

                            final long monetaryReward = (streakLevel * 5) + 10;
                            Economy.deposit(player, monetaryReward);
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                    "%sYou've received &d$%s &7for voting (&d%s/5&7 daily votes- streak level &d%s&7).",
                                    Config.getPrefix(), monetaryReward, (totalVotes + 1), streakLevel
                            )));
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_COW_BELL, 1.0f, 1.0f);

                            if (totalVotes + 1 == 3) {
                                try (PreparedStatement statement1 = connection.prepareStatement("UPDATE votes SET " +
                                        "last_vote=?, votes=? WHERE uuid=?")) {
                                    statement1.setLong(1, System.currentTimeMillis());
                                    statement1.setInt(2, totalVotes + 1);
                                    statement1.setString(3, player.getUniqueId().toString());
                                    statement1.executeUpdate();

                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                            "%sYou've received a crate key- vote &d2 &7more times to level up your streak.",
                                            Config.getPrefix()
                                    )));
                                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                                    Bukkit.getScheduler().runTask(Inferno.getInferno(), () -> Bukkit.dispatchCommand(
                                            Bukkit.getConsoleSender(), Config.getString("reward")
                                                    .replace("[player]", player.getName())
                                    ));
                                }
                            } else if (totalVotes + 1 == 5) {
                                try (PreparedStatement statement1 = connection.prepareStatement("UPDATE votes SET " +
                                        "level=?, votes = 0 WHERE uuid=?")) {
                                    statement1.setInt(1, streakLevel + 1);
                                    statement1.setString(2, player.getUniqueId().toString());
                                    statement1.executeUpdate();

                                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                            "%sYou've maintained your streak, which is now &d%s&7 %s long- great work!",
                                            Config.getPrefix(), (streakLevel + 1), Inferno.getDayString(streakLevel + 1)
                                    )));
                                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                                }
                            } else {
                                try (PreparedStatement statement1 = connection.prepareStatement("UPDATE votes SET votes=? WHERE uuid=?")) {
                                    statement1.setInt(1, totalVotes + 1);
                                    statement1.setString(2, player.getUniqueId().toString());
                                    statement1.executeUpdate();
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
