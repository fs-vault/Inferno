package xyz.nkomarn.Inferno.util;

/**
 * Represents a voting streak
 */
public class Streak {
    private int votes, level;
    private String username;

    public Streak(int votes, int level, String username) {
        this.votes = votes;
        this.level = level;
        this.username = username;
    }

    public int getVotes() {
        return this.votes;
    }

    public int getLevel() {
        return this.level;
    }

    public String getUsername() {
        return this.username;
    }
}
