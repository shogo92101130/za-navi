# 座・Navi セットアップ手順（MySQLインストール → データベース作成 → Eclipse実行確認）

この手順を上から順番にやれば、データベース接続からEclipseでの動作確認まで一通り完了します。
PCで作業する前提のドキュメントです（このファイルはプロジェクト内 `docs/` フォルダに置いてあるので、
Eclipseのプロジェクトエクスプローラーからも開けます）。

---

## STEP 0：今の状態の確認

- MySQL（データベースソフト）が **まだパソコンに入っていない** 状態からのスタートです
- 完成しているコードは GitHub と `eclipse-workspace\za-navi` に用意済みです
- MySQL接続用のドライバ（jarファイル）も `eclipse-workspace\za-navi\WebContent\WEB-INF\lib\` に追加済みです

---

## STEP 1：MySQLをインストールする

1. ブラウザで以下にアクセスする
   `https://dev.mysql.com/downloads/installer/`
2. 「MySQL Installer for Windows」をダウンロードする
   - 容量が小さい方（Web版・`mysql-installer-web-community`）でOK
   - 会員登録を求められるが、ページ下の「No thanks, just start my download.」というリンクから登録なしでダウンロードできる
3. ダウンロードしたインストーラーを実行する
4. セットアップタイプを選ぶ画面が出たら **「Developer Default」** を選択（一通り必要なものが入る）
5. 進めていくと「Accounts and Roles」という画面で **rootユーザーのパスワード設定** を求められる
   - **★ここで設定したパスワードは絶対に忘れないようにメモしておく**
   - これが `DBUtil.java` の `PASS` に書く値になる（STEP 4で使う）
6. そのまま「Execute」を押して、インストール・設定を完了させる

### インストール後の確認
- スタートメニューに「MySQL 8.0 Command Line Client」が追加されていればOK
- それを開いて、さっき設定したパスワードを入力してログインできれば、インストール成功

---

## STEP 2：データベースを作成し、テーブルとデータを一括で用意する

ここで使うSQLファイルは、もう用意済みです：
`C:\Users\shogo\za-navi\docs\db\za_navi_schema.sql`

このファイル1つで「テーブルを作る」「初期データを入れる」の**両方**が一度にできます
（部署6件・座席180件・ユーザー200件のサンプルデータが最初から入っています）。

### 手順
1. 「MySQL 8.0 Command Line Client」を開き、rootパスワードでログインする
2. 以下を1行ずつ入力していく（コピペ推奨）

```sql
DROP DATABASE IF EXISTS zaseki1;
CREATE DATABASE zaseki1;
USE zaseki1;
SOURCE C:/Users/shogo/za-navi/docs/db/za_navi_schema.sql;
```

3. エラーが出ずに完了すれば成功。確認のため、以下を入力してみる

```sql
SHOW TABLES;
SELECT COUNT(*) FROM users;
SELECT COUNT(*) FROM seats;
```

- `departments` `users` `seats` `reservations` の4つのテーブルが表示される
- `users` が200件、`seats` が180件と表示されればOK

> ※ データベース名は`zaseki1`でOKです（`CREATE DATABASE zaseki1;`まではそのまま使えます）。
> ただしテーブルを作る部分は、以前ご自身で途中まで打ち込んでいたCREATE TABLE文ではなく、
> 上記のように`SOURCE ...za_navi_schema.sql;`で**修正済み・完成版のスキーマ**を読み込んでください
> （古い方には今回見つかったバグの原因になっていた制約がそのまま入っています）。

---

## STEP 3：プロジェクトフォルダをEclipseに追加する

すでにEclipse用のフォルダ（`C:\Users\shogo\eclipse-workspace\za-navi`）は用意済みなので、
「インポート」でEclipseに認識させるだけです。

1. Eclipseを起動する
2. メニューから「File」→「Import...」
3. 「General」フォルダを開き、「Existing Projects into Workspace」を選んで「Next」
4. 「Select root directory」で「Browse...」を押し、`C:\Users\shogo\eclipse-workspace\za-navi` を選択
5. プロジェクト一覧に「za-navi」が表示されるのでチェックを入れて「Finish」
6. プロジェクトエクスプローラーに「za-navi」が表示されればOK

> すでに一度開いたことがある場合は、この手順は不要です（プロジェクトが残っていればそのまま使えます）。

---

## STEP 4：データベース接続情報を書き込む（★今回のキモ）

1. プロジェクトエクスプローラーで `za-navi` → `src` → `dao` → `DBUtil.java` を開く
2. 以下の3行を見つける

```java
private static final String URL  = "jdbc:mysql://localhost:3306/zaseki1?useSSL=false&serverTimezone=Asia/Tokyo&characterEncoding=UTF-8";
private static final String USER = "root";
private static final String PASS = "your_password";
```

3. `PASS` の `"your_password"` の部分だけを、**STEP 1の⑤で設定した実際のrootパスワード**に書き換える

```java
private static final String PASS = "ここに自分で設定したパスワード";
```

- `URL` と `USER` はそのままでOK（`zaseki1` というデータベース名・`root` というユーザー名は、
  STEP 2で作ったものと一致しているので変更不要）
- 書き換えたら `Ctrl+S` で保存する

---

## STEP 5：MySQL接続用のドライバ（jar）が認識されているか確認する

1. プロジェクトエクスプローラーで `za-navi` → `WebContent` → `WEB-INF` → `lib` を開く
2. `mysql-connector-j-8.4.0.jar` というファイルがあることを確認する（すでに追加済みのはず）
3. 念のため、プロジェクトを右クリック →「Properties」→「Java Build Path」→「Libraries」タブで
   このjarが一覧に出ているか確認する
   - もし出ていなければ、プロジェクトを右クリック →「Refresh」(F5) してから、
     「Project」メニュー →「Clean...」→ 対象プロジェクトを選んで実行する

---

## STEP 6：Eclipseでサーバーを起動して動作確認する

1. プロジェクトエクスプローラーで `za-navi` を右クリック
2. 「Run As」→「Run on Server」を選ぶ
3. Tomcatサーバー（Apache Tomcat v9.0）を選んで「Finish」
4. ブラウザ（内蔵ブラウザ or 外部ブラウザ）でログイン画面が表示されればサーバー起動は成功
5. ログインしてみて、以下が動けば**データベース接続も成功**
   - メニュー画面で「本日の予約」などDBから取得した情報が表示される
   - 「行先登録」→「在宅」または「出張」でメモを入れて登録 → エラーが出ずにメニューに戻る
   - 「予約確認」→ キャンセルボタンを押してエラーが出ない

---

## うまくいかないときのチェックポイント

| 症状 | 考えられる原因 |
|---|---|
| ログイン画面すら出ない | Tomcatサーバーが起動していない（STEP 6を再確認） |
| 画面は出るがログイン後すぐエラー | `DBUtil.java`のPASSが間違っている、またはMySQLが起動していない |
| 「Unknown database 'zaseki1'」というエラー | STEP 2のデータベース作成がうまくいっていない |
| 「ClassNotFoundException: com.mysql.cj.jdbc.Driver」というエラー | jarが認識されていない（STEP 5を再確認、プロジェクトをCleanする） |
| 在宅・出張のメモでエラー、キャンセルでエラー | STEP 2を **修正済みの`za_navi_schema.sql`** で実行したか確認（古いSQLを使っていないか） |

エラーが出た場合は、画面に表示されたエラーメッセージをそのまま教えてもらえれば、一緒に解決できます。
