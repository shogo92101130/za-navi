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
import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReserveLogic implements Logic {

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String action = req.getParameter("action");

        if ("doReserve".equals(action)) {
            return doReserve(req);
        }
        // action=reserve: stage で段階表示を切り替える
        return showReserve(req);
    }

    // ─────────────── 座席選択フロー ───────────────

    private String showReserve(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        SeatsDAO seatsDAO   = new SeatsDAO();
        ReservationsDAO resDAO = new ReservationsDAO();
        String today = LocalDate.now().toString();

        String stage = req.getParameter("stage");
        if (stage == null || stage.isEmpty()) stage = "base";

        // パラメータをセッションへ蓄積
        String base = req.getParameter("base");
        String floor = req.getParameter("floor");
        String area  = req.getParameter("area");
        String date  = req.getParameter("date");

        if (base  != null && !base.isEmpty())  session.setAttribute("resBase",  base);
        if (floor != null && !floor.isEmpty()) session.setAttribute("resFloor", floor);
        if (area  != null && !area.isEmpty())  session.setAttribute("resArea",  area);

        // セッションから取得
        String sBase  = (String) session.getAttribute("resBase");
        String sFloor = (String) session.getAttribute("resFloor");
        String sArea  = (String) session.getAttribute("resArea");

        req.setAttribute("stage", stage);
        req.setAttribute("selBase",  sBase);
        req.setAttribute("selFloor", sFloor);
        req.setAttribute("selArea",  sArea);

        // 今日の座席利用マップ（埋有率計算用）
        Map<Integer, String> todayUsage = resDAO.getSeatUsageForDate(today);

        switch (stage) {
            case "base": {
                // 拠点ごとの埋有率
                // 分母：その拠点の全席数（全フロア・全エリア合計）
                List<Map<String, Object>> items = new ArrayList<>();
                for (String b : seatsDAO.getBases()) {
                    List<Seat> baseSeats = seatsDAO.findByBase(b);
                    long used = baseSeats.stream().filter(s -> todayUsage.containsKey(s.getSeatId())).count();
                    int total = baseSeats.size();
                    int rate  = total > 0 ? (int)(used * 100 / total) : 0;
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", b); m.put("total", total); m.put("used", used); m.put("rate", rate);
                    items.add(m);
                }
                req.setAttribute("items", items);
                break;
            }
            case "floor": {
                // フロアごとの埋有率
                // 分母：そのフロアの全席数（全エリア合計）
                List<Map<String, Object>> items = new ArrayList<>();
                for (String f : seatsDAO.getFloors(sBase)) {
                    List<Seat> floorSeats = seatsDAO.findByFloor(sBase, f);
                    long used = floorSeats.stream().filter(s -> todayUsage.containsKey(s.getSeatId())).count();
                    int total = floorSeats.size();
                    int rate  = total > 0 ? (int)(used * 100 / total) : 0;
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", f); m.put("total", total); m.put("used", used); m.put("rate", rate);
                    items.add(m);
                }
                req.setAttribute("items", items);
                break;
            }
            case "area": {
                // エリアごとの埋有率
                // 分母：そのエリアの席のみ
                List<Map<String, Object>> items = new ArrayList<>();
                for (String a : seatsDAO.getAreas(sBase, sFloor)) {
                    List<Seat> areaSeats = seatsDAO.findByArea(sBase, sFloor, a);
                    long used = areaSeats.stream().filter(s -> todayUsage.containsKey(s.getSeatId())).count();
                    int total = areaSeats.size();
                    int rate  = total > 0 ? (int)(used * 100 / total) : 0;
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", a); m.put("total", total); m.put("used", used); m.put("rate", rate);
                    items.add(m);
                }
                req.setAttribute("items", items);
                break;
            }
            case "seat": {
                // 日付プルダウン（予約できるのは今日から2週間先まで）
                List<String> dates = new ArrayList<>();
                LocalDate d = LocalDate.now();
                for (int i = 0; i < 15; i++) {
                    dates.add(d.plusDays(i).toString());
                }
                req.setAttribute("dates", dates);

                // 選択日の座席利用状況
                String selectedDate = (date != null && !date.isEmpty()) ? date : today;
                req.setAttribute("selectedDate", selectedDate);
                session.setAttribute("resDate", selectedDate);

                Map<Integer, String> usage = resDAO.getSeatUsageForDate(selectedDate);
                List<Seat> areaSeats = seatsDAO.findByArea(sBase, sFloor, sArea);

                // 使用中の座席にカーソルを合わせたときに、予約者の氏名・部署名が
                // ひと目で分かるように（座席ボタンには userId しか入っていないため変換する）
                AccountsDAO accDAO = new AccountsDAO();
                LocationsDAO locDAO = new LocationsDAO();
                Map<Integer, String> seatUserNameMap = new HashMap<>();
                Map<Integer, String> seatUserDeptMap = new HashMap<>();
                for (Map.Entry<Integer, String> e : usage.entrySet()) {
                    Account acc = accDAO.findById(e.getValue());
                    seatUserNameMap.put(e.getKey(), acc != null ? acc.getName() : e.getValue());
                    seatUserDeptMap.put(e.getKey(), acc != null ? locDAO.getBNameById(acc.getBId()) : "");
                }

                // エリア埋有率（選択日）
                long usedCount = areaSeats.stream().filter(s -> usage.containsKey(s.getSeatId())).count();
                int total = areaSeats.size();
                int rate  = total > 0 ? (int)(usedCount * 100 / total) : 0;

                req.setAttribute("areaSeats",  areaSeats);
                req.setAttribute("seatUsage",  usage);
                req.setAttribute("seatUserNameMap", seatUserNameMap);
                req.setAttribute("seatUserDeptMap", seatUserDeptMap);
                req.setAttribute("areaRate",   rate);
                req.setAttribute("areaUsed",   usedCount);
                req.setAttribute("areaTotal",  total);
                break;
            }
        }
        return "/WEB-INF/jsp/reserve.jsp";
    }

    // ─────────────── 予約実行 ───────────────

    private String doReserve(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        String userId = (String) session.getAttribute("userId");
        String seatIdStr = req.getParameter("seatId");
        String date  = req.getParameter("date");
        String memo  = req.getParameter("memo");
        if (date == null || date.isEmpty()) date = LocalDate.now().toString();

        if (seatIdStr == null) {
            req.setAttribute("errorMsg", "座席が選択されていません。");
            return "/WEB-INF/jsp/reserveNG.jsp";
        }
        int seatId = Integer.parseInt(seatIdStr);

        ReservationsDAO resDAO = new ReservationsDAO();

        // 同じ日にすでに有効な予約（出社・在宅・出張のいずれか）を持っていないか確認
        // （DB側のUNIQUE制約 uq_reservations_user_active が1人1日1件に制限しているため、
        //   ここでチェックせずINSERTすると制約違反の生エラーがそのまま「システムエラー」になってしまう）
        if (resDAO.findActiveByUserAndDate(userId, date) != null) {
            req.setAttribute("errorMsg", "その日はすでに予約（出社・在宅・出張のいずれか）が登録されています。"
                    + "先に「予約確認」から既存の予約を取り消してください。");
            return "/WEB-INF/jsp/reserveNG.jsp";
        }

        // 同一日・同一席に既存予約がないか確認
        Map<Integer, String> usage = resDAO.getSeatUsageForDate(date);
        if (usage.containsKey(seatId)) {
            req.setAttribute("errorMsg", "選択した座席はすでに予約済みです。");
            return "/WEB-INF/jsp/reserveNG.jsp";
        }

        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setRegisterId(userId);
        r.setReserveDate(date);
        r.setGyosaki("出社");
        r.setSeatId(seatId);
        r.setMemo(memo != null && !memo.isEmpty() ? memo : null);
        r.setCancelFlag(0);
        resDAO.add(r);

        // 座席選択の中間値をクリア
        session.removeAttribute("resBase");
        session.removeAttribute("resFloor");
        session.removeAttribute("resArea");
        session.removeAttribute("resDate");

        Seat seat = new SeatsDAO().findById(seatId);
        req.setAttribute("reservation", r);
        req.setAttribute("seat", seat);
        return "/WEB-INF/jsp/reserveOK.jsp";
    }
}
