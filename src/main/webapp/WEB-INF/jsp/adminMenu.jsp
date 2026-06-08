<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.Map, entity.Reservation, dao.SeatsDAO, entity.Seat" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>管理者メニュー - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/headerAdmin.jspf" %>

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
    <h2>本日の予約状況（管理者ビュー）</h2>
    <%
      List<Reservation> todayRes = (List<Reservation>) session.getAttribute("todayReservations");
      if (todayRes == null) todayRes = new java.util.ArrayList<>();
      SeatsDAO seatsDAO = new SeatsDAO();
    %>
    <% if (todayRes.isEmpty()) { %>
      <p style="color:#757575;">本日の予約はありません。</p>
    <% } else { %>
      <table class="table">
        <thead><tr><th>日付</th><th>行先</th><th>座席</th><th>メモ</th></tr></thead>
        <tbody>
        <% for (Reservation r : todayRes) {
             Seat seat = r.getSeatId() > 0 ? seatsDAO.findById(r.getSeatId()) : null;
             String seatLabel = seat != null ? seat.getBaseName()+" "+seat.getFName()+" "+seat.getSeatName() : "─";
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
    List<Map<String, Object>> deptSeatList =
        (List<Map<String, Object>>) request.getAttribute("deptSeatList");
  %>
  <% if (request.getAttribute("deptMemberCount") != null) { %>
  <div class="card">
    <h2>同じ部署の本日の出社状況</h2>
    <div class="summary-row">
      <span class="badge badge-green">本日出社：<%= request.getAttribute("deptOfficeCount") %> 名</span>
      <span class="badge badge-gray">部署人数：<%= request.getAttribute("deptMemberCount") %> 名</span>
    </div>
    <% if (deptSeatList != null && !deptSeatList.isEmpty()) { %>
    <div class="summary-row" style="margin-top:10px; gap:8px;">
      <% for (Map<String, Object> row : deptSeatList) {
           Seat seat = (Seat) row.get("seat");
           String seatLabel = seat != null
               ? seat.getFName() + " " + seat.getAreaName() + " " + seat.getSeatName()
               : "座席未定";
      %>
      <span class="badge badge-gray"><%= row.get("name") %>　<span style="color:#00695C;">(<%= seatLabel %>)</span></span>
      <% } %>
    </div>
    <% } %>
  </div>
  <% } %>

  <!-- 2週間分の予約状況（管理者のみ）：行をクリックすると氏名・座席の詳細が開く -->
  <%
    List<Map<String, Object>> twoWeekSummary = (List<Map<String, Object>>) request.getAttribute("twoWeekSummary");
  %>
  <% if (twoWeekSummary != null) { %>
  <div class="card">
    <h2>今後2週間の予約状況</h2>
    <p style="font-size:12px; color:#757575; margin-bottom:8px;">日付をクリックすると、その日に出社する人の氏名と座席が確認できます。</p>
    <% for (Map<String, Object> row : twoWeekSummary) {
         @SuppressWarnings("unchecked")
         List<Map<String, Object>> people = (List<Map<String, Object>>) row.get("people");
    %>
    <details class="day-summary">
      <summary>
        <span class="day-date"><%= row.get("date") %></span>
        <span class="badge badge-green"><%= row.get("officeCount") %> 名出社</span>
        <span class="badge badge-gray"><%= row.get("seatCount") %> 席予約済み</span>
        <span class="caret">&#9662;</span>
      </summary>
      <div class="day-detail">
        <% if (people == null || people.isEmpty()) { %>
        <p style="color:#9E9E9E; font-size:13px; margin:0;">座席が確定している予約はまだありません。</p>
        <% } else { %>
        <div class="summary-row" style="gap:8px;">
          <% for (Map<String, Object> p : people) {
               Seat seat = (Seat) p.get("seat");
               String seatLabel = seat != null
                   ? seat.getBaseName() + " " + seat.getFName() + " " + seat.getAreaName() + " " + seat.getSeatName()
                   : "─";
          %>
          <span class="badge badge-gray"><%= p.get("name") %>　<span style="color:#00695C;">(<%= seatLabel %>)</span></span>
          <% } %>
        </div>
        <% } %>
      </div>
    </details>
    <% } %>
  </div>
  <% } %>

  <!-- 一般機能 -->
  <div class="card">
    <h2>一般機能</h2>
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

  <!-- 管理機能 -->
  <div class="card">
    <h2>管理機能</h2>
    <div class="menu-grid">
      <a href="<%= request.getContextPath() %>/ControlServlet?action=seatStatus" class="menu-btn">
        <img class="ico-lg" src="<%= request.getContextPath() %>/images/ico_status.png" alt="座席利用状況">座席利用状況
      </a>
      <a href="<%= request.getContextPath() %>/ControlServlet?action=adminReserve" class="menu-btn">
        <img class="ico-lg" src="<%= request.getContextPath() %>/images/ico_proxy.png" alt="代理予約">代理予約
      </a>
      <a href="<%= request.getContextPath() %>/ControlServlet?action=masterUpdate" class="menu-btn">
        <img class="ico-lg" src="<%= request.getContextPath() %>/images/ico_master.png" alt="マスタ更新">マスタ更新
      </a>
    </div>
  </div>

</div>
</body>
</html>
