package com.firestartermc.inferno.command;

import com.firestartermc.inferno.Inferno;
import com.firestartermc.kerosene.util.MessageUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class VoteCommand implements CommandExecutor {

    private final String message;

    public VoteCommand(Inferno inferno) {
        this.message = MessageUtils.formatColors("&r \n" +
                "&#f779ed&l&nVote for us!\n" +
                "&7You can vote up to 5 times per day. Each vote gives you money, vote tokens, and levels your streak. Type &#f779ed/rewards&7.\n" +
                "&#fcdcfaVote here: &#f779ed" + inferno.getConfig().getString("link") + "\n" +
                "&r ", true);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        sender.sendMessage(message);
        return true;
    }
}
