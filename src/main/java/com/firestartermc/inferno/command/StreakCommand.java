package com.firestartermc.inferno.command;

import com.firestartermc.inferno.Inferno;
import com.firestartermc.kerosene.util.Constants;
import com.firestartermc.kerosene.util.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StreakCommand implements CommandExecutor {

    private final Inferno inferno;

    public StreakCommand(@NotNull Inferno inferno) {
        this.inferno = inferno;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        var player = (Player) sender;
        var data = inferno.getCache().getData(player);
        if (data == null) {
            player.sendMessage(Constants.FAILED_TO_LOAD_DATA);
            return true;
        }

        player.sendMessage(inferno.getPrefix() + MessageUtils.formatColors("Your vote streak is " + data.getStreak() + " " + getDayString(data.getStreak()) + " long.", true));
        return true;
    }

    public String getDayString(int level) {
        return level == 1 ? "day" : "days";
    }
}
