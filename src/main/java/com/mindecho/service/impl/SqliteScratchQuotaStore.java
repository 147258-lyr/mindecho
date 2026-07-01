package com.mindecho.service.impl;

import com.mindecho.service.ScratchQuotaService;
import com.mindecho.util.DatabaseHelper;

import java.sql.*;
import java.time.LocalDate;

public class SqliteScratchQuotaStore implements ScratchQuotaService {

    @Override
    public int getRemainingToday() {
        String today = LocalDate.now().toString();
        String sql = "SELECT remaining FROM scratch_quota WHERE quota_date = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, today);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("remaining");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 3;
    }

    @Override
    public void consumeOne() {
        String today = LocalDate.now().toString();
        
        String checkSql = "SELECT remaining FROM scratch_quota WHERE quota_date = ?";
        String insertSql = "INSERT OR REPLACE INTO scratch_quota (quota_date, remaining) VALUES (?, ?)";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setString(1, today);
            ResultSet rs = checkStmt.executeQuery();
            
            int current = rs.next() ? rs.getInt("remaining") : 3;
            int newRemaining = Math.max(0, current - 1);
            
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, today);
                insertStmt.setInt(2, newRemaining);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isExhausted() {
        return getRemainingToday() <= 0;
    }
}
