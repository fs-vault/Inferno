package com.firestartermc.inferno.util;

import com.firestartermc.inferno.Inferno;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.NotNull;
import xyz.nkomarn.kerosene.Kerosene;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Streaks {

    private final Inferno inferno;
    private final Object2IntMap<UUID> streaks;

    public Streaks(@NotNull Inferno inferno) {
        this.inferno = inferno;
        this.streaks = new Object2IntOpenHashMap<>();
    }

    public void cache() {
        Kerosene.getPool().submit(() -> {
            try {
                Connection connection = inferno.getStorage().getConnection();
                PreparedStatement statement = connection.prepareStatement("SELECT uuid, level FROM votes;");
                ResultSet result = statement.executeQuery();

                try (connection; statement; result) {
                    while (result.next()) {
                        streaks.put(UUID.fromString(result.getString(1)), result.getInt(2));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public int getStreak(@NotNull UUID uuid) {
        return streaks.getInt(uuid);
    }

    public void setStreak(@NotNull UUID uuid, int streak) {
        streaks.put(uuid, streak);

        Kerosene.getPool().submit(() -> {
            try {
                Connection connection = inferno.getStorage().getConnection();
                PreparedStatement statement = connection.prepareStatement("UPDATE votes SET level = ? WHERE uuid = ?;");
                statement.setInt(1, streak);
                statement.setString(2, uuid.toString());

                try (connection; statement) {
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}
