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
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * ==========================================================
 * EstadisticasSerieRunner
 * ==========================================================
 *
 * Runner que:
 *
 * 1️⃣ Obtiene todos los episodios de una serie.
 * 2️⃣ Filtra evaluaciones mayores a 0.
 * 3️⃣ Agrupa por temporada y calcula promedio.
 * 4️⃣ Calcula estadísticas globales usando DoubleSummaryStatistics.
 * 5️⃣ Imprime resultados en formato profesional y claro.
 *
 * PERFIL:
 *      dev-estadisticas-serie
 *
 * CONCEPTOS:
 *  ✔ groupingBy
 *  ✔ averagingDouble
 *  ✔ DoubleSummaryStatistics
 *  ✔ Optional
 *  ✔ TreeMap
 */
@Component
@Profile("dev-estadisticas-serie")
public class EstadisticasSerieRunner implements CommandLineRunner {

    private final String urlBase;
    private final String apiKey;

    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos convierteDatos = new ConvierteDatos();

    public EstadisticasSerieRunner(
            @Qualifier("omdbUrl") String urlBase,
            @Qualifier("omdbApiKey") String apiKey) {
        this.urlBase = urlBase;
        this.apiKey = apiKey;
    }

    @Override
    public void run(String... args) {

        System.out.println("==================================================");
        System.out.println("       ESTADÍSTICAS COMPLETAS DE LA SERIE");
        System.out.println("==================================================");

        System.out.print("Ingrese el nombre de la serie: ");
        String nombreSerie = teclado.nextLine().trim();

        obtenerEpisodios(nombreSerie)
                .ifPresentOrElse(
                        episodios -> mostrarResultados(nombreSerie, episodios),
                        () -> System.out.println("❌ No se encontraron datos válidos.")
                );
    }

    /**
     * Obtiene todos los episodios con evaluación > 0.
     */
    private Optional<List<Episodio>> obtenerEpisodios(String nombreSerie) {

        try {
            String urlSerie = construirUrl(nombreSerie, null);
            String jsonSerie = consumoAPI.obtenerDatos(urlSerie);

            if (jsonSerie == null || jsonSerie.isBlank())
                return Optional.empty();

            DatosSerie serie =
                    convierteDatos.obtenerDatos(jsonSerie, DatosSerie.class);

            if (serie == null || "False".equalsIgnoreCase(serie.respuesta()))
                return Optional.empty();

            int totalTemporadas =
                    Integer.parseInt(serie.totalTemporadas());

            List<Episodio> episodios = IntStream
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
                    .toList();

            return episodios.isEmpty()
                    ? Optional.empty()
                    : Optional.of(episodios);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Construye URL OMDb.
     */
    private String construirUrl(String nombreSerie, Integer temporada) {

        return urlBase +
                "?t=" + nombreSerie.replace(" ", "+") +
                (temporada != null ? "&Season=" + temporada : "") +
                "&apikey=" + apiKey;
    }

    /**
     * Muestra promedio por temporada y estadísticas globales.
     */
    private void mostrarResultados(String nombreSerie,
                                   List<Episodio> episodios) {

        Map<Integer, Double> promedioPorTemporada =
                episodios.stream()
                        .collect(Collectors.groupingBy(
                                Episodio::getTemporada,
                                TreeMap::new,
                                Collectors.averagingDouble(
                                        Episodio::getEvaluacion
                                )
                        ));

        DoubleSummaryStatistics stats =
                episodios.stream()
                        .mapToDouble(Episodio::getEvaluacion)
                        .summaryStatistics();

        System.out.println("\n==================================================");
        System.out.println(" Serie: " + nombreSerie);
        System.out.println("==================================================");

        System.out.println("\n🎬 PROMEDIO POR TEMPORADA");
        System.out.println("--------------------------------------------------");

        promedioPorTemporada.forEach((temp, prom) ->
                System.out.printf(
                        "Temporada %-3d ➜ ⭐ Promedio: %.2f%n",
                        temp,
                        prom
                )
        );

        System.out.println("\n📊 ESTADÍSTICAS GLOBALES");
        System.out.println("--------------------------------------------------");
        System.out.printf("Cantidad de episodios evaluados: %d%n", stats.getCount());
        System.out.printf("Evaluación mínima: %.2f%n", stats.getMin());
        System.out.printf("Evaluación máxima: %.2f%n", stats.getMax());
        System.out.printf("Promedio general: %.2f%n", stats.getAverage());
        System.out.printf("Suma total evaluaciones: %.2f%n", stats.getSum());
        System.out.println("==================================================\n");
    }
}