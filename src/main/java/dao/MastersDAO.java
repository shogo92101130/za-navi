package dao;

import entity.Seat;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 座席マスタCRUD用DAO（JDBC版）
 * SeatsDAO と同じ seats テーブルを操作する。
 */
public class MastersDAO {

    private Seat map(ResultSet rs) throws SQLException {
        return new Seat(
                rs.getInt("seat_id"),
                rs.getString("base_name"),
                rs.getString("f_name"),
                rs.getString("area_name"),
                rs.getString("seat_name")
        );
    }

    public List<Seat> getAll() {
        String sql = "SELECT seat_id, base_name, area_name, f_name, seat_name FROM seats "
                   + "ORDER BY base_name, f_name, area_name, seat_name";
        List<Seat> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("座席マスタ一覧の取得に失敗しました", e);
        }
        return list;
    }

    /** 指定の拠点・フロア・エリア・席名と同じ座席が既に存在するか（excludeSeatIdの席は除く） */
    public boolean exists(String baseName, String fName, String areaName, String seatName, int excludeSeatId) {
        String sql = "SELECT 1 FROM seats WHERE base_name=? AND f_name=? AND area_name=? AND seat_name=? AND seat_id<>?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, baseName);
            ps.setString(2, fName);
            ps.setString(3, areaName);
            ps.setString(4, seatName);
            ps.setInt(5, excludeSeatId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("座席の重複チェックに失敗しました", e);
        }
    }

    public Seat findById(int seatId) {
        String sql = "SELECT seat_id, base_name, area_name, f_name, seat_name FROM seats WHERE seat_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("座席の取得に失敗しました", e);
        }
        return null;
    }

    public void add(Seat seat) {
        // seat_id は AUTO_INCREMENT ではないため、現在の最大値+1を採番する
        String selectMaxSql = "SELECT IFNULL(MAX(seat_id), 0) + 1 AS next_id FROM seats";
        String insertSql = "INSERT INTO seats (seat_id, base_name, area_name, f_name, seat_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection()) {
            int nextId;
            try (PreparedStatement ps = conn.prepareStatement(selectMaxSql);
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                nextId = rs.getInt("next_id");
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, nextId);
                ps.setString(2, seat.getBaseName());
                ps.setString(3, seat.getAreaName());
                ps.setString(4, seat.getFName());
                ps.setString(5, seat.getSeatName());
                ps.executeUpdate();
            }
            seat.setSeatId(nextId);
        } catch (SQLException e) {
            throw new RuntimeException("座席の追加に失敗しました", e);
        }
    }

    public boolean update(Seat updated) {
        String sql = "UPDATE seats SET base_name=?, area_name=?, f_name=?, seat_name=? WHERE seat_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, updated.getBaseName());
            ps.setString(2, updated.getAreaName());
            ps.setString(3, updated.getFName());
            ps.setString(4, updated.getSeatName());
            ps.setInt(5, updated.getSeatId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("座席の更新に失敗しました", e);
        }
    }

    public boolean delete(int seatId) {
        String sql = "DELETE FROM seats WHERE seat_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, seatId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("座席の削除に失敗しました", e);
        }
    }
}
