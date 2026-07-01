package com.mindecho.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    private static final Path DB_DIR = resolveDatabaseDir();
    private static final Path DB_FILE = DB_DIR.resolve("mindecho.db");
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE.toAbsolutePath();

    private static Path resolveDatabaseDir() {
        return Path.of(System.getProperty("user.dir"), "data");
    }

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void ensureDatabaseDir() {
        try {
            Files.createDirectories(DB_DIR);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create database directory: " + DB_DIR, e);
        }
    }

    public static synchronized Connection getConnection() throws SQLException {
        ensureDatabaseDir();
        return DriverManager.getConnection(DB_URL);
    }

    public static void initDatabase() {
        ensureDatabaseDir();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS destruction_log (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    encrypted_text BLOB NOT NULL,
                    ai_response TEXT NOT NULL,
                    emotion_label TEXT NOT NULL CHECK (emotion_label IN ('ANGER','ANXIETY','SADNESS','CALM')),
                    ai_style TEXT NOT NULL CHECK (ai_style IN ('GENTLE','SHARP')),
                    created_at TEXT NOT NULL
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS scratch_quota (
                    quota_date TEXT PRIMARY KEY,
                    remaining INTEGER NOT NULL DEFAULT 3 CHECK (remaining >= 0 AND remaining <= 3)
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS app_metadata (
                    key TEXT PRIMARY KEY,
                    value TEXT NOT NULL
                )
            """);
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS emotion_capsule (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    content TEXT NOT NULL,
                    emotion TEXT NOT NULL CHECK (emotion IN ('ANGER','ANXIETY','SADNESS','HAPPY','CALM')),
                    stress_level INTEGER NOT NULL DEFAULT 1 CHECK (stress_level >= 1 AND stress_level <= 5),
                    keywords TEXT,
                    ai_advice TEXT,
                    create_time TEXT NOT NULL,
                    open_time TEXT,
                    opened INTEGER NOT NULL DEFAULT 0 CHECK (opened IN (0,1)),
                    review TEXT
                )
            """);
            
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_emotion_capsule_create_time
                ON emotion_capsule (create_time)
            """);
            
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_emotion_capsule_open_time
                ON emotion_capsule (open_time)
            """);
            
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_destruction_log_created_at
                ON destruction_log (created_at)
            """);
            
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_destruction_log_emotion_label
                ON destruction_log (emotion_label)
            """);
            
            stmt.execute("""
                CREATE INDEX IF NOT EXISTS idx_destruction_log_ai_style
                ON destruction_log (ai_style)
            """);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
