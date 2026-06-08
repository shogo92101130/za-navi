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
 * 部門検索（指定した部署のメンバー一覧と、各メンバーの本日の出社状況を表示する）。
 *
 * 流れ：
 *   1) 画面表示時：部署の一覧（プルダウン用）を取得して渡す
 *   2) 部署が選択されたとき（bIdパラメータあり）：
 *      その部署のメンバーを取得し、1人ずつ「本日の予約」を調べて
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
        if (bId != null && !bId.isEmpty()) {
            AccountsDAO accDAO = new AccountsDAO();
            ReservationsDAO resDAO = new ReservationsDAO();
            SeatsDAO seatDAO = new SeatsDAO();

            // 選んだ部署に所属するメンバー全員分を、1人ずつ「本日の状況」付きで組み立てる
            List<Account> members = accDAO.findByBId(bId);
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
            req.setAttribute("selectedBId", bId);
            req.setAttribute("selectedBName", locDAO.getBNameById(bId));
            req.setAttribute("results", rows);
        }
        return "/WEB-INF/jsp/departmentSearch.jsp";
    }
}
