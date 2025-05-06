package com.bistro.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Database utility class for connection pooling and resource cleanup
 */
public class DBUtil {
    
    private static HikariDataSource dataSource;
    private static String dbUrl = "jdbc:mysql://localhost:3306/bistro_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static String dbUser = "root";
    private static String dbPassword = "root";
    private static int maxPoolSize = 10;
    
    static {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Load properties from file
            loadDatabaseProperties();
            
            // Configure the connection pool
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(dbUrl);
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
            config.setMaximumPoolSize(maxPoolSize);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(30000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            // Create the data source
            dataSource = new HikariDataSource(config);
            System.out.println("Database connection pool initialized successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("Failed to load MySQL JDBC driver: " + e.getMessage());
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }
    
    /**
     * Load database connection properties from the properties file
     */
    private static void loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = DBUtil.class.getClassLoader().getResourceAsStream("db/database.properties")) {
            if (input != null) {
                props.load(input);
                
                // Override default values with properties from file
                dbUrl = props.getProperty("db.url", dbUrl);
                dbUser = props.getProperty("db.user", dbUser);
                dbPassword = props.getProperty("db.password", dbPassword);
                
                try {
                    maxPoolSize = Integer.parseInt(props.getProperty("db.maxPoolSize", String.valueOf(maxPoolSize)));
                } catch (NumberFormatException e) {
                    System.err.println("Invalid max pool size in properties, using default: " + maxPoolSize);
                }
                
                System.out.println("Loaded database properties from file");
                System.out.println("Database URL: " + dbUrl);
                System.out.println("Database User: " + dbUser);
                System.out.println("Max Pool Size: " + maxPoolSize);
            } else {
                System.err.println("database.properties file not found, using default values");
            }
        } catch (IOException e) {
            System.err.println("Error loading database properties: " + e.getMessage());
        }
    }
    
    /**
     * Get a connection from the pool
     */
    public static Connection getConnection() throws SQLException {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            System.err.println("Failed to get database connection: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Close a connection safely
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Failed to close database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Close a statement safely
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.err.println("Failed to close statement: " + e.getMessage());
            }
        }
    }
    
    /**
     * Close a result set safely
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("Failed to close result set: " + e.getMessage());
            }
        }
    }
}