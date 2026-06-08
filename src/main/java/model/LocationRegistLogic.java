package model;

import dao.ReservationsDAO;
import dao.SeatsDAO;
import entity.Reservation;
import entity.Seat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 行先登録（出社・在宅・出張のいずれかを指定日の行先として登録する）。
 *
 * 「在宅」「出張」はそれぞれ専用のメモ入力画面を経由して登録する
 * （出張先とメモが混在して分かりにくくならないよう、表示時にも区別できる形式で保存する）。
 * 対象日は座席予約と合わせて「今日から2週間先まで」選択できる。
 * 「出社」だけは特別で、ここでは登録せず座席予約画面（ReserveLogic）へリダイレクトし、
 * 座席が決まった時点で「出社＋座席」をまとめて1件登録する
 * （そうしないと「出社（座席なし）」と「出社（座席あり）」の2件ができてしまうため）。
 */
public class LocationRegistLogic implements Logic {

    /** 選択可能日数（今日を含めて2週間先まで＝15日分） */
    private static final int SELECTABLE_DAYS = 15;

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String action = req.getParameter("action");

        switch (action) {
            case "locationRegist":
                return "/WEB-INF/jsp/locationRegist.jsp";

            case "doLocationRegist":
                return doRegist(req, res);

            case "businessTrip":
                return showDateForm(req, "/WEB-INF/jsp/businessTrip.jsp");

            case "doBusinessTrip":
                return doBusinessTrip(req, res);

            case "remoteWork":
                return showDateForm(req, "/WEB-INF/jsp/remoteWork.jsp");

            case "doRemoteWork":
                return doRemoteWork(req, res);

            default:
                return "/WEB-INF/jsp/locationRegist.jsp";
        }
    }

    /** 今日から2週間先まで（座席予約の選択可能期間と合わせる） */
    private List<String> buildSelectableDates() {
        List<String> dates = new ArrayList<>();
        LocalDate d = LocalDate.now();
        for (int i = 0; i < SELECTABLE_DAYS; i++) {
            dates.add(d.plusDays(i).toString());
        }
        return dates;
    }

    /**
     * 在宅・出張の対象日選択画面を表示する。
     * 「すでに別の予定（出社の座席予約や、別の在宅・出張）が入っている日」が
     * 選択肢の中で一目で分かるよう、日付ごとの予定状況をあわせて渡す
     * （二重登録に気づかず選んでエラーになるのを防ぐため）。
     */
    private String showDateForm(HttpServletRequest req, String jspPath) {
        HttpSession session = req.getSession(false);
        String userId = (String) session.getAttribute("userId");
        List<String> dates = buildSelectableDates();

        req.setAttribute("dates", dates);
        req.setAttribute("dateStatusLabels", buildDateStatusLabels(userId, dates));
        return jspPath;
    }

    /** 選択可能な各日付について、その人がすでに持っている有効な予定をラベル化して返す（予定がない日は含めない） */
    private Map<String, String> buildDateStatusLabels(String userId, List<String> dates) {
        Map<String, String> labels = new LinkedHashMap<>();
        ReservationsDAO resDAO = new ReservationsDAO();
        SeatsDAO seatsDAO = new SeatsDAO();

        for (String date : dates) {
            Reservation r = resDAO.findActiveByUserAndDate(userId, date);
            if (r == null) continue;

            String label;
            if ("出社".equals(r.getGyosaki()) && r.getSeatId() > 0) {
                Seat seat = seatsDAO.findById(r.getSeatId());
                label = seat != null
                        ? "予定あり：出社（" + seat.getBaseName() + " " + seat.getSeatName() + "）"
                        : "予定あり：出社";
            } else {
                label = "予定あり：" + r.getGyosaki();
            }
            labels.put(date, label);
        }
        return labels;
    }

    /** 画面から渡された日付が選択可能期間内であればそのまま使い、それ以外は今日にする */
    private String resolveDate(String dateParam) {
        if (dateParam == null || dateParam.isEmpty()) return LocalDate.now().toString();
        return buildSelectableDates().contains(dateParam) ? dateParam : LocalDate.now().toString();
    }

    private String doRegist(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String gyosaki = req.getParameter("gyosaki"); // 出社 / 在宅 / 出張
        if ("出張".equals(gyosaki)) {
            return showDateForm(req, "/WEB-INF/jsp/businessTrip.jsp");
        }
        if ("在宅".equals(gyosaki)) {
            return showDateForm(req, "/WEB-INF/jsp/remoteWork.jsp");
        }
        // 出社：ここでは登録せず座席予約画面（ReserveLogic）へリダイレクトし、
        // 座席が決まった時点で「出社＋座席」をまとめて1件登録する
        // （そうしないと「出社（座席なし）」と「出社（座席あり）」の2件ができてしまうため）
        res.sendRedirect(req.getContextPath() + "/ControlServlet?action=reserve");
        return null;
    }

    private String doRemoteWork(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String date = resolveDate(req.getParameter("date"));
        String memo = req.getParameter("memo");
        return saveAndGoMenu(req, res, date, "在宅", memo != null ? memo : "");
    }

    private String doBusinessTrip(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String date        = resolveDate(req.getParameter("date"));
        String destination = req.getParameter("destination");
        String memo        = req.getParameter("memo");
        // 出張先とメモを混ぜると後で見たときにどちらか分からなくなるため、
        // 「出張先:○○/メモ:○○」の形式で保存し、表示時にも一目で区別できるようにする
        StringBuilder fullMemo = new StringBuilder("出張先:").append(destination != null ? destination : "");
        if (memo != null && !memo.isEmpty()) {
            fullMemo.append("/メモ:").append(memo);
        }
        return saveAndGoMenu(req, res, date, "出張", fullMemo.toString());
    }

    /** 登録を試みて、失敗した場合はエラーメッセージを返す（成功時はnull） */
    private String saveReservation(HttpServletRequest req, String date, String gyosaki, String memo) {
        HttpSession session = req.getSession(false);
        String userId = (String) session.getAttribute("userId");

        // すでにその日の有効な予約（出社・在宅・出張のいずれか）を持っている場合はDB制約違反になるため、
        // ここで先にチェックして分かりやすいメッセージを返す
        // （DB側のUNIQUE制約 uq_reservations_user_active が1人1日1件に制限している）
        if (new ReservationsDAO().findActiveByUserAndDate(userId, date) != null) {
            return date + " はすでに予約（出社・在宅・出張のいずれか）が登録されています。"
                    + "先に「予約確認」から既存の予約を取り消してください。";
        }

        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setRegisterId(userId);
        r.setReserveDate(date);
        r.setGyosaki(gyosaki);
        r.setMemo(memo);
        r.setSeatId(0);
        r.setCancelFlag(0);

        new ReservationsDAO().add(r);

        // メニューへ戻る前に本日の予約を更新
        session.setAttribute("todayReservations",
                new ReservationsDAO().findTodayByUserId(userId));
        return null;
    }

    private String saveAndGoMenu(HttpServletRequest req, HttpServletResponse res, String date, String gyosaki, String memo) throws Exception {
        String error = saveReservation(req, date, gyosaki, memo);
        if (error != null) {
            req.setAttribute("errorMsg", error);
            return "/WEB-INF/jsp/reserveNG.jsp";
        }

        HttpSession session = req.getSession(false);
        // リダイレクト後の画面で表示するため、リクエスト属性ではなくセッションに積んでおく
        session.setAttribute("successMsg", date + " の" + gyosaki + "登録が完了しました。");

        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        res.sendRedirect(req.getContextPath() + "/ControlServlet?action=" + (isAdmin ? "adminMenu" : "menu"));
        return null;
    }
}
