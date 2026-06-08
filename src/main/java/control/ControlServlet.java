package control;

import dao.AccountsDAO;
import dao.ReservationsDAO;
import dao.SeatsDAO;
import entity.Account;
import entity.Reservation;
import entity.Seat;
import model.*;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * Za-Navi 統合フロントコントローラ。
 * すべてのリクエストを /ControlServlet?action=○○ で受け取り、
 * 対応する Logic を呼び出してから JSP へ forward する。
 */
public class ControlServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        process(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        process(req, res);
    }

    private void process(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        res.setCharacterEncoding("UTF-8");

        // 予約状況などセッション依存の動的画面をブラウザにキャッシュさせない
        // （キャッシュされた古いページが表示され、「予約したのに反映されない」ように見えるのを防ぐ）
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        res.setHeader("Pragma", "no-cache");
        res.setDateHeader("Expires", 0);

        String action = req.getParameter("action");
        if (action == null || action.isEmpty()) action = "login";

        // セッションチェック（ログイン不要アクションを除く）
        if (!action.equals("login") && !action.equals("doLogin")) {
            HttpSession session = req.getSession(false);
            if (session == null || session.getAttribute("userId") == null) {
                try {
                    res.sendRedirect(req.getContextPath() + "/ControlServlet?action=login");
                } catch (IOException e) { /* already committed */ }
                return;
            }
        }

        String jspPath = null;
        try {
            jspPath = dispatch(action, req, res);
        } catch (Exception e) {
            req.setAttribute("errorMsg", "システムエラー: " + e.getMessage());
            jspPath = "/WEB-INF/jsp/loginNG.jsp";
        }

        if (jspPath != null && !res.isCommitted()) {
            req.getRequestDispatcher(jspPath).forward(req, res);
        }
    }

    // ─────────────── アクション振り分け ───────────────

    private String dispatch(String action, HttpServletRequest req, HttpServletResponse res)
            throws Exception {

        switch (action) {

            // ── ログイン / ログアウト ──
            case "login":
            case "doLogin":
                return new LoginLogic().execute(req, res);

            case "logout":
                return new LogoutLogic().execute(req, res);

            // ── 一般メニュー ──
            case "menu":
                return showMenu(req, false);

            // ── 管理者メニュー ──
            case "adminMenu":
                return showMenu(req, true);

            // ── 行先登録 ──
            case "locationRegist":
            case "doLocationRegist":
            case "businessTrip":
            case "doBusinessTrip":
            case "remoteWork":
            case "doRemoteWork":
                return new LocationRegistLogic().execute(req, res);

            // ── 座席予約 ──
            case "reserve":
            case "doReserve":
                return new ReserveLogic().execute(req, res);

            // ── 予約確認・キャンセル ──
            case "reserveConfirm":
            case "doCancel":
                return new ReserveConfirmLogic().execute(req, res);

            // ── 氏名検索 ──
            case "nameSearch":
            case "doNameSearch":
                return new NameSearchLogic().execute(req, res);

            // ── 部門検索 ──
            case "departmentSearch":
            case "doDepartmentSearch":
                return new DepartmentSearchLogic().execute(req, res);

            // ── 座席利用状況（管理）──
            case "seatStatus":
            case "doSeatStatus":
                return showSeatStatus(req);

            // ── 代理予約（管理）──
            case "adminReserve":
            case "doAdminReserve":
                return new AdminReserveLogic().execute(req, res);

            // ── マスタ更新（管理）──
            case "masterUpdate":
            case "masterEdit":
            case "doMasterAdd":
            case "doMasterUpdate":
            case "doMasterDelete":
                return new MasterUpdateLogic().execute(req, res);

            default:
                return "/WEB-INF/jsp/login.jsp";
        }
    }

    // ─────────────── メニュー共通 ───────────────

    private String showMenu(HttpServletRequest req, boolean isAdmin) {
        HttpSession session = req.getSession(false);
        String userId = (String) session.getAttribute("userId");

        AccountsDAO accDAO = new AccountsDAO();
        ReservationsDAO resDAO = new ReservationsDAO();

        List<Reservation> todayRes = resDAO.findTodayByUserId(userId);
        session.setAttribute("todayReservations", todayRes);
        req.setAttribute("todayReservations", todayRes);

        // 同じ部署の本日の出社人数・座席一覧（管理者メニューだけに表示する情報なので、管理者のときだけ集計する）
        Account me = accDAO.findById(userId);
        if (isAdmin && me != null) {
            SeatsDAO seatDAO = new SeatsDAO();
            List<Account> deptMembers = accDAO.findByBId(me.getBId());

            List<Map<String, Object>> deptSeatList = new ArrayList<>();
            long officeCount = 0;
            for (Account a : deptMembers) {
                List<Reservation> mRes = resDAO.findTodayByUserId(a.getUserId());

                // 本日の状況（未登録の場合は「未登録」のままにする）
                String todayStatus = "未登録";
                Seat seat = null;
                if (!mRes.isEmpty()) {
                    Reservation r = mRes.get(0);
                    todayStatus = r.getGyosaki();
                    if ("出社".equals(todayStatus)) {
                        officeCount++;
                        seat = r.getSeatId() > 0 ? seatDAO.findById(r.getSeatId()) : null;
                    }
                }

                Map<String, Object> row = new HashMap<>();
                row.put("name", a.getName());
                row.put("todayStatus", todayStatus);
                row.put("seat", seat);
                deptSeatList.add(row);
            }
            req.setAttribute("deptMemberCount", deptMembers.size());
            req.setAttribute("deptOfficeCount", officeCount);
            req.setAttribute("deptSeatList", deptSeatList);
        }

        // 管理者向け：今日から2週間分の予約状況サマリ（人数だけでなく、誰がどの席かも見られるように）
        if (isAdmin) {
            SeatsDAO seatDAO = new SeatsDAO();
            dao.LocationsDAO locDAO2 = new dao.LocationsDAO();

            // 部署一覧は最初に1回だけ取得して bId→bName のマップを作っておく
            // （1人ずつ部署名を問い合わせると、14日分×人数のDB接続が発生し重くなるため）
            Map<String, String> deptNameMap = new HashMap<>();
            for (entity.Location loc : locDAO2.findAll()) {
                deptNameMap.put(loc.getBId(), loc.getBName());
            }

            List<Map<String, Object>> twoWeekSummary = new ArrayList<>();
            for (int i = 0; i < 14; i++) {
                String date = LocalDate.now().plusDays(i).toString();
                List<Reservation> dayRes = resDAO.findByDate(date);
                long officeNum = dayRes.stream().filter(r -> "出社".equals(r.getGyosaki())).count();
                long seatNum   = dayRes.stream().filter(r -> r.getSeatId() > 0).count();

                // 出社・座席が確定している人の「氏名＋部署＋座席」明細（折りたたみ表示用）
                List<Map<String, Object>> people = new ArrayList<>();
                for (Reservation r : dayRes) {
                    if (!"出社".equals(r.getGyosaki()) || r.getSeatId() <= 0) continue;
                    Account acc = accDAO.findById(r.getUserId());
                    Seat seat = seatDAO.findById(r.getSeatId());
                    Map<String, Object> p = new HashMap<>();
                    p.put("name", acc != null ? acc.getName() : r.getUserId());
                    p.put("bName", acc != null ? deptNameMap.getOrDefault(acc.getBId(), "不明") : "");
                    p.put("seat", seat);
                    people.add(p);
                }

                Map<String, Object> row = new HashMap<>();
                row.put("date", date);
                row.put("officeCount", officeNum);
                row.put("seatCount", seatNum);
                row.put("people", people);
                twoWeekSummary.add(row);
            }
            req.setAttribute("twoWeekSummary", twoWeekSummary);
        }

        return isAdmin ? "/WEB-INF/jsp/adminMenu.jsp" : "/WEB-INF/jsp/menu.jsp";
    }

    // ─────────────── 座席利用状況 ───────────────

    private String showSeatStatus(HttpServletRequest req) {
        SeatsDAO seatsDAO  = new SeatsDAO();
        ReservationsDAO resDAO = new ReservationsDAO();
        AccountsDAO accDAO = new AccountsDAO();
        dao.LocationsDAO locDAO = new dao.LocationsDAO();

        String filterBase  = req.getParameter("filterBase");
        String filterFloor = req.getParameter("filterFloor");
        String filterArea  = req.getParameter("filterArea");
        String filterDate  = req.getParameter("filterDate");
        if (filterDate == null || filterDate.isEmpty()) filterDate = LocalDate.now().toString();

        // 座席利用状況の確認は「今日を基準に2週間前〜2週間後」までに限定する
        // （カレンダーで自由に選べてしまうと際限なく過去・未来を確認できてしまうため）
        LocalDate today    = LocalDate.now();
        LocalDate minDate  = today.minusDays(14);
        LocalDate maxDate  = today.plusDays(14);
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(filterDate);
        } catch (Exception e) {
            parsedDate = today;
        }
        if (parsedDate.isBefore(minDate)) parsedDate = minDate;
        if (parsedDate.isAfter(maxDate))  parsedDate = maxDate;
        filterDate = parsedDate.toString();
        req.setAttribute("minDate", minDate.toString());
        req.setAttribute("maxDate", maxDate.toString());

        // 日付はカレンダーUIだと押しづらいとの声があったため、他の項目と同じ
        // セレクトボックス形式（2週間前〜2週間後を一覧）で選べるようにする
        String[] youbi = {"日", "月", "火", "水", "木", "金", "土"};
        LinkedHashMap<String, String> dateOptions = new LinkedHashMap<>();
        for (LocalDate d = minDate; !d.isAfter(maxDate); d = d.plusDays(1)) {
            String label = d.getMonthValue() + "/" + d.getDayOfMonth()
                    + "（" + youbi[d.getDayOfWeek().getValue() % 7] + "）"
                    + (d.equals(today) ? " ※本日" : "");
            dateOptions.put(d.toString(), label);
        }
        req.setAttribute("dateOptions", dateOptions);

        // ドロップダウン用リスト（「すべて」は用意せず、拠点→フロア→エリアの順に選んでもらう）
        req.setAttribute("bases", seatsDAO.getBases());
        boolean baseChosen  = filterBase  != null && !filterBase.isEmpty();
        boolean floorChosen = baseChosen && filterFloor != null && !filterFloor.isEmpty();
        boolean areaChosen  = floorChosen && filterArea != null && !filterArea.isEmpty();

        if (baseChosen)  req.setAttribute("floors", seatsDAO.getFloors(filterBase));
        if (floorChosen) req.setAttribute("areas",  seatsDAO.getAreas(filterBase, filterFloor));

        // 拠点・フロア・エリアがすべて決まったときだけ座席を表示する
        // （「○○オフィスの○○エリア」のように、常に具体的な場所が一意に決まる状態にするため）
        List<Seat> filteredSeats = areaChosen
                ? seatsDAO.filterSeats(filterBase, filterFloor, filterArea)
                : new ArrayList<>();

        // 指定日の利用状況
        Map<Integer, String> seatUsageMap = resDAO.getSeatUsageForDate(filterDate);

        // 使用中ユーザーの「氏名」「部署名」マップ（座席マップ上で氏名の横に部署名も出すため）
        Map<Integer, String> seatUserNameMap = new HashMap<>();
        Map<Integer, String> seatUserDeptMap = new HashMap<>();
        for (Map.Entry<Integer, String> e : seatUsageMap.entrySet()) {
            entity.Account acc = accDAO.findById(e.getValue());
            seatUserNameMap.put(e.getKey(), acc != null ? acc.getName() : e.getValue());
            seatUserDeptMap.put(e.getKey(), acc != null ? locDAO.getBNameById(acc.getBId()) : "");
        }

        long usedCount = filteredSeats.stream()
                .filter(s -> seatUsageMap.containsKey(s.getSeatId())).count();
        long freeCount = filteredSeats.size() - usedCount;

        // ログイン中の管理者と同じ部署で、本日「出社」する人の一覧（氏名・座席）
        // ※座席利用状況の確認画面で「自分の部署のメンバーが今日どこに座っているか」をすぐ把握できるようにするため
        HttpSession statusSession = req.getSession(false);
        String myUserId = (String) statusSession.getAttribute("userId");
        Account me = accDAO.findById(myUserId);
        List<Map<String, Object>> deptTodayList = new ArrayList<>();
        if (me != null) {
            for (Account a : accDAO.findByBId(me.getBId())) {
                List<Reservation> mRes = resDAO.findTodayByUserId(a.getUserId());
                if (mRes.isEmpty() || !"出社".equals(mRes.get(0).getGyosaki())) continue;
                Reservation r = mRes.get(0);
                Seat seat = r.getSeatId() > 0 ? seatsDAO.findById(r.getSeatId()) : null;
                Map<String, Object> row = new HashMap<>();
                row.put("name", a.getName());
                row.put("seat", seat);
                deptTodayList.add(row);
            }
        }

        req.setAttribute("filteredSeats",   filteredSeats);
        req.setAttribute("seatUsageMap",    seatUsageMap);
        req.setAttribute("seatUserNameMap", seatUserNameMap);
        req.setAttribute("seatUserDeptMap", seatUserDeptMap);
        req.setAttribute("usedCount",       usedCount);
        req.setAttribute("freeCount",       freeCount);
        req.setAttribute("filterBase",      filterBase);
        req.setAttribute("filterFloor",     filterFloor);
        req.setAttribute("filterArea",      filterArea);
        req.setAttribute("filterDate",      filterDate);
        req.setAttribute("myDeptName",      me != null ? locDAO.getBNameById(me.getBId()) : null);
        req.setAttribute("deptTodayList",   deptTodayList);

        return "/WEB-INF/jsp/seatStatus.jsp";
    }
}
