package com.mindecho.service.impl;

import com.mindecho.model.EmotionCapsule;
import com.mindecho.service.EmotionCapsuleService;
import com.mindecho.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SqliteCapsuleStore implements EmotionCapsuleService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void save(EmotionCapsule capsule) throws Exception {
        String sql = """
            INSERT INTO emotion_capsule (content, emotion, stress_level, keywords, 
                ai_advice, create_time, open_time, opened, review)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, capsule.getContent());
            stmt.setString(2, capsule.getEmotion());
            stmt.setInt(3, capsule.getStressLevel());
            stmt.setString(4, capsule.getKeywords());
            stmt.setString(5, capsule.getAiAdvice());
            stmt.setString(6, capsule.getCreateTime().format(FORMATTER));
            stmt.setString(7, capsule.getOpenTime() != null ? capsule.getOpenTime().format(FORMATTER) : null);
            stmt.setBoolean(8, capsule.isOpened());
            stmt.setString(9, capsule.getReview());

            int rows = stmt.executeUpdate();
            System.out.println("[DEBUG] SqliteCapsuleStore.save: 插入 " + rows + " 行, emotion=" + capsule.getEmotion());

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    capsule.setId(rs.getLong(1));
                    System.out.println("[DEBUG] SqliteCapsuleStore.save: 生成ID=" + capsule.getId());
                }
            }
        }
    }

    @Override
    public void update(EmotionCapsule capsule) throws Exception {
        String sql = """
            UPDATE emotion_capsule SET opened = ?, review = ? WHERE id = ?
            """;

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, capsule.isOpened());
            stmt.setString(2, capsule.getReview());
            stmt.setLong(3, capsule.getId());

            stmt.executeUpdate();
        }
    }

    @Override
    public Optional<EmotionCapsule> findById(Long id) {
        String sql = "SELECT * FROM emotion_capsule WHERE id = ?";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<EmotionCapsule> findAll() {
        List<EmotionCapsule> capsules = new ArrayList<>();
        String sql = "SELECT * FROM emotion_capsule ORDER BY create_time DESC";
        System.out.println("[DEBUG] SqliteCapsuleStore.findAll: 执行查询...");

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                capsules.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("[DEBUG] SqliteCapsuleStore.findAll: 返回 " + capsules.size() + " 个胶囊");
        return capsules;
    }

    @Override
    public List<EmotionCapsule> findByDate(LocalDate date) {
        List<EmotionCapsule> capsules = new ArrayList<>();
        String sql = "SELECT * FROM emotion_capsule WHERE date(create_time) = ? ORDER BY create_time DESC";

        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, date.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    capsules.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return capsules;
    }

    @Override
    public List<EmotionCapsule> findUnlocked() {
        List<EmotionCapsule> capsules = new ArrayList<>();
        String sql = """
            SELECT * FROM emotion_capsule 
            WHERE opened = 1 OR open_time <= datetime('now') 
            ORDER BY create_time DESC
            """;

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                capsules.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return capsules;
    }

    @Override
    public List<EmotionCapsule> findLocked() {
        List<EmotionCapsule> capsules = new ArrayList<>();
        String sql = """
            SELECT * FROM emotion_capsule 
            WHERE opened = 0 AND open_time > datetime('now') 
            ORDER BY create_time DESC
            """;

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                capsules.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return capsules;
    }

    @Override
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM emotion_capsule";

        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private EmotionCapsule mapResultSet(ResultSet rs) throws SQLException {
        EmotionCapsule capsule = new EmotionCapsule();
        capsule.setId(rs.getLong("id"));
        capsule.setContent(rs.getString("content"));
        capsule.setEmotion(rs.getString("emotion"));
        capsule.setStressLevel(rs.getInt("stress_level"));
        capsule.setKeywords(rs.getString("keywords"));
        capsule.setAiAdvice(rs.getString("ai_advice"));
        capsule.setCreateTime(LocalDateTime.parse(rs.getString("create_time"), FORMATTER));

        String openTimeStr = rs.getString("open_time");
        if (openTimeStr != null) {
            capsule.setOpenTime(LocalDateTime.parse(openTimeStr, FORMATTER));
        }

        capsule.setOpened(rs.getBoolean("opened"));
        capsule.setReview(rs.getString("review"));
        return capsule;
    }
}