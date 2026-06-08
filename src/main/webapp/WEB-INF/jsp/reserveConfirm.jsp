<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, entity.Reservation, entity.Seat" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>予約確認 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<div class="container">
  <div class="card">
    <h2>予約確認・キャンセル</h2>

    <!-- キャンセル完了メッセージ -->
    <% if (request.getAttribute("cancelMessage") != null) { %>
    <div class="alert alert-success" style="font-size:15px; font-weight:600;">
      &#10003; <%= request.getAttribute("cancelMessage") %>
    </div>
    <% } %>

    <%
      List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
      Map<Integer, Seat> seatMap = (Map<Integer, Seat>) request.getAttribute("seatMap");
      String ctx = request.getContextPath();
      String menuAction = Boolean.TRUE.equals(session.getAttribute("isAdmin")) ? "adminMenu" : "menu";
    %>

    <% if (reservations == null || reservations.isEmpty()) { %>
      <p style="color:#757575;">有効な予約はありません。</p>
    <% } else { %>
      <table class="table">
        <thead>
          <tr>
            <th>予約ID</th><th>予約日</th><th>行先</th><th>座席</th><th>メモ</th><th>操作</th>
          </tr>
        </thead>
        <tbody>
          <% for (Reservation r : reservations) {
               Seat seat = r.getSeatId() > 0 && seatMap != null ? seatMap.get(r.getSeatId()) : null;
               String seatLabel = seat != null
                   ? seat.getBaseName() + " " + seat.getFName() + " " + seat.getSeatName()
                   : "─";
          %>
          <tr>
            <td><%= r.getReserveId() %></td>
            <td><%= r.getReserveDate() %></td>
            <td><span class="badge badge-green"><%= r.getGyosaki() %></span></td>
            <td><%= seatLabel %></td>
            <td><%= r.getMemo() != null ? r.getMemo() : "" %></td>
            <td>
              <!-- キャンセルボタン：POSTで送信（論理削除） -->
              <button type="submit" form="cancelForm<%= r.getReserveId() %>" class="btn btn-danger btn-sm">
                キャンセル
              </button>
            </td>
          </tr>
          <% } %>
        </tbody>
      </table>

      <!-- キャンセル用フォーム（テーブル外に配置） -->
      <% for (Reservation r : reservations) { %>
      <form id="cancelForm<%= r.getReserveId() %>" action="<%= ctx %>/ControlServlet" method="post" style="display:none;">
        <input type="hidden" name="action"    value="doCancel">
        <input type="hidden" name="reserveId" value="<%= r.getReserveId() %>">
      </form>
      <% } %>
    <% } %>

    <div style="margin-top:20px;">
      <a href="<%= ctx %>/ControlServlet?action=<%= menuAction %>" class="btn btn-secondary">&larr; メニューへ戻る</a>
    </div>
  </div>
</div>
</body>
</html>
