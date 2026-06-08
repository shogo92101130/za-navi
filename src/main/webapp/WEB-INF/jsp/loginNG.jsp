<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>ログイン失敗 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="login-page">
  <div class="login-card">
    <div class="login-title">座・Navi</div>
    <div class="alert alert-danger" style="margin:16px 0;">
      <%= request.getAttribute("errorMsg") != null ? request.getAttribute("errorMsg") : "ログインに失敗しました。" %>
    </div>
    <a href="<%= request.getContextPath() %>/ControlServlet?action=login" class="btn btn-primary" style="width:100%; justify-content:center;">
      ログイン画面に戻る
    </a>
  </div>
</div>
</body>
</html>
