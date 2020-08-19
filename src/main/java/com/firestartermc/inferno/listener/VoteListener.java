package com.firestartermc.inferno.listener;

import com.firestartermc.inferno.Inferno;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import xyz.nkomarn.kerosene.Kerosene;
import xyz.nkomarn.kerosene.util.Economy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VoteListener implements Listener {

    private final Inferno inferno;

    public VoteListener(@NotNull Inferno inferno) {
        this.inferno = inferno;
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        Player player = Bukkit.getServer().getPlayer(event.getVote().getUsername());

        if (player == null) {
            return;
        }

        Kerosene.getPool().submit(() -> {
            try {
                Connection connection = inferno.getStorage().getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT last_vote, level, votes FROM votes WHERE uuid = ?;");
                statement.setString(1, player.getUniqueId().toString());
                ResultSet result = statement.executeQuery();

                try (connection; statement; result) {
                    long lastVote = 0;
                    int streak = 0;
                    int totalVotes = 0;

                    if (result.next()) {
                        lastVote = result.getLong(1);
                        streak = result.getInt(2);
                        totalVotes = result.getInt(3);
                    }

                    long daysSinceLastVote = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastVote);
                    if (daysSinceLastVote >= 2) {
                        inferno.getStreaks().setStreak(player.getUniqueId(), 0);
                    }

                    long money = (streak * 3) + 10;
                    Economy.deposit(player, money);
                    player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0f, 1.0f);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                            "%sReceived &d$%s &7(&d%s/5&7 daily votes- streak level &d%s&7).",
                            inferno.getPrefix(), money, (totalVotes + 1), streak
                    )));

                    if (totalVotes + 1 == 3) {
                        setVotes(connection, player.getUniqueId(), totalVotes + 1, System.currentTimeMillis());

                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                "%s&fReceived a crate key. Vote &d2 times &fto boost streak.",
                                inferno.getPrefix()
                        )));
                        Bukkit.getScheduler().runTask(inferno, () -> Bukkit.dispatchCommand(
                                Bukkit.getConsoleSender(),
                                inferno.getConfig().getString("reward", "").replace("[player]", player.getName())
                        ));
                    } else if (totalVotes + 1 == 5) {
                        inferno.getStreaks().setStreak(player.getUniqueId(), streak + 1);
                        setVotes(connection, player.getUniqueId(), 0, System.currentTimeMillis());

                        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                "%s&fMaintained your streak, which is now &d%s&f %s long!",
                                inferno.getPrefix(), (streak + 1), Inferno.getDayString(streak + 1)
                        )));
                    } else {
                        setVotes(connection, player.getUniqueId(), totalVotes + 1, System.currentTimeMillis());
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void setVotes(@NotNull Connection connection, @NotNull UUID uuid, int votes, long lastVote) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("INSERT INTO votes (uuid, last_vote, votes) VALUES " +
                "(?, ?, ?) ON CONFLICT(uuid) DO UPDATE SET last_vote = ?, votes = ? WHERE uuid = ?;");
        statement.setString(1, uuid.toString());
        statement.setLong(2, lastVote);
        statement.setInt(3, votes);
        statement.setLong(4, lastVote);
        statement.setInt(5, votes);
        statement.setString(6, uuid.toString());
        statement.executeUpdate();
    }
}
