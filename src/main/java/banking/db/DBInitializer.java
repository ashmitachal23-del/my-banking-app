package banking.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Simple DB initializer that runs a SQL script over a server-level JDBC connection.
 * Usage: java -cp ".\bin;.\lib\mysql-connector-java.jar" banking.db.DBInitializer "sql/database_setup.sql"
 */
public class DBInitializer {

    // Adjust if your server is on a different host/port or uses different params
    private static final String URL_SERVER = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    // Embedded fallback (H2) URL when MySQL credentials are unknown or inaccessible.
    private static final String URL_H2 = "jdbc:h2:./data/banking_system;MODE=MySQL;DATABASE_TO_UPPER=false";

    public static void main(String[] args) {
        String sqlFile = args.length > 0 ? args[0] : "sql/database_setup.sql";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found on classpath: " + e.getMessage());
            System.exit(2);
        }

        String sql = null;
        try {
            sql = Files.readString(Path.of(sqlFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to read SQL file: " + e.getMessage());
            System.exit(3);
        }

        // First try MySQL server
        try (Connection conn = DriverManager.getConnection(URL_SERVER, USER, PASSWORD);
             Statement st = conn.createStatement()) {

            executeSqlStatements(st, sql);
            System.out.println("SQL import completed into MySQL server.");
            return;

        } catch (SQLException mysqlEx) {
            System.err.println("MySQL import failed: " + mysqlEx.getMessage());
            System.err.println("Falling back to embedded H2 database.");
        }

        // Fallback: embedded H2
        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = DriverManager.getConnection(URL_H2, "sa", "");
                 Statement st = conn.createStatement()) {

                executeSqlStatements(st, sql);
                System.out.println("SQL import completed into embedded H2 database.");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("H2 fallback failed: " + e.getMessage());
            System.exit(4);
        }
    }

    private static void executeSqlStatements(Statement st, String sql) {
        String[] parts = sql.split(";\\s*(?=\\r?$)");
        for (String part : parts) {
            String stmt = part.trim();
            if (stmt.isEmpty()) continue;
            try {
                st.execute(stmt);
            } catch (SQLException ex) {
                System.err.println("Failed to execute statement: " + stmt);
                ex.printStackTrace(System.err);
            }
        }
    }
}
