package com.kencana.salesservice;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Memetakan URL /uploads/** ke folder fisik uploads/ di direktori server
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}