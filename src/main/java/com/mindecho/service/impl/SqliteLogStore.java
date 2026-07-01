package com.mindecho.service.impl;

import com.mindecho.model.DestructionLog;
import com.mindecho.model.AiStyle;
import com.mindecho.model.EmotionLabel;
import com.mindecho.service.LogStoreService;
import com.mindecho.util.DatabaseHelper;
import com.mindecho.util.Encryptor;
import com.mindecho.util.EmotionEventBus;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SqliteLogStore implements LogStoreService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final Encryptor encryptor;
    private final EmotionEventBus eventBus;

    public SqliteLogStore(Encryptor encryptor, EmotionEventBus eventBus) {
        this.encryptor = encryptor;
        this.eventBus = eventBus;
    }

    @Override
    public void save(DestructionLog log) throws Exception {
        String sql = "INSERT INTO destruction_log (encrypted_text, ai_response, emotion_label, ai_style, created_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBytes(1, log.getEncryptedText());
            pstmt.setString(2, log.getAiResponse());
            pstmt.setString(3, log.getEmotionLabel().name());
            pstmt.setString(4, log.getAiStyle().name());
            pstmt.setString(5, log.getCreatedAt().format(DATE_TIME_FORMATTER));
            pstmt.executeUpdate();
        }
        
        eventBus.publish(log.getEmotionLabel());
    }

    @Override
    public List<DestructionLog> findByDate(LocalDate date) {
        String sql = "SELECT id, ai_response, emotion_label, ai_style, created_at FROM destruction_log WHERE created_at LIKE ? ORDER BY created_at DESC";
        List<DestructionLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, date.toString() + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(extractLogWithoutEncrypted(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }

    @Override
    public List<DestructionLog> findByMonth(YearMonth month) {
        String sql = "SELECT id, ai_response, emotion_label, ai_style, created_at FROM destruction_log WHERE created_at LIKE ? ORDER BY created_at DESC";
        List<DestructionLog> logs = new ArrayList<>();
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, month.toString() + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                logs.add(extractLogWithoutEncrypted(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return logs;
    }

    @Override
    public Optional<DestructionLog> findRandom() {
        String sql = "SELECT id, encrypted_text, ai_response, emotion_label, ai_style, created_at FROM destruction_log ORDER BY RANDOM() LIMIT 1";
        
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return Optional.of(extractLogWithEncrypted(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return Optional.empty();
    }

    @Override
    public void deleteAll() throws Exception {
        String sql = "DELETE FROM destruction_log";
        
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
        }
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) as count FROM destruction_log";
        
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

    private DestructionLog extractLogWithoutEncrypted(ResultSet rs) throws SQLException {
        DestructionLog log = new DestructionLog();
        log.setId(rs.getLong("id"));
        log.setAiResponse(rs.getString("ai_response"));
        log.setEmotionLabel(EmotionLabel.valueOf(rs.getString("emotion_label")));
        log.setAiStyle(AiStyle.valueOf(rs.getString("ai_style")));
        log.setCreatedAt(LocalDateTime.parse(rs.getString("created_at"), DATE_TIME_FORMATTER));
        return log;
    }

    private DestructionLog extractLogWithEncrypted(ResultSet rs) throws SQLException {
        DestructionLog log = extractLogWithoutEncrypted(rs);
        log.setEncryptedText(rs.getBytes("encrypted_text"));
        return log;
    }
}
