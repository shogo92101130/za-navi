package model;

import dao.AccountsDAO;
import dao.ReservationsDAO;
import entity.Account;
import entity.Reservation;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * ログイン処理（action=login で画面表示／action=doLogin で認証）。
 *
 * ★ここで session にセットする userId / userName / isAdmin / bId の4つは、
 *   ログイン後すべての画面・Logicから session.getAttribute(...) で参照される
 *   「誰がログインしているか」の基本情報。新しい画面を作るときもこの4つを使えばよい。
 */
public class LoginLogic implements Logic {

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
        String action = req.getParameter("action");

        if ("doLogin".equals(action)) {
            return doLogin(req, res);
        }
        // action=login: ログイン画面を表示するだけ
        return "/WEB-INF/jsp/login.jsp";
    }

    private String doLogin(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String userId   = req.getParameter("userId");
        String password = req.getParameter("password");

        Account account = new AccountsDAO().authenticate(userId, password);
        if (account == null) {
            req.setAttribute("errorMsg", "ユーザーIDまたはパスワードが正しくありません。");
            return "/WEB-INF/jsp/loginNG.jsp";
        }

        // 「管理者ログイン」フォームから入力された場合は、管理者権限を持つアカウントのみ許可する
        // （一般社員が管理者ログインから一般メニューに入れてしまわないようにするため）
        if ("admin".equals(req.getParameter("loginType")) && account.getAdmin() != 1) {
            req.setAttribute("errorMsg", "管理者権限がありません。");
            return "/WEB-INF/jsp/loginNG.jsp";
        }

        // セッションを新規発行してユーザー情報を格納
        HttpSession session = req.getSession(true);
        session.setAttribute("userId",   account.getUserId());
        session.setAttribute("userName", account.getName());
        session.setAttribute("isAdmin",  account.getAdmin() == 1);
        session.setAttribute("bId",      account.getBId());

        // 本日の予約を取得してセッションに保持
        List<Reservation> todayRes = new ReservationsDAO().findTodayByUserId(account.getUserId());
        session.setAttribute("todayReservations", todayRes);

        // メニューJSPへ直接forwardすると、ControlServlet#showMenu()が用意する
        // 「同じ部署の出社状況」「本日の予約」「今週の予約状況」などの表示用データが
        // セットされないまま画面が表示されてしまう。必ず action=menu / adminMenu を
        // 経由させて showMenu() を実行させるため、リダイレクトする。
        if (account.getAdmin() == 1) {
            res.sendRedirect(req.getContextPath() + "/ControlServlet?action=adminMenu");
        } else {
            res.sendRedirect(req.getContextPath() + "/ControlServlet?action=menu");
        }
        return null;
    }
}
