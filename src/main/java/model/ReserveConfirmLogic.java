package model;

import dao.AccountsDAO;
import dao.ReservationsDAO;
import dao.SeatsDAO;
import entity.Account;
import entity.Reservation;
import entity.Seat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * 予約確認・キャンセル（action=reserveConfirm で一覧表示／action=doCancel で取消）。
 * ログイン中の本人の予約一覧を表示し、各行の「取消」ボタンから
 * doCancel を呼ぶと該当予約に取消フラグを立てる（物理削除ではなく論理削除）。
 */
public class ReserveConfirmLogic implements Logic {

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String action = req.getParameter("action");
        if ("doCancel".equals(action)) {
            return doCancel(req);
        }
        return showConfirm(req);
    }

    private String showConfirm(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        String userId = (String) session.getAttribute("userId");
        loadReservations(req, userId);
        return "/WEB-INF/jsp/reserveConfirm.jsp";
    }

    private String doCancel(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        String userId = (String) session.getAttribute("userId");
        String reserveIdStr = req.getParameter("reserveId");

        if (reserveIdStr != null && !reserveIdStr.isEmpty()) {
            int reserveId = Integer.parseInt(reserveIdStr);
            ReservationsDAO resDAO = new ReservationsDAO();
            Reservation r = resDAO.findById(reserveId);
            // 自分の予約のみキャンセル可（セキュリティチェック）
            if (r != null && r.getUserId().equals(userId)) {
                resDAO.cancel(reserveId);
                req.setAttribute("cancelMessage", "予約をキャンセルしました。");
            }
        }
        loadReservations(req, userId);
        return "/WEB-INF/jsp/reserveConfirm.jsp";
    }

    private void loadReservations(HttpServletRequest req, String userId) {
        ReservationsDAO resDAO = new ReservationsDAO();
        SeatsDAO seatsDAO = new SeatsDAO();
        AccountsDAO accDAO = new AccountsDAO();

        List<Reservation> list = resDAO.findByUserId(userId);

        // 座席名マップ（seatId → 座席情報）
        Map<Integer, Seat> seatMap = new HashMap<>();
        for (Reservation r : list) {
            if (r.getSeatId() > 0 && !seatMap.containsKey(r.getSeatId())) {
                Seat s = seatsDAO.findById(r.getSeatId());
                if (s != null) seatMap.put(r.getSeatId(), s);
            }
        }

        req.setAttribute("reservations", list);
        req.setAttribute("seatMap", seatMap);
    }
}
