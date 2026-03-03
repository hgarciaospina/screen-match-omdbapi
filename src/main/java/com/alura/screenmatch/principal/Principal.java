package com.alura.screenmatch.principal;

import com.alura.screenmatch.model.DatosSerie;
import com.alura.screenmatch.model.DatosTemporada;
import com.alura.screenmatch.service.ConsumoAPI;
import com.alura.screenmatch.service.ConvierteDatos;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.IntStream;

/**
 * ==========================================================
 * Principal
 * ==========================================================
 *
 * Clase responsable de la interacción con el usuario vía consola.
 *
 * RESPONSABILIDADES:
 *  ✔ Solicitar entrada del usuario.
 *  ✔ Construir dinámicamente URLs para la API OMDb.
 *  ✔ Validar respuestas externas.
 *  ✔ Evitar NullPointerException.
 *  ✔ Orquestar la obtención de:
 *        - Datos generales de la serie
 *        - Temporadas
 *        - Episodios
 *  ✔ Presentar la información de manera estructurada.
 *
 * CARACTERÍSTICAS TÉCNICAS:
 *  - Uso de programación funcional (Streams y Lambdas).
 *  - Uso de Optional para manejo seguro de valores.
 *  - Validación explícita del campo "Response" devuelto por OMDb.
 *  - Manejo defensivo ante fallos de red o datos inválidos.
 *
 * Esta clase actúa como ORQUESTADOR entre:
 *  - Usuario
 *  - ConsumoAPI (cliente HTTP)
 *  - ConvierteDatos (deserialización JSON → Objetos)
 *
 * No contiene lógica de negocio compleja.
 */
public class Principal {

    private final String urlBase;
    private final String apiKey;

    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos convierteDatos = new ConvierteDatos();

    /**
     * Constructor que recibe configuración externa.
     *
     * @param omdbUrl    URL base de OMDb
     * @param omdbApiKey API Key de autenticación
     */
    public Principal(String omdbUrl, String omdbApiKey) {
        this.urlBase = omdbUrl;
        this.apiKey = omdbApiKey;
    }

    /**
     * Método principal que controla el flujo completo:
     *
     * 1️⃣ Solicita nombre de serie.
     * 2️⃣ Consulta datos generales.
     * 3️⃣ Valida respuesta lógica de la API.
     * 4️⃣ Consulta temporadas.
     * 5️⃣ Presenta información estructurada.
     *
     * Nunca lanza excepciones visibles al usuario.
     */
    public void muestraElMenu() {

        mostrarEncabezado();

        String nombreSerie = teclado.nextLine().trim();

        if (nombreSerie.isEmpty()) {
            System.out.println("❌ Debe ingresar un nombre válido.");
            return;
        }

        obtenerSerie(nombreSerie)
                .ifPresentOrElse(serie -> {

                    mostrarDatosSerie(serie);

                    obtenerTemporadas(nombreSerie, serie)
                            .ifPresentOrElse(
                                    this::mostrarTemporadas,
                                    () -> System.out.println("⚠ No se encontraron temporadas.")
                            );

                }, () -> System.out.println("❌ Serie no encontrada o sin acceso a la API."));
    }

