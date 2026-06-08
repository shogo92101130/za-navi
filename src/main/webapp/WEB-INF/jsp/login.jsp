<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>ログイン - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="login-page">
  <div class="login-card">
    <div class="login-title">座・Navi</div>
    <div class="login-subtitle">座席管理システム</div>

    <% if (request.getAttribute("errorMsg") != null) { %>
    <div class="alert alert-danger"><%= request.getAttribute("errorMsg") %></div>
    <% } %>

    <form action="<%= request.getContextPath() %>/ControlServlet" method="post">
      <input type="hidden" name="action" value="doLogin">

      <div class="form-group">
        <label>ユーザーID</label>
        <input type="text" name="userId" placeholder="例: user001" required autofocus>
      </div>
      <div class="form-group">
        <label>パスワード</label>
        <input type="password" name="password" placeholder="パスワードを入力" required>
      </div>

      <div style="display:flex; gap:10px; margin-top:24px;">
        <button type="submit" class="btn btn-primary btn-lg" style="flex:1;">
          ログイン
        </button>
      </div>
    </form>

    <div style="margin-top:20px; padding-top:16px; border-top:1px solid #ECEFF1; text-align:center;">
      <p style="font-size:12px; color:#757575; margin-bottom:10px;">管理者の方はこちら</p>
      <form action="<%= request.getContextPath() %>/ControlServlet" method="post">
        <input type="hidden" name="action" value="doLogin">
        <div class="form-group" style="margin-bottom:8px;">
          <input type="text" name="userId" placeholder="管理者ID（例: admin001）" required>
        </div>
        <div class="form-group" style="margin-bottom:12px;">
          <input type="password" name="password" placeholder="パスワード" required>
        </div>
        <button type="submit" class="btn btn-outline" style="width:100%;">
          管理者ログイン
        </button>
      </form>
    </div>

    <%-- ▼▼▼ 差し替え注意: ここは「動作確認用のテストID/パスワードを画面に表示している」サンプル表示です。
         本番環境（実際のデータベースに繋いだ後）では、ログイン情報を画面に表示するのはセキュリティ上NGなので、
         この <div> ごと削除すること。 ▼▼▼ --%>
    <div style="margin-top:16px; font-size:11px; color:#aaa; text-align:center;">
      ※ デモ: user001/pass001 ・ admin001/admin001
    </div>
    <%-- ▲▲▲ 差し替え注意ここまで ▲▲▲ --%>
  </div>
</div>
</body>
</html>
