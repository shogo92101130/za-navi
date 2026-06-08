package model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 業務ロジックの共通インターフェース（MVC2の「Model」側の入口）。
 *
 * 画面の流れはどの機能でも共通でこうなっている：
 *   ① ブラウザ → ControlServlet（action=○○ で振り分け）
 *   ② ControlServlet → このインターフェースを実装したLogicクラスの execute()
 *   ③ Logic → DAO を呼び出してデータを取得・更新し、req.setAttribute() でJSPに値を渡す
 *   ④ Logic → 次に表示するJSPのパスを return し、ControlServlet がそこへ forward する
 *
 * execute() は処理を行い、次に表示するJSPのパスを返す。
 * ControlServlet は返されたパスへ forward する。
 * 新しい機能を追加するときは、この Logic を実装したクラスを1つ作り、
 * ControlServlet の dispatch() に case を1行追加すればよい。
 */
public interface Logic {
    /**
     * @return forward先のJSPパス（"/WEB-INF/jsp/..."）。
     *         null の場合、ControlServlet は forward しない（Logic内でredirect済みの場合）。
     */
    String execute(HttpServletRequest req, HttpServletResponse res) throws Exception;
}
