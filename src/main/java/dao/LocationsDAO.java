package dao;

import entity.Location;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 部署マスタDAO（JDBC版）。
 * ※クラス名・entity名は Location（場所）だが、扱っているのは「部署（基幹システム1部・営業システム本部など）」であり、
 *   座席の「拠点（三田・芝浦など）」とは別物なので注意（entity.Location のコメントも参照）。
 */
public class LocationsDAO {

    public List<Location> findAll() {
        String sql = "SELECT b_id, b_name FROM departments ORDER BY b_id";
        List<Location> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Location(rs.getString("b_id"), rs.getString("b_name")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("部署一覧の取得に失敗しました", e);
        }
        return list;
    }

    public Location findById(String bId) {
        String sql = "SELECT b_id, b_name FROM departments WHERE b_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Location(rs.getString("b_id"), rs.getString("b_name"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("部署の取得に失敗しました", e);
        }
        return null;
    }

    public String getBNameById(String bId) {
        Location loc = findById(bId);
        return loc != null ? loc.getBName() : "不明";
    }
}
