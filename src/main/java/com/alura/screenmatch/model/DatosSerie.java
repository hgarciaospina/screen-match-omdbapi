package com.alura.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Representa la respuesta general de una serie
 * proveniente de la API OMDb.
 *
 * Se utiliza como DTO (Data Transfer Object)
 * para deserializar el JSON recibido.
 *
 * @JsonIgnoreProperties(ignoreUnknown = true)
 * permite ignorar campos del JSON que no estén definidos aquí.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record DatosSerie(

        @JsonAlias("Title")
        String titulo,

        @JsonAlias("Year")
        String anio,

        @JsonAlias("imdbRating")
        String caificacion,

        @JsonAlias("Type")
        String tipo,

        @JsonAlias("totalSeasons")
        String totalTemporadas,

        // 🔥 CAMPO CLAVE PARA VALIDACIÓN
        @JsonAlias("Response")
        String respuesta

) {}