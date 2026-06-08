package dao;

import entity.Account;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ユーザーアカウントDAO（JDBC版）
 *
 * DB側の user_id は INT だが、画面・ロジック側では従来どおり String として扱う
 * （影響範囲を抑えるため、JDBCの境界でだけ int ⇔ String を変換する）。
 */
public class AccountsDAO {

    private Account map(ResultSet rs) throws SQLException {
        return new Account(
                String.valueOf(rs.getInt("user_id")),
                rs.getString("password"),
                rs.getString("name"),
                rs.getString("b_id"),
                rs.getInt("admin")
        );
    }

    /** "1001" のような数字文字列を user_id として解釈する（数値でなければ null＝該当なし扱い） */
    private Integer parseUserId(String userId) {
        try {
            return Integer.valueOf(userId);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    public Account findById(String userId) {
        Integer id = parseUserId(userId);
        if (id == null) return null;

        String sql = "SELECT user_id, password, b_id, name, admin FROM users WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("アカウントの取得に失敗しました", e);
        }
        return null;
    }

    public Account authenticate(String userId, String password) {
        Integer id = parseUserId(userId);
        if (id == null) return null;

        String sql = "SELECT user_id, password, b_id, name, admin FROM users WHERE user_id = ? AND password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("ログイン認証に失敗しました", e);
        }
        return null;
    }

    public List<Account> findAll() {
        String sql = "SELECT user_id, password, b_id, name, admin FROM users ORDER BY user_id";
        List<Account> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("アカウント一覧の取得に失敗しました", e);
        }
        return list;
    }

    public List<Account> findByName(String keyword) {
        String sql = (keyword == null || keyword.isEmpty())
                ? "SELECT user_id, password, b_id, name, admin FROM users ORDER BY user_id"
                : "SELECT user_id, password, b_id, name, admin FROM users WHERE name LIKE ? ORDER BY user_id";
        List<Account> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(1, "%" + keyword + "%");
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("氏名検索に失敗しました", e);
        }
        return list;
    }

    public List<Account> findByBId(String bId) {
        String sql = "SELECT user_id, password, b_id, name, admin FROM users WHERE b_id = ? ORDER BY user_id";
        List<Account> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("部署メンバーの取得に失敗しました", e);
        }
        return list;
    }
}
