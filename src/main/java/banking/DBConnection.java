package banking.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager.
 *
 * Now loads credentials from `config/db.properties` if present.
 * Create `config/db.properties` with keys: `url`, `user`, `password`.
 */
public class DBConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/banking_system?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";

    private static final String CONFIG_PATH = "config/db.properties";

    private static final Properties CONFIG = new Properties();

    static {
        Path p = Path.of(CONFIG_PATH);
        if (Files.exists(p)) {
            try (FileInputStream in = new FileInputStream(p.toFile())) {
                CONFIG.load(in);
            } catch (IOException ignored) {}
        }
    }

    // H2 fallback settings
    private static final String H2_JAR = "lib/h2.jar";
    private static final String H2_URL = "jdbc:h2:./data/banking_system;MODE=MySQL;DATABASE_TO_UPPER=false";

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = CONFIG.getProperty("url", DEFAULT_URL);
            String user = CONFIG.getProperty("user", DEFAULT_USER);
            String password = CONFIG.getProperty("password", DEFAULT_PASSWORD);

            try {
                return DriverManager.getConnection(url, user, password);
            } catch (SQLException e) {
                // If MySQL auth fails and H2 jar exists, fall back to embedded H2 DB.
                if (Files.exists(Path.of(H2_JAR))) {
                    System.err.println("Warning: MySQL connection failed, using embedded H2 database as fallback.");
                    return DriverManager.getConnection(H2_URL, "sa", "");
                }
                throw e;
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC Driver not found!\nPlace mysql-connector-j-*.jar in the lib/ folder.", e);
        }
    }

    public static void close() {
        // no-op: callers should close connections they obtain
    }
}
