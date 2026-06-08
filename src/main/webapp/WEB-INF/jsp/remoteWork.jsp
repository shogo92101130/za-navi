<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>在宅メモ入力 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<div class="container">
  <div class="card" style="max-width:500px; margin:0 auto;">
    <h2>在宅勤務として登録します</h2>
    <p style="color:#757575; margin-bottom:24px;">必要であれば、業務内容などのメモを残せます（任意）。</p>

    <%
      List<String> dates = (List<String>) request.getAttribute("dates");
      Map<String, String> dateStatusLabels = (Map<String, String>) request.getAttribute("dateStatusLabels");
    %>
    <form action="<%= request.getContextPath() %>/ControlServlet" method="post">
      <input type="hidden" name="action" value="doRemoteWork">

      <div class="form-group">
        <label>対象日</label>
        <select name="date" style="width:100%;">
          <% if (dates != null) for (String d : dates) {
               String status = dateStatusLabels != null ? dateStatusLabels.get(d) : null;
          %>
            <option value="<%= d %>"><%= d %><%= status != null ? "　― " + status : "" %></option>
          <% } %>
        </select>
        <p style="color:#757575; font-size:0.85em; margin-top:4px;">
          「予定あり」の日は、すでに出社・在宅・出張のいずれかの予定が登録されています（重複登録するとエラーになります）。
        </p>
      </div>

      <div class="form-group">
        <label>メモ（任意）</label>
        <textarea name="memo" rows="2" placeholder="例：午後から商談対応のため在宅 など" style="width:100%;"></textarea>
      </div>

      <div style="display:flex; gap:10px; margin-top:8px;">
        <button type="submit" class="btn btn-primary" style="flex:1;">
          登録する
        </button>
        <a href="<%= request.getContextPath() %>/ControlServlet?action=locationRegist" class="btn btn-secondary">
          戻る
        </a>
      </div>
    </form>
  </div>
</div>
</body>
</html>
