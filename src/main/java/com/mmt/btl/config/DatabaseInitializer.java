package com.mmt.btl.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        // Tạo Trigger khi ứng dụng khởi động
        String sql = "CREATE TRIGGER before_insert_piece " +
                "BEFORE INSERT ON piece " +
                "FOR EACH ROW BEGIN " +
                "DECLARE max_id BIGINT; " +
                "SELECT COALESCE(MAX(id), 0) + 1 INTO max_id " +
                "FROM piece " +
                "WHERE file_or_folder_id = NEW.file_or_folder_id; " +
                "SET NEW.id = max_id; " +
                "END;";
        jdbcTemplate.execute(sql);
    }
}