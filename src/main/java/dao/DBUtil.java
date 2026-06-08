package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MySQL接続の共通クラス。
 * 各DAOはここから Connection を取得してJDBC操作を行う（接続情報を一箇所にまとめるため）。
 *
 * 接続先DB: za_navi_db（docs/db/za_navi_schema.sql で作成）
 */
public class DBUtil {
    private static final String URL  = "jdbc:mysql://localhost:3306/za_navi_db?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASS = "your_password";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBCドライバが見つかりません。jarファイルを追加したか確認してください。", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
