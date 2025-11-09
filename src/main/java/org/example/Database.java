package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This is a very naive database connection class.
 * In real life, you should make use of a decent database API,
 * such as Spring Data or Hibernate.
 */
public class Database {

    private static Connection connection;

    public Database() {
        try {
            // Reuse existing open connection
            if (connection != null && !connection.isClosed()) {
                return;
            }

            // Create persistent in-memory HSQLDB instance
            connection = DriverManager.getConnection(
                    "jdbc:hsqldb:mem:mymemdb;DB_CLOSE_DELAY=-1;LOCK_MODE=0",
                    "SA",
                    ""
            );

            connection.setAutoCommit(false);

            try (var st = connection.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS invoice (name VARCHAR(100), value DOUBLE)");
            }
            connection.commit();

        } catch (SQLException e) {
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void resetDatabase() {
        withSql(() -> {
            try (var preparedStatement = connection.prepareStatement("DELETE FROM invoice")) {
                preparedStatement.execute();
                connection.commit();
            }
            return null;
        });
    }

    public interface SqlSupplier<T> {
        T doSql() throws SQLException;
    }

    public <T> T withSql(SqlSupplier<T> sqlSupplier) {
        try {
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Connection is not available");
            }
            return sqlSupplier.doSql();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Keep the connection open for the lifetime of the JVM during testing
    public void close() {
        // no-op so that the in-memory DB isn't destroyed mid-test
    }
}