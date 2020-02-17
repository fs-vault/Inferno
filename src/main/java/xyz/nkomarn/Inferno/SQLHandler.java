package xyz.nkomarn.Inferno;

import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.io.File;
import java.util.logging.Logger;

public class SQLHandler {
    private Logger logger;
    private File database;

    public SQLHandler(Logger logger, final String name, final String location) {
        this.logger = logger;
        final File folder = new File(location);
        if (!folder.exists()) folder.mkdir();
        this.database = new File(String.format("%s%s%s.db", folder.getAbsolutePath(), File.separator, name));
    }

    private boolean initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            return true;
        } catch (ClassNotFoundException e) {
            logger.warning("Cannot open SQLite connection. The driver class is missing.");
            return false;
        }
    }

    public Connection open() throws SQLException {
        if (this.initialize()) {
            final Connection connection = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
            return connection;
        }
        throw new SQLException("Cannot open SQLite connection. The driver class is missing.");
    }
}
