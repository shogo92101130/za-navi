<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, entity.Account, entity.Location, entity.Seat" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>部門検索 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<%
  String menuAction = Boolean.TRUE.equals(session.getAttribute("isAdmin")) ? "adminMenu" : "menu";
%>

<div class="container">
  <div class="card">
    <h2>部門検索</h2>

    <% if (request.getAttribute("errorMsg") != null) { %>
    <div class="alert alert-danger"><%= request.getAttribute("errorMsg") %></div>
    <% } %>

    <%
      List<Location> departments = (List<Location>) request.getAttribute("departments");
      String selectedBId = (String) request.getAttribute("selectedBId");
      String selectedUserIdKeyword = (String) request.getAttribute("selectedUserIdKeyword");
      String resultLabel = (String) request.getAttribute("resultLabel");
    %>

    <form action="<%= request.getContextPath() %>/ControlServlet" method="get">
      <input type="hidden" name="action" value="doDepartmentSearch">
      <div style="display:flex; gap:10px; align-items:flex-end; flex-wrap:wrap;">
        <div class="form-group" style="flex:1; min-width:180px; margin:0;">
          <label>部門で絞り込み（任意）</label>
          <select name="bId">
            <option value="">-- 指定なし --</option>
            <% if (departments != null) for (Location dept : departments) { %>
            <option value="<%= dept.getBId() %>" <%= (selectedBId != null && selectedBId.equals(dept.getBId())) ? "selected" : "" %>>
              <%= dept.getBName() %>
            </option>
            <% } %>
          </select>
        </div>
        <div class="form-group" style="flex:1; min-width:160px; margin:0;">
          <label>社員IDで絞り込み（任意）</label>
          <input type="text" name="userIdKeyword" placeholder="例: 1001"
                 value="<%= selectedUserIdKeyword != null ? selectedUserIdKeyword : "" %>">
        </div>
        <button type="submit" class="btn btn-primary">
          検索
        </button>
      </div>
      <p style="font-size:12px; color:#757575; margin-top:6px;">
        部門・社員IDのどちらか一方、または両方を指定して検索できます。
      </p>
    </form>

    <% List<Map<String, Object>> results = (List<Map<String, Object>>) request.getAttribute("results"); %>
    <% if (results != null) { %>
    <div style="margin-top:20px;">
      <p style="font-size:13px; color:#757575; margin-bottom:8px;">
        <%= resultLabel %>：<%= results.size() %> 名
      </p>
      <table class="table">
        <thead>
          <tr><th>ユーザーID</th><th>氏名</th><th>本日の行先</th><th>本日の予約座席</th></tr>
        </thead>
        <tbody>
          <% for (Map<String, Object> row : results) {
               Account a = (Account) row.get("account");
               Seat seat = (Seat) row.get("seat");
          %>
          <tr>
            <td><%= a.getUserId() %></td>
            <td><%= a.getName() %></td>
            <td><span class="badge badge-green"><%= row.get("todayStatus") %></span></td>
            <td>
              <% if (seat != null) { %>
                <%= seat.getBaseName() %>　<%= seat.getFName() %> <%= seat.getAreaName() %> <%= seat.getSeatName() %>
              <% } else { %>
                <span style="color:var(--text-muted);">-</span>
              <% } %>
            </td>
          </tr>
          <% } %>
        </tbody>
      </table>
    </div>
    <% } %>

    <div style="margin-top:20px;">
      <a href="<%= request.getContextPath() %>/ControlServlet?action=<%= menuAction %>" class="btn btn-secondary">&larr; メニューへ戻る</a>
    </div>
  </div>
</div>
</body>
</html>
