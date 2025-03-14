package com.gptuessr.ai_game.config;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class DotenvConfig {

    private static final Logger logger = LoggerFactory.getLogger(DotenvConfig.class);
    private final ConfigurableEnvironment environment;

    public DotenvConfig(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @PostConstruct
    public void loadDotEnv() {
        try {
            // Load .env file
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // Create map of environment variables from .env
            Map<String, Object> envVars = new HashMap<>();

            // Load Clerk variables
            addVariable(envVars, dotenv, "CLERK_API_KEY");
            addVariable(envVars, dotenv, "CLERK_WEBHOOK_SECRET");
            addVariable(envVars, dotenv, "CLERK_FRONTEND_API");
            addVariable(envVars, dotenv, "CLERK_ALLOWED_ORIGINS");

            // Add any other environment variables you need here
            
            // Add to Spring environment if we have any variables
            if (!envVars.isEmpty()) {
                environment.getPropertySources()
                        .addFirst(new MapPropertySource("dotenvProperties", envVars));
                logger.info("Loaded environment variables from .env file");
            } else {
                logger.warn("No environment variables loaded from .env file");
            }

        } catch (Exception e) {
            logger.error("Error loading .env file", e);
        }
    }

    private void addVariable(Map<String, Object> envVars, Dotenv dotenv, String key) {
        String value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            envVars.put(key, value);
            logger.debug("Loaded environment variable: {}", key);
        } else {
            logger.warn("Environment variable not found in .env: {}", key);
        }
    }
}
