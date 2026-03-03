package com.alura.screenmatch.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Clase de configuración que carga las variables
 * desde el archivo .env y las expone como Beans
 * identificados por nombre.
 */
@Configuration
public class EnvConfig {

    private final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    @Bean(name = "omdbUrl")
    public String omdbUrl() {
        return dotenv.get("OMDB_URL");
    }

    @Bean(name = "omdbApiKey")
    public String omdbApiKey() {
        return dotenv.get("OMDB_API_KEY");
    }
}