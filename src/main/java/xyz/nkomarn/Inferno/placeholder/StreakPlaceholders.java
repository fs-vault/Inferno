package xyz.nkomarn.Inferno.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import xyz.nkomarn.Inferno.Inferno;
import xyz.nkomarn.Inferno.util.Config;
import xyz.nkomarn.Inferno.util.Streak;

import java.text.DecimalFormat;

public class StreakPlaceholders extends PlaceholderExpansion {
    private Inferno inferno;
    private DecimalFormat formatter = new DecimalFormat("#,###");

    public StreakPlaceholders(Inferno inferno){
        this.inferno = inferno;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return inferno.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "inferno";
    }

    @Override
    public String getVersion() {
        return inferno.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (!identifier.contains("leaderboard_")) return "h";
        return getFormattedEntry(Integer.parseInt(identifier.replace("leaderboard_", "")) - 1);
    }

    /**
     * Get formatted entry for the leaderboard
     * @param index Leaderboard place to get entry for
     * @return Formatted string for use int he leaderboard
     */
    private String getFormattedEntry(int index) {
        if (index > Inferno.LEADERBOARD.size() - 1) return "&a&lNobody";
        Streak streak = Inferno.LEADERBOARD.get(index);
        if (streak == null) return "&c&lError";
        return ChatColor.translateAlternateColorCodes('&', String.format(
                Config.getString("leaderboard"), (index + 1), streak.getUsername(), formatter.format(streak.getLevel())
        ));
    }
}
