package data.repository.jdbc;

import java.sql.*;

public class JdbcRepository {

    private final String JDBC_URL = "jdbc:sqlite:cloudstore.db";
    private final String JDBC_CLASS = "org.sqlite.JDBC";

    private Connection connection;

    private JdbcRepository() {
        try {
            Class.forName(JDBC_CLASS);
            connection = DriverManager.getConnection(JDBC_URL);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class ConnectionHolder {
        private static final JdbcRepository jdbc = new JdbcRepository();

        public static Connection getConnection() {
            return jdbc.connection;
        }
    }

    // Отключиться от базы
    public void disconnect() {
        try {
            connection.close();
        } catch (Exception e) {e.printStackTrace();}
    }
}
