package com.firestartermc.inferno.command;

import com.firestartermc.inferno.Inferno;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SendVoteCommand implements CommandExecutor {

    private final Inferno inferno;

    public SendVoteCommand(@NotNull Inferno inferno) {
        this.inferno = inferno;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(inferno.getPrefix() + "Send a test vote using /sendvote <player>.");
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player == null) {
            sender.sendMessage(inferno.getPrefix() + args[0] + " is offline.");
            return true;
        }


        Bukkit.getServer().getPluginManager().callEvent(new VotifierEvent(new Vote(
                "Test",
                args[0],
                "127.0.0.1",
                ""
        )));
        sender.sendMessage(inferno.getPrefix() + "The test vote was sent!");
        return true;
    }
}
