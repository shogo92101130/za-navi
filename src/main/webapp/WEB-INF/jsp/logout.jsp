<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>ログアウト - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="login-page">
  <div class="login-card" style="text-align:center;">
    <div class="login-title">座・Navi</div>
    <div style="margin:24px 0; font-size:48px;">&#128075;</div>
    <p style="font-size:16px; font-weight:600; margin-bottom:8px;">ログアウトしました</p>
    <p style="font-size:13px; color:#757575; margin-bottom:28px;">ご利用ありがとうございました。</p>
    <a href="<%= request.getContextPath() %>/ControlServlet?action=login" class="btn btn-primary btn-lg" style="width:100%; justify-content:center;">
      ログイン画面へ
    </a>
  </div>
</div>
</body>
</html>
