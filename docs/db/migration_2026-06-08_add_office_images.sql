-- =========================================================
-- 既存DB向け修正スクリプト（2026-06-08 その3）
--
-- 内容：オフィスのフロアマップ画像をマスタ更新画面から登録できるようにするため、
--       拠点名 → 画像ファイル名の対応を保持する office_images テーブルを追加する。
--       （これまでは SeatsDAO 内に拠点名と画像ファイル名の対応をハードコーディングしていたが、
--         新しいオフィスを追加するたびにコード修正・再デプロイが必要だったため、
--         マスタ更新画面から登録できるDB管理に変更した）
--
-- 適用方法：MySQLクライアントで
--   USE zaseki1;
--   SOURCE migration_2026-06-08_add_office_images.sql;
-- のように実行する。
-- =========================================================

CREATE TABLE IF NOT EXISTS office_images (
    base_name  VARCHAR(50)  PRIMARY KEY,
    image_file VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- これまでハードコーディングされていた対応を初期データとして投入
INSERT INTO office_images (base_name, image_file) VALUES
    ('三田', 'office_mita.png'),
    ('芝浦', 'office_shibaura.png')
ON DUPLICATE KEY UPDATE image_file = VALUES(image_file);
