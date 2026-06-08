package entity;

/**
 * 予約・行先登録1件分のデータを表すentity（DBの reservations テーブル1行に相当）。
 * 「出社」「在宅」「出張」のいずれであっても、すべてこの1つのクラスで表す
 * （行き先の種類によって座席ID(seatId)があったり0だったりする、という違いだけ）。
 *
 *   reserveId   : 予約ID（一意の連番）
 *   userId      : 予約の本人（誰の予定か）
 *   registerId  : 予約を登録した人（自分で登録ならuserIdと同じ。管理者の代理予約なら管理者のID）
 *   reserveDate : 対象日（YYYY-MM-DD形式の文字列）
 *   gyosaki     : 行先の種類（"出社" / "在宅" / "出張" のいずれか）
 *   memo        : 自由記入のメモ
 *   seatId      : 予約した座席ID（0の場合は座席なし＝在宅・出張など）
 *   cancelFlag  : 取消状態（0=有効な予約, 1=取消済み）
 */
public class Reservation {
    private int reserveId;
    private String userId;
    private String registerId;
    private String reserveDate;
    private String gyosaki;
    private String memo;
    private int seatId;
    private int cancelFlag;

    public Reservation() {}

    public Reservation(int reserveId, String userId, String registerId,
                       String reserveDate, String gyosaki, String memo,
                       int seatId, int cancelFlag) {
        this.reserveId = reserveId;
        this.userId = userId;
        this.registerId = registerId;
        this.reserveDate = reserveDate;
        this.gyosaki = gyosaki;
        this.memo = memo;
        this.seatId = seatId;
        this.cancelFlag = cancelFlag;
    }

    public int getReserveId() { return reserveId; }
    public void setReserveId(int reserveId) { this.reserveId = reserveId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getRegisterId() { return registerId; }
    public void setRegisterId(String registerId) { this.registerId = registerId; }
    public String getReserveDate() { return reserveDate; }
    public void setReserveDate(String reserveDate) { this.reserveDate = reserveDate; }
    public String getGyosaki() { return gyosaki; }
    public void setGyosaki(String gyosaki) { this.gyosaki = gyosaki; }
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    public int getSeatId() { return seatId; }
    public void setSeatId(int seatId) { this.seatId = seatId; }
    public int getCancelFlag() { return cancelFlag; }
    public void setCancelFlag(int cancelFlag) { this.cancelFlag = cancelFlag; }
}
