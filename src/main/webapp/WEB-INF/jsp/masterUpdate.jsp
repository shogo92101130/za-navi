<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, entity.Seat" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>マスタ更新 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/headerAdmin.jspf" %>

<div class="container">
  <div class="card">
    <h2>座席マスタ更新</h2>

    <!-- 完了メッセージ -->
    <% if (request.getAttribute("message") != null) { %>
    <div class="alert alert-success">&#10003; <%= request.getAttribute("message") %></div>
    <% } %>

    <%
      List<Seat> seats    = (List<Seat>) request.getAttribute("seats");
      List<String> bases  = (List<String>) request.getAttribute("bases");
      Seat editSeat       = (Seat) request.getAttribute("editSeat");
      String selectedBase = (String) request.getAttribute("selectedBase");
      String officeImage  = (String) request.getAttribute("officeImage");
      String ctx = request.getContextPath();
    %>

    <p style="font-size:12px; color:#757575; margin-bottom:10px;">
      ① オフィスを選ぶと、フロアマップ画像とそのオフィスの座席だけを表示できます（見やすさ・確認用の絞り込みです）。
    </p>

    <!-- ① オフィスを選んで表示・絞り込み -->
    <form action="<%= ctx %>/ControlServlet" method="get" style="margin-bottom:14px;">
      <input type="hidden" name="action" value="masterUpdate">
      <div class="filter-row">
        <div class="form-group">
          <label>① オフィスを選択</label>
          <select name="filterBase" onchange="submitMasterFilterForm(this)">
            <option value="">-- すべてのオフィスを表示 --</option>
            <% if (bases != null) for (String b : bases) { %>
            <option value="<%= b %>" <%= b.equals(selectedBase) ? "selected" : "" %>><%= b %></option>
            <% } %>
          </select>
        </div>
      </div>
    </form>

    <!-- 選んだオフィスのフロアマップ画像（画像を見ながら座席を確認・追加できるように） -->
    <% if (officeImage != null) { %>
    <div style="margin-bottom:16px;">
      <img src="<%= ctx %>/images/<%= officeImage %>" alt="<%= selectedBase %>のフロアマップ"
           style="max-width:100%; border:1px solid #ECEFF1; border-radius:8px;">
    </div>
    <% } else if (selectedBase != null && !selectedBase.isEmpty()) { %>
    <p style="color:#9E9E9E; font-size:12px; margin-bottom:16px;">
      ※「<%= selectedBase %>」のフロアマップ画像はまだ登録されていません。画像を用意したら
      <code>SeatsDAO</code> の <code>OFFICE_IMAGES</code> に追加すると、ここにも表示されます。
    </p>
    <% } %>

    <!-- 座席一覧（①で選んだオフィスのみ表示。未選択ならすべて表示） -->
    <h3 style="margin-bottom:8px; color:var(--teal-dark);">
      <%= (selectedBase != null && !selectedBase.isEmpty()) ? (selectedBase + "の座席一覧") : "座席一覧（全オフィス）" %>
      <span style="font-size:12px; color:#9E9E9E; font-weight:400;">（<%= seats != null ? seats.size() : 0 %> 席）</span>
    </h3>
    <!--
      件数が増えてもページ全体が縦に伸び続けないよう、一覧をカード内の
      スクロール領域（最大高さ固定 + ヘッダー追従）に収める。
      「①オフィスを選択」で絞り込めば、その拠点だけの短い一覧として確認できる。
    -->
    <div style="max-height:480px; overflow-y:auto; border:1px solid #ECEFF1; border-radius:8px; margin-bottom:24px;">
    <table class="table" style="margin-bottom:0;">
      <thead>
        <tr>
          <th style="position:sticky; top:0; background:#fff; z-index:1;">ID</th>
          <th style="position:sticky; top:0; background:#fff; z-index:1;">拠点</th>
          <th style="position:sticky; top:0; background:#fff; z-index:1;">フロア</th>
          <th style="position:sticky; top:0; background:#fff; z-index:1;">エリア</th>
          <th style="position:sticky; top:0; background:#fff; z-index:1;">席名</th>
          <th style="position:sticky; top:0; background:#fff; z-index:1; width:140px;">操作</th>
        </tr>
      </thead>
      <tbody>
        <%
          String rowBase = null;
          if (seats != null) for (Seat s : seats) {
            boolean newBase = !s.getBaseName().equals(rowBase);
            rowBase = s.getBaseName();
            if (newBase && (selectedBase == null || selectedBase.isEmpty())) {
        %>
        <!-- 全オフィス表示時は拠点の切れ目が分かるように見出し行を挟む -->
        <tr style="background:#F5F5F5;">
          <td colspan="6" style="font-weight:700; color:var(--teal-dark);"><%= rowBase %></td>
        </tr>
        <% } %>
        <tr>
          <td><%= s.getSeatId() %></td>
          <td><%= s.getBaseName() %></td>
          <td><%= s.getFName() %></td>
          <td><%= s.getAreaName() %></td>
          <td><%= s.getSeatName() %></td>
          <td>
            <!-- 更新リンク → 編集フォームにIDを渡す -->
            <a href="<%= ctx %>/ControlServlet?action=masterEdit&seatId=<%= s.getSeatId() %>&filterBase=<%= selectedBase != null ? selectedBase : "" %>"
               class="btn btn-primary btn-sm">更新</a>
            <!-- 削除ボタン（POSTフォームを外部に配置し form= で紐づけ） -->
            <button type="submit" form="delForm<%= s.getSeatId() %>" class="btn btn-danger btn-sm"
                    onclick="return confirm('座席ID <%= s.getSeatId() %> を削除しますか？')">削除</button>
          </td>
        </tr>
        <!-- 削除用フォーム（テーブル外） -->
        <% } %>
      </tbody>
    </table>
    </div>

    <!-- 削除フォーム（テーブル外に配置） -->
    <% if (seats != null) for (Seat s : seats) { %>
    <form id="delForm<%= s.getSeatId() %>" action="<%= ctx %>/ControlServlet" method="post" style="display:none;">
      <input type="hidden" name="action"  value="doMasterDelete">
      <input type="hidden" name="seatId"  value="<%= s.getSeatId() %>">
    </form>
    <% } %>

    <!-- 更新フォーム（masterEditで対象座席が設定された場合に表示） -->
    <% if (editSeat != null) { %>
    <div class="card" style="background:#E3F2FD; border:2px solid #42A5F5; margin-bottom:20px;">
      <h2 style="color:#1565C0;">座席 ID <%= editSeat.getSeatId() %> を更新</h2>
      <form action="<%= ctx %>/ControlServlet" method="post">
        <input type="hidden" name="action"  value="doMasterUpdate">
        <input type="hidden" name="seatId"  value="<%= editSeat.getSeatId() %>">
        <div class="filter-row">
          <div class="form-group">
            <label>拠点</label>
            <input type="text" name="baseName" value="<%= editSeat.getBaseName() %>" required>
          </div>
          <div class="form-group">
            <label>フロア</label>
            <input type="text" name="fName" value="<%= editSeat.getFName() %>" required>
          </div>
          <div class="form-group">
            <label>エリア</label>
            <input type="text" name="areaName" value="<%= editSeat.getAreaName() %>" required>
          </div>
          <div class="form-group">
            <label>席名</label>
            <input type="text" name="seatName" value="<%= editSeat.getSeatName() %>" required>
          </div>
          <div>
            <button type="submit" class="btn btn-primary">更新する</button>
            <a href="<%= ctx %>/ControlServlet?action=masterUpdate&filterBase=<%= selectedBase != null ? selectedBase : "" %>" class="btn btn-secondary">キャンセル</a>
          </div>
        </div>
      </form>
    </div>
    <% } %>

    <!-- 新規追加フォーム -->
    <div class="card" style="background:#F9FBE7; border:2px solid #AFB42B;">
      <h2 style="color:#558B2F;">② 座席を追加する</h2>
      <p style="font-size:12px; color:#757575; margin-bottom:10px;">
        既存のオフィスに座席を増やすときは「拠点」で一覧から選び、まだ無いオフィスを増やしたいときは
        「＋ 新しいオフィスを追加する」を選んでオフィス名を入力してください。
        ※オフィスという独立したマスタ行はなく、座席を1件追加した時点でそのオフィス名が新しく登場する仕組みです（③参照）。
      </p>
      <form action="<%= ctx %>/ControlServlet" method="post" id="addSeatForm">
        <input type="hidden" name="action" value="doMasterAdd">
        <div class="filter-row">
          <div class="form-group">
            <label>拠点</label>
            <select name="baseName" id="baseSelect" onchange="onBaseSelectChange()">
              <% if (bases != null) for (String b : bases) { %>
              <option value="<%= b %>" <%= b.equals(selectedBase) ? "selected" : "" %>><%= b %></option>
              <% } %>
              <option value="__NEW__">＋ 新しいオフィスを追加する</option>
            </select>
          </div>
          <div class="form-group" id="newBaseGroup" style="display:none;">
            <label>新しいオフィス名</label>
            <input type="text" name="newBaseName" id="newBaseName" placeholder="例: 渋谷オフィス">
          </div>
          <div class="form-group">
            <label>フロア</label>
            <input type="text" name="fName" placeholder="例: 3F" required>
          </div>
          <div class="form-group">
            <label>エリア</label>
            <input type="text" name="areaName" placeholder="例: Cエリア" required>
          </div>
          <div class="form-group">
            <label>席名</label>
            <input type="text" name="seatName" placeholder="例: C-01" required>
          </div>
          <div>
            <button type="submit" class="btn btn-green">追加する</button>
          </div>
        </div>
      </form>
      <p style="font-size:11px; color:#9E9E9E; margin-top:10px;">
        ③ 新しいオフィスを増やしたときは、フロアマップ画像も用意して
        <code>SeatsDAO</code> の <code>OFFICE_IMAGES</code> に登録すると、この画面・座席利用状況・代理予約の画面にも
        画像が表示されるようになります（手順は <code>SeatsDAO.java</code> 冒頭のコメントを参照）。
      </p>
    </div>

    <script>
      // 「拠点」で「＋ 新しいオフィスを追加する」を選んだときだけ、
      // 新しいオフィス名の入力欄を表示／必須にする（vanilla JS、フレームワーク不使用）
      function onBaseSelectChange() {
        var sel = document.getElementById('baseSelect');
        var grp = document.getElementById('newBaseGroup');
        var isNew = (sel.value === '__NEW__');
        grp.style.display = isNew ? '' : 'none';
        document.getElementById('newBaseName').required = isNew;
      }
      onBaseSelectChange();
    </script>

    <div style="margin-top:10px;">
      <a href="<%= ctx %>/ControlServlet?action=adminMenu" class="btn btn-secondary">&larr; 管理メニューへ戻る</a>
    </div>
  </div>
