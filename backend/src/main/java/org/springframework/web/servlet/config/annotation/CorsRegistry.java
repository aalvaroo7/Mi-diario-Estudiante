package org.springframework.web.servlet.config.annotation;

public class CorsRegistry {
    public CorsRegistration addMapping(String pattern) {
        return new CorsRegistration();
    }

    public static class CorsRegistration {
        public CorsRegistration allowedOrigins(String... origins) {
            return this;
        }

        public CorsRegistration allowedMethods(String... methods) {
            return this;
        }

        public CorsRegistration allowedHeaders(String... headers) {
            return this;
        }

        public CorsRegistration allowCredentials(boolean allow) {
            return this;
        }
    }
}
