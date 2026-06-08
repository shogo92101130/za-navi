<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>行先登録 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<%
  String menuAction = Boolean.TRUE.equals(session.getAttribute("isAdmin")) ? "adminMenu" : "menu";
%>

<div class="container">
  <div class="card">
    <h2>行先登録</h2>
    <p style="color:#757575; margin-bottom:24px;">本日の行先を選択してください。</p>

    <div class="gyosaki-group">
      <!-- 出社 -->
      <form action="<%= request.getContextPath() %>/ControlServlet" method="post" style="flex:1; min-width:120px;">
        <input type="hidden" name="action" value="doLocationRegist">
        <input type="hidden" name="gyosaki" value="出社">
        <button type="submit" class="gyosaki-btn office" style="width:100%;">
          <img class="ico-lg" style="margin:0 auto 8px;display:block;" src="<%= request.getContextPath() %>/images/ico_office.png" alt="出社">
          出社
        </button>
      </form>

      <!-- 在宅（次の画面で任意のメモを入力） -->
      <form action="<%= request.getContextPath() %>/ControlServlet" method="post" style="flex:1; min-width:120px;">
        <input type="hidden" name="action" value="doLocationRegist">
        <input type="hidden" name="gyosaki" value="在宅">
        <button type="submit" class="gyosaki-btn remote" style="width:100%;">
          <img class="ico-lg" style="margin:0 auto 8px;display:block;" src="<%= request.getContextPath() %>/images/ico_remote.png" alt="在宅">
          在宅
        </button>
      </form>

      <!-- 出張 -->
      <form action="<%= request.getContextPath() %>/ControlServlet" method="post" style="flex:1; min-width:120px;">
        <input type="hidden" name="action" value="doLocationRegist">
        <input type="hidden" name="gyosaki" value="出張">
        <button type="submit" class="gyosaki-btn trip" style="width:100%;">
          <img class="ico-lg" style="margin:0 auto 8px;display:block;" src="<%= request.getContextPath() %>/images/ico_trip.png" alt="出張">
          出張
        </button>
      </form>
    </div>

    <div style="margin-top:24px;">
      <a href="<%= request.getContextPath() %>/ControlServlet?action=<%= menuAction %>" class="btn btn-secondary">
        &larr; メニューへ戻る
      </a>
    </div>
  </div>
</div>
</body>
</html>
