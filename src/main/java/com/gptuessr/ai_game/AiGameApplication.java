package com.gptuessr.ai_game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RestController
@SpringBootApplication
public class AiGameApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiGameApplication.class, args);
	}

	@GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
  	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public String helloWorld() {
		return "The application is running.";
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
		@Override
		public void addCorsMappings(CorsRegistry registry) {
			registry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
		}
		};
	}

}