    /**
     * Consulta la información general de una serie.
     *
     * VALIDACIONES:
     *  - Respuesta nula o vacía.
     *  - Objeto deserializado nulo.
     *  - Campo "Response" = "False" (serie inexistente).
     *
     * @param nombreSerie Nombre ingresado por el usuario.
     * @return Optional con DatosSerie válido.
     */
    private Optional<DatosSerie> obtenerSerie(String nombreSerie) {

        try {

            String url = construirUrl(nombreSerie, null);
            String json = consumoAPI.obtenerDatos(url);

            if (json == null || json.isBlank()) {
                return Optional.empty();
            }

            DatosSerie serie =
                    convierteDatos.obtenerDatos(json, DatosSerie.class);

            if (serie == null) {
                return Optional.empty();
            }

            // 🔥 VALIDACIÓN CLAVE
            if ("False".equalsIgnoreCase(serie.respuesta())) {
                return Optional.empty();
            }

            return Optional.of(serie);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Obtiene todas las temporadas de la serie usando programación funcional.
     *
     * Utiliza IntStream para evitar bucles imperativos.
     *
     * VALIDACIONES:
     *  - totalTemporadas numéricamente válido.
     *  - Respuestas JSON válidas.
     *  - Temporadas no nulas.
     *
     * @param nombreSerie Nombre de la serie.
     * @param serie       Datos generales previamente validados.
     * @return Optional con lista de temporadas.
     */
    private Optional<List<DatosTemporada>> obtenerTemporadas(
            String nombreSerie,
            DatosSerie serie) {

        try {

            int totalTemporadas = Integer.parseInt(serie.totalTemporadas());

            List<DatosTemporada> temporadas = IntStream
                    .rangeClosed(1, totalTemporadas)
                    .mapToObj(numeroTemporada -> {

                        String url = construirUrl(nombreSerie, numeroTemporada);
                        String json = consumoAPI.obtenerDatos(url);

                        if (json == null || json.isBlank()) {
                            return null;
                        }

                        return convierteDatos.obtenerDatos(
                                json,
                                DatosTemporada.class
                        );
                    })
                    .filter(temporada ->
                            temporada != null &&
                                    temporada.episodios() != null)
                    .toList();

            return temporadas.isEmpty()
                    ? Optional.empty()
                    : Optional.of(temporadas);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Construye dinámicamente la URL para consumir la API OMDb.
     *
     * @param nombreSerie Nombre de la serie.
     * @param temporada   Número de temporada (null si es consulta general).
     * @return URL correctamente formateada.
     */
    private String construirUrl(String nombreSerie, Integer temporada) {

        return urlBase +
                "?t=" + nombreSerie.replace(" ", "+") +
                (temporada != null ? "&Season=" + temporada : "") +
                "&apikey=" + apiKey;
    }

    /**
     * Muestra encabezado visual de la aplicación.
     */
    private void mostrarEncabezado() {

        System.out.println("==================================================");
        System.out.println("           BÚSQUEDA DE SERIES - OMDb");
        System.out.println("==================================================");
        System.out.print("Ingrese el nombre de la serie: ");
    }

    /**
     * Presenta la información general validada de la serie.
     *
     * @param datos Serie previamente validada.
     */
    private void mostrarDatosSerie(DatosSerie datos) {

        System.out.println("\n==================================================");
        System.out.println("                INFORMACIÓN GENERAL");
        System.out.println("==================================================");

        System.out.printf("Título:           %s%n", datos.titulo());
        System.out.printf("Año:              %s%n", datos.anio());
        System.out.printf("Calificación:     %s%n", datos.caificacion());
        System.out.printf("Tipo:             %s%n", datos.tipo());
        System.out.printf("Total Temporadas: %s%n", datos.totalTemporadas());

        System.out.println("==================================================\n");
    }

    /**
     * Presenta temporadas y episodios utilizando programación funcional.
     *
     * @param temporadas Lista validada de temporadas.
     */
    private void mostrarTemporadas(List<DatosTemporada> temporadas) {

        System.out.println("==================================================");
        System.out.println("              DETALLE DE TEMPORADAS");
        System.out.println("==================================================");

        temporadas.forEach(temporada -> {

            System.out.printf("%nTemporada %s%n", temporada.temporada());
            System.out.println("--------------------------------------------------");

            temporada.episodios()
                    .forEach(episodio ->
                            System.out.printf(
                                    "E%02d | %-40s | ⭐ %-4s | %s%n",
                                    episodio.numeroEpisodio(),
                                    episodio.titulo(),
                                    episodio.evaluacion(),
                                    episodio.fechaDeLanzamiento()
                            )
                    );
        });

        System.out.println("\n==================================================");
    }
}