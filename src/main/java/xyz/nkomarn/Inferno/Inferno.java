package xyz.nkomarn.Inferno;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.nkomarn.Inferno.command.StreakCommand;
import xyz.nkomarn.Inferno.listener.PlayerJoinListener;
import xyz.nkomarn.Inferno.listener.VoteListener;
import xyz.nkomarn.Inferno.placeholder.StreakPlaceholders;
import xyz.nkomarn.Inferno.task.StreakTask;
import xyz.nkomarn.Inferno.util.Streak;

public class Inferno extends JavaPlugin {
    private static Inferno inferno;
    private static SQLHandler handler;
    private static Economy economy = null;

    public static ArrayList<Streak> LEADERBOARD = new ArrayList<>();

    public void onEnable() {
        inferno = this;
        saveDefaultConfig();
        loadDatabase();
        getCommand("streak").setExecutor(new StreakCommand());
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new VoteListener(), this);

        if (!initializeEconomy()) {
            return;
        }

        // Register placeholders
        new StreakPlaceholders(this).register();
        Bukkit.getServer().getScheduler().runTaskTimerAsynchronously(this,
                new StreakTask(), 0L, 120 * 20);
    }
    
    public void onDisable() { }
    
    public static Inferno getInferno() {
        return inferno;
    }
    
    public static SQLHandler getHandler() {
        return handler;
    }

    public static Economy getEconomy() {
        return economy;
    }
    
    private void loadDatabase() {
        handler = new SQLHandler(getLogger(), "inferno", getDataFolder().getAbsolutePath());
        new BukkitRunnable() {
            public void run() {
                try {
                    final Connection connection = handler.open();
                    if (connection == null) {
                        Inferno.getInferno().getLogger().warning("Error while connecting to database.");
                        return;
                    }

                    PreparedStatement statement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS inferno " +
                            "(uuid CHAR(36) PRIMARY KEY, last_vote INTEGER NOT NULL DEFAULT '0', level UNSIGNED " +
                            "INTEGER(10) NOT NULL DEFAULT '0', votes INTEGER(10) NOT NULL DEFAULT '0');");
                    statement.executeUpdate();
                }
                catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    private boolean initializeEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Phase requires Vault to operate.");
            getServer().getPluginManager().disablePlugin(this);
        }
        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager()
                .getRegistration(Economy.class);
        if (provider == null) return false;
        economy = provider.getProvider();
        return true;
    }
}
