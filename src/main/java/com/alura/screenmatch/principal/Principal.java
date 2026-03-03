package com.alura.screenmatch.principal;

import com.alura.screenmatch.model.DatosEpisodio;
import com.alura.screenmatch.model.DatosSerie;
import com.alura.screenmatch.model.DatosTemporada;
import com.alura.screenmatch.service.ConsumoAPI;
import com.alura.screenmatch.service.ConvierteDatos;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Clase responsable de la interacción con el usuario vía consola.
 *
 * Responsabilidades:
 * - Solicitar entrada al usuario.
 * - Construir las URLs necesarias para consumir la API OMDb.
 * - Validar respuestas.
 * - Coordinar la obtención de datos generales y temporadas.
 * - Delegar la presentación formateada en métodos internos.
 *
 * No contiene lógica de negocio compleja.
 * Actúa como orquestador entre:
 * - Usuario
 * - ConsumoAPI (HTTP)
 * - ConvierteDatos (JSON → Objetos)
 */
public class Principal {

    private final String urlBase;
    private final String apiKey;

    private final Scanner teclado = new Scanner(System.in);
    private final ConsumoAPI consumoAPI = new ConsumoAPI();
    private final ConvierteDatos convierteDatos = new ConvierteDatos();

    /**
     * Constructor que recibe configuración externa (variables de entorno).
     *
     * @param omdbUrl    URL base de OMDb
     * @param omdbApiKey API Key de autenticación
     */
    public Principal(String omdbUrl, String omdbApiKey) {
        this.urlBase = omdbUrl;
        this.apiKey = omdbApiKey;
    }

    /**
     * Método principal de ejecución.
     * Controla todo el flujo de consulta.
     */
    public void muestraElMenu() {

        mostrarEncabezado();

        String nombreSerie = teclado.nextLine().trim();

        if (nombreSerie.isEmpty()) {
            System.out.println("❌ Debe ingresar un nombre válido.");
            return;
        }

        // ====== Consulta de información general ======
        String url = construirUrl(nombreSerie, null);
        String json = consumoAPI.obtenerDatos(url);
        DatosSerie datos = convierteDatos.obtenerDatos(json, DatosSerie.class);

        if (datos == null || datos.totalTemporadas() == null) {
            System.out.println("❌ Serie no encontrada.");
            return;
        }

        mostrarDatosSerie(datos);

        int totalTemporadas;

        try {
            totalTemporadas = Integer.parseInt(datos.totalTemporadas());
        } catch (NumberFormatException e) {
            System.out.println("❌ Error al interpretar el número de temporadas.");
            return;
        }

        // ====== Consulta de temporadas ======
        List<DatosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= totalTemporadas; i++) {

            url = construirUrl(nombreSerie, i);
            json = consumoAPI.obtenerDatos(url);

            DatosTemporada temporada =
                    convierteDatos.obtenerDatos(json, DatosTemporada.class);

            if (temporada != null) {
                temporadas.add(temporada);
            }
        }

        mostrarTemporadas(temporadas);
    }

    /**
     * Construye dinámicamente la URL para consultar la API.
     *
     * @param nombreSerie Nombre de la serie.
     * @param temporada   Número de temporada (null si es consulta general).
     * @return URL correctamente formateada.
     */
    private String construirUrl(String nombreSerie, Integer temporada) {

        StringBuilder url = new StringBuilder();
        url.append(urlBase)
                .append("?t=")
                .append(nombreSerie.replace(" ", "+"));

        if (temporada != null) {
            url.append("&Season=").append(temporada);
        }

        url.append("&apikey=").append(apiKey);

        return url.toString();
    }

    /**
     * Muestra el encabezado principal de la aplicación.
     */
    private void mostrarEncabezado() {
        System.out.println("==================================================");
        System.out.println("           BÚSQUEDA DE SERIES - OMDb");
        System.out.println("==================================================");
        System.out.print("Ingrese el nombre de la serie: ");
    }

    /**
     * Presenta la información general de la serie
     * en formato estructurado y alineado.
     *
     * @param datos Información general de la serie.
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
     * Presenta las temporadas y episodios formateados.
     *
     * @param temporadas Lista de temporadas obtenidas.
     */
    private void mostrarTemporadas(List<DatosTemporada> temporadas) {

        System.out.println("==================================================");
        System.out.println("              DETALLE DE TEMPORADAS");
        System.out.println("==================================================");

        for (DatosTemporada temporada : temporadas) {

            System.out.printf("%nTemporada %s%n", temporada.temporada());
            System.out.println("--------------------------------------------------");

            for (DatosEpisodio episodio : temporada.episodios()) {
                System.out.printf(
                        "E%02d | %-40s | ⭐ %-4s | %s%n",
                        episodio.numeroEpisodio(),
                        episodio.titulo(),
                        episodio.evaluacion(),
                        episodio.fechaDeLanzamiento()
                );
            }
        }

        System.out.println("\n==================================================");
    }
}