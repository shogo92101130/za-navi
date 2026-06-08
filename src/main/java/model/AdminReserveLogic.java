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

public class AdminReserveLogic implements Logic {

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String action = req.getParameter("action");

        if ("doAdminReserve".equals(action)) {
            return doReserve(req);
        }
        return showForm(req);
    }

    private String showForm(HttpServletRequest req) {
        SeatsDAO seatsDAO = new SeatsDAO();
        AccountsDAO accDAO = new AccountsDAO();
        LocationsDAO locDAO = new LocationsDAO();

        // 「○○部署の○○」形式で検索・表示できるように、社員一覧に部署名を付与
        List<Map<String, Object>> accountRows = new ArrayList<>();
        for (Account a : accDAO.findAll()) {
            Map<String, Object> row = new HashMap<>();
            row.put("userId", a.getUserId());
            row.put("name",   a.getName());
            row.put("bName",  locDAO.getBNameById(a.getBId()));
            row.put("label",  locDAO.getBNameById(a.getBId()) + "の" + a.getName());
            accountRows.add(row);
        }
        req.setAttribute("accountRows", accountRows);
        req.setAttribute("deptNames", locDAO.findAll().stream()
                .map(loc -> loc.getBName()).collect(Collectors.toList()));
        req.setAttribute("bases",    seatsDAO.getBases());

        // パラメータ引き継ぎ（段階選択用）
        String base  = req.getParameter("base");
        String floor = req.getParameter("floor");
        String area  = req.getParameter("area");
        String date  = req.getParameter("date");

        if (base != null && !base.isEmpty()) {
            req.setAttribute("selBase",  base);
            req.setAttribute("floors",   seatsDAO.getFloors(base));
        }
        if (floor != null && !floor.isEmpty() && base != null) {
            req.setAttribute("selFloor", floor);
            req.setAttribute("areas",    seatsDAO.getAreas(base, floor));
        }
        if (area != null && !area.isEmpty() && base != null && floor != null) {
            req.setAttribute("selArea", area);
            String selectedDate = (date != null && !date.isEmpty()) ? date : LocalDate.now().toString();
            req.setAttribute("selectedDate", selectedDate);

            // 指定日の座席状況
            ReservationsDAO resDAO = new ReservationsDAO();
            Map<Integer, String> usage = resDAO.getSeatUsageForDate(selectedDate);
            List<Seat> seats = seatsDAO.findByArea(base, floor, area);

            // 使用中の座席にカーソルを合わせたときに、予約者の氏名・部署名が
            // ひと目で分かるように（座席ボタンには userId しか入っていないため変換する）
            Map<Integer, String> seatUserNameMap = new HashMap<>();
            Map<Integer, String> seatUserDeptMap = new HashMap<>();
            for (Map.Entry<Integer, String> e : usage.entrySet()) {
                Account acc = accDAO.findById(e.getValue());
                seatUserNameMap.put(e.getKey(), acc != null ? acc.getName() : e.getValue());
                seatUserDeptMap.put(e.getKey(), acc != null ? locDAO.getBNameById(acc.getBId()) : "");
            }

            req.setAttribute("areaSeats",  seats);
            req.setAttribute("seatUsage",  usage);
            req.setAttribute("seatUserNameMap", seatUserNameMap);
            req.setAttribute("seatUserDeptMap", seatUserDeptMap);
        }

        // 日付プルダウン（予約できるのは今日から2週間先まで）
        List<String> dates = new ArrayList<>();
        LocalDate d = LocalDate.now();
        for (int i = 0; i < 15; i++) dates.add(d.plusDays(i).toString());
        req.setAttribute("dates", dates);

        return "/WEB-INF/jsp/adminReserve.jsp";
    }

    private String doReserve(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        String adminId = (String) session.getAttribute("userId");
        ReservationsDAO resDAO = new ReservationsDAO();
        SeatsDAO seatsDAO = new SeatsDAO();

        String mode    = req.getParameter("mode");     // individual / meeting
        String date    = req.getParameter("date");
        String memo    = req.getParameter("memo");
        String[] seatIds = req.getParameterValues("seatIds"); // 複数選択

        if (date == null || date.isEmpty()) date = LocalDate.now().toString();
        if (seatIds == null || seatIds.length == 0) {
            req.setAttribute("errorMsg", "座席を選択してください。");
            return showForm(req);
        }

        Map<Integer, String> usage = resDAO.getSeatUsageForDate(date);
        List<String> created = new ArrayList<>();

        if ("individual".equals(mode)) {
            // 個人の代理予約（1名・1席）
            if (seatIds.length > 1) {
                req.setAttribute("errorMsg", "個人の代理予約では座席は1席のみ選択してください。");
                return showForm(req);
            }
            String targetUserId = req.getParameter("targetUserId");
            if (targetUserId == null || targetUserId.isEmpty()) {
                req.setAttribute("errorMsg", "対象社員を選択してください。");
                return showForm(req);
            }
            int seatId = Integer.parseInt(seatIds[0]);
            if (usage.containsKey(seatId)) {
                req.setAttribute("errorMsg", "選択した座席はすでに予約済みです。");
                return showForm(req);
            }
            Reservation r = makeReservation(adminId, targetUserId, date, seatId, memo);
            resDAO.add(r);
            created.add(targetUserId + " → " + getSeatLabel(seatsDAO.findById(seatId)));

        } else {
            // 会議用（複数名×複数席）
            String[] targetUserIds = req.getParameterValues("targetUserIds");
            if (targetUserIds == null || targetUserIds.length == 0) {
                req.setAttribute("errorMsg", "対象社員を選択してください。");
                return showForm(req);
            }
            if (targetUserIds.length != seatIds.length) {
                req.setAttribute("errorMsg",
                    "参加者数（" + targetUserIds.length + "名）と選択座席数（" + seatIds.length + "席）が一致しません。");
                return showForm(req);
            }
            for (int i = 0; i < targetUserIds.length; i++) {
                int seatId = Integer.parseInt(seatIds[i]);
                if (usage.containsKey(seatId)) continue; // すでに使用中はスキップ
                Reservation r = makeReservation(adminId, targetUserIds[i], date, seatId, memo);
                resDAO.add(r);
                created.add(targetUserIds[i] + " → " + getSeatLabel(seatsDAO.findById(seatId)));
            }
        }

        req.setAttribute("createdList", created);
        req.setAttribute("reserveDate", date);
        return "/WEB-INF/jsp/reserveOK.jsp";
    }

    private Reservation makeReservation(String registerId, String userId, String date, int seatId, String memo) {
        Reservation r = new Reservation();
        r.setRegisterId(registerId); // 予約した管理者
        r.setUserId(userId);         // 実際に座る人
        r.setReserveDate(date);
        r.setGyosaki("出社");
        r.setSeatId(seatId);
        r.setMemo(memo != null && !memo.isEmpty() ? memo : null);
        r.setCancelFlag(0);
        return r;
    }

    private String getSeatLabel(Seat s) {
        if (s == null) return "不明";
        return s.getBaseName() + " " + s.getFName() + " " + s.getAreaName() + " " + s.getSeatName();
    }
}
