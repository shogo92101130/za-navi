package entity;

/**
 * 「部署マスタ」1件分のデータを表すentity（DBの locations／departments テーブル1行に相当）。
 *
 * ※クラス名は Location（場所）だが、中身は「部署ID（bId）と部署名（bName）」であり、
 *   座席の「拠点（三田オフィス・芝浦オフィスなど）」とは別物なので注意。
 *   "b" は「部署 = bumon」の頭文字。
 *
 *   bId   : 部署ID（Account.bId と対応する）
 *   bName : 部署名（画面に表示する文字列。例:「営業部」「開発部」）
 */
public class Location {
    private String bId;
    private String bName;

    public Location() {}

    public Location(String bId, String bName) {
        this.bId = bId;
        this.bName = bName;
    }

    public String getBId() { return bId; }
    public void setBId(String bId) { this.bId = bId; }
    public String getBName() { return bName; }
    public void setBName(String bName) { this.bName = bName; }
}
