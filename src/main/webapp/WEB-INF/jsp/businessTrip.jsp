<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>出張先入力 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/header.jspf" %>

<div class="container">
  <div class="card" style="max-width:500px; margin:0 auto;">
    <h2>出張先を入力してください</h2>

    <form action="<%= request.getContextPath() %>/ControlServlet" method="post">
      <input type="hidden" name="action" value="doBusinessTrip">

      <div class="form-group">
        <label>出張先</label>
        <input type="text" name="destination" placeholder="例: 大阪支社" required autofocus>
      </div>

      <div class="form-group">
        <label>メモ（任意）</label>
        <textarea name="memo" rows="2" placeholder="例：午後から客先訪問のため帰社は夕方 など" style="width:100%;"></textarea>
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
