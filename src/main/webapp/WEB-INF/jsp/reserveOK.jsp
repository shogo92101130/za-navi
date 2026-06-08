<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, entity.Reservation, entity.Seat" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>予約完了 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<div class="container">
  <div class="card" style="max-width:600px; margin:0 auto; text-align:center;">
    <div style="font-size:56px; margin-bottom:16px;">&#10003;</div>
    <h2 style="color:var(--green); border:none; text-align:center; margin-bottom:20px;">予約が完了しました</h2>

    <%
      // 通常の単席予約
      Reservation r = (Reservation) request.getAttribute("reservation");
      Seat seat = (Seat) request.getAttribute("seat");
      // 代理予約（複数）
      List<String> createdList = (List<String>) request.getAttribute("createdList");
      String reserveDate = (String) request.getAttribute("reserveDate");
      String menuAction = Boolean.TRUE.equals(session.getAttribute("isAdmin")) ? "adminMenu" : "menu";
    %>

    <% if (r != null && seat != null) { %>
    <table class="table" style="text-align:left; max-width:400px; margin:0 auto 24px;">
      <tr><th style="width:40%;">予約日</th><td><%= r.getReserveDate() %></td></tr>
      <tr><th>拠点</th>  <td><%= seat.getBaseName() %></td></tr>
      <tr><th>フロア</th><td><%= seat.getFName() %></td></tr>
      <tr><th>エリア</th><td><%= seat.getAreaName() %></td></tr>
      <tr><th>座席</th>  <td><strong><%= seat.getSeatName() %></strong></td></tr>
      <% if (r.getMemo() != null && !r.getMemo().isEmpty()) { %>
      <tr><th>メモ</th>  <td><%= r.getMemo() %></td></tr>
      <% } %>
    </table>
    <% } %>

    <% if (createdList != null && !createdList.isEmpty()) { %>
    <p style="font-weight:700; margin-bottom:10px;">代理予約完了（<%= reserveDate %>）</p>
    <ul style="list-style:none; text-align:left; display:inline-block; margin-bottom:20px;">
      <% for (String line : createdList) { %>
      <li style="padding:4px 0; border-bottom:1px solid #eee;">&#10003; <%= line %></li>
      <% } %>
    </ul>
    <% } %>

    <div style="display:flex; gap:12px; justify-content:center;">
      <a href="<%= request.getContextPath() %>/ControlServlet?action=reserveConfirm" class="btn btn-primary">
        予約一覧を確認する
      </a>
      <a href="<%= request.getContextPath() %>/ControlServlet?action=<%= menuAction %>" class="btn btn-secondary">
        メニューへ戻る
      </a>
    </div>
  </div>
</div>
</body>
</html>
