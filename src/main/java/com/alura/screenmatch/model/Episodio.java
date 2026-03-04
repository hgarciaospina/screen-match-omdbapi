package com.alura.screenmatch.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * ==========================================================
 * Episodio (Modelo de Dominio)
 * ==========================================================
 *
 * Representa un episodio procesado dentro del dominio de la
 * aplicación ScreenMatch.
 *
 * Esta clase NO representa directamente el JSON de la API.
 * Para eso existe la clase DatosEpisodio (DTO).
 *
 * DIFERENCIA ARQUITECTÓNICA:
 *
 *  - DatosEpisodio → Modelo de transferencia (API / JSON crudo)
 *  - Episodio      → Modelo de dominio interno tipado y limpio
 *
 * RESPONSABILIDADES:
 *  ✔ Transformar datos provenientes del DTO.
 *  ✔ Convertir evaluación String → Double.
 *  ✔ Convertir fecha String → LocalDate.
 *  ✔ Manejar errores de conversión sin lanzar excepciones.
 *  ✔ Proveer representación estética mediante toString().
 *
 * BENEFICIOS:
 *  - Evita propagar Strings crudos por la aplicación.
 *  - Centraliza la lógica de conversión.
 *  - Facilita ordenamientos por evaluación.
 *  - Permite trabajar con fechas reales (LocalDate).
 *
 * DISEÑO:
 *  - Clase mutable (posee setters).
 *  - Pensada para uso en procesamiento funcional (Streams).
 */
public class Episodio {

    private Integer temporada;
    private String titulo;
    private Integer numeroEpisodio;
    private Double evaluacion;
    private LocalDate fechaLanzamiento;

    /**
     * Constructor que transforma un objeto DatosEpisodio
     * en una entidad de dominio tipada.
     *
     * @param numero número de temporada a la que pertenece
     * @param d      datos crudos provenientes de la API
     */
    public Episodio(Integer numero, DatosEpisodio d) {

        this.temporada = numero;
        this.titulo = d.titulo();
        this.numeroEpisodio = d.numeroEpisodio();

        // Conversión segura de evaluación
        try {
            this.evaluacion = Double.valueOf(d.evaluacion());
        } catch (NumberFormatException e) {
            this.evaluacion = 0.0;
        }

        // Conversión segura de fecha
        try {
            this.fechaLanzamiento = LocalDate.parse(d.fechaDeLanzamiento());
        } catch (DateTimeParseException e) {
            this.fechaLanzamiento = null;
        }
    }

    // ==============================
    // Getters y Setters
    // ==============================

    public Integer getTemporada() {
        return temporada;
    }

    public void setTemporada(Integer temporada) {
        this.temporada = temporada;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getNumeroEpisodio() {
        return numeroEpisodio;
    }

    public void setNumeroEpisodio(Integer numeroEpisodio) {
        this.numeroEpisodio = numeroEpisodio;
    }

    public Double getEvaluacion() {
        return evaluacion;
    }

    public void setEvaluacion(Double evaluacion) {
        this.evaluacion = evaluacion;
    }

    public LocalDate getFechaLanzamiento() {
        return fechaLanzamiento;
    }

    public void setFechaLanzamiento(LocalDate fechaLanzamiento) {
        this.fechaLanzamiento = fechaLanzamiento;
    }

    // ==============================
    // Representación Estética
    // ==============================

    /**
     * Devuelve una representación visual elegante del episodio.
     *
     * Formato:
     * ⭐ 9.5 | T01E03 | Título del episodio               | 📅 12/04/2014
     *
     * Si no existe fecha:
     * 📅 Sin fecha
     *
     * Si la evaluación fue inválida:
     * Se mostrará 0.0
     *
     * @return String formateado listo para impresión
     */
    @Override
    public String toString() {

        String fechaFormateada = (fechaLanzamiento != null)
                ? fechaLanzamiento.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "No disponible";

        return """
            ──────────────────────────────────────────────
            📺 Título del episodio : %s
            🎬 Temporada           : %d
            🎞  Número de episodio : %d
            ⭐ Evaluación          : %.1f
            📅 Fecha de estreno    : %s
            ──────────────────────────────────────────────
            """.formatted(
                titulo,
                temporada,
                numeroEpisodio,
                evaluacion,
                fechaFormateada
        );
    }
}