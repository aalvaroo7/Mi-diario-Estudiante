package org.springframework.web.servlet.config.annotation;

public interface WebMvcConfigurer {
    default void addCorsMappings(CorsRegistry registry) {
    }

    default void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    default void addViewControllers(ViewControllerRegistry registry) {
    }
}
