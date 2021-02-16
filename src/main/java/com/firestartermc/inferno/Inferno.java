package com.firestartermc.inferno;

import com.firestartermc.inferno.command.InfernoCommand;
import com.firestartermc.inferno.command.StreakCommand;
import com.firestartermc.inferno.command.VoteCommand;
import com.firestartermc.inferno.command.VoteShopCommand;
import com.firestartermc.inferno.listener.VoteListener;
import com.firestartermc.inferno.util.Leaderboard;
import com.firestartermc.inferno.util.VoteShop;
import com.firestartermc.kerosene.util.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutionException;

public class Inferno extends JavaPlugin {

    private Leaderboard leaderboard;
    private VoteShop voteShop;
    private Cache cache;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        cache = new Cache(this);
        leaderboard = new Leaderboard(this);
        voteShop = new VoteShop(this);

        getCommand("vote").setExecutor(new VoteCommand(this));
        getCommand("inferno").setExecutor(new InfernoCommand(this));
        getCommand("streak").setExecutor(new StreakCommand(this));
        getCommand("voteshop").setExecutor(new VoteShopCommand(this));

        getServer().getPluginManager().registerEvents(cache, this);
        getServer().getPluginManager().registerEvents(new VoteListener(this), this);
        getServer().getOnlinePlayers().forEach(player -> cache.cachePlayer(player));
    }

    @Override
    public void onDisable() {
        getLeaderboard().removeHologram();
        for (var player : getServer().getOnlinePlayers()) {
            var data = cache.getData(player);
            if (data == null) {
                continue;
            }

            try {
                cache.update(data).get();
                cache.invalidate(data);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPrefix() {
        return MessageUtils.formatColors("&#fcc295\u2BEA &lVOTE: &f", true);
    }

    public Cache getCache() {
        return cache;
    }

    public Leaderboard getLeaderboard() {
        return leaderboard;
    }

    public VoteShop getVoteShop() {
        return voteShop;
    }
}
