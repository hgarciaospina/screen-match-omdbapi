package com.alura.screenmatch.runner;

import com.alura.screenmatch.model.*;
import com.alura.screenmatch.service.ConsumoAPI;
import com.alura.screenmatch.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * ==========================================================
 * PromedioPorTemporadaRunner
 * ==========================================================
 *
 * Runner especializado que:
 *
 * 1️⃣ Solicita el nombre de una serie.
 * 2️⃣ Obtiene todas las temporadas.
 * 3️⃣ Convierte DatosEpisodio → Episodio.
 * 4️⃣ Filtra evaluaciones mayores a cero.
 * 5️⃣ Agrupa episodios por temporada.
 * 6️⃣ Calcula promedio de evaluación por temporada.
 * 7️⃣ Almacena resultado en Map<Integer, Double>.
 * 8️⃣ Muestra resultado formateado profesionalmente.
 *
 * PERFIL:
 *      dev-promedio-temporadas
 *
 * CONCEPTOS APLICADOS:
 *  ✔ groupingBy
 *  ✔ averagingDouble
 *  ✔ flatMap
 *  ✔ Optional
 *  ✔ Map ordenado
 */
@Component
@Profile("dev-promedio-temporadas")
public class PromedioPorTemporadaRunner implements CommandLineRunner {

    private final String urlBase;
    private final String apiKey;

    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos convierteDatos = new ConvierteDatos();

    public PromedioPorTemporadaRunner(
            @Qualifier("omdbUrl") String urlBase,
            @Qualifier("omdbApiKey") String apiKey) {
        this.urlBase = urlBase;
        this.apiKey = apiKey;
    }

    @Override
    public void run(String... args) {

        System.out.println("==================================================");
        System.out.println("     PROMEDIO DE EVALUACIÓN POR TEMPORADA");
        System.out.println("==================================================");

        System.out.print("Ingrese el nombre de la serie: ");
        String nombreSerie = teclado.nextLine().trim();

        if (nombreSerie.isEmpty()) {
            System.out.println("❌ Debe ingresar un nombre válido.");
            return;
        }

        obtenerSerie(nombreSerie)
                .flatMap(serie -> calcularPromedios(nombreSerie, serie))
                .ifPresentOrElse(
                        mapa -> mostrarResultados(nombreSerie, mapa),
                        () -> System.out.println("⚠ No se encontraron datos suficientes para calcular promedios.")
                );
    }

    /**
     * Obtiene información general de la serie.
     */
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

    /**
     * Agrupa episodios por temporada y calcula promedio.
     */
    private Optional<Map<Integer, Double>> calcularPromedios(
            String nombreSerie,
            DatosSerie serie) {

        try {

            int totalTemporadas =
                    Integer.parseInt(serie.totalTemporadas());

            Map<Integer, Double> promedioPorTemporada = IntStream
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
                            .map(e -> new Episodio(
                                    Integer.parseInt(String.valueOf(t.temporada())), e
                            ))
                    )
                    .filter(e -> e.getEvaluacion() > 0)
                    .collect(Collectors.groupingBy(
                            Episodio::getTemporada,
                            Collectors.averagingDouble(Episodio::getEvaluacion)
                    ));

            return promedioPorTemporada.isEmpty()
                    ? Optional.empty()
                    : Optional.of(new TreeMap<>(promedioPorTemporada));

        } catch (Exception e) {
            return Optional.empty();
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
     * Muestra resultados de forma clara y profesional.
     */
    private void mostrarResultados(String nombreSerie,
                                   Map<Integer, Double> promedios) {

        System.out.println("\n==================================================");
        System.out.println(" Serie: " + nombreSerie);
        System.out.println(" Promedio de evaluación por temporada");
        System.out.println("==================================================");

        promedios.forEach((temporada, promedio) ->
                System.out.printf(
                        "🎬 Temporada %-3d ➜ ⭐ Promedio: %.2f%n",
                        temporada,
                        promedio
                )
        );

        System.out.println("==================================================\n");
    }
}