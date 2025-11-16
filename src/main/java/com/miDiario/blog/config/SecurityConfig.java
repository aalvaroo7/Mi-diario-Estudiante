package com.miDiario.blog.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                // DESACTIVAR LOGIN POR DEFECTO
                .formLogin(form -> form.disable())
                // DESACTIVAR LOGOUT DE SPRING SECURITY
                .logout(logout -> logout.disable())
                // DESACTIVAR AUTENTICACIÓN BÁSICA
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
