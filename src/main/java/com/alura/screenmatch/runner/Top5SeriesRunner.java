package com.alura.screenmatch.runner;

import com.alura.screenmatch.model.DatosEpisodio;
import com.alura.screenmatch.model.DatosSerie;
import com.alura.screenmatch.model.DatosTemporada;
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
 * Top5SeriesRunner
 * ==========================================================
 *
 * Runner especializado que:
 *
 * 1️⃣ Solicita al usuario el nombre de una serie.
 * 2️⃣ Consulta todas sus temporadas.
 * 3️⃣ Extrae todos los episodios.
 * 4️⃣ Filtra episodios cuya evaluación sea válida (≠ "N/A").
 * 5️⃣ Convierte evaluaciones a Double.
 * 6️⃣ Ordena de mayor a menor.
 * 7️⃣ Devuelve los 5 episodios mejor evaluados.
 *
 * CARACTERÍSTICAS TÉCNICAS:
 *  ✔ Programación funcional pura (Streams y Lambdas)
 *  ✔ Sin bucles imperativos
 *  ✔ Sin NullPointerException
 *  ✔ Manejo defensivo ante errores externos
 *  ✔ Ordenamiento con Comparator
 *
 * Perfil requerido:
 *      dev-top5
 */
@Component
@Profile("dev-top5")
public class Top5SeriesRunner implements CommandLineRunner {

    private final String urlBase;
    private final String apiKey;

    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos convierteDatos = new ConvierteDatos();

    public Top5SeriesRunner(
            @Qualifier("omdbUrl") String urlBase,
            @Qualifier("omdbApiKey") String apiKey) {
        this.urlBase = urlBase;
        this.apiKey = apiKey;
    }

    @Override
    public void run(String... args) {

        System.out.println("==================================================");
        System.out.println("        TOP 5 EPISODIOS MEJOR EVALUADOS");
        System.out.println("==================================================");
        System.out.print("Ingrese el nombre de la serie: ");

        String nombreSerie = teclado.nextLine().trim();

        if (nombreSerie.isEmpty()) {
            System.out.println("❌ Debe ingresar un nombre válido.");
            return;
        }

        obtenerSerie(nombreSerie)
                .ifPresentOrElse(
                        serie -> obtenerTop5Episodios(nombreSerie, serie)
                                .ifPresentOrElse(
                                        this::mostrarTop5,
                                        () -> System.out.println("⚠ No se encontraron episodios válidos.")
                                ),
                        () -> System.out.println("❌ Serie no encontrada.")
                );
    }

    /**
     * Obtiene información general validando campo "Response".
     */
    private Optional<DatosSerie> obtenerSerie(String nombreSerie) {

        try {
            String url = construirUrl(nombreSerie, null);
            String json = consumoAPI.obtenerDatos(url);

            if (json == null || json.isBlank()) return Optional.empty();

            DatosSerie serie =
                    convierteDatos.obtenerDatos(json, DatosSerie.class);

            if (serie == null || "False".equalsIgnoreCase(serie.respuesta()))
                return Optional.empty();

            return Optional.of(serie);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Obtiene y procesa todos los episodios usando programación funcional.
     */
    private Optional<List<DatosEpisodio>> obtenerTop5Episodios(
            String nombreSerie,
            DatosSerie serie) {

        try {

            int totalTemporadas = Integer.parseInt(serie.totalTemporadas());

            List<DatosEpisodio> top5 = IntStream
                    .rangeClosed(1, totalTemporadas)
                    .mapToObj(temp -> {

                        String url = construirUrl(nombreSerie, temp);
                        String json = consumoAPI.obtenerDatos(url);

                        if (json == null || json.isBlank()) return null;

                        return convierteDatos.obtenerDatos(
                                json,
                                DatosTemporada.class
                        );
                    })
                    .filter(t -> t != null && t.episodios() != null)
                    .flatMap(t -> t.episodios().stream())
                    .filter(e -> e.evaluacion() != null)
                    .filter(e -> !e.evaluacion().equalsIgnoreCase("N/A"))
                    .filter(e -> esNumeroValido(e.evaluacion()))
                    .sorted(Comparator.comparingDouble(
                            (DatosEpisodio e) -> Double.parseDouble(e.evaluacion())
                    ).reversed())
                    .limit(5)
                    .toList();

            return top5.isEmpty()
                    ? Optional.empty()
                    : Optional.of(top5);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Valida que la evaluación sea numérica.
     */
    private boolean esNumeroValido(String valor) {
        try {
            Double.parseDouble(valor);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Construye URL dinámica para OMDb.
     */
    private String construirUrl(String nombreSerie, Integer temporada) {

        return urlBase +
                "?t=" + nombreSerie.replace(" ", "+") +
                (temporada != null ? "&Season=" + temporada : "") +
                "&apikey=" + apiKey;
    }

    /**
     * Muestra los 5 episodios mejor evaluados.
     */
    private void mostrarTop5(List<DatosEpisodio> episodios) {

        System.out.println("\n==================================================");
        System.out.println("        TOP 5 EPISODIOS MEJOR EVALUADOS");
        System.out.println("==================================================");

        episodios.forEach(ep ->
                System.out.printf(
                        "⭐ %-4s | E%02d | %-40s | %s%n",
                        ep.evaluacion(),
                        ep.numeroEpisodio(),
                        ep.titulo(),
                        ep.fechaDeLanzamiento()
                )
        );

        System.out.println("==================================================\n");
    }
}