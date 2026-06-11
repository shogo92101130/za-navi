package model;

import dao.AccountsDAO;
import dao.LocationsDAO;
import dao.ReservationsDAO;
import dao.SeatsDAO;
import entity.Account;
import entity.Location;
import entity.Reservation;
import entity.Seat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 部門検索（指定した部署・社員IDに合致するメンバー一覧と、各メンバーの本日の出社状況を表示する）。
 *
 * 流れ：
 *   1) 画面表示時：部署の一覧（プルダウン用）を取得して渡す
 *   2) 部署（bId）・社員ID（userIdKeyword）のどちらか、または両方が指定されたとき：
 *      条件に合うメンバーを取得し、1人ずつ「本日の予約」を調べて
 *      「氏名」「本日の行先（出社/在宅/出張/未登録）」「座席（出社の場合）」をまとめてJSPに渡す
 *
 * 複数のDAO（Accounts → Reservations → Seats）を組み合わせて1画面分のデータを作る、
 * Logicクラスの典型的な役割が分かりやすい例になっている。
 */
public class DepartmentSearchLogic implements Logic {

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
        LocationsDAO locDAO = new LocationsDAO();
        List<Location> departments = locDAO.findAll();
        req.setAttribute("departments", departments); // 検索フォームの部署プルダウン用

        String bId = req.getParameter("bId");
        String userIdKeyword = req.getParameter("userIdKeyword");
        boolean hasBId = bId != null && !bId.isEmpty();
        boolean hasUserIdKeyword = userIdKeyword != null && !userIdKeyword.isEmpty();

        if (hasBId || hasUserIdKeyword) {
            AccountsDAO accDAO = new AccountsDAO();
            ReservationsDAO resDAO = new ReservationsDAO();
            SeatsDAO seatDAO = new SeatsDAO();

            // 条件に合うメンバー全員分を、1人ずつ「本日の状況」付きで組み立てる
            List<Account> members = accDAO.search(bId, userIdKeyword);
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Account a : members) {
                Map<String, Object> row = new HashMap<>();
                row.put("account", a);

                // 本日分の予約を取得し、行先（出社/在宅/出張）と座席（出社の場合のみ）を判定する
                List<Reservation> todayRes = resDAO.findTodayByUserId(a.getUserId());
                String todayStatus = "未登録";
                Seat seat = null;
                if (!todayRes.isEmpty()) {
                    Reservation r = todayRes.get(0);
                    todayStatus = r.getGyosaki();
                    if (r.getSeatId() > 0) {
                        seat = seatDAO.findById(r.getSeatId());
                    }
                }
                row.put("todayStatus", todayStatus);
                row.put("seat", seat);
                rows.add(row);
            }

            // 検索条件に応じた結果見出し（部署名・社員IDのどちらを条件に使ったか分かるように）
            String resultLabel;
            if (hasBId && hasUserIdKeyword) {
                resultLabel = "「" + locDAO.getBNameById(bId) + "」かつ社員ID「" + userIdKeyword + "」に一致する社員";
            } else if (hasBId) {
                resultLabel = "「" + locDAO.getBNameById(bId) + "」のメンバー";
            } else {
                resultLabel = "社員ID「" + userIdKeyword + "」に一致する社員";
            }

            req.setAttribute("selectedBId", bId);
            req.setAttribute("selectedUserIdKeyword", userIdKeyword);
            req.setAttribute("resultLabel", resultLabel);
            req.setAttribute("results", rows);
        } else if (bId != null || userIdKeyword != null) {
            // フォームが送信された（=画面初回表示ではない）のに、部門・社員IDのどちらも
            // 指定されていない場合は、検索条件が無いまま全員を表示してしまわないように
            // 入力を促すメッセージを出す
            req.setAttribute("errorMsg", "部門または社員IDを入力してください。");
        }
        return "/WEB-INF/jsp/departmentSearch.jsp";
    }
}
