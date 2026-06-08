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
                dao.add(s);
                req.setAttribute("message", "座席「" + s.getSeatName() + "」を追加しました。");
                setListAttributes(req, dao);
                return "/WEB-INF/jsp/masterUpdate.jsp";
            }

            case "doMasterUpdate": {
                // 更新
                int id = Integer.parseInt(req.getParameter("seatId"));
                Seat s = buildSeat(req, id);
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

            case "doSetOfficeImage": {
                // フロアマップ画像の登録・変更（新しいオフィスを増やしたときも、
                // ここで対応付けを登録するだけで座席利用状況・代理予約にも画像が表示される）
                String base = req.getParameter("imageBaseName");
                String file = req.getParameter("imageFileName");
                if (base == null || base.isEmpty() || file == null || file.isEmpty()) {
                    req.setAttribute("message", "オフィスと画像ファイル名の両方を入力してください。");
                } else {
                    new SeatsDAO().setOfficeImage(base, file.trim());
                    req.setAttribute("message", "「" + base + "」のフロアマップ画像を「" + file.trim() + "」として登録しました。");
                }
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
     * 「① オフィスを選んで見る」ための bases / selectedBase / officeImage と、
     * 選んだオフィスだけに絞り込んだ座席一覧 seats をリクエストに積む。
     */
    private void setListAttributes(HttpServletRequest req, MastersDAO dao) {
        SeatsDAO seatsDAO = new SeatsDAO();
        String selectedBase = req.getParameter("filterBase");

        List<Seat> all = dao.getAll();
        List<Seat> seats = (selectedBase != null && !selectedBase.isEmpty())
                ? all.stream().filter(s -> selectedBase.equals(s.getBaseName())).collect(Collectors.toList())
                : all;

        req.setAttribute("seats",        seats);
        req.setAttribute("bases",        seatsDAO.getBases());
        req.setAttribute("selectedBase", selectedBase);
        req.setAttribute("officeImage",  (selectedBase != null && !selectedBase.isEmpty())
                ? seatsDAO.getOfficeImage(selectedBase) : null);
        req.setAttribute("officeImages", seatsDAO.getOfficeImages());
    }
}
