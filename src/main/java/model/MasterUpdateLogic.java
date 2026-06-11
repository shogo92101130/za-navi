package model;

import dao.MastersDAO;
import dao.SeatsDAO;
import entity.Seat;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

public class MasterUpdateLogic implements Logic {

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String action = req.getParameter("action");
        MastersDAO dao = new MastersDAO();

        switch (action) {
            case "masterUpdate":
                // 一覧表示
                setListAttributes(req, dao);
                return "/WEB-INF/jsp/masterUpdate.jsp";

            case "masterEdit": {
                // 更新フォームを表示（対象行を editSeat にセット）
                int id = Integer.parseInt(req.getParameter("seatId"));
                Seat editSeat = dao.findById(id);
                req.setAttribute("editSeat", editSeat);
                setListAttributes(req, dao);
                return "/WEB-INF/jsp/masterUpdate.jsp";
            }

            case "doMasterAdd": {
                // 新規追加
                Seat s = buildSeat(req, 0);
                if (dao.exists(s.getBaseName(), s.getFName(), s.getAreaName(), s.getSeatName(), -1)) {
                    req.setAttribute("errorMsg",
                        "同じ座席（" + s.getBaseName() + " " + s.getFName() + " " + s.getAreaName() + " " + s.getSeatName() + "）はすでに存在します。");
                    setListAttributes(req, dao);
                    return "/WEB-INF/jsp/masterUpdate.jsp";
                }
                dao.add(s);
                req.setAttribute("message", "座席「" + s.getSeatName() + "」を追加しました。");
                setListAttributes(req, dao);
                return "/WEB-INF/jsp/masterUpdate.jsp";
            }

            case "doMasterUpdate": {
                // 更新
                int id = Integer.parseInt(req.getParameter("seatId"));
                Seat s = buildSeat(req, id);
                if (dao.exists(s.getBaseName(), s.getFName(), s.getAreaName(), s.getSeatName(), id)) {
                    req.setAttribute("errorMsg",
                        "同じ座席（" + s.getBaseName() + " " + s.getFName() + " " + s.getAreaName() + " " + s.getSeatName() + "）はすでに存在します。");
                    req.setAttribute("editSeat", dao.findById(id));
                    setListAttributes(req, dao);
                    return "/WEB-INF/jsp/masterUpdate.jsp";
                }
                dao.update(s);
                req.setAttribute("message", "座席ID " + id + " を更新しました。");
                setListAttributes(req, dao);
                return "/WEB-INF/jsp/masterUpdate.jsp";
            }

            case "doMasterDelete": {
                // 削除
                int id = Integer.parseInt(req.getParameter("seatId"));
                dao.delete(id);
                req.setAttribute("message", "座席ID " + id + " を削除しました。");
                setListAttributes(req, dao);
                return "/WEB-INF/jsp/masterUpdate.jsp";
            }

            default:
                setListAttributes(req, dao);
                return "/WEB-INF/jsp/masterUpdate.jsp";
        }
    }

    private Seat buildSeat(HttpServletRequest req, int seatId) {
        Seat s = new Seat();
        s.setSeatId(seatId);

        // 「既存オフィスから選択」または「新しいオフィス名を入力」のどちらかが baseName に入る。
        // 新規オフィス入力欄（newBaseName）に値があれば、そちらを優先する。
        // ※オフィスという名前のマスタ行は無く、座席を1件追加した時点でそのオフィスが新しく登場する仕組み。
        String newBase = req.getParameter("newBaseName");
        String baseName = (newBase != null && !newBase.trim().isEmpty())
                ? newBase.trim() : req.getParameter("baseName");

        s.setBaseName(baseName);
        s.setFName(req.getParameter("fName"));
        s.setAreaName(req.getParameter("areaName"));
        s.setSeatName(req.getParameter("seatName"));
        return s;
    }

    /**
     * 一覧表示・絞り込み・フロアマップ画像表示に必要な属性をまとめてセットする。
     * 「① オフィスを選んで見る」「② フロアを選んで見る」ための
     * bases / selectedBase / floors / selectedFloor / officeImage と、
     * 選んだオフィス・フロアだけに絞り込んだ座席一覧 seats をリクエストに積む。
     *
     * フロアまで選ぶと、そのフロア専用のマップ（"拠点名_フロア名"）があれば優先表示される
     * （無ければ拠点単位の画像にフォールバック）→ 新しいオフィスを追加したときも、
     * フロアを選んだ時点でそのフロアのマップ画像に切り替わる。
     */
    private void setListAttributes(HttpServletRequest req, MastersDAO dao) {
        SeatsDAO seatsDAO = new SeatsDAO();
        String selectedBase  = req.getParameter("filterBase");
        String selectedFloor = req.getParameter("filterFloor");
        boolean hasBase  = selectedBase  != null && !selectedBase.isEmpty();
        boolean hasFloor = selectedFloor != null && !selectedFloor.isEmpty();

        List<Seat> all = dao.getAll();
        List<Seat> seats = all;
        if (hasBase)  seats = seats.stream().filter(s -> selectedBase.equals(s.getBaseName())).collect(Collectors.toList());
        if (hasFloor) seats = seats.stream().filter(s -> selectedFloor.equals(s.getFName())).collect(Collectors.toList());

        req.setAttribute("seats",         seats);
        req.setAttribute("bases",         seatsDAO.getBases());
        req.setAttribute("selectedBase",  selectedBase);
        req.setAttribute("selectedFloor", selectedFloor);
        req.setAttribute("floors",        hasBase ? seatsDAO.getFloors(selectedBase) : null);
        req.setAttribute("officeImage",   hasBase ? seatsDAO.getExpectedFloorImage(selectedBase, selectedFloor) : null);
    }
}
