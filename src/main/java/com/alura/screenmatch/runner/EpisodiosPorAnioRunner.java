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
 * EpisodiosPorAnioRunner
 * ==========================================================
 *
 * Runner especializado que permite:
 *
 * 1️⃣ Solicitar el nombre de una serie.
 * 2️⃣ Solicitar a partir de qué año desea consultar episodios.
 * 3️⃣ Obtener todas las temporadas desde OMDb.
 * 4️⃣ Convertir DatosEpisodio → Episodio (modelo dominio).
 * 5️⃣ Filtrar episodios cuya fecha sea >= año ingresado.
 * 6️⃣ Ordenar cronológicamente.
 * 7️⃣ Mostrar resultados usando toString() estético.
 *
 * CARACTERÍSTICAS TÉCNICAS:
 *  ✔ Programación funcional (Streams y Lambdas)
 *  ✔ Uso de Optional
 *  ✔ Manejo defensivo ante errores
 *  ✔ Conversión segura de fechas
 *  ✔ Separación DTO → Modelo Dominio
 *
 * Perfil requerido:
 *      dev-episodios-por-anio
 */
@Component
@Profile("dev-episodios-por-anio")
public class EpisodiosPorAnioRunner implements CommandLineRunner {

    private final String urlBase;
    private final String apiKey;

    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos convierteDatos = new ConvierteDatos();

    public EpisodiosPorAnioRunner(
            @Qualifier("omdbUrl") String urlBase,
            @Qualifier("omdbApiKey") String apiKey) {
        this.urlBase = urlBase;
        this.apiKey = apiKey;
    }

    @Override
    public void run(String... args) {

        System.out.println("==================================================");
        System.out.println("      EPISODIOS FILTRADOS POR AÑO");
        System.out.println("==================================================");

        System.out.print("Ingrese el nombre de la serie: ");
        String nombreSerie = teclado.nextLine().trim();

        if (nombreSerie.isEmpty()) {
            System.out.println("❌ Debe ingresar un nombre válido.");
            return;
        }

        System.out.print("¿A partir de qué año desea ver episodios?: ");
        String entradaAnio = teclado.nextLine().trim();

        int anioFiltro;

        try {
            anioFiltro = Integer.parseInt(entradaAnio);
        } catch (NumberFormatException e) {
            System.out.println("❌ Año inválido.");
            return;
        }

        obtenerSerie(nombreSerie)
                .ifPresentOrElse(
                        serie -> obtenerEpisodiosDesde(nombreSerie, serie, anioFiltro)
                                .ifPresentOrElse(
                                        this::mostrarResultado,
                                        () -> System.out.println("⚠ No se encontraron episodios desde ese año.")
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
     * Obtiene episodios filtrados desde un año específico.
     */
    private Optional<List<Episodio>> obtenerEpisodiosDesde(
            String nombreSerie,
            DatosSerie serie,
            int anioFiltro) {

        try {

            int totalTemporadas =
                    Integer.parseInt(serie.totalTemporadas());

            List<Episodio> episodiosFiltrados = IntStream
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
                    .filter(e -> e.getFechaLanzamiento() != null)
                    .filter(e -> e.getFechaLanzamiento().getYear() >= anioFiltro)
                    .sorted(Comparator.comparing(Episodio::getFechaLanzamiento))
                    .toList();

            return episodiosFiltrados.isEmpty()
                    ? Optional.empty()
                    : Optional.of(episodiosFiltrados);

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
     * Muestra episodios usando toString().
     */
    private void mostrarResultado(List<Episodio> episodios) {

        System.out.println("\n==================================================");
        System.out.println("          RESULTADOS FILTRADOS");
        System.out.println("==================================================");

        episodios.forEach(System.out::println);

        System.out.println("==================================================\n");
    }
}