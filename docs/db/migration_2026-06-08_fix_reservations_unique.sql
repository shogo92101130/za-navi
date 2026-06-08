-- =========================================================
-- 既存DB向け修正スクリプト（2026-06-08）
--
-- 不具合：「出張・在宅のメモを登録するとエラーになる」「予約確認からキャンセルできない」
-- 原因  ：reservations テーブルの UNIQUE 制約 (uq_reservations_seat_active /
--         uq_reservations_user_active) が、座席を伴わない予約（在宅・出張＝seat_id=0）や
--         キャンセル済み行（cancel_flag=1）を考慮しておらず、
--         2人目以降の在宅・出張登録や、2件目のキャンセル操作が重複キーエラーになっていた。
--
-- 対応  ：各UNIQUE制約を、生成列（GENERATED COLUMN）を介した「有効な予約だけを対象にした
--         一意制約」に置き換える。対象外のケースはNULLになり、MySQLのUNIQUEインデックスは
--         NULL同士を重複と見なさないため、意図した重複だけを防げるようになる。
--
-- 適用方法：MySQLクライアント（コマンドプロンプトやMySQL Workbenchなど）で
--   USE za_navi_db;
--   SOURCE migration_2026-06-08_fix_reservations_unique.sql;
-- のように実行する。
-- =========================================================

ALTER TABLE reservations
    DROP INDEX uq_reservations_seat_active,
    DROP INDEX uq_reservations_user_active;

ALTER TABLE reservations
    ADD COLUMN seat_active_key INT(3) GENERATED ALWAYS AS
        (CASE WHEN cancel_flag = 0 AND seat_id > 0 THEN seat_id END) STORED AFTER seat_id,
    ADD COLUMN user_active_key INT(10) GENERATED ALWAYS AS
        (CASE WHEN cancel_flag = 0 THEN user_id END) STORED AFTER user_id;

ALTER TABLE reservations
    ADD CONSTRAINT uq_reservations_seat_active UNIQUE (reserve_date, seat_active_key),
    ADD CONSTRAINT uq_reservations_user_active UNIQUE (reserve_date, user_active_key);
