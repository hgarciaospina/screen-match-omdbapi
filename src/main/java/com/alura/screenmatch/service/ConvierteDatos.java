package com.alura.screenmatch.service;

import tools.jackson.databind.ObjectMapper;

public class ConvierteDatos implements  IConvierteDatos{
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> T obtenerDatos(String json, Class<T> clase) {
        return objectMapper.readValue(json, clase);
    }
}