</div>

<script>
  // 更新・追加・削除・絞り込みのたびにページ上部へ戻されてしまうのを防ぐため、
  // 送信前のスクロール位置を覚えておき、再読み込み後に同じ位置へ戻す。
  //
  // つまずきポイント1: <select onchange="this.form.submit()"> のように
  //   フォームの submit() メソッドを直接呼び出すと "submit" イベントは発火しない
  //   （addEventListener('submit', ...) では拾えない）。そのため絞り込み用セレクトは
  //   保存してから送信する関数を直接 onchange から呼ぶ形にしている。
  // つまずきポイント2: ページ読み込み直後（スクリプト実行時点）に scrollTo すると、
  //   画像（フロアマップ）の読み込みで後からレイアウトが変わり、
  //   結局トップへ戻ってしまう。window の load イベント（画像読み込み完了後）まで待つ。
  var MASTER_SCROLL_KEY = 'masterUpdateScrollY';

  function submitMasterFilterForm(sel) {
    sessionStorage.setItem(MASTER_SCROLL_KEY, String(window.scrollY));
    sel.form.submit();
  }

  document.querySelectorAll('form').forEach(function(f) {
    f.addEventListener('submit', function() {
      sessionStorage.setItem(MASTER_SCROLL_KEY, String(window.scrollY));
    });
  });

  (function() {
    var saved = sessionStorage.getItem(MASTER_SCROLL_KEY);
    if (saved === null) return;
    sessionStorage.removeItem(MASTER_SCROLL_KEY);
    window.addEventListener('load', function() {
      window.scrollTo(0, parseInt(saved, 10));
    });
  })();
</script>
</body>
</html>
