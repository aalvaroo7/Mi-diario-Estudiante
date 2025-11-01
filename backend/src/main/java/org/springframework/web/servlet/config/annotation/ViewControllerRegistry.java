package org.springframework.web.servlet.config.annotation;

public class ViewControllerRegistry {
    public ViewControllerRegistration addViewController(String path) {
        return new ViewControllerRegistration();
    }

    public static class ViewControllerRegistration {
        public ViewControllerRegistration setViewName(String viewName) {
            return this;
        }
    }
}
