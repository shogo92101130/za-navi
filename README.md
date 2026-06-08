# 座・Navi（Za-Navi）座席管理システム

研修課題用デモアプリ。Java Servlet/JSP（MVC モデル2）。DB 接続なし（モックデータ）。

---

## Eclipse でのプロジェクト作成・起動手順

### 1. 動的 Web プロジェクトを作成する

1. Eclipse を起動し、**File → New → Dynamic Web Project** を選択
2. 以下を設定して **Finish**
   | 項目 | 設定値 |
   |------|--------|
   | Project name | `za-navi` |
   | Target runtime | Apache Tomcat v9.0（または v10.1） |
   | Dynamic web module version | 4.0 |
   | Context root | `za-navi` |

> Tomcat がまだ登録されていない場合は  
> **Servers ビュー → New → Server → Apache → Tomcat v9.0**  
> で Tomcat のインストールフォルダを指定してから作成してください。

---

### 2. ファイルを配置する

このリポジトリのファイルを Eclipse のプロジェクトフォルダへコピーします。

| このリポジトリ | Eclipse プロジェクト |
|----------------|----------------------|
| `src/main/java/**` | `src/` 直下（パッケージごと） |
| `src/main/webapp/**` | `WebContent/` 直下 |

**コピー手順（Windows エクスプローラー）**

```
za-navi/
  src/main/java/control/   → Eclipse/za-navi/src/control/
  src/main/java/model/     → Eclipse/za-navi/src/model/
  src/main/java/dao/       → Eclipse/za-navi/src/dao/
  src/main/java/entity/    → Eclipse/za-navi/src/entity/
  src/main/webapp/css/     → Eclipse/za-navi/WebContent/css/
  src/main/webapp/images/  → Eclipse/za-navi/WebContent/images/
  src/main/webapp/index.jsp→ Eclipse/za-navi/WebContent/index.jsp
  src/main/webapp/WEB-INF/ → Eclipse/za-navi/WebContent/WEB-INF/
```

> Maven プロジェクトとして使う場合はそのまま `src/main/java` / `src/main/webapp` で OK です。

---

### 3. Servlet API の設定（Eclipse が警告を出す場合）

- **Properties → Java Build Path → Libraries**
- **Add Library → Server Runtime → Apache Tomcat v9.0** を追加

---

### 4. Tomcat で起動する

1. プロジェクトを右クリック → **Run As → Run on Server**
2. **Tomcat v9.0 Server at localhost** を選択して **Finish**
3. Console に `Server startup in XXX ms` と出たら起動完了

---

### 5. ブラウザで開く

```
http://localhost:8080/za-navi/ControlServlet?action=login
```

---

## デモアカウント

| ユーザーID | パスワード | 氏名 | 種別 |
|------------|------------|------|------|
| `user001`  | `pass001`  | 田中太郎 | 一般 |
| `user002`  | `pass002`  | 鈴木花子 | 一般 |
| `admin001` | `admin001` | 管理者山田 | 管理者 |

---

## 画面・機能一覧

| URL（action=） | 画面名 | 説明 |
|----------------|--------|------|
| `login` | ログイン | ID/PW を入力してログイン |
| `menu` | 一般メニュー | 本日の予約表示・各機能へのリンク |
| `adminMenu` | 管理者メニュー | 管理者専用メニュー |
| `locationRegist` | 行先登録 | 出社/在宅/出張を選択 |
| `businessTrip` | 出張先入力 | 出張先テキストを入力 |
| `reserve` | 座席予約 | 拠点→フロア→エリア→座席の 4 段階選択 |
| `reserveConfirm` | 予約確認 | 予約一覧表示・キャンセル |
| `nameSearch` | 氏名検索 | 社員名で検索（部分一致） |
| `departmentSearch` | 部門検索 | 部門で絞り込み |
| `seatStatus` | 座席利用状況 | 拠点・フロア・エリアで絞り込み（管理者） |
| `adminReserve` | 代理予約 | 個人 1 席 or 会議用複数席（管理者） |
| `masterUpdate` | マスタ更新 | 座席の追加・更新・削除（管理者） |
| `logout` | ログアウト | セッション破棄 |

---

## アーキテクチャ

```
Browser
  └─ /ControlServlet?action=○○
       └─ ControlServlet (doGet/doPost)
            └─ Logic#execute()   ← 業務処理
                 └─ DAO          ← データアクセス（モック）
            └─ forward → /WEB-INF/jsp/○○.jsp
```

### DAO を MySQL 接続に差し替える方法

各 DAO ファイルに `// TODO: 後でMySQL接続に差し替え` コメントがあります。  
その行のすぐ下にあるメソッドの中身を、JDBC の SQL クエリに置き換えてください。

**例: AccountsDAO#findById()**

```java
// 現在（モック）
public Account findById(String userId) {
    // TODO: 後でMySQL接続に差し替え: SELECT * FROM accounts WHERE user_id = ?
    return ACCOUNTS.stream()
            .filter(a -> a.getUserId().equals(userId))
            .findFirst().orElse(null);
}

// ↓ MySQL接続後
public Account findById(String userId) throws SQLException {
    String sql = "SELECT * FROM accounts WHERE user_id = ?";
    try (Connection conn = DBUtil.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Account(
                rs.getString("user_id"), rs.getString("password"),
                rs.getString("name"), rs.getInt("b_id"), rs.getInt("admin")
            );
        }
        return null;
    }
}
```

---

## ディレクトリ構成

```
za-navi/
├── src/main/java/
│   ├── control/ControlServlet.java      # フロントコントローラ
│   ├── model/
│   │   ├── Logic.java                   # インターフェース
│   │   ├── LoginLogic.java
│   │   ├── LogoutLogic.java
│   │   ├── LocationRegistLogic.java
│   │   ├── ReserveLogic.java
│   │   ├── ReserveConfirmLogic.java
│   │   ├── NameSearchLogic.java
│   │   ├── DepartmentSearchLogic.java
│   │   ├── AdminReserveLogic.java
│   │   └── MasterUpdateLogic.java
│   │   └── entity/
│   │       ├── Account.java
│   │       ├── Reservation.java
│   │       ├── Seat.java
│   │       └── Location.java
│   └── dao/
│       ├── AccountsDAO.java
│       ├── ReservationsDAO.java
│       ├── SeatsDAO.java
│       ├── LocationsDAO.java
│       └── MastersDAO.java
└── src/main/webapp/
    ├── index.jsp                        # ログイン画面へリダイレクト
    ├── css/style.css                    # ティールブルー基調スタイル
    ├── images/                          # アイコン用（空）
    └── WEB-INF/
        ├── web.xml
        └── jsp/
            ├── common/
            │   ├── header.jspf          # 一般ユーザー用ヘッダー
            │   └── headerAdmin.jspf     # 管理者用ヘッダー
            ├── login.jsp / loginNG.jsp
            ├── menu.jsp / adminMenu.jsp
            ├── locationRegist.jsp / businessTrip.jsp
            ├── reserve.jsp              # stage切り替え+埋有率カード
            ├── reserveConfirm.jsp
            ├── reserveOK.jsp / reserveNG.jsp
            ├── nameSearch.jsp / departmentSearch.jsp
            ├── seatStatus.jsp           # 拠点・フロア・エリア絞り込み
            ├── adminReserve.jsp         # 個人+会議用代理予約
            ├── masterUpdate.jsp         # CRUD全対応
            └── logout.jsp
```
