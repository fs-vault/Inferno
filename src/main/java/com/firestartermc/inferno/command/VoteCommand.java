package com.firestartermc.inferno.command;

import com.firestartermc.inferno.Inferno;
import com.firestartermc.kerosene.util.MessageUtils;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VoteCommand implements CommandExecutor {

    private final String message;

    public VoteCommand(Inferno inferno) {
        this.message = MessageUtils.formatColors("&r \n" +
                "&r &#fcc295\u2BEA &lVOTE FOR US!\n&r \n" +
                "&r &fYou can vote up to 5 times per day. Each vote gives you money, vote tokens, and levels your streak! View &#ffdac7/rewards&7&f.\n" +
                "&r &#fcc295Vote here: &#ffdac7" + inferno.getConfig().getString("link") + "\n" +
                "&r ", true);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        sender.sendMessage(message);

        if (sender instanceof Player) {
            var player = (Player) sender;
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.1f);
        }
        return true;
    }
}
