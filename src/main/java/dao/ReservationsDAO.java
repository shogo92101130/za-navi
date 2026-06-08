package dao;

import entity.Reservation;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

/**
 * 予約データDAO（JDBC版）
 *
 * DB側の user_id / register_id は INT だが、画面・ロジック側では従来どおり String として扱う
 * （影響範囲を抑えるため、JDBCの境界でだけ int ⇔ String を変換する）。
 * また、行先の種類（"出社"/"在宅"/"出張"）は entity 上は gyosaki だが、
 * DBのカラム名は place なので、ここでマッピングする。
 */
public class ReservationsDAO {

    private Reservation map(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("reserve_id"),
                String.valueOf(rs.getInt("user_id")),
                String.valueOf(rs.getInt("register_id")),
                rs.getDate("reserve_date").toString(),
                rs.getString("place"),
                rs.getString("memo"),
                rs.getInt("seat_id"),
                rs.getInt("cancel_flag")
        );
    }

    private static final String SELECT_COLUMNS =
            "SELECT reserve_id, user_id, register_id, reserve_date, place, memo, seat_id, cancel_flag FROM reservations ";

    public List<Reservation> findByUserId(String userId) {
        String sql = SELECT_COLUMNS + "WHERE user_id = ? AND cancel_flag = 0 ORDER BY reserve_date";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(userId));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("予約一覧の取得に失敗しました", e);
        }
        return list;
    }

    public List<Reservation> findTodayByUserId(String userId) {
        String sql = SELECT_COLUMNS + "WHERE user_id = ? AND reserve_date = ? AND cancel_flag = 0";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(userId));
            ps.setDate(2, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("本日の予約の取得に失敗しました", e);
        }
        return list;
    }

    public List<Reservation> findByDate(String date) {
        String sql = SELECT_COLUMNS + "WHERE reserve_date = ? AND cancel_flag = 0";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.parse(date)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("指定日の予約の取得に失敗しました", e);
        }
        return list;
    }

    /**
     * 指定日のseatId→userId マップを返す（座席ステータス表示用）
     * key=seatId, value=userId
     */
    public Map<Integer, String> getSeatUsageForDate(String date) {
        String sql = "SELECT seat_id, user_id FROM reservations "
                   + "WHERE reserve_date = ? AND cancel_flag = 0 AND seat_id > 0";
        Map<Integer, String> map = new HashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(LocalDate.parse(date)));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("seat_id"), String.valueOf(rs.getInt("user_id")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("座席利用状況の取得に失敗しました", e);
        }
        return map;
    }

    public void add(Reservation r) {
        String sql = "INSERT INTO reservations (user_id, register_id, reserve_date, place, memo, seat_id, cancel_flag) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, Integer.parseInt(r.getUserId()));
            ps.setInt(2, Integer.parseInt(r.getRegisterId()));
            ps.setDate(3, Date.valueOf(LocalDate.parse(r.getReserveDate())));
            ps.setString(4, r.getGyosaki());
            ps.setString(5, r.getMemo());
            ps.setInt(6, r.getSeatId());
            ps.setInt(7, r.getCancelFlag());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) r.setReserveId(keys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("予約の登録に失敗しました", e);
        }
    }

    public void cancel(int reserveId) {
        String sql = "UPDATE reservations SET cancel_flag = 1 WHERE reserve_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reserveId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("予約の取消に失敗しました", e);
        }
    }

    public Reservation findById(int reserveId) {
        String sql = SELECT_COLUMNS + "WHERE reserve_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reserveId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("予約の取得に失敗しました", e);
        }
        return null;
    }
}
