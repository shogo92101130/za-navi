<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>予約失敗 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<%
  String menuAction = Boolean.TRUE.equals(session.getAttribute("isAdmin")) ? "adminMenu" : "menu";
%>

<div class="container">
  <div class="card" style="max-width:500px; margin:0 auto; text-align:center;">
    <div style="font-size:56px; color:var(--red); margin-bottom:16px;">&#10007;</div>
    <h2 style="color:var(--red); border:none; text-align:center; margin-bottom:16px;">予約できませんでした</h2>
    <div class="alert alert-danger">
      <%= request.getAttribute("errorMsg") != null ? request.getAttribute("errorMsg") : "予約処理中にエラーが発生しました。" %>
    </div>
    <div style="display:flex; gap:12px; justify-content:center; margin-top:16px;">
      <a href="<%= request.getContextPath() %>/ControlServlet?action=reserve" class="btn btn-primary">
        座席選択に戻る
      </a>
      <a href="<%= request.getContextPath() %>/ControlServlet?action=<%= menuAction %>" class="btn btn-secondary">
        メニューへ
      </a>
    </div>
  </div>
</div>
</body>
</html>
