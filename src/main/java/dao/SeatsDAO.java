package dao;

import entity.Seat;
import java.sql.*;
import java.util.*;

/**
 * 座席データアクセスオブジェクト（JDBC版）
 */
public class SeatsDAO {

    /**
     * 拠点（オフィス）・フロア → フロアマップ画像ファイル名の対応表。
     * 座席利用状況・代理予約・座席予約の各画面で、選んだ拠点（とフロア）に応じた
     * 画像を切り替えて表示するために使う。
     *
     * キーは「拠点名」または「拠点名_フロア名」。
     * フロアごとに画像が異なる場合は "拠点名_フロア名" で個別に登録すると、
     * getOfficeImage(拠点, フロア) がそちらを優先して返す
     * （例: 芝浦の1階と2階で画像が違う → "芝浦_1F" "芝浦_2F" をそれぞれ登録）。
     * フロアで画像が変わらない拠点は「拠点名」のみのキーで登録すればよい。
     *
     * 画像が用意できていない拠点・フロアはここに追加しない
     * → getOfficeImage() が null を返し、JSP側は画像なしで表示する。
     *
     * 新しいオフィス（やフロア）の画像を追加したときは、ここにも対応を追記すること。
     */
    static final Map<String, String> OFFICE_IMAGES = new LinkedHashMap<>();
    static {
        // フロアごとの専用マップ（1Fと2Fでレイアウトが異なるため、フロアを選んだ時点で
        // 該当フロアの画像に切り替わる。代理予約・座席利用状況・座席予約・マスタ更新の
        // どの画面でも、拠点とフロアの両方が決まった時点でこちらが優先表示される）
        OFFICE_IMAGES.put("三田_1F", "office_mita_1F.png");
        OFFICE_IMAGES.put("三田_2F", "office_mita_2F.png");
        OFFICE_IMAGES.put("芝浦_1F", "office_shibaura_1F.png");
        OFFICE_IMAGES.put("芝浦_2F", "office_shibaura_2F.png");
        OFFICE_IMAGES.put("中野_1F", "office_nakano_1F.png");
        OFFICE_IMAGES.put("中野_2F", "office_nakano_2F.png");

        // 拠点単位のフォールバック画像（フロアが未選択、またはそのフロア専用の画像が
        // まだ無い場合に使われる。例: 新しいフロアを増設したがまだ画像を用意していない時）
        OFFICE_IMAGES.put("三田", "office_mita.png");
        OFFICE_IMAGES.put("芝浦", "office_shibaura.png");
        OFFICE_IMAGES.put("中野", "office_nakano.png");

        // エリア専用画像（座席選択画面でエリアを選んだときに表示）
        // ファイルが images/ フォルダに存在しない場合は getAreaImage() が null を返し、
        // JSP 側は動的グリッド（緑/赤の空き状況マップ）にフォールバックする
        // エリア画像はファイル名も日本語ベースの命名規則で統一する。
        // 命名規則: office_{拠点}_{フロア}_{エリア}.png （例: office_三田_1F_A.png）
        // JSP からの inline 生成（getOfficeImage を使わない場合）と一致させるため。
        OFFICE_IMAGES.put("三田_1F_A", "office_三田_1F_A.png");
        OFFICE_IMAGES.put("三田_1F_B", "office_三田_1F_B.png");
        OFFICE_IMAGES.put("三田_1F_C", "office_三田_1F_C.png");
        OFFICE_IMAGES.put("三田_2F_A", "office_三田_2F_A.png");
        OFFICE_IMAGES.put("三田_2F_B", "office_三田_2F_B.png");
        OFFICE_IMAGES.put("三田_2F_C", "office_三田_2F_C.png");
        OFFICE_IMAGES.put("芝浦_1F_A", "office_芝浦_1F_A.png");
        OFFICE_IMAGES.put("芝浦_1F_B", "office_芝浦_1F_B.png");
        OFFICE_IMAGES.put("芝浦_1F_C", "office_芝浦_1F_C.png");
        OFFICE_IMAGES.put("芝浦_2F_A", "office_芝浦_2F_A.png");
        OFFICE_IMAGES.put("芝浦_2F_B", "office_芝浦_2F_B.png");
        OFFICE_IMAGES.put("芝浦_2F_C", "office_芝浦_2F_C.png");
        OFFICE_IMAGES.put("中野_1F_A", "office_中野_1F_A.png");
        OFFICE_IMAGES.put("中野_1F_B", "office_中野_1F_B.png");
        OFFICE_IMAGES.put("中野_1F_C", "office_中野_1F_C.png");
        OFFICE_IMAGES.put("中野_2F_A", "office_中野_2F_A.png");
        OFFICE_IMAGES.put("中野_2F_B", "office_中野_2F_B.png");
        OFFICE_IMAGES.put("中野_2F_C", "office_中野_2F_C.png");
    }

    /** 拠点名に対応するフロアマップ画像ファイル名を返す（未登録の拠点はnull） */
    public String getOfficeImage(String baseName) {
        return getOfficeImage(baseName, null);
    }

    /**
     * 拠点名・フロア名に対応するフロアマップ画像ファイル名を返す。
     * "拠点名_フロア名" の専用画像が登録されていればそれを優先し、
     * 無ければ「拠点名のみ」の画像にフォールバックする（どちらも無ければ null）。
     */
    public String getOfficeImage(String baseName, String floorName) {
        if (baseName == null || baseName.isEmpty()) return null;
        if (floorName != null && !floorName.isEmpty()) {
            String perFloor = OFFICE_IMAGES.get(baseName + "_" + floorName);
            if (perFloor != null) return perFloor;
        }
        return OFFICE_IMAGES.get(baseName);
    }

    /**
     * エリア専用のフロアマップ画像ファイル名を返す。
     * "拠点名_フロア名_エリア名" キーが登録されていればそのファイル名を、
     * 無ければ null を返す（フロア/拠点へのフォールバックはしない）。
     * null の場合、JSP 側は動的グリッドにフォールバックする。
     */
    public String getAreaImage(String baseName, String floorName, String areaName) {
        if (baseName == null || floorName == null || areaName == null) return null;
        return OFFICE_IMAGES.get(baseName + "_" + floorName + "_" + areaName);
    }

    /**
     * フロアマップの「期待されるファイル名」を返す。常に非null（baseName が非空のとき）。
     * OFFICE_IMAGES に登録済みならその名、未登録（新規追加オフィス等）なら
     * "office_{拠点}_{フロア}.png" を自動生成して返す。
     * JSP側は常にこのファイル名で <img> を生成し、ファイルが存在しない場合は
     * onerror でプレースホルダーを表示する。
     */
    public String getExpectedFloorImage(String baseName, String floorName) {
        if (baseName == null || baseName.isEmpty()) return null;
        String registered = getOfficeImage(baseName, floorName);
        if (registered != null) return registered;
        if (floorName != null && !floorName.isEmpty()) {
            return "office_" + baseName + "_" + floorName + ".png";
        }
        return "office_" + baseName + ".png";
    }

    /**
     * エリアマップの「期待されるファイル名」を返す。常に非null（全引数が非空のとき）。
     * OFFICE_IMAGES に登録済みならその名、未登録なら "office_{拠点}_{フロア}_{エリア}.png" を自動生成。
     */
    public String getExpectedAreaImage(String baseName, String floorName, String areaName) {
        if (baseName == null || floorName == null || areaName == null) return null;
        String registered = getAreaImage(baseName, floorName, areaName);
        if (registered != null) return registered;
        return "office_" + baseName + "_" + floorName + "_" + areaName + ".png";
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
