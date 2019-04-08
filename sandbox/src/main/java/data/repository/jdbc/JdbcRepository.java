package data.repository.jdbc;

import java.sql.*;

public class JdbcRepository {

    public static final String JDBC_URL = "jdbc:sqlite:customers.db";
    public static final String JDBC_CLASS = "org.sqlite.JDBC";

    public Connection connection;
    public Statement statement;

    private JdbcRepository() {
        try {
            Class.forName(JDBC_CLASS);
            connection = DriverManager.getConnection(JDBC_URL);
            statement = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static class JdbcRepositoryHolder {
        public static JdbcRepository instance = new JdbcRepository();
    }

    public static JdbcRepository getJdbc() {
        return JdbcRepositoryHolder.instance;
    }

    // Отключиться от базы
    public void disconnect() {
        try {
            connection.close();
        } catch (Exception e) {e.printStackTrace();}
    }

    // Выполнить SELECT
    public synchronized ResultSet executeQuery(String sql) {
        ResultSet rs = null;

        try {
            rs = statement.executeQuery(sql);
        } catch (SQLException e) {e.printStackTrace();}

        return rs;
    }

    // Выполнить INSERT
    public synchronized void executeUpdate(String sql) {
        try {
            statement.executeUpdate(sql);
        } catch (SQLException e) {e.printStackTrace();}
    }

}
