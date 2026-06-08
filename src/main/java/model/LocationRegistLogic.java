package model;

import dao.ReservationsDAO;
import entity.Reservation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.time.LocalDate;

/**
 * 行先登録（出社・在宅・出張のいずれかを今日の行先として登録する）。
 *
 * 「在宅」「出張」はそれぞれ専用のメモ入力画面を経由して登録する
 * （出張先とメモが混在して分かりにくくならないよう、表示時にも区別できる形式で保存する）。
 * 「出社」だけは特別で、ここでは登録せず座席予約画面（ReserveLogic）へリダイレクトし、
 * 座席が決まった時点で「出社＋座席」をまとめて1件登録する
 * （そうしないと「出社（座席なし）」と「出社（座席あり）」の2件ができてしまうため）。
 */
public class LocationRegistLogic implements Logic {

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String action = req.getParameter("action");

        switch (action) {
            case "locationRegist":
                return "/WEB-INF/jsp/locationRegist.jsp";

            case "doLocationRegist":
                return doRegist(req, res);

            case "businessTrip":
                return "/WEB-INF/jsp/businessTrip.jsp";

            case "doBusinessTrip":
                return doBusinessTrip(req, res);

            case "remoteWork":
                return "/WEB-INF/jsp/remoteWork.jsp";

            case "doRemoteWork":
                return doRemoteWork(req, res);

            default:
                return "/WEB-INF/jsp/locationRegist.jsp";
        }
    }

    private String doRegist(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String gyosaki = req.getParameter("gyosaki"); // 出社 / 在宅 / 出張
        if ("出張".equals(gyosaki)) {
            return "/WEB-INF/jsp/businessTrip.jsp";
        }
        if ("在宅".equals(gyosaki)) {
            return "/WEB-INF/jsp/remoteWork.jsp";
        }
        // 出社：ここでは登録せず座席予約画面（ReserveLogic）へリダイレクトし、
        // 座席が決まった時点で「出社＋座席」をまとめて1件登録する
        // （そうしないと「出社（座席なし）」と「出社（座席あり）」の2件ができてしまうため）
        res.sendRedirect(req.getContextPath() + "/ControlServlet?action=reserve");
        return null;
    }

    private String doRemoteWork(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String memo = req.getParameter("memo");
        return saveAndGoMenu(req, res, "在宅", memo != null ? memo : "");
    }

    private String doBusinessTrip(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String destination = req.getParameter("destination");
        String memo        = req.getParameter("memo");
        // 出張先とメモを混ぜると後で見たときにどちらか分からなくなるため、
        // 「出張先:○○/メモ:○○」の形式で保存し、表示時にも一目で区別できるようにする
        StringBuilder fullMemo = new StringBuilder("出張先:").append(destination != null ? destination : "");
        if (memo != null && !memo.isEmpty()) {
            fullMemo.append("/メモ:").append(memo);
        }
        return saveAndGoMenu(req, res, "出張", fullMemo.toString());
    }

    private void saveReservation(HttpServletRequest req, String gyosaki, String memo) {
        HttpSession session = req.getSession(false);
        String userId = (String) session.getAttribute("userId");
        String today  = LocalDate.now().toString();

        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setRegisterId(userId);
        r.setReserveDate(today);
        r.setGyosaki(gyosaki);
        r.setMemo(memo);
        r.setSeatId(0);
        r.setCancelFlag(0);

        new ReservationsDAO().add(r);

        // メニューへ戻る前に本日の予約を更新
        session.setAttribute("todayReservations",
                new ReservationsDAO().findTodayByUserId(userId));
    }

    private String saveAndGoMenu(HttpServletRequest req, HttpServletResponse res, String gyosaki, String memo) throws Exception {
        saveReservation(req, gyosaki, memo);

        HttpSession session = req.getSession(false);
        // リダイレクト後の画面で表示するため、リクエスト属性ではなくセッションに積んでおく
        session.setAttribute("successMsg", gyosaki + "の行先登録が完了しました。");

        boolean isAdmin = Boolean.TRUE.equals(session.getAttribute("isAdmin"));
        res.sendRedirect(req.getContextPath() + "/ControlServlet?action=" + (isAdmin ? "adminMenu" : "menu"));
        return null;
    }
}
