<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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

    <form action="<%= request.getContextPath() %>/ControlServlet" method="post">
      <input type="hidden" name="action" value="doRemoteWork">

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
