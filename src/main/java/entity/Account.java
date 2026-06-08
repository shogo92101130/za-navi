package entity;

/**
 * 社員アカウント1件分のデータを表すentity（DBの accounts テーブル1行に相当）。
 * MVC2では「Model」のうち、データそのものを表す部分。
 * DAOがDBやモックデータから取り出した値をこのクラスに詰め替えて、Logic・JSPへ渡す。
 *
 *   userId   : ログインID（社員番号など）
 *   password : ログインパスワード
 *   name     : 氏名
 *   bId      : 所属部署ID（'D001' のような文字列。部署名は LocationsDAO.getBNameById(bId) で取得する）
 *   admin    : 権限種別（0=一般ユーザー, 1=管理者）
 */
public class Account {
    private String userId;
    private String password;
    private String name;
    private String bId;
    private int admin; // 0=一般, 1=管理者

    public Account() {}

    public Account(String userId, String password, String name, String bId, int admin) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.bId = bId;
        this.admin = admin;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBId() { return bId; }
    public void setBId(String bId) { this.bId = bId; }
    public int getAdmin() { return admin; }
    public void setAdmin(int admin) { this.admin = admin; }
}
