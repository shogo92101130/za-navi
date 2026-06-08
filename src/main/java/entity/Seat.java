package entity;

/**
 * 座席1件分のデータを表すentity（DBの seats テーブル1行に相当）。
 * 「拠点 → フロア → エリア → 席名」の4段階で、座席の場所を一意に表す。
 * 例: baseName="三田オフィス", fName="2F", areaName="Aエリア", seatName="A-01"
 *
 *   seatId   : 座席ID（一意の連番）
 *   baseName : 拠点（オフィス名。例:「三田オフィス」）
 *   fName    : フロア（例:「2F」）
 *   areaName : エリア（例:「Aエリア」）
 *   seatName : 席名・席番号（例:「A-01」）
 */
public class Seat {
    private int seatId;
    private String baseName; // 拠点
    private String fName;    // フロア
    private String areaName; // エリア
    private String seatName; // 席番号

    public Seat() {}

    public Seat(int seatId, String baseName, String fName, String areaName, String seatName) {
        this.seatId = seatId;
        this.baseName = baseName;
        this.fName = fName;
        this.areaName = areaName;
        this.seatName = seatName;
    }

    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }
    public String getBaseName() { return baseName; }
    public void setBaseName(String baseName) { this.baseName = baseName; }
    public String getFName() { return fName; }
    public void setFName(String fName) { this.fName = fName; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public String getSeatName() { return seatName; }
    public void setSeatName(String seatName) { this.seatName = seatName; }
}
