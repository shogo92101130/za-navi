package model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * ログアウト処理。
 * セッション（ログイン情報を保持している入れ物）を破棄して、ログアウト完了画面を表示する。
 * これ以降、ログインし直さない限り action=login 以外のページへはアクセスできない
 * （ControlServlet.process() のセッションチェックで弾かれる）。
 */
public class LogoutLogic implements Logic {

    @Override
    public String execute(HttpServletRequest req, HttpServletResponse res) throws Exception {
        // false を渡しているので、セッションが無ければ新規作成しない（無ければ何もしなくてよい）
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate(); // セッション内の userId / isAdmin などをすべて破棄する
        }
        return "/WEB-INF/jsp/logout.jsp";
    }
}
