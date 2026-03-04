package com.alura.screenmatch.runner;

import com.alura.screenmatch.model.*;
import com.alura.screenmatch.service.ConsumoAPI;
import com.alura.screenmatch.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * ==========================================================
 * BusquedaParcialTituloRunner
 * ==========================================================
 *
 * Runner especializado que permite:
 *
 * 1️⃣ Solicitar el nombre de una serie.
 * 2️⃣ Solicitar parte del título de un episodio.
 * 3️⃣ Buscar la PRIMERA coincidencia.
 * 4️⃣ Ignorar mayúsculas/minúsculas en la comparación.
 * 5️⃣ Utilizar Optional para manejo seguro del resultado.
 * 6️⃣ Mostrar mensaje claro si no existe coincidencia.
 *
 * CARACTERÍSTICAS TÉCNICAS:
 *  ✔ Programación funcional (Streams)
 *  ✔ Uso de flatMap
 *  ✔ Uso correcto de Optional
 *  ✔ Comparación estandarizada con toLowerCase()
 *  ✔ Manejo defensivo de API
 *
 * Perfil:
 *      dev-busqueda-parcial
 */
@Component
@Profile("dev-busqueda-parcial")
public class BusquedaParcialTituloRunner implements CommandLineRunner {

    private final String urlBase;
    private final String apiKey;

    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos convierteDatos = new ConvierteDatos();

    public BusquedaParcialTituloRunner(
            @Qualifier("omdbUrl") String urlBase,
            @Qualifier("omdbApiKey") String apiKey) {
        this.urlBase = urlBase;
        this.apiKey = apiKey;
    }

    @Override
    public void run(String... args) {

        System.out.println("==================================================");
        System.out.println("     BÚSQUEDA DE EPISODIO POR TÍTULO PARCIAL");
        System.out.println("==================================================");

        System.out.print("Ingrese el nombre de la serie: ");
        String nombreSerie = teclado.nextLine().trim();

        if (nombreSerie.isEmpty()) {
            System.out.println("❌ Debe ingresar un nombre válido.");
            return;
        }

        System.out.print("Ingrese parte del título del episodio: ");
        String tituloParcial = teclado.nextLine().trim();

        if (tituloParcial.isEmpty()) {
            System.out.println("❌ Debe ingresar texto para buscar.");
            return;
        }

        obtenerSerie(nombreSerie)
                .flatMap(serie -> buscarPrimerCoincidencia(nombreSerie, serie, tituloParcial))
                .ifPresentOrElse(
                        episodio -> {
                            System.out.println("\n✅ Episodio encontrado:");
                            System.out.println(episodio);
                        },
                        () -> System.out.println("⚠ No se encontraron episodios que coincidan con ese título.")
                );
    }

    /**
     * Obtiene información general de la serie validando campo Response.
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
     * Busca la primera coincidencia de título parcial ignorando mayúsculas.
     */
    private Optional<Episodio> buscarPrimerCoincidencia(
            String nombreSerie,
            DatosSerie serie,
            String tituloParcial) {

        try {

            int totalTemporadas =
                    Integer.parseInt(serie.totalTemporadas());

            String tituloNormalizado = tituloParcial.toLowerCase();

            return IntStream
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
                    .filter(ep ->
                            ep.getTitulo() != null &&
                                    ep.getTitulo()
                                            .toLowerCase()
                                            .contains(tituloNormalizado)
                    )
                    .findFirst();

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
}