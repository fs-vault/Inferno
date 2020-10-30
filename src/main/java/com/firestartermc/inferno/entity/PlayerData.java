package com.firestartermc.inferno.entity;

import org.bukkit.entity.Player;

public class PlayerData {

    private final Player player;
    private int votes;
    private int streak;
    private long lastVote;
    private int tokens;
    private boolean dirty;

    public PlayerData(Player player, int votes, int streak, long lastVote, int tokens) {
        this.player = player;
        this.votes = votes;
        this.streak = streak;
        this.lastVote = lastVote;
        this.tokens = tokens;
    }

    public Player getPlayer() {
        return player;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
        this.dirty = true;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int level) {
        this.streak = level;
        this.dirty = true;
    }

    public long getLastVote() {
        return lastVote;
    }

    public void setLastVote(long lastVote) {
        this.lastVote = lastVote;
        this.dirty = true;
    }

    public int getTokens() {
        return tokens;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
