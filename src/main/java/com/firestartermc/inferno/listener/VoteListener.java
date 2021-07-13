package com.firestartermc.inferno.listener;

import com.firestartermc.inferno.Inferno;
import com.firestartermc.kerosene.Kerosene;
import com.firestartermc.kerosene.economy.EconomyWrapper;
import com.firestartermc.kerosene.item.BookBuilder;
import com.firestartermc.kerosene.item.ItemBuilder;
import com.firestartermc.kerosene.util.ConcurrentUtils;
import com.firestartermc.kerosene.util.Constants;
import com.firestartermc.kerosene.util.MessageUtils;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class VoteListener implements Listener {

    private final Inferno inferno;
    private final EconomyWrapper economy;

    public VoteListener(@NotNull Inferno inferno) {
        this.inferno = inferno;
        this.economy = Kerosene.getKerosene().getEconomy();
    }

    @EventHandler
    public void onVote(VotifierEvent event) {
        var player = inferno.getServer().getPlayer(event.getVote().getUsername());
        if (player == null) {
            return;
        }

        var data = inferno.getCache().getData(player);
        if (data == null) {
            player.sendMessage(Constants.FAILED_TO_LOAD_DATA);
            return;
        }

        data.setLastVote(System.currentTimeMillis());
        var daysSinceLastVote = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - data.getLastVote());
        if (daysSinceLastVote >= 2) {
            data.setStreak(0);
        }

        var money = (data.getStreak() * 3) + 10;
        economy.depositPlayer(player, money);
        data.setVotes(data.getVotes() + 1);
        data.setTokens(data.getTokens() + 1);

        player.playSound(player.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_ON, 1.0f, 1.0f);
        player.sendMessage(inferno.getPrefix() + MessageUtils.formatColors("Received &#ffdac7$" + money + " &fand &#ffdac71 token &7(" + data.getVotes() + "/5 daily votes)", true));

        if (data.getVotes() == 5) {
            data.setVotes(0);
            data.setStreak(data.getStreak() + 1);
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.4f);
            player.sendMessage(inferno.getPrefix() + MessageUtils.formatColors("Maintained your streak, which is now "
                    + data.getStreak() + " " + getDayString(data.getStreak()) + " long. Check out &#ffdac7/rewards&f "
                    + "to redeem your vote tokens!", true));

            displayThankYou(player);
        }
    }

    private String getDayString(int level) {
        return level == 1 ? "day" : "days";
    }

    private void displayThankYou(@NotNull Player player) {
        var item = ItemBuilder.of(Material.WRITTEN_BOOK).build();
        var meta = (BookMeta) item.getItemMeta();
        var page = inferno.getConfig().getStringList("thank-you-book").stream()
                .map(line -> MessageUtils.formatColors(line, true))
                .toArray(String[]::new);

        meta.addPage(page);
        meta.setTitle("thx for voting uwu");
        meta.setAuthor("Firestarter Staff");
        item.setItemMeta(meta);
        player.openBook(item);
    }

    private void giveReward(@NotNull String playerName) {
        var command = inferno.getConfig().getString("reward", "").replace("[player]", playerName);
        Bukkit.getScheduler().runTask(inferno, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }
}
