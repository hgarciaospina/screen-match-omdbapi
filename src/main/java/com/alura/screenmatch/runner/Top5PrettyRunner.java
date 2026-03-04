package com.alura.screenmatch.runner;

import com.alura.screenmatch.model.*;
import com.alura.screenmatch.service.ConsumoAPI;
import com.alura.screenmatch.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * ==========================================================
 * Top5PrettyRunner
 * ==========================================================
 *
 * Runner que:
 *
 * 1️⃣ Solicita nombre de serie.
 * 2️⃣ Obtiene todas las temporadas.
 * 3️⃣ Convierte DatosEpisodio → Episodio (modelo de dominio).
 * 4️⃣ Ordena por evaluación descendente.
 * 5️⃣ Muestra el Top 5 utilizando toString().
 *
 * PRINCIPIOS APLICADOS:
 *  ✔ Programación funcional (Streams)
 *  ✔ Separación modelo API vs modelo dominio
 *  ✔ Manejo defensivo de errores
 *  ✔ Comparator y lambdas
 *  ✔ Código limpio y legible
 *
 * Perfil:
 *      dev-top5-pretty
 */
@Component
@Profile("dev-top5-pretty")
public class Top5PrettyRunner implements CommandLineRunner {

    private final String urlBase;
    private final String apiKey;

    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos convierteDatos = new ConvierteDatos();

    public Top5PrettyRunner(
            @Qualifier("omdbUrl") String urlBase,
            @Qualifier("omdbApiKey") String apiKey) {
        this.urlBase = urlBase;
        this.apiKey = apiKey;
    }

    @Override
    public void run(String... args) {

        System.out.println("==================================================");
        System.out.println("              TOP 5 EPISODIOS                     ");
        System.out.println("==================================================");
        System.out.print("Ingrese el nombre de la serie: ");

        String nombreSerie = teclado.nextLine().trim();

        if (nombreSerie.isEmpty()) {
            System.out.println("❌ Debe ingresar un nombre válido.");
            return;
        }

        obtenerSerie(nombreSerie)
                .ifPresentOrElse(
                        serie -> obtenerTop5(nombreSerie, serie)
                                .ifPresentOrElse(
                                        this::mostrarResultado,
                                        () -> System.out.println("⚠ No se encontraron episodios válidos.")
                                ),
                        () -> System.out.println("❌ Serie no encontrada.")
                );
    }

    private Optional<DatosSerie> obtenerSerie(String nombreSerie) {

        try {
            String url = construirUrl(nombreSerie, null);
            String json = consumoAPI.obtenerDatos(url);

            if (json == null || json.isBlank())
                return Optional.empty();

            DatosSerie serie =
                    convierteDatos.obtenerDatos(json, DatosSerie.class);

            if (serie == null || "False".equalsIgnoreCase(serie.respuesta()))
                return Optional.empty();

            return Optional.of(serie);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<List<Episodio>> obtenerTop5(
            String nombreSerie,
            DatosSerie serie) {

        try {

            int totalTemporadas =
                    Integer.parseInt(serie.totalTemporadas());

            List<Episodio> top5 = IntStream
                    .rangeClosed(1, totalTemporadas)
                    .mapToObj(temp -> {

                        String url = construirUrl(nombreSerie, temp);
                        String json = consumoAPI.obtenerDatos(url);

                        if (json == null || json.isBlank())
                            return null;

                        return convierteDatos.obtenerDatos(
                                json,
                                DatosTemporada.class
                        );
                    })
                    .filter(t -> t != null && t.episodios() != null)
                    .flatMap(t -> t.episodios()
                            .stream()
                            .filter(e -> e.evaluacion() != null)
                            .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))
                            .map(e -> new Episodio(
                                    Integer.parseInt(String.valueOf(t.temporada())), e
                            ))
                    )
                    .sorted(Comparator
                            .comparingDouble(Episodio::getEvaluacion)
                            .reversed())
                    .limit(5)
                    .toList();

            return top5.isEmpty()
                    ? Optional.empty()
                    : Optional.of(top5);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String construirUrl(String nombreSerie, Integer temporada) {

        return urlBase +
                "?t=" + nombreSerie.replace(" ", "+") +
                (temporada != null ? "&Season=" + temporada : "") +
                "&apikey=" + apiKey;
    }

    private void mostrarResultado(List<Episodio> episodios) {

        System.out.println("\n==================================================");
        System.out.println("                 TOP 5");
        System.out.println("==================================================");

        episodios.forEach(System.out::println);

        System.out.println("==================================================\n");
    }
}