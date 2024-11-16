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

-- CREATE TABLE IF NOT EXISTS file_or_folder_id_sequence (
--     file_or_folder_id INT PRIMARY KEY,
--     next_id BIGINT
-- );

-- CREATE TABLE IF NOT EXISTS user_id_sequence (
--     user_id INT PRIMARY KEY,
--     next_id BIGINT
-- );

INSERT INTO tracker(url_tracker) VALUES
("http://localhost/8001"),
("http://localhost/8002"),
("http://localhost/8003"),
("http://localhost/8004");