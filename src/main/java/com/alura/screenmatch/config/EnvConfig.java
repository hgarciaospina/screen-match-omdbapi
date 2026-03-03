package com.alura.screenmatch.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    private final Dotenv dotenv = Dotenv.load();

    @Bean
    public String omdbUrl() {
        return dotenv.get("OMDB_URL");
    }

    @Bean
    public String omdbApiKey() {
        return dotenv.get("OMDB_API_KEY");
    }
}