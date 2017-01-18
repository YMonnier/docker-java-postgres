package com.yseemonnier.dbDocker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * The Class DatabaseManager.
 */
public class DatabaseManager {

    /**
     * The Constant freeConnections.
     */
    private static final Queue<Connection> freeConnections = new LinkedList<Connection>();

    /**
     * The Constant numberOfInitialConnections.
     */
    private static final int numberOfInitialConnections = 5;

    /**
     * The Constant password.
     */
    private static final String password = "root";

    /**
     * The Constant url.
     */
    private static final String url = "jdbc:postgresql://database/app";

    /**
     * The Constant user.
     */
    private static final String user = "root";

    static {
        for (int i = 0; i < numberOfInitialConnections; i++) {
            try {
                freeConnections.add(DriverManager.getConnection(url, user,
                        password));
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the connection.
     *
     * @return the connection
     * @throws SQLException the SQL exception
     */
    public static synchronized Connection getConnection() throws SQLException {
        Connection connection = null;
        if (freeConnections.isEmpty()) {
            connection = DriverManager.getConnection(url, user, password);
        } else {
            connection = freeConnections.remove();
        }
        return connection;

    }

    /**
     * Release connection.
     *
     * @param connection the connection
     */
    public static synchronized void releaseConnection(Connection connection) {
        if (freeConnections.size() < numberOfInitialConnections) {
            freeConnections.add(connection);
        } else {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ClientEntity {
        public static String TABLE_NAME = "Clients";
        public static String COLUMN_NAME = "name";
        public static String COLUMN_AGE = "age";

        public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_NAME + " varchar(100)," +
                COLUMN_AGE + " integer" +
                ")";
        public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

        public static void insert(final Connection connection, Client client) throws SQLException {
            String sqlInsert = "INSERT INTO " + TABLE_NAME + "(" + COLUMN_NAME + ", " + COLUMN_AGE + ") VALUES(?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(sqlInsert)) {
                ps.setString(1, client.getName());
                ps.setInt(2, client.getAge());
                ps.execute();
            }
        }
    }
}