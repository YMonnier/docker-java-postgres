package com.yseemonnier.dbDocker;

import java.sql.*;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World from Docker Container!");
        try (Connection c = DatabaseManager.getConnection()) {

            System.out.println("Deleting table '" + DatabaseManager.ClientEntity.TABLE_NAME + "' if exists.");
            try (Statement statement = c.createStatement()) {
                statement.execute(DatabaseManager.ClientEntity.DELETE_TABLE);
            }

            System.out.println("Creating table '" + DatabaseManager.ClientEntity.TABLE_NAME + "' if not exists.");
            try (Statement statement = c.createStatement()) {
                statement.execute(DatabaseManager.ClientEntity.CREATE_TABLE);
            }

            Client client = new Client("Pierre", 45);

            System.out.println("Inserting a client...");
            DatabaseManager.ClientEntity.insert(c, client);

            System.out.println("Selecting all clients...");
            String sqlGet = "SELECT * FROM " + DatabaseManager.ClientEntity.TABLE_NAME;
            try (Statement stmt = c.createStatement()) {
                ResultSet clients = stmt.executeQuery(sqlGet);
                while (clients.next()) {
                    String name = clients.getString(DatabaseManager.ClientEntity.COLUMN_NAME);
                    int age= clients.getInt(DatabaseManager.ClientEntity.COLUMN_AGE);
                    System.out.println("Client " + name + " - " + age);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
