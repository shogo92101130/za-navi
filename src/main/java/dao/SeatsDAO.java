package dao;

import entity.Seat;
import java.sql.*;
import java.util.*;

/**
 * 座席データアクセスオブジェクト（JDBC版）
 */
public class SeatsDAO {

    /**
     * 拠点（オフィス）名 → フロアマップ画像ファイル名の対応は office_images テーブルで管理する。
     * マスタ更新画面から登録・変更でき、新しいオフィスが増えたときもコード修正なしで
     * 画像を差し込めるようにするため（画像ファイル自体は src/main/webapp/images/ に置く）。
     * 未登録の拠点は getOfficeImage() が null を返し、JSP側は画像なしで表示する。
     */

    /** 拠点名に対応するフロアマップ画像ファイル名を返す（未登録の拠点はnull） */
    public String getOfficeImage(String baseName) {
        if (baseName == null || baseName.isEmpty()) return null;
        String sql = "SELECT image_file FROM office_images WHERE base_name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, baseName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("image_file") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("オフィス画像の取得に失敗しました", e);
        }
    }

    /** 拠点名に対応するフロアマップ画像ファイル名を登録・変更する（新規オフィスにも対応） */
    public void setOfficeImage(String baseName, String imageFile) {
        String sql = "INSERT INTO office_images (base_name, image_file) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE image_file = VALUES(image_file)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, baseName);
            ps.setString(2, imageFile);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("オフィス画像の登録に失敗しました", e);
        }
    }

    /** マスタ更新画面で「画像が登録済みの拠点」も一覧できるように、登録済みの対応を全件返す */
    public Map<String, String> getOfficeImages() {
        String sql = "SELECT base_name, image_file FROM office_images ORDER BY base_name";
        Map<String, String> map = new LinkedHashMap<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.put(rs.getString("base_name"), rs.getString("image_file"));
        } catch (SQLException e) {
            throw new RuntimeException("オフィス画像一覧の取得に失敗しました", e);
        }
        return map;
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
