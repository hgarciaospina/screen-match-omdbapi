package com.alura.screenmatch.runner;

import com.alura.screenmatch.principal.Principal;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Runner que ejecuta la versión interactiva por consola.
 * Se activa únicamente cuando el perfil "dev-cli" está activo.
 */
@Component
@Profile("dev-cli")
public class CliRunner implements CommandLineRunner {

    private final String urlBase;
    private final String apiKey;

    public CliRunner(
            @Qualifier("omdbUrl") String urlBase,
            @Qualifier("omdbApiKey") String apiKey) {
        this.urlBase = urlBase;
        this.apiKey = apiKey;
    }

    @Override
    public void run(String... args) {
        Principal principal = new Principal(urlBase, apiKey);
        principal.muestraElMenu();
    }
}