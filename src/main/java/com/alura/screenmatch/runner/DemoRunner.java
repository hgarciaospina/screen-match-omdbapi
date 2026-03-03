package com.alura.screenmatch.runner;

import com.alura.screenmatch.model.*;
import com.alura.screenmatch.service.ConsumoAPI;
import com.alura.screenmatch.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Runner demostrativo estructurado.
 *
 * - Imprime la información jerárquicamente:
 *   Serie
 *     └─ Temporadas
 *         └─ Episodios
 *
 * - Maneja validaciones internas para evitar excepciones.
 * - No imprime JSON crudo.
 *
 * Perfil: dev-demo
 */
@Component
@Profile("dev-demo")
public class DemoRunner implements CommandLineRunner {

    private final String urlBase;
    private final String apiKey;

    public DemoRunner(
            @Qualifier("omdbUrl") String urlBase,
            @Qualifier("omdbApiKey") String apiKey) {
        this.urlBase = urlBase;
        this.apiKey = apiKey;
    }

    @Override
    public void run(String... args) {

        ConsumoAPI consumoApi = new ConsumoAPI();
        ConvierteDatos convierteDatos = new ConvierteDatos();

        String nombreSerie = "game+of+thrones";

        try {

            // =========================
            // 1️⃣ CONSULTA SERIE
            // =========================
            String urlSerie = urlBase + "?t=" + nombreSerie + "&apikey=" + apiKey;
            String jsonSerie = consumoApi.obtenerDatos(urlSerie);

            if (jsonSerie == null || jsonSerie.isBlank()) {
                System.out.println("❌ No se pudo obtener respuesta de la API.");
                return;
            }

            DatosSerie serie =
                    convierteDatos.obtenerDatos(jsonSerie, DatosSerie.class);

            if (serie == null || serie.totalTemporadas() == null) {
                System.out.println("❌ Serie no encontrada.");
                return;
            }

            imprimirSerie(serie);

            int totalTemporadas;

            try {
                totalTemporadas = Integer.parseInt(serie.totalTemporadas());
            } catch (NumberFormatException e) {
                System.out.println("❌ Error al interpretar el número de temporadas.");
                return;
            }

            // =========================
            // 2️⃣ CONSULTA TEMPORADAS
            // =========================
            List<DatosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= totalTemporadas; i++) {

                String urlTemporada = urlBase
                        + "?t=" + nombreSerie
                        + "&Season=" + i
                        + "&apikey=" + apiKey;

                String jsonTemporada = consumoApi.obtenerDatos(urlTemporada);

                if (jsonTemporada == null || jsonTemporada.isBlank()) {
                    continue; // evita romper ejecución
                }

                DatosTemporada temporada =
                        convierteDatos.obtenerDatos(jsonTemporada, DatosTemporada.class);

                if (temporada != null && temporada.episodios() != null) {
                    temporadas.add(temporada);
                }
            }

            imprimirTemporadas(temporadas);

        } catch (Exception e) {
            System.out.println("❌ Error inesperado al consumir la API.");
        }
    }

    /**
     * Imprime datos principales de la serie.
     */
    private void imprimirSerie(DatosSerie serie) {

        System.out.println("\n══════════════════════════════════════");
        System.out.println("                SERIE");
        System.out.println("══════════════════════════════════════");

        System.out.printf("Título:           %s%n", serie.titulo());
        System.out.printf("Año:              %s%n", serie.anio());
        System.out.printf("Calificación:     %s%n", serie.caificacion());
        System.out.printf("Tipo:             %s%n", serie.tipo());
        System.out.printf("Total Temporadas: %s%n", serie.totalTemporadas());

        System.out.println("══════════════════════════════════════");
    }

    /**
     * Imprime temporadas y episodios jerárquicamente.
     */
    private void imprimirTemporadas(List<DatosTemporada> temporadas) {

        if (temporadas.isEmpty()) {
            System.out.println("\n⚠ No se encontraron temporadas.");
            return;
        }

        System.out.println("\nTEMPORADAS");
        System.out.println("──────────────────────────────────────");

        for (DatosTemporada temporada : temporadas) {

            System.out.printf("%n┌─ Temporada %s%n", temporada.temporada());
            System.out.println("│");

            for (DatosEpisodio episodio : temporada.episodios()) {

                System.out.printf(
                        "│  ├─ E%02d | %-35s | ⭐ %-4s | %s%n",
                        episodio.numeroEpisodio(),
                        episodio.titulo(),
                        episodio.evaluacion(),
                        episodio.fechaDeLanzamiento()
                );
            }

            System.out.println("│");
        }

        System.out.println("──────────────────────────────────────\n");
    }
}