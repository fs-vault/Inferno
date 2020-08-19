package com.firestartermc.inferno.command;

import com.firestartermc.inferno.Inferno;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import xyz.nkomarn.kerosene.Kerosene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class AddStreakCommand implements CommandExecutor {

    private final Inferno inferno;

    public AddStreakCommand(@NotNull Inferno inferno) {
        this.inferno = inferno;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(inferno.getPrefix() + "Add streak levels to a player using /addstreak <player> <levels>.");
            return true;
        }

        if (!args[1].matches(".*\\d.*")) {
            sender.sendMessage(inferno.getPrefix() + "Levels must be a number.");
            return true;
        }

        int levels = Integer.parseInt(args[1]);
        UUID uuid = Bukkit.getOfflinePlayer(args[0]).getUniqueId();

        inferno.getStreaks().setStreak(uuid, inferno.getStreaks().getStreak(uuid));
        sender.sendMessage(inferno.getPrefix() + "Added " + ChatColor.LIGHT_PURPLE + levels + ChatColor.GRAY + " streak levels!");
        return true;
    }
}
