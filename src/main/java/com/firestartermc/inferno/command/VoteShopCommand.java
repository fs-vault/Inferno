package com.firestartermc.inferno.command;

import com.firestartermc.inferno.Inferno;
import com.firestartermc.inferno.menu.VoteShopMenu;
import com.firestartermc.kerosene.util.Constants;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class VoteShopCommand implements TabExecutor {

    private final Inferno inferno;

    public VoteShopCommand(Inferno inferno) {
        this.inferno = inferno;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Constants.NON_PLAYER);
            return true;
        }

        new VoteShopMenu(inferno, (Player) sender);
        return true;
    }

    @Override
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
