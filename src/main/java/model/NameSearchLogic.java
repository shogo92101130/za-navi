package model;

import dao.AccountsDAO;
import dao.LocationsDAO;
import dao.ReservationsDAO;
import dao.SeatsDAO;
import entity.Account;
import entity.Reservation;
import entity.Seat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.*;

/**
 * 氏名検索（入力されたキーワードを含む氏名のアカウントを探し、各人の所属部署と
 * 本日の出社状況・座席を一覧表示する）。
 *
 * keyword パラメータが無ければ検索フォームだけを表示し、
 * あれば AccountsDAO.findByName() で部分一致検索した結果に
 * 部署名・本日の行先・座席情報を付け加えて JSP に渡す。
 */
public class NameSearchLogic implements Logic {

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String keyword = req.getParameter("keyword");

        if (keyword != null) {
            req.setAttribute("keyword", keyword);

            if (keyword.trim().isEmpty()) {
                // 何も入力せずに検索すると全員がヒットしてしまい一覧の意味がないため、
                // 入力を促すメッセージを出して検索は行わない
                req.setAttribute("errorMsg", "氏名を入力してください。");
            } else {
            // 検索実行
            AccountsDAO accDAO = new AccountsDAO();
            LocationsDAO locDAO = new LocationsDAO();
            ReservationsDAO resDAO = new ReservationsDAO();
            SeatsDAO seatDAO = new SeatsDAO();
            String today = LocalDate.now().toString();
            Map<Integer, String> todayUsage = resDAO.getSeatUsageForDate(today);

            List<Account> results = accDAO.findByName(keyword.trim());

            // 検索結果に各ユーザーの本日のステータスと予約座席を付与
            List<Map<String, Object>> rows = new ArrayList<>();
            for (Account a : results) {
                Map<String, Object> row = new HashMap<>();
                row.put("account", a);
                row.put("bName", locDAO.getBNameById(a.getBId()));

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
            req.setAttribute("results", rows);
            }
        }
        return "/WEB-INF/jsp/nameSearch.jsp";
    }
}
