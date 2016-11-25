/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leoni.mfa.reporting.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

/**
 *
 * @author fathi jemli
 */
public class MySQLConnexion {

    static Connection connection = null;
    static String jdbcDriver = "com.mysql.jdbc.Driver";
    static String database = "jdbc:mysql://localhost/smart_buyer";
    static String user = "root";
    static String password = "admin";

    public MySQLConnexion() {
        try {
            Class.forName(jdbcDriver).newInstance();
            connection = DriverManager.getConnection(database, user, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection connecter() {
        try {
            if (connection == null) {
                new MySQLConnexion();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public ResultSet selectQuery(String command) {
        ResultSet resultSet = null;
        try {
            connection = connecter();
            resultSet = connection.createStatement().executeQuery(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public boolean updateQuery(String command) {
        boolean result = false;
        try {
            connection = connecter();
            result = (connection.createStatement().executeUpdate(command) > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
