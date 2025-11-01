package com.MiDiarioEstudiante;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        // This starts an embedded Tomcat server on port 8080
        SpringApplication.run(Main.class, args);
    }
}
