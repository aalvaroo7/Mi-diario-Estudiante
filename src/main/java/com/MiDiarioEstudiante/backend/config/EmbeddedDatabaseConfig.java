package com.MiDiarioEstudiante.backend.config;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService;
import jakarta.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.sql.SQLException;

@Configuration
public class EmbeddedDatabaseConfig {

    private final MariaDB4jSpringService mariaDB4jSpringService;

    public EmbeddedDatabaseConfig(Environment environment) {
        this.mariaDB4jSpringService = new MariaDB4jSpringService();
        int configuredPort = environment.getProperty("spring.mariadb4j.port", Integer.class, 0);
        if (configuredPort > 0) {
            this.mariaDB4jSpringService.getConfiguration().setPort(configuredPort);
        }
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public MariaDB4jSpringService mariaDB4jSpringService() {
        return this.mariaDB4jSpringService;
    }

    @Bean
    public DataSource dataSource(
            MariaDB4jSpringService mariaDB4jSpringService,
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password
    ) throws SQLException, ManagedProcessException {
        try {
            mariaDB4jSpringService.getDB().createDB("mi_diario");
        } catch (ManagedProcessException ex) {
            String message = ex.getMessage();
            if (message == null || !message.contains("database exists")) {
                throw ex;
            }
        }
        return DataSourceBuilder.create()
                .driverClassName("org.mariadb.jdbc.Driver")
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}
