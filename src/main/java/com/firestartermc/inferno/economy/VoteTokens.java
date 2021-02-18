package com.firestartermc.inferno.economy;

import com.firestartermc.inferno.Inferno;
import com.firestartermc.inferno.entity.PlayerData;
import net.brcdev.shopgui.provider.economy.EconomyProvider;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VoteTokens extends EconomyProvider {

    private final Inferno inferno;

    public VoteTokens(@NotNull Inferno inferno) {
        this.inferno = inferno;
        this.currencySuffix = " tokens";
    }

    @Override
    public String getName() {
        return "VoteToken";
    }

    @Override
    public double getBalance(Player player) {
        return Optional.ofNullable(inferno.getCache().getData(player))
                .map(PlayerData::getTokens)
                .orElse(0);
    }

    @Override
    public void deposit(Player player, double v) {
        var data = inferno.getCache().getData(player);

        if (data != null) {
            data.setTokens(data.getTokens() + (int) v);
        }
    }

    @Override
    public void withdraw(Player player, double v) {
        var data = inferno.getCache().getData(player);

        if (data != null) {
            data.setTokens(data.getTokens() - (int) v);
        }
    }
}
