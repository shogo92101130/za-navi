<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, entity.Reservation, dao.SeatsDAO, entity.Seat" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>メニュー - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<div class="container">

  <%
    // 行先登録などリダイレクト後に表示するメッセージはセッションに積まれているため、
    // 表示したら一度だけ出るように消しておく（フラッシュメッセージ）
    Object successMsg = session.getAttribute("successMsg");
    if (successMsg != null) session.removeAttribute("successMsg");
  %>
  <% if (successMsg != null) { %>
  <div class="alert alert-success"><%= successMsg %></div>
  <% } %>

  <!-- 本日の予約 -->
  <div class="card">
    <h2>本日の予約状況</h2>
    <%
      List<Reservation> todayRes = (List<Reservation>) session.getAttribute("todayReservations");
      if (todayRes == null) todayRes = new java.util.ArrayList<>();
      SeatsDAO seatsDAO = new SeatsDAO();
    %>
    <% if (todayRes.isEmpty()) { %>
      <p style="color:#757575;">本日の予約はまだありません。</p>
    <% } else { %>
      <table class="table">
        <thead>
          <tr><th>日付</th><th>行先</th><th>座席</th><th>メモ</th></tr>
        </thead>
        <tbody>
          <% for (Reservation r : todayRes) {
               Seat seat = (r.getSeatId() > 0) ? seatsDAO.findById(r.getSeatId()) : null;
               String seatLabel = seat != null
                   ? seat.getBaseName() + " " + seat.getFName() + " " + seat.getSeatName()
                   : "─";
          %>
          <tr>
            <td><%= r.getReserveDate() %></td>
            <td><span class="badge badge-green"><%= r.getGyosaki() %></span></td>
            <td><%= seatLabel %></td>
            <td><%= r.getMemo() != null ? r.getMemo() : "" %></td>
          </tr>
          <% } %>
        </tbody>
      </table>
    <% } %>
  </div>

  <!-- 同じ部署の出社状況 -->
  <%
    @SuppressWarnings("unchecked")
    List<java.util.Map<String, Object>> deptSeatList =
        (List<java.util.Map<String, Object>>) request.getAttribute("deptSeatList");
  %>
  <% if (request.getAttribute("deptMemberCount") != null) { %>
  <div class="card">
    <h2>同じ部署の本日の出社状況</h2>
    <div class="summary-row">
      <span class="badge badge-green">本日出社：<%= request.getAttribute("deptOfficeCount") %> 名</span>
      <span class="badge badge-gray">部署人数：<%= request.getAttribute("deptMemberCount") %> 名</span>
    </div>
    <% if (deptSeatList != null && !deptSeatList.isEmpty()) { %>
    <table class="table" style="margin-top:12px;">
      <thead><tr><th>氏名</th><th>本日の状況</th><th>座席</th></tr></thead>
      <tbody>
        <% for (java.util.Map<String, Object> row : deptSeatList) {
             Seat seat = (Seat) row.get("seat");
             String seatLabel = seat != null
                 ? seat.getBaseName() + " " + seat.getFName() + " " + seat.getAreaName() + " " + seat.getSeatName()
                 : "─";
        %>
        <tr>
          <td><%= row.get("name") %></td>
          <td><span class="badge badge-green"><%= row.get("todayStatus") %></span></td>
          <td><%= seatLabel %></td>
        </tr>
        <% } %>
      </tbody>
    </table>
    <% } %>
  </div>
  <% } %>

  <!-- 機能メニュー -->
  <div class="card">
    <h2>機能メニュー</h2>
    <div class="menu-grid">
      <a href="<%= request.getContextPath() %>/ControlServlet?action=locationRegist" class="menu-btn">
        <img class="ico-lg" src="<%= request.getContextPath() %>/images/ico_location.png" alt="行先登録">行先登録
      </a>
      <a href="<%= request.getContextPath() %>/ControlServlet?action=reserve" class="menu-btn">
        <img class="ico-lg" src="<%= request.getContextPath() %>/images/ico_seat.png" alt="座席予約">座席予約
      </a>
      <a href="<%= request.getContextPath() %>/ControlServlet?action=reserveConfirm" class="menu-btn">
        <img class="ico-lg" src="<%= request.getContextPath() %>/images/ico_confirm.png" alt="予約確認">予約確認
      </a>
      <a href="<%= request.getContextPath() %>/ControlServlet?action=nameSearch" class="menu-btn">
        <img class="ico-lg" src="<%= request.getContextPath() %>/images/ico_search_name.png" alt="氏名検索">氏名検索
      </a>
      <a href="<%= request.getContextPath() %>/ControlServlet?action=departmentSearch" class="menu-btn">
        <img class="ico-lg" src="<%= request.getContextPath() %>/images/ico_search_dept.png" alt="部門検索">部門検索
      </a>
    </div>
  </div>

</div>
</body>
</html>
