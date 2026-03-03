package com.alura.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
        String totalTemporadas
){}