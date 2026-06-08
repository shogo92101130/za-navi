<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, entity.Seat" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>座席予約 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<%@ include file="common/header.jspf" %>
<%
  String stage    = (String) request.getAttribute("stage");
  if (stage == null) stage = "base";
  String selBase  = (String) request.getAttribute("selBase");
  String selFloor = (String) request.getAttribute("selFloor");
  String selArea  = (String) request.getAttribute("selArea");
  String officeImage = (String) request.getAttribute("officeImage");
  String ctx      = request.getContextPath();
  String menuAction = Boolean.TRUE.equals(session.getAttribute("isAdmin")) ? "adminMenu" : "menu";

  // stage → 番号マッピング（ステージバー用）
  int stageNum = "base".equals(stage) ? 1 : "floor".equals(stage) ? 2 : "area".equals(stage) ? 3 : 4;
%>

<div class="container">
  <div class="card">
    <h2>座席予約</h2>

    <!-- ステージバー -->
    <div class="stage-bar">
      <div class="stage-step <%= stageNum >= 1 ? (stageNum > 1 ? "done" : "active") : "" %>">① 拠点</div>
      <div class="stage-step <%= stageNum >= 2 ? (stageNum > 2 ? "done" : "active") : "" %>">② フロア</div>
      <div class="stage-step <%= stageNum >= 3 ? (stageNum > 3 ? "done" : "active") : "" %>">③ エリア</div>
      <div class="stage-step <%= stageNum >= 4 ? "active" : "" %>">④ 座席</div>
    </div>

    <!-- パンくず -->
    <div class="breadcrumb">
      <a href="<%= ctx %>/ControlServlet?action=reserve">拠点選択</a>
      <% if (selBase != null) { %>
        <span class="sep">&rsaquo;</span>
        <a href="<%= ctx %>/ControlServlet?action=reserve&stage=floor&base=<%= java.net.URLEncoder.encode(selBase,"UTF-8") %>"><%= selBase %></a>
      <% } %>
      <% if (selFloor != null && selBase != null) { %>
        <span class="sep">&rsaquo;</span>
        <a href="<%= ctx %>/ControlServlet?action=reserve&stage=area&base=<%= java.net.URLEncoder.encode(selBase,"UTF-8") %>&floor=<%= java.net.URLEncoder.encode(selFloor,"UTF-8") %>"><%= selFloor %></a>
      <% } %>
      <% if (selArea != null && selFloor != null) { %>
        <span class="sep">&rsaquo;</span>
        <span><%= selArea %></span>
      <% } %>
    </div>

    <!-- ═══════════ 拠点ステージ ═══════════ -->
    <% if ("base".equals(stage)) { %>
      <p style="color:#757575; margin-bottom:16px;">予約したい拠点を選んでください。</p>
      <div class="occ-grid">
      <%
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.getAttribute("items");
        if (items != null) for (Map<String, Object> item : items) {
          String name = (String) item.get("name");
          int rate    = (Integer) item.get("rate");
          int total   = (Integer) item.get("total");
          Object usedObj = item.get("used");
          long used = usedObj instanceof Long ? (Long)usedObj : ((Integer)usedObj).longValue();
          String barClass = rate < 50 ? "" : rate < 80 ? "med" : rate < 100 ? "high" : "full";
          String encoded  = java.net.URLEncoder.encode(name, "UTF-8");
      %>
        <a class="occ-card"
           href="<%= ctx %>/ControlServlet?action=reserve&stage=floor&base=<%= encoded %>">
          <div class="occ-name"><%= name %></div>
          <div class="occ-bar-bg">
            <div class="occ-bar <%= barClass %>" style="width:<%= rate %>%;"></div>
          </div>
          <div class="occ-pct"><%= rate %><span style="font-size:14px;">%</span></div>
          <!-- 分母：その拠点の全席（全フロア・全エリア合計）が分母 -->
          <div class="occ-label"><%= used %> / <%= total %> 席 使用中</div>
        </a>
      <% } %>
      </div>

    <!-- ═══════════ フロアステージ ═══════════ -->
    <% } else if ("floor".equals(stage)) { %>
      <p style="color:#757575; margin-bottom:16px;">フロアを選んでください。</p>
      <div class="occ-grid">
      <%
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.getAttribute("items");
        if (items != null) for (Map<String, Object> item : items) {
          String name = (String) item.get("name");
          int rate    = (Integer) item.get("rate");
          int total   = (Integer) item.get("total");
          Object usedObj = item.get("used");
          long used = usedObj instanceof Long ? (Long)usedObj : ((Integer)usedObj).longValue();
          String barClass = rate < 50 ? "" : rate < 80 ? "med" : rate < 100 ? "high" : "full";
          String encodedBase  = java.net.URLEncoder.encode(selBase, "UTF-8");
          String encodedFloor = java.net.URLEncoder.encode(name, "UTF-8");
      %>
        <a class="occ-card"
           href="<%= ctx %>/ControlServlet?action=reserve&stage=area&base=<%= encodedBase %>&floor=<%= encodedFloor %>">
          <div class="occ-name"><%= name %></div>
          <div class="occ-bar-bg">
            <div class="occ-bar <%= barClass %>" style="width:<%= rate %>%;"></div>
          </div>
          <div class="occ-pct"><%= rate %><span style="font-size:14px;">%</span></div>
          <!-- 分母：そのフロアの全席（全エリア合計）が分母 -->
          <div class="occ-label"><%= used %> / <%= total %> 席 使用中</div>
        </a>
      <% } %>
      </div>

    <!-- ═══════════ エリアステージ ═══════════ -->
    <% } else if ("area".equals(stage)) { %>
      <p style="color:#757575; margin-bottom:16px;">エリアを選んでください。</p>

      <!-- フロアマップ画像（どのエリアがどこにあるか確認できるように） -->
      <% if (officeImage != null) { %>
      <div style="margin-bottom:16px;">
        <img src="<%= ctx %>/images/<%= officeImage %>" alt="<%= selBase %><%= selFloor %>のフロアマップ"
             style="max-width:100%; border:1px solid #ECEFF1; border-radius:8px;">
      </div>
      <% } %>

      <div class="occ-grid">
      <%
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.getAttribute("items");
        if (items != null) for (Map<String, Object> item : items) {
          String name = (String) item.get("name");
          int rate    = (Integer) item.get("rate");
          int total   = (Integer) item.get("total");
          Object usedObj = item.get("used");
          long used = usedObj instanceof Long ? (Long)usedObj : ((Integer)usedObj).longValue();
          String barClass = rate < 50 ? "" : rate < 80 ? "med" : rate < 100 ? "high" : "full";
          String encBase  = java.net.URLEncoder.encode(selBase, "UTF-8");
          String encFloor = java.net.URLEncoder.encode(selFloor, "UTF-8");
          String encArea  = java.net.URLEncoder.encode(name, "UTF-8");
      %>
        <a class="occ-card"
           href="<%= ctx %>/ControlServlet?action=reserve&stage=seat&base=<%= encBase %>&floor=<%= encFloor %>&area=<%= encArea %>">
          <div class="occ-name"><%= name %></div>
          <div class="occ-bar-bg">
            <div class="occ-bar <%= barClass %>" style="width:<%= rate %>%;"></div>
          </div>
          <div class="occ-pct"><%= rate %><span style="font-size:14px;">%</span></div>
          <!-- 分母：そのエリアの席のみ -->
          <div class="occ-label"><%= used %> / <%= total %> 席 使用中</div>
        </a>
      <% } %>
      </div>

    <!-- ═══════════ 座席ステージ ═══════════ -->
    <% } else if ("seat".equals(stage)) {
         List<String> dates       = (List<String>) request.getAttribute("dates");
         String selectedDate      = (String) request.getAttribute("selectedDate");
         List<Seat> areaSeats     = (List<Seat>) request.getAttribute("areaSeats");
         Map<Integer,String> usage= (Map<Integer,String>) request.getAttribute("seatUsage");
         Map<Integer,String> userNameMap = (Map<Integer,String>) request.getAttribute("seatUserNameMap");
         Map<Integer,String> userDeptMap = (Map<Integer,String>) request.getAttribute("seatUserDeptMap");
         int areaRate  = request.getAttribute("areaRate") != null ? (Integer) request.getAttribute("areaRate") : 0;
         Object usedObj2 = request.getAttribute("areaUsed");
         long areaUsed = usedObj2 instanceof Long ? (Long)usedObj2 : (usedObj2 != null ? ((Integer)usedObj2).longValue() : 0);
         int areaTotal = request.getAttribute("areaTotal") != null ? (Integer) request.getAttribute("areaTotal") : 0;
         String encBase  = selBase  != null ? java.net.URLEncoder.encode(selBase, "UTF-8") : "";
         String encFloor = selFloor != null ? java.net.URLEncoder.encode(selFloor, "UTF-8") : "";
         String encArea  = selArea  != null ? java.net.URLEncoder.encode(selArea, "UTF-8") : "";
    %>

      <!-- ① 日付プルダウン（今日から2週間先まで：15日分） -->
      <form method="get" action="<%= ctx %>/ControlServlet" style="margin-bottom:20px;">
        <input type="hidden" name="action" value="reserve">
        <input type="hidden" name="stage"  value="seat">
        <input type="hidden" name="base"   value="<%= selBase %>">
        <input type="hidden" name="floor"  value="<%= selFloor %>">
        <input type="hidden" name="area"   value="<%= selArea %>">
        <div style="display:flex; gap:10px; align-items:flex-end; flex-wrap:wrap;">
          <div class="form-group" style="margin:0; flex:1; min-width:180px;">
            <label>予約日を選択してください（今日から2週間先まで）</label>
            <select id="dateSelect" name="date" onchange="submitReserveDateForm(this)">
              <% if (dates != null) for (String d : dates) { %>
                <option value="<%= d %>" <%= d.equals(selectedDate) ? "selected" : "" %>><%= d %></option>
              <% } %>
            </select>
          </div>
        </div>
      </form>

      <!-- ② エリア埋有率（選択日） -->
      <div style="margin-bottom:20px;">
        <% String barClass2 = areaRate < 50 ? "" : areaRate < 80 ? "med" : areaRate < 100 ? "high" : "full"; %>
        <div style="display:inline-block; min-width:220px;">
          <div style="font-size:13px; font-weight:700; color:#757575; margin-bottom:6px;">
            <%= selectedDate %> の埋有率（<%= selArea %>）
          </div>
          <div class="occ-bar-bg">
            <div class="occ-bar <%= barClass2 %>" style="width:<%= areaRate %>%;"></div>
          </div>
          <!-- 分母：そのエリアの席のみ -->
          <div style="margin-top:4px; font-size:13px;">
            <span class="occ-pct" style="font-size:20px;"><%= areaRate %>%</span>
            <span class="occ-label"> （<%= areaUsed %> / <%= areaTotal %> 席使用中）</span>
          </div>
        </div>
      </div>

      <!-- ③ オフィスレイアウト画像（エリア選択画面と同じフロアマップを再掲し、座席の場所を確認できるように） -->
      <% if (officeImage != null) { %>
      <div style="margin-bottom:16px;">
        <img src="<%= ctx %>/images/<%= officeImage %>" alt="<%= selBase %><%= selFloor %>のフロアマップ"
             style="max-width:100%; border:1px solid #ECEFF1; border-radius:8px;">
      </div>
      <% } else { %>
      <div class="layout-placeholder">
        オフィスレイアウト画像<br>（images フォルダに画像を追加して差し替え）
      </div>
      <% } %>

      <!-- ④ 座席ボタン（緑=空き、赤=使用中） -->
      <p style="font-size:13px; font-weight:700; margin-bottom:8px;">座席を選択してください</p>
      <div class="summary-row">
        <span class="badge badge-green">■ 空き席</span>
        <span class="badge badge-red">■ 使用中</span>
      </div>

      <!-- 空き席ボタンを押すと、下のメモを添えて予約する -->
      <form method="post" action="<%= ctx %>/ControlServlet">
        <input type="hidden" name="action" value="doReserve">
        <input type="hidden" name="date"   value="<%= selectedDate %>">

        <div class="form-group">
          <label for="memo">メモ（任意）</label>
          <textarea id="memo" name="memo" rows="2" placeholder="例：午後から外出予定 など" style="width:100%; max-width:480px;"></textarea>
        </div>

        <div class="seat-grid">
        <% if (areaSeats != null) for (Seat s : areaSeats) {
             boolean occupied = usage != null && usage.containsKey(s.getSeatId());
             String uname = (occupied && userNameMap != null && userNameMap.containsKey(s.getSeatId()))
                            ? userNameMap.get(s.getSeatId()) : "";
             String udept = (occupied && userDeptMap != null && userDeptMap.containsKey(s.getSeatId()))
                            ? userDeptMap.get(s.getSeatId()) : "";
             String hoverInfo = (udept != null && !udept.isEmpty()) ? (udept + " " + uname) : uname;
        %>
          <% if (!occupied) { %>
            <!-- 緑ボタン：押すと予約完了 -->
            <button type="submit" name="seatId" value="<%= s.getSeatId() %>" class="seat-btn available">
              <span class="seat-id"><%= s.getSeatName() %></span>
              <span class="seat-user">空き</span>
            </button>
          <% } else { %>
            <!-- 赤ボタン：使用中（クリック不可）。誰が使っているか分かるよう、
                 氏名（部署名）を表示しつつ、カーソルを合わせると title 属性で
                 「部署名 氏名」がツールチップ表示される -->
            <button type="button" class="seat-btn occupied" disabled title="<%= hoverInfo %>">
              <span class="seat-id"><%= s.getSeatName() %></span>
              <span class="seat-user"><%= uname %><% if (udept != null && !udept.isEmpty()) { %><span style="font-size:11px; opacity:.8;">（<%= udept %>）</span><% } %></span>
            </button>
          <% } %>
        <% } %>
        </div>
      </form>

      <script>
        // 日付プルダウンを変更すると画面が再読み込みされてメモが消えてしまうため、
        // 切り替え前の入力内容を一時保存しておき、再読み込み後に書き戻す。
        // ※ select.form.submit() はブラウザ標準のメソッド呼び出しで "submit" イベントが
        //   発火しないため、addEventListener('submit', ...) では保存できない。
        //   そのため、保存してから送信する関数を onchange から直接呼び出す形にしている。
        var RESERVE_MEMO_KEY = 'reserveMemoDraft';

        function submitReserveDateForm(sel) {
          var memo = document.getElementById('memo');
          if (memo) sessionStorage.setItem(RESERVE_MEMO_KEY, memo.value);
          sel.form.submit();
        }

        (function() {
          var memo = document.getElementById('memo');
          if (!memo) return;
          var saved = sessionStorage.getItem(RESERVE_MEMO_KEY);
          if (saved !== null) {
            memo.value = saved;
            sessionStorage.removeItem(RESERVE_MEMO_KEY);
          }
        })();
      </script>

    <% } %>

    <div style="margin-top:24px;">
      <a href="<%= ctx %>/ControlServlet?action=<%= menuAction %>" class="btn btn-secondary">&larr; メニューへ戻る</a>
    </div>
  </div>
</div>
</body>
</html>
