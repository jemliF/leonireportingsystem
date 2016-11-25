package Test;

import com.leoni.mfa.reporting.db.*;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.log4j.Logger;

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

    public static void main(String[] args) {
        MSSQLConnection mSSQLConnection = new MSSQLConnection();
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
