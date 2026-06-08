<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, entity.Seat, dao.SeatsDAO" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>座席利用状況 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/headerAdmin.jspf" %>

<div class="container">
  <div class="card">
    <h2>座席利用状況確認</h2>

    <%
      List<String> bases  = (List<String>) request.getAttribute("bases");
      List<String> floors = (List<String>) request.getAttribute("floors");
      List<String> areas  = (List<String>) request.getAttribute("areas");
      String filterBase   = (String) request.getAttribute("filterBase");
      String filterFloor  = (String) request.getAttribute("filterFloor");
      String filterArea   = (String) request.getAttribute("filterArea");
      String filterDate   = (String) request.getAttribute("filterDate");
      LinkedHashMap<String, String> dateOptions = (LinkedHashMap<String, String>) request.getAttribute("dateOptions");
      String ctx = request.getContextPath();

      String myDeptName = (String) request.getAttribute("myDeptName");
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> deptTodayList = (List<Map<String, Object>>) request.getAttribute("deptTodayList");
    %>

    <!-- 同じ部署の本日の出社者一覧（自分の部署のメンバーが今日どこにいるかをひと目で確認） -->
    <% if (myDeptName != null) { %>
    <div style="background:#F5F5F5; border-radius:8px; padding:12px 16px; margin-bottom:16px;">
      <strong style="font-size:13px; color:#424242;"><%= myDeptName %>の本日の出社者</strong>
      <% if (deptTodayList == null || deptTodayList.isEmpty()) { %>
      <p style="color:#9E9E9E; font-size:13px; margin:6px 0 0;">本日「出社」の予定があるメンバーはいません。</p>
      <% } else { %>
      <div class="summary-row" style="margin-top:8px; gap:8px;">
        <% for (Map<String, Object> row : deptTodayList) {
             Seat dSeat = (Seat) row.get("seat");
             String dSeatLabel = dSeat != null
                 ? dSeat.getBaseName() + " " + dSeat.getFName() + " " + dSeat.getAreaName() + " " + dSeat.getSeatName()
                 : "座席未定";
        %>
        <span class="badge badge-gray"><%= row.get("name") %>　<span style="color:#00695C;">(<%= dSeatLabel %>)</span></span>
        <% } %>
      </div>
      <% } %>
    </div>
    <% } %>

    <p style="font-size:12px; color:#757575; margin-bottom:10px;">
      「○○オフィスの○○エリア」のように、拠点 → フロア → エリアの順に具体的な場所を選んで確認します。
    </p>

    <!-- 絞り込みフォーム（拠点 → フロア → エリアの順に選択。前の項目を選ぶまで次は選べない） -->
    <form action="<%= ctx %>/ControlServlet" method="get">
      <input type="hidden" name="action" value="doSeatStatus">
      <div class="filter-row">
        <div class="form-group">
          <label>① 拠点</label>
          <select name="filterBase" onchange="this.form.submit()">
            <option value="">-- 拠点を選択 --</option>
            <% if (bases != null) for (String b : bases) { %>
            <option value="<%= b %>" <%= b.equals(filterBase) ? "selected" : "" %>><%= b %></option>
            <% } %>
          </select>
        </div>
        <div class="form-group">
          <label>② フロア</label>
          <select name="filterFloor" onchange="this.form.submit()" <%= (floors == null) ? "disabled" : "" %>>
            <option value="">-- フロアを選択 --</option>
            <% if (floors != null) for (String f : floors) { %>
            <option value="<%= f %>" <%= f.equals(filterFloor) ? "selected" : "" %>><%= f %></option>
            <% } %>
          </select>
        </div>
        <div class="form-group">
          <label>③ エリア</label>
          <select name="filterArea" onchange="this.form.submit()" <%= (areas == null) ? "disabled" : "" %>>
            <option value="">-- エリアを選択 --</option>
            <% if (areas != null) for (String a : areas) { %>
            <option value="<%= a %>" <%= a.equals(filterArea) ? "selected" : "" %>><%= a %></option>
            <% } %>
          </select>
        </div>
        <div class="form-group">
          <label>④ 日付（2週間前〜2週間後まで確認できます）</label>
          <select name="filterDate" onchange="this.form.submit()">
            <% if (dateOptions != null) for (Map.Entry<String, String> e : dateOptions.entrySet()) { %>
            <option value="<%= e.getKey() %>" <%= e.getKey().equals(filterDate) ? "selected" : "" %>><%= e.getValue() %></option>
            <% } %>
          </select>
        </div>
      </div>
    </form>

    <!-- 拠点・フロアごとのフロアマップ画像（選ぶと該当の画像に切り替わる） -->
    <%
      String officeImage = (filterBase != null && !filterBase.isEmpty())
              ? new SeatsDAO().getOfficeImage(filterBase, filterFloor) : null;
    %>
    <% if (officeImage != null) { %>
    <div style="margin:16px 0;">
      <img src="<%= ctx %>/images/<%= officeImage %>" alt="<%= filterBase %>のフロアマップ"
           style="max-width:100%; border:1px solid #ECEFF1; border-radius:8px;">
    </div>
    <% } %>

    <%
      List<Seat> filteredSeats = (List<Seat>) request.getAttribute("filteredSeats");
      Map<Integer, String> seatUsageMap  = (Map<Integer, String>) request.getAttribute("seatUsageMap");
      Map<Integer, String> seatUserNameMap = (Map<Integer, String>) request.getAttribute("seatUserNameMap");
      Map<Integer, String> seatUserDeptMap = (Map<Integer, String>) request.getAttribute("seatUserDeptMap");
      Object usedObj = request.getAttribute("usedCount");
      Object freeObj = request.getAttribute("freeCount");
      long usedCount = usedObj instanceof Long ? (Long)usedObj : (usedObj != null ? ((Integer)usedObj).longValue() : 0);
      long freeCount = freeObj instanceof Long ? (Long)freeObj : (freeObj != null ? ((Integer)freeObj).longValue() : 0);
    %>

    <% if (filterArea != null && !filterArea.isEmpty()) { %>
    <!-- 結果見出し：常に「○○オフィスの○○ ○○エリア」のように具体的な場所を一意に表示する -->
    <h3 style="margin:18px 0 10px; color:#00695C;">
      <%= filterBase %>の<%= filterFloor %> <%= filterArea %>
    </h3>

    <!-- サマリ -->
    <div class="summary-row" style="margin-bottom:16px;">
      <span class="badge badge-gray">対象 <%= filteredSeats.size() %> 席</span>
      <span class="badge badge-red">使用中 <%= usedCount %> 席</span>
      <span class="badge badge-green">空き <%= freeCount %> 席</span>
    </div>

    <!-- 凡例 -->
    <div class="summary-row" style="font-size:12px;">
      <span class="badge badge-green">■ 空き</span>
      <span class="badge badge-red">■ 使用中</span>
    </div>

    <!--
      座席マップ：席のボタンは76×54pxと小さく「氏名（部署名）」を入れると見切れてしまうため、
      ここでは座席名と状態だけを示すミニマップに留め、カーソルを合わせると title 属性で
      「部署名 氏名」をツールチップ表示する（誰がどこにいるかの全体像をひと目で把握する用途）。
      詳しい内訳は、すぐ下の「使用中の座席一覧」で省略なしに確認できる。
    -->
    <div class="seat-grid">
      <% for (Seat s : filteredSeats) {
           boolean occupied = seatUsageMap != null && seatUsageMap.containsKey(s.getSeatId());
           String uname = (seatUserNameMap != null && seatUserNameMap.containsKey(s.getSeatId()))
                          ? seatUserNameMap.get(s.getSeatId()) : "";
           String udept = (seatUserDeptMap != null && seatUserDeptMap.containsKey(s.getSeatId()))
                          ? seatUserDeptMap.get(s.getSeatId()) : "";
           String hoverInfo = occupied ? ((udept != null && !udept.isEmpty()) ? (udept + " " + uname) : uname) : "空き";
      %>
        <div class="seat-btn <%= occupied ? "occupied" : "available" %>" style="cursor:default;" title="<%= hoverInfo %>">
          <span class="seat-id"><%= s.getSeatName() %></span>
          <span class="seat-user"><%= occupied ? "使用中" : "空き" %></span>
        </div>
      <% } %>
    </div>

    <!-- 使用中の座席一覧：誰がどの座席を使っているかを、省略せず一覧で確認できるようにする -->
    <% if (usedCount > 0) { %>
    <h4 style="margin:22px 0 8px; color:#424242; font-size:14px;">使用中の座席一覧（<%= usedCount %> 件）</h4>
    <table class="table" style="margin-bottom:8px;">
      <thead>
        <tr><th style="width:140px;">座席</th><th>部署</th><th>氏名</th></tr>
      </thead>
      <tbody>
        <% for (Seat s : filteredSeats) {
             if (seatUsageMap == null || !seatUsageMap.containsKey(s.getSeatId())) continue;
             String uname = (seatUserNameMap != null && seatUserNameMap.containsKey(s.getSeatId()))
                            ? seatUserNameMap.get(s.getSeatId()) : "";
             String udept = (seatUserDeptMap != null && seatUserDeptMap.containsKey(s.getSeatId()))
                            ? seatUserDeptMap.get(s.getSeatId()) : "";
        %>
        <tr>
          <td><span class="badge badge-red" style="white-space:nowrap;"><%= s.getSeatName() %></span></td>
          <td><%= udept %></td>
          <td><%= uname %></td>
        </tr>
        <% } %>
      </tbody>
    </table>
    <% } %>
    <% } else { %>
    <!-- 拠点・フロア・エリアがすべて選ばれるまでは、案内メッセージのみ表示する -->
    <p style="color:#9E9E9E; padding:24px 0; text-align:center;">
      拠点・フロア・エリアを順番に選択すると、その場所の座席利用状況が表示されます。
    </p>
    <% } %>

    <div style="margin-top:24px;">
      <a href="<%= ctx %>/ControlServlet?action=adminMenu" class="btn btn-secondary">&larr; 管理メニューへ戻る</a>
    </div>
  </div>
</div>
</body>
</html>
