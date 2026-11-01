package com.wishdiary.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.wishdiary.adapter.in.web.auth.AuthenticatedSessionArgumentResolver;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticatedSessionArgumentResolver authenticatedSessionArgumentResolver;
    private final List<String> allowedOrigins;

    public WebConfig(AuthenticatedSessionArgumentResolver authenticatedSessionArgumentResolver,
                     @Value("${wishdiary.web.allowed-origins}") List<String> allowedOrigins) {
        this.authenticatedSessionArgumentResolver = authenticatedSessionArgumentResolver;
        this.allowedOrigins = List.copyOf(allowedOrigins);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedSessionArgumentResolver);
    }
}
