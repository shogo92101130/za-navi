package dao;

import entity.Seat;
import java.sql.*;
import java.util.*;

/**
 * 座席データアクセスオブジェクト（JDBC版）
 */
public class SeatsDAO {

    /**
     * 拠点（オフィス）名 → フロアマップ画像ファイル名の対応表。
     * 座席利用状況確認画面で、選んだ拠点に応じた画像を切り替えて表示するために使う。
     * 画像が用意できていない拠点（例:「中野」）はここに追加しない
     * → getOfficeImage() が null を返し、JSP側は画像なしで表示する。
     *
     * 新しいオフィスの画像を追加したときは、ここにも対応を追記すること。
     */
    static final Map<String, String> OFFICE_IMAGES = new LinkedHashMap<>();
    static {
        OFFICE_IMAGES.put("三田", "office_mita.png");
        OFFICE_IMAGES.put("芝浦", "office_shibaura.png");
    }

    /** 拠点名に対応するフロアマップ画像ファイル名を返す（未登録の拠点はnull） */
    public String getOfficeImage(String baseName) {
        return OFFICE_IMAGES.get(baseName);
    }

    private Seat map(ResultSet rs) throws SQLException {
        return new Seat(
                rs.getInt("seat_id"),
                rs.getString("base_name"),
                rs.getString("f_name"),
                rs.getString("area_name"),
                rs.getString("seat_name")
        );
    }

    public List<Seat> getAllSeats() {
        String sql = "SELECT seat_id, base_name, area_name, f_name, seat_name FROM seats ORDER BY seat_id";
        List<Seat> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            throw new RuntimeException("座席一覧の取得に失敗しました", e);
        }
        return list;
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

    public List<Seat> findByBase(String baseName) {
        return filterSeats(baseName, null, null);
    }

    public List<Seat> findByFloor(String baseName, String fName) {
        return filterSeats(baseName, fName, null);
    }

    public List<Seat> findByArea(String baseName, String fName, String areaName) {
        return filterSeats(baseName, fName, areaName);
    }

    /** 絞り込み検索（nullや空文字は条件なし扱い） */
    public List<Seat> filterSeats(String baseName, String fName, String areaName) {
        StringBuilder sql = new StringBuilder(
                "SELECT seat_id, base_name, area_name, f_name, seat_name FROM seats WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (baseName != null && !baseName.isEmpty()) { sql.append(" AND base_name = ?"); params.add(baseName); }
        if (fName    != null && !fName.isEmpty())    { sql.append(" AND f_name = ?");    params.add(fName); }
        if (areaName != null && !areaName.isEmpty()) { sql.append(" AND area_name = ?"); params.add(areaName); }
        sql.append(" ORDER BY seat_id");

        List<Seat> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("座席の絞り込みに失敗しました", e);
        }
        return list;
    }

    /** 拠点一覧（重複除去） */
    public List<String> getBases() {
        return distinctColumn("base_name", null, null, null);
    }

    /** 指定拠点のフロア一覧 */
    public List<String> getFloors(String baseName) {
        return distinctColumn("f_name", baseName, null, null);
    }

    /** 指定拠点・フロアのエリア一覧 */
    public List<String> getAreas(String baseName, String fName) {
        return distinctColumn("area_name", baseName, fName, null);
    }

    private List<String> distinctColumn(String column, String baseName, String fName, String areaName) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT ").append(column).append(" FROM seats WHERE 1=1");
        List<String> params = new ArrayList<>();
        if (baseName != null && !baseName.isEmpty()) { sql.append(" AND base_name = ?"); params.add(baseName); }
        if (fName    != null && !fName.isEmpty())    { sql.append(" AND f_name = ?");    params.add(fName); }
        if (areaName != null && !areaName.isEmpty()) { sql.append(" AND area_name = ?"); params.add(areaName); }
        sql.append(" ORDER BY ").append(column);

        List<String> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setString(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("座席候補一覧の取得に失敗しました", e);
        }
        return list;
    }
}
