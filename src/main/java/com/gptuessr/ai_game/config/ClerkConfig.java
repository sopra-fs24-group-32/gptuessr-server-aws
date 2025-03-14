package com.gptuessr.ai_game.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class ClerkConfig implements WebMvcConfigurer {
    @Value("${clerk.api.key}")
    private String clerkApiKey;
   
    @Value("${clerk.frontend.api}")
    private String clerkFrontendApi;
   
    @Value("${clerk.allowed.origins}")
    private String allowedOrigins;
   
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
   
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add authentication interceptor for protected routes
        registry.addInterceptor(clerkAuthInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/users/webhook",
                    "/api/users/register",
                    "/api/users/login",
                    "/api/users/logout",
                    "/api/health"
                );
    }
   
    @Bean
    public HandlerInterceptor clerkAuthInterceptor() {
        return new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                // Get authorization header
                String authHeader = request.getHeader("Authorization");
               
                // Check if header exists and starts with Bearer
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Unauthorized: Missing or invalid Authorization header");
                    return false;
                }
               
                // Extract the JWT token
                String token = authHeader.substring(7);
               
                // In a real implementation, you would verify the JWT token with Clerk's API
                // This is a simplified version that just checks if a token is present
                // For production, you should use Clerk's SDK or validate with their API
               
                // Add user info to request attributes for use in controllers
                // You might decode the JWT to get user information
                request.setAttribute("clerkToken", token);
               
                return true;
            }
        };
    }
}