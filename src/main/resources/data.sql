-- ALTER TABLE piece
-- DROP FOREIGN KEY FK_piece_file_or_folder_id,
-- DROP PRIMARY KEY;

-- ALTER TABLE piece
-- ADD CONSTRAINT FK_file_or_folder FOREIGN KEY (file_or_folder_id) REFERENCES file_or_folder(id),
-- ADD COLUMN id BIGINT AUTO_INCREMENT,
-- ADD PRIMARY KEY(id, file_or_folder_id);

-- CREATE TABLE peer_piece(
--     peer_id BIGINT,
--     piece_id BIGINT,
--     file_or_folder_id BIGINT,
--     CONSTRAINT fk_file_or_folder FOREIGN KEY (file_or_folder_id) REFERENCES piece(file_or_folder_id),
--     CONSTRAINT fk_peer FOREIGN KEY (peer_id) REFERENCES peer(id),
--     CONSTRAINT fk_piece FOREIGN KEY (piece_id) REFERENCES piece(id),
--     PRIMARY KEY(peer_id, piece_id, file_or_folder_id)
-- );

CREATE TABLE IF NOT EXISTS file_or_folder_id_sequence (
    file_or_folder_id INT PRIMARY KEY,
    next_id BIGINT
);

DELIMITER $$

CREATE TRIGGER before_insert_piece
BEFORE INSERT ON piece
FOR EACH ROW
BEGIN
    DECLARE max_id BIGINT;

    -- Lấy giá trị lớn nhất của id cho file_or_folder_id hiện tại, hoặc 1 nếu không có
    SELECT COALESCE(MAX(id), 0) + 1 INTO max_id
    FROM piece
    WHERE file_or_folder_id = NEW.file_or_folder_id;

    -- Gán giá trị id cho bản ghi mới
    SET NEW.id = max_id;
END$$

DELIMITER ;