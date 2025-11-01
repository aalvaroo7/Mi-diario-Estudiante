package org.springframework.boot;

public final class SpringApplication {
    private SpringApplication() {
    }

    public static void run(Class<?> primarySource, String... args) {
        try {
            primarySource.getDeclaredConstructor().newInstance();
        } catch (Exception ignored) {
            // No-op stub implementation
        }
    }
}
