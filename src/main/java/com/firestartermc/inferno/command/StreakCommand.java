package com.firestartermc.inferno.command;

import com.firestartermc.inferno.Inferno;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.jetbrains.annotations.NotNull;

public class StreakCommand implements CommandExecutor {

    private final Inferno inferno;

    public StreakCommand(@NotNull Inferno inferno) {
        this.inferno = inferno;
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        int streak = inferno.getStreaks().getStreak(player.getUniqueId());
        player.sendMessage(inferno.getPrefix() + "Your vote streak is " + ChatColor.LIGHT_PURPLE + streak + " " +
                Inferno.getDayString(streak) + ChatColor.GRAY + " long.");

        return true;
    }
}
