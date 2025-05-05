package com.bistro.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Database configuration utility class for managing database connections
 * using HikariCP connection pooling.
 */
public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;

    // Default database configuration constants
    private static String DB_URL = "jdbc:mysql://localhost:3306/bistro_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static String DB_USER = "root";
    private static String DB_PASSWORD = "root";
    private static int MAX_POOL_SIZE = 10;

    static {
        try {
            loadDatabaseProperties();
            initializeDataSource();
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
        }
    }

    /**
     * Loads database properties from a file if it exists.
     * Falls back to default values if not found.
     */
    private static void loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("db/database.properties")) {
            if (input != null) {
                props.load(input);
                
                // Override defaults with properties from file if they exist
                DB_URL = props.getProperty("db.url", DB_URL);
                DB_USER = props.getProperty("db.user", DB_USER);
                DB_PASSWORD = props.getProperty("db.password", DB_PASSWORD);
                
                try {
                    MAX_POOL_SIZE = Integer.parseInt(props.getProperty("db.maxPoolSize", String.valueOf(MAX_POOL_SIZE)));
                } catch (NumberFormatException e) {
                    logger.warn("Invalid max pool size in properties, using default: {}", MAX_POOL_SIZE);
                }
                
                logger.info("Loaded database properties from file");
            } else {
                // Also check for system environment variables
                String envUrl = System.getenv("BISTRO_DB_URL");
                String envUser = System.getenv("BISTRO_DB_USER");
                String envPassword = System.getenv("BISTRO_DB_PASSWORD");
                
                if (envUrl != null) DB_URL = envUrl;
                if (envUser != null) DB_USER = envUser;
                if (envPassword != null) DB_PASSWORD = envPassword;
                
                logger.info("Using default database properties or environment variables");
            }
        } catch (IOException e) {
            logger.warn("Could not load database properties file, using defaults", e);
        }
    }

    private static void initializeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setUsername(DB_USER);
        config.setPassword(DB_PASSWORD);
        config.setMaximumPoolSize(MAX_POOL_SIZE);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setPoolName("BistroDBPool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        try {
            dataSource = new HikariDataSource(config);
            logger.info("Database connection pool initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw e;
        }
    }

    /**
     * Get a database connection from the connection pool
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initializeDataSource();
        }
        return dataSource.getConnection();
    }

    /**
     * Close the connection pool
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }
}
