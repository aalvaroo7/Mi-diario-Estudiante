package com.MiDiarioEstudiante.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("schema-inspection")
public class DatabaseSchemaLogger implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaLogger.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaLogger(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'mi_diario' ORDER BY table_name",
                String.class
        );

        if (tables.isEmpty()) {
            log.info("No tables found in schema 'mi_diario'.");
            return;
        }

        log.info("Tables in schema 'mi_diario': {}", tables);

        tables.forEach(table -> {
            log.info("Columns for table '{}':", table);
            jdbcTemplate.query(
                    "SELECT column_name, column_type, is_nullable FROM information_schema.columns " +
                            "WHERE table_schema = 'mi_diario' AND table_name = ? ORDER BY ordinal_position",
                    ps -> ps.setString(1, table),
                    rs -> log.info(" - {} ({}) nullable={}",
                            rs.getString("column_name"),
                            rs.getString("column_type"),
                            rs.getString("is_nullable"))
            );
        });
    }
}
