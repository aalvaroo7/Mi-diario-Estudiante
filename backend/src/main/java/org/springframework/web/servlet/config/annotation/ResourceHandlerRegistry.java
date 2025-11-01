package org.springframework.web.servlet.config.annotation;

public class ResourceHandlerRegistry {
    public ResourceHandlerRegistration addResourceHandler(String... patterns) {
        return new ResourceHandlerRegistration();
    }

    public static class ResourceHandlerRegistration {
        public ResourceHandlerRegistration addResourceLocations(String... locations) {
            return this;
        }
    }
}
