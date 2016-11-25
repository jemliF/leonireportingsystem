package com.leoni.mfa.reporting.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author bewa1022
 */
public class MSSQLConnection {

    private String driver;
    private String url;
    private String user;
    private String password;
    private Connection connection;
    private int interval = 10;

    private static Logger logger = org.apache.log4j.LogManager.getRootLogger();

    public MSSQLConnection() {
        loadDBConnectionProperties();
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public MSSQLConnection(int interval) {
        this.interval = interval;
        loadDBConnectionPropertiesXML();
        try {
            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, password);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * loads database connection properties from dcconfig file
     */
    public void loadDBConnectionProperties() {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("dbconfig.properties");
            properties.load(inputStream);

            driver = properties.getProperty("driver");
            user = properties.getProperty("user");
            password = properties.getProperty("password");
            String serverName = properties.getProperty("servername");
            String serverPort = properties.getProperty("serverport");

            String database = properties.getProperty("database");

            url = "jdbc:sqlserver://" + serverName + ":" + serverPort + ";DatabaseName=" + database;

        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * loads database connection properties from dbconfig file
     */
    public void loadDBConnectionPropertiesXML() {
        SAXBuilder builder = new SAXBuilder();
        try {
            Document doc = builder.build(new File("./config.xml"));
            Element root = doc.getRootElement();
            Element db = root.getChild("db");
            driver = db.getChildText("driver");
            System.out.println("driver: " + driver);
            user = db.getChildText("user");
            System.out.println("user: " + user);
            password = db.getChildText("password");
            System.out.println("password: " + password);
            String serverName = db.getChildText("servername");
            String serverPort = db.getChildText("serverport");

            int arch_interval = 10;
            arch_interval = Integer.parseInt(db.getChildText("archive_interval"));
            String database;
            if (arch_interval > this.interval) {
                database = db.getChildText("database");
            } else {
                database = db.getChildText("archive_db");
            }

            url = "jdbc:sqlserver://" + serverName + ":" + serverPort + ";DatabaseName=" + database;
            System.out.println("url connection: " + url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MSSQLConnection mSSQLConnection = new MSSQLConnection(9);
    }

    public Connection connecter() {
        try {
            if (connection == null) {
                new MSSQLConnection();
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
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                logger.error(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
