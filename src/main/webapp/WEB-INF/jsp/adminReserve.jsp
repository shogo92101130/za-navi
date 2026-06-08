<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*, entity.Account, entity.Seat, dao.SeatsDAO" %>
<!DOCTYPE html>
<html lang="ja">
<head>
<meta charset="UTF-8">
<title>代理予約 - 座・Navi</title>
<link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
<style>
  .mode-card { border:2px solid #CFD8DC; border-radius:8px; padding:16px; cursor:pointer; transition:.15s; }
  .mode-card.active { border-color:var(--teal); background:#E0F2F1; }
  .step-section { margin-top:20px; padding-top:20px; border-top:1px solid #ECEFF1; }
</style>
</head>
<body>
<%@ include file="common/headerAdmin.jspf" %>

<div class="container">
  <div class="card">
    <h2>代理予約</h2>

    <% if (request.getAttribute("errorMsg") != null) { %>
    <div class="alert alert-danger"><%= request.getAttribute("errorMsg") %></div>
    <% } %>

    <%
      @SuppressWarnings("unchecked")
      List<Map<String,Object>> accountRows = (List<Map<String,Object>>) request.getAttribute("accountRows");
      List<String> bases     = (List<String>) request.getAttribute("bases");
      List<String> floors    = (List<String>) request.getAttribute("floors");
      List<String> areas     = (List<String>) request.getAttribute("areas");
      List<String> dates     = (List<String>) request.getAttribute("dates");
      List<Seat> areaSeats   = (List<Seat>) request.getAttribute("areaSeats");
      Map<Integer,String> seatUsage = (Map<Integer,String>) request.getAttribute("seatUsage");
      Map<Integer,String> seatUserNameMap = (Map<Integer,String>) request.getAttribute("seatUserNameMap");
      Map<Integer,String> seatUserDeptMap = (Map<Integer,String>) request.getAttribute("seatUserDeptMap");
      String selBase   = (String) request.getAttribute("selBase");
      String selFloor  = (String) request.getAttribute("selFloor");
      String selArea   = (String) request.getAttribute("selArea");
      String selDate   = (String) request.getAttribute("selectedDate");
      String ctx = request.getContextPath();
    %>

    <!-- 予約タイプ選択 -->
    <p style="font-weight:700; margin-bottom:12px;">① 予約タイプを選択</p>
    <div style="display:flex; gap:14px; margin-bottom:20px;">
      <div class="mode-card" id="modeCardIndividual" onclick="setMode('individual')" style="flex:1;">
        <strong>個人の代理予約（1席）</strong>
        <p style="font-size:12px; color:#757575; margin-top:6px;">対象社員1名を選び、1席を予約します</p>
      </div>
      <div class="mode-card" id="modeCardMeeting" onclick="setMode('meeting')" style="flex:1;">
        <strong>会議用（複数席まとめて予約）</strong>
        <p style="font-size:12px; color:#757575; margin-top:6px;">複数の参加者と座席をまとめて予約します</p>
      </div>
    </div>

    <form action="<%= ctx %>/ControlServlet" method="post" id="adminReserveForm">
      <input type="hidden" name="action" value="doAdminReserve">
      <input type="hidden" name="mode"   value="individual" id="modeInput">

      <%
        @SuppressWarnings("unchecked")
        List<String> deptNames = (List<String>) request.getAttribute("deptNames");
      %>

      <!-- ② 対象社員を検索して追加（modeによって切り替え） -->
      <div class="step-section" id="sectionIndividual">
        <p style="font-weight:700; margin-bottom:10px;">② 対象社員を検索して追加（個人・1名）</p>
        <div class="filter-row">
          <div class="form-group">
            <label>部署で絞り込み（任意）</label>
            <select id="searchDeptIndividual">
              <option value="">すべての部署</option>
              <% if (deptNames != null) for (String d : deptNames) { %>
              <option value="<%= d %>"><%= d %></option>
              <% } %>
            </select>
          </div>
          <div class="form-group">
            <label>氏名で検索（任意）</label>
            <input type="text" id="searchNameIndividual" placeholder="例: 山田">
          </div>
          <div>
            <button type="button" class="btn btn-outline" onclick="searchAccounts('individual')">検索</button>
          </div>
        </div>
        <div id="searchResultIndividual" class="check-group" style="margin-top:10px;">
          <p style="color:#9E9E9E; font-size:13px;">部署または氏名で検索すると、ここに候補が表示されます。</p>
        </div>

        <p style="font-weight:700; margin:18px 0 8px;">選択中の対象社員</p>
        <div id="selectedIndividual" class="check-group">
          <p style="color:#9E9E9E; font-size:13px;">まだ誰も追加されていません。検索して「追加」を押してください。</p>
        </div>
        <div id="hiddenIndividual"></div>
      </div>

      <div class="step-section" id="sectionMeeting" style="display:none;">
        <p style="font-weight:700; margin-bottom:10px;">② 参加者を検索して追加（複数可）</p>
        <div class="filter-row">
          <div class="form-group">
            <label>部署で絞り込み（任意）</label>
            <select id="searchDeptMeeting">
              <option value="">すべての部署</option>
              <% if (deptNames != null) for (String d : deptNames) { %>
              <option value="<%= d %>"><%= d %></option>
              <% } %>
            </select>
          </div>
          <div class="form-group">
            <label>氏名で検索（任意）</label>
            <input type="text" id="searchNameMeeting" placeholder="例: 佐藤">
          </div>
          <div>
            <button type="button" class="btn btn-outline" onclick="searchAccounts('meeting')">検索</button>
          </div>
        </div>
        <div id="searchResultMeeting" class="check-group" style="margin-top:10px;">
          <p style="color:#9E9E9E; font-size:13px;">部署または氏名で検索すると、ここに候補が表示されます。</p>
        </div>

        <p style="font-weight:700; margin:18px 0 8px;">選択中の参加者</p>
        <div id="selectedMeeting" class="check-group">
          <p style="color:#9E9E9E; font-size:13px;">まだ誰も追加されていません。検索して「追加」を押してください。</p>
        </div>
        <div id="hiddenMeeting"></div>
      </div>

      <!-- ③ 日付・拠点・フロア・エリア選択 -->
      <div class="step-section">
        <p style="font-weight:700; margin-bottom:10px;">③ 日付と場所を選択</p>
        <div class="filter-row">
          <div class="form-group">
            <label>予約日</label>
            <select name="date">
              <% if (dates != null) for (String d : dates) { %>
              <option value="<%= d %>" <%= d.equals(selDate) ? "selected" : "" %>><%= d %></option>
              <% } %>
            </select>
          </div>
          <div class="form-group">
            <label>拠点</label>
            <select name="base" onchange="reloadWith('base', this.value)">
              <option value="">-- 選択 --</option>
              <% if (bases != null) for (String b : bases) { %>
              <option value="<%= b %>" <%= b.equals(selBase) ? "selected" : "" %>><%= b %></option>
              <% } %>
            </select>
          </div>
          <div class="form-group">
            <label>フロア</label>
            <select name="floor" onchange="reloadWith('floor', this.value)">
              <option value="">-- 選択 --</option>
              <% if (floors != null) for (String f : floors) { %>
              <option value="<%= f %>" <%= f.equals(selFloor) ? "selected" : "" %>><%= f %></option>
              <% } %>
            </select>
          </div>
          <div class="form-group">
            <label>エリア</label>
            <select name="area" onchange="reloadWith('area', this.value)">
              <option value="">-- 選択 --</option>
              <% if (areas != null) for (String a : areas) { %>
              <option value="<%= a %>" <%= a.equals(selArea) ? "selected" : "" %>><%= a %></option>
              <% } %>
            </select>
          </div>
        </div>

        <!-- 拠点を選ぶと、そのオフィスのフロアマップ画像を表示する -->
        <%
          String officeImage = (selBase != null && !selBase.isEmpty())
                  ? new SeatsDAO().getOfficeImage(selBase) : null;
        %>
        <% if (officeImage != null) { %>
        <div style="margin-top:14px;">
          <img src="<%= ctx %>/images/<%= officeImage %>" alt="<%= selBase %>のフロアマップ"
               style="max-width:100%; border:1px solid #ECEFF1; border-radius:8px;">
        </div>
        <% } %>
      </div>

      <!-- ④ 座席選択 -->
      <% if (areaSeats != null && !areaSeats.isEmpty()) { %>
      <div class="step-section">
        <p style="font-weight:700; margin-bottom:10px;">④ 座席を選択してください（個人は1席、会議用は複数選択可）</p>
        <div class="summary-row">
          <span class="badge badge-green">■ 空き（クリックで選択）</span>
          <span class="badge badge-red">■ 使用中</span>
          <span class="badge" style="background:#E3F2FD; color:#1565C0;">■ 選択中</span>
        </div>
        <div class="seat-grid">
        <% for (Seat s : areaSeats) {
             boolean occupied = seatUsage != null && seatUsage.containsKey(s.getSeatId());
             String uname = (occupied && seatUserNameMap != null && seatUserNameMap.containsKey(s.getSeatId()))
                            ? seatUserNameMap.get(s.getSeatId()) : "";
             String udept = (occupied && seatUserDeptMap != null && seatUserDeptMap.containsKey(s.getSeatId()))
                            ? seatUserDeptMap.get(s.getSeatId()) : "";
             String hoverInfo = (udept != null && !udept.isEmpty()) ? (udept + " " + uname) : uname;
        %>
          <% if (!occupied) { %>
          <label style="margin:0; cursor:pointer;">
            <input type="checkbox" name="seatIds" value="<%= s.getSeatId() %>"
                   style="display:none;" onchange="toggleSeat(this)">
            <div class="seat-btn available" id="seatBtn<%= s.getSeatId() %>">
              <span class="seat-id"><%= s.getSeatName() %></span>
              <span class="seat-user">空き</span>
            </div>
          </label>
          <% } else { %>
          <!-- カーソルを合わせると title 属性で「部署名 氏名」がツールチップ表示される -->
          <div class="seat-btn occupied" style="cursor:not-allowed;" title="<%= hoverInfo %>">
            <span class="seat-id"><%= s.getSeatName() %></span>
            <span class="seat-user"><%= uname %><% if (udept != null && !udept.isEmpty()) { %><span style="font-size:11px; opacity:.8;">（<%= udept %>）</span><% } %></span>
          </div>
          <% } %>
        <% } %>
        </div>

        <div class="form-group" style="margin-top:16px; max-width:480px;">
          <label for="adminMemo">メモ（任意・この予約全件に共通で設定されます）</label>
          <textarea id="adminMemo" name="memo" rows="2" placeholder="例：来客対応のため など" style="width:100%;"></textarea>
        </div>

        <div style="margin-top:20px;">
          <button type="submit" class="btn btn-primary btn-lg">
            選択した席で予約する
          </button>
        </div>
      </div>
      <% } else if (selArea != null && !selArea.isEmpty()) { %>
      <div class="step-section">
        <div class="alert alert-info">エリアを選択すると座席が表示されます。</div>
      </div>
      <% } %>

    </form>

    <div style="margin-top:20px;">
      <a href="<%= ctx %>/ControlServlet?action=adminMenu" class="btn btn-secondary">&larr; 管理メニューへ戻る</a>
    </div>
  </div>
</div>

<script>
// 社員一覧をJSの配列として用意しておき、検索・追加をブラウザ側だけで行えるようにする
var ACCOUNTS = [
<% if (accountRows != null) {
     boolean first = true;
     for (Map<String,Object> row : accountRows) {
       if (!first) { %>,<% }
       first = false;
%>  { userId: "<%= row.get("userId") %>", name: "<%= row.get("name") %>", bName: "<%= row.get("bName") %>" }
<%   }
   } %>
];

var selectedIndividual = null;   // 個人モード：1名のみ
var selectedMeeting = {};        // 会議モード：userId をキーにした複数名

function findAccount(userId) {
  for (var i = 0; i < ACCOUNTS.length; i++) {
    if (ACCOUNTS[i].userId === userId) return ACCOUNTS[i];
  }
  return null;
}

function searchAccounts(mode) {
  var cap   = (mode === 'individual') ? 'Individual' : 'Meeting';
  var dept  = document.getElementById('searchDept' + cap).value;
  var name  = document.getElementById('searchName' + cap).value.trim();
  var box   = document.getElementById('searchResult' + cap);
  box.innerHTML = '';

  var hits = ACCOUNTS.filter(function(acc) {
    if (dept !== '' && acc.bName !== dept) return false;
    if (name !== '' && acc.name.indexOf(name) === -1) return false;
    return true;
  });

  if (hits.length === 0) {
    box.innerHTML = '<p style="color:#9E9E9E; font-size:13px;">該当する社員が見つかりませんでした。条件を変えて検索してください。</p>';
    return;
  }

  hits.forEach(function(acc) {
    var row = document.createElement('div');
    row.className = 'account-row';
    row.style.display = 'flex';
    row.style.alignItems = 'center';
    row.style.gap = '12px';

    var label = document.createElement('span');
    label.textContent = acc.bName + 'の' + acc.name + '（' + acc.userId + '）';
    row.appendChild(label);

    var addBtn = document.createElement('button');
    addBtn.type = 'button';
    addBtn.className = 'btn btn-sm btn-primary';
    addBtn.textContent = '追加';
    addBtn.onclick = (function(uid) { return function() { addPerson(mode, uid); }; })(acc.userId);
    row.appendChild(addBtn);

    box.appendChild(row);
  });
}

function addPerson(mode, userId) {
  var acc = findAccount(userId);
  if (!acc) return;

  if (mode === 'individual') {
    selectedIndividual = acc; // 個人は1名だけなので、後から選び直すと置き換わる
  } else if (!selectedMeeting[userId]) {
    selectedMeeting[userId] = acc; // 同じ人を二重に追加しない
  }
  renderSelected(mode);
}

function removePerson(mode, userId) {
  if (mode === 'individual') {
    selectedIndividual = null;
  } else {
    delete selectedMeeting[userId];
    // 参加者を減らして選択座席数の上限を下回ったら、超過分の座席選択を解除する
    var limit = Object.keys(selectedMeeting).length;
    var checked = document.querySelectorAll('input[name="seatIds"]:checked');
    for (var i = checked.length - 1; i >= limit; i--) {
      checked[i].checked = false;
      var btn = document.getElementById('seatBtn' + checked[i].value);
      if (btn) { btn.classList.remove('selected'); btn.classList.add('available'); }
    }
  }
  renderSelected(mode);
}

function renderSelected(mode) {
  var cap  = (mode === 'individual') ? 'Individual' : 'Meeting';
  var box  = document.getElementById('selected' + cap);
  var list = (mode === 'individual')
      ? (selectedIndividual ? [selectedIndividual] : [])
      : Object.keys(selectedMeeting).map(function(uid) { return selectedMeeting[uid]; });

  box.innerHTML = '';
  if (list.length === 0) {
    box.innerHTML = '<p style="color:#9E9E9E; font-size:13px;">まだ誰も追加されていません。検索して「追加」を押してください。</p>';
  } else {
    list.forEach(function(acc) {
      var chip = document.createElement('span');
      chip.className = 'badge';
      chip.style.background = '#E0F2F1';
      chip.style.color = '#00695C';
      chip.textContent = acc.bName + 'の' + acc.name;

      var removeBtn = document.createElement('a');
      removeBtn.href = 'javascript:void(0)';
      removeBtn.style.marginLeft = '8px';
      removeBtn.style.color = '#C62828';
      removeBtn.style.fontWeight = '700';
      removeBtn.textContent = '×';
      removeBtn.onclick = (function(uid) { return function() { removePerson(mode, uid); }; })(acc.userId);

      chip.appendChild(removeBtn);
      box.appendChild(chip);
    });
  }

  // フォーム送信用の隠しinputを作り直す
  var hidden = document.getElementById('hidden' + cap);
  hidden.innerHTML = '';
  if (mode === 'individual') {
    if (selectedIndividual) {
      hidden.innerHTML = '<input type="hidden" name="targetUserId" value="' + selectedIndividual.userId + '">';
    }
  } else {
    list.forEach(function(acc) {
      hidden.innerHTML += '<input type="hidden" name="targetUserIds" value="' + acc.userId + '">';
    });
  }
}

renderSelected('individual');
renderSelected('meeting');

function setMode(mode) {
  document.getElementById('modeInput').value = mode;
  document.getElementById('modeCardIndividual').classList.toggle('active', mode === 'individual');
  document.getElementById('modeCardMeeting').classList.toggle('active', mode === 'meeting');
  document.getElementById('sectionIndividual').style.display = mode === 'individual' ? '' : 'none';
  document.getElementById('sectionMeeting').style.display    = mode === 'meeting'    ? '' : 'none';
  // モード切り替え時は座席選択をリセット（個人⇄会議で選択数の意味が変わるため）
  document.querySelectorAll('input[name="seatIds"]:checked').forEach(function(cb) {
    cb.checked = false;
    var btn = document.getElementById('seatBtn' + cb.value);
    if (btn) { btn.classList.remove('selected'); btn.classList.add('available'); }
  });
}
setMode('individual'); // 初期表示

function toggleSeat(cb) {
  var modeVal = document.getElementById('modeInput').value;

  // 個人の代理予約は1名・1席のため、他の座席が選択済みなら解除する
  if (modeVal === 'individual' && cb.checked) {
    document.querySelectorAll('input[name="seatIds"]:checked').forEach(function(other) {
      if (other !== cb) {
        other.checked = false;
        var otherBtn = document.getElementById('seatBtn' + other.value);
        if (otherBtn) { otherBtn.classList.remove('selected'); otherBtn.classList.add('available'); }
      }
    });
  }

  // 会議用は「選んだ参加者の人数分」までしか座席を選べないようにする
  if (modeVal === 'meeting' && cb.checked) {
    var participantCount = Object.keys(selectedMeeting).length;
    var checkedCount = document.querySelectorAll('input[name="seatIds"]:checked').length;
    if (participantCount === 0) {
      cb.checked = false;
      alert('先に参加者を追加してください。');
      return;
    }
    if (checkedCount > participantCount) {
      cb.checked = false;
      alert('参加者数（' + participantCount + '名）分までしか座席を選択できません。');
      return;
    }
  }

  var btn = document.getElementById('seatBtn' + cb.value);
  if (cb.checked) {
    btn.classList.remove('available'); btn.classList.add('selected');
  } else {
    btn.classList.remove('selected'); btn.classList.add('available');
  }
}

// 拠点・フロア・エリアのプルダウンを変えるたびにページ上部へ戻されてしまうのを防ぐため、
// 遷移前のスクロール位置を覚えておき、再読み込み後（画像読み込み完了後）に同じ位置へ戻す
var ADMIN_RESERVE_SCROLL_KEY = 'adminReserveScrollY';
(function() {
  var saved = sessionStorage.getItem(ADMIN_RESERVE_SCROLL_KEY);
  if (saved === null) return;
  sessionStorage.removeItem(ADMIN_RESERVE_SCROLL_KEY);
  window.addEventListener('load', function() {
    window.scrollTo(0, parseInt(saved, 10));
  });
})();

function reloadWith(paramName, value) {
  sessionStorage.setItem(ADMIN_RESERVE_SCROLL_KEY, String(window.scrollY));
  var base  = document.querySelector('[name=base]')  ? document.querySelector('[name=base]').value  : '';
  var floor = document.querySelector('[name=floor]') ? document.querySelector('[name=floor]').value : '';
  var area  = document.querySelector('[name=area]')  ? document.querySelector('[name=area]').value  : '';
  var date  = document.querySelector('[name=date]')  ? document.querySelector('[name=date]').value  : '';
  if (paramName === 'base')  { base = value; floor = ''; area = ''; }
  if (paramName === 'floor') { floor = value; area = ''; }
  if (paramName === 'area')  { area = value; }

  // 拠点名・フロア名・エリア名は日本語のため、GETのクエリ文字列で送ると
  // サーバー側の文字コード設定によっては文字化けし、フロア/エリアの選択肢が
  // 出てこなくなることがある。POSTで送ればサーバー側のUTF-8変換が確実に効くため、
  // 隠しフォームを組み立ててPOST送信する。
  var form = document.createElement('form');
  form.method = 'post';
  form.action = '<%= request.getContextPath() %>/ControlServlet';
  [['action', 'adminReserve'], ['base', base], ['floor', floor], ['area', area], ['date', date]]
    .forEach(function(pair) {
      var input = document.createElement('input');
      input.type = 'hidden';
      input.name = pair[0];
      input.value = pair[1];
      form.appendChild(input);
    });
  document.body.appendChild(form);
  form.submit();
}
</script>
</body>
</html>
