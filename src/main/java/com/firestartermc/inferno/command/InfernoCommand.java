package com.firestartermc.inferno.command;

import com.firestartermc.inferno.Inferno;
import com.google.common.collect.ImmutableList;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class InfernoCommand implements TabExecutor {

    private final Inferno inferno;

    public InfernoCommand(@NotNull Inferno inferno) {
        this.inferno = inferno;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length < 1) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "addstreak" -> {
                return handleAddStreak(sender, args);
            }
            case "addtokens" -> {
                return handleAddTokens(sender, args);
            }
            case "sendvote" -> {
                return sendVote(sender, args);
            }
        }

        return true;
    }

    public boolean handleAddStreak(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            return false;
        }
        // TODO online players only

        /*var uuid = Bukkit.getOfflinePlayer(args[1]).getUniqueId();
        int levels;
        if (!NumberUtils.isNumber(args[2])) {
            return false;
        } else {
            levels = Integer.parseInt(args[2]);
        }

        var data = inferno.getCache().getData(player);

        inferno.getStreaks().getStreak(uuid)
                .thenApply(level -> inferno.getStreaks().setStreak(uuid, level + levels))
                .thenAccept(a -> sender.sendMessage(ChatColor.GREEN + "Added " + levels + " streak levels."))
                .exceptionally(throwable -> {
                    sender.sendMessage(ChatColor.RED + "Failed to update streak. Check console.");
                    throwable.printStackTrace();
                    return null;
                });*/
        return true;
    }

    public boolean handleAddTokens(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 3) {
            return false;
        }

        var player = Bukkit.getPlayer(args[1]);
        if (player == null) {
            return false;
        }

        var data = inferno.getCache().getData(player);
        if (data == null) {
            return false;
        }

        int tokens;
        if (!NumberUtils.isNumber(args[2])) {
            return false;
        } else {
            tokens = Integer.parseInt(args[2]);
        }

        data.setTokens(data.getTokens() + tokens);
        sender.sendMessage(ChatColor.GREEN + "Added " + tokens + " tokens.");
        return true;
    }

    public boolean sendVote(@NotNull CommandSender sender, @NotNull String[] args) {
        var vote = new Vote("Test", args[1], "127.0.0.1", "");
        Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(vote));
        sender.sendMessage(ChatColor.GREEN + "The test vote was sent.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 1) {
            return ImmutableList.of("addstreak", "addtokens", "sendvote");
        }

        return Collections.emptyList();
    }
}
