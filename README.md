# Screenmatch - Explorador de Series con Java y Spring Boot

## Descripción del Proyecto

**Screenmatch** es una aplicación desarrollada en **Java con Spring
Boot** que permite consultar información de series de televisión
utilizando la API pública **OMDb (Open Movie Database)**.

El proyecto demuestra cómo integrar diferentes conceptos modernos del
ecosistema Java como:

-   Consumo de APIs REST
-   Procesamiento de datos JSON
-   Uso de **Streams y Lambdas**
-   Programación funcional
-   Manejo de **Optional**
-   Procesamiento estadístico con **DoubleSummaryStatistics**
-   Organización de aplicaciones con **Spring Boot Profiles**
-   Manejo de variables sensibles mediante **archivo `.env`**

La aplicación funciona en modo **consola (CLI)** y permite explorar:

-   Información general de una serie
-   Temporadas
-   Episodios
-   Promedios de evaluación por temporada
-   Estadísticas globales

------------------------------------------------------------------------

# Arquitectura del Proyecto

El proyecto sigue una estructura limpia basada en responsabilidades:

    src/main/java/com/alura/screenmatch

    model/
        DatosSerie
        DatosTemporada
        DatosEpisodio
        Episodio

    service/
        ConsumoAPI
        ConvierteDatos

    runner/
        DemoRunner
        CLIRunner
        FiltroSerieRunner
        PromedioTemporadasRunner
        EstadisticasSerieRunner

    ScreenmatchApplicationPrincipal

### Responsabilidades

  Capa          Responsabilidad
  ------------- ------------------------------------------------
  model         Representa los datos recibidos de la API
  service       Encapsula consumo de API y conversión de datos
  runner        Ejecuta diferentes escenarios de uso
  application   Punto de arranque de Spring Boot

------------------------------------------------------------------------

# Tecnologías Utilizadas

  Tecnología                Uso
  ------------------------- ---------------------------
  Java 17+                  Lenguaje principal
  Spring Boot               Framework principal
  Jackson                   Conversión JSON → Objetos
  Streams API               Procesamiento funcional
  OMDb API                  Fuente de datos
  Optional                  Manejo seguro de datos
  DoubleSummaryStatistics   Estadísticas de datos
  Maven                     Gestión de dependencias

------------------------------------------------------------------------

# Conceptos de Java Utilizados

## Streams

Los **Streams** permiten procesar colecciones de forma funcional.

Ejemplo del proyecto:

``` java
episodios.stream()
        .filter(e -> e.getEvaluacion() > 0)
        .collect(Collectors.groupingBy(
                Episodio::getTemporada,
                Collectors.averagingDouble(Episodio::getEvaluacion)
        ));
```

Este flujo:

1.  Obtiene todos los episodios
2.  Filtra evaluaciones válidas
3.  Agrupa por temporada
4.  Calcula el promedio

------------------------------------------------------------------------

## Lambdas

Las **expresiones lambda** permiten escribir funciones de forma
compacta.

Ejemplo:

``` java
e -> e.getEvaluacion() > 0
```

Esto representa una función que recibe un episodio y retorna `true` si
su evaluación es mayor que cero.

------------------------------------------------------------------------

## Optional

Se utiliza **Optional** para evitar errores cuando no existen
resultados.

Ejemplo:

``` java
Optional<List<Episodio>> episodios
```

Si no hay datos disponibles, el programa muestra un mensaje amigable en
lugar de lanzar una excepción.

------------------------------------------------------------------------

## DoubleSummaryStatistics

Permite calcular estadísticas en un solo paso.

Ejemplo:

``` java
DoubleSummaryStatistics stats =
        episodios.stream()
                 .mapToDouble(Episodio::getEvaluacion)
                 .summaryStatistics();
```

Esto calcula automáticamente:

-   cantidad
-   suma
-   promedio
-   mínimo
-   máximo

------------------------------------------------------------------------

# Consumo de la API OMDb

La aplicación utiliza la API:

https://www.omdbapi.com/

Ejemplo de petición:

    https://www.omdbapi.com/?t=Game+of+Thrones&apikey=TU_API_KEY

Respuesta ejemplo:

``` json
{
  "Title": "Game of Thrones",
  "Year": "2011–2019",
  "TotalSeasons": "8",
  "imdbRating": "9.2"
}
```

Los datos JSON son convertidos a objetos Java mediante **Jackson**.

------------------------------------------------------------------------

# Configuración del Archivo .env

Para evitar exponer datos sensibles como la **API KEY**, se utiliza un
archivo `.env`.

## Ubicación del archivo

Debe colocarse en la **raíz del proyecto**:

    Screenmatch/
        src/
        pom.xml
        .env

------------------------------------------------------------------------

## Ejemplo de archivo `.env`

    OMDB_URL=https://www.omdbapi.com/
    OMDB_API_KEY=123456789abcdef

### Explicación

  Variable       Descripción
  -------------- ---------------------------------
  OMDB_URL       URL base de la API
  OMDB_API_KEY   Clave de acceso personal a OMDb

------------------------------------------------------------------------

# Cómo Obtener una API Key

1.  Ir a:

https://www.omdbapi.com/apikey.aspx

2.  Registrar un email

3.  Recibir la API Key

4.  Colocarla en el archivo `.env`

------------------------------------------------------------------------

# Ejecución del Proyecto

El proyecto utiliza **Spring Profiles** para ejecutar diferentes
runners.

Cada runner representa un escenario distinto.

------------------------------------------------------------------------

# Cambiar de Perfil

Para ejecutar un runner específico se utiliza:

    spring.profiles.active

en:

    application.properties

Ejemplo:

    spring.profiles.active=dev-estadisticas-serie

------------------------------------------------------------------------

# Runners Disponibles

  Runner                     Descripción
  -------------------------- -----------------------------------
  DemoRunner                 Versión básica de prueba
  CLIRunner                  Interfaz de consola
  FiltroSerieRunner          Búsqueda por coincidencia parcial
  PromedioTemporadasRunner   Promedio por temporada
  EstadisticasSerieRunner    Estadísticas completas

------------------------------------------------------------------------

# Ejemplo de Ejecución

Entrada del usuario:

    Ingrese el nombre de la serie: Game of Thrones

Salida:

    ==================================================
    Serie: Game of Thrones
    ==================================================

    PROMEDIO POR TEMPORADA
    Temporada 1  -> Promedio: 8.75
    Temporada 2  -> Promedio: 8.63
    Temporada 3  -> Promedio: 9.12

    ESTADÍSTICAS GENERALES
    Cantidad de episodios: 60
    Evaluación mínima: 7.50
    Evaluación máxima: 9.90
    Promedio general: 8.92

------------------------------------------------------------------------

# Buenas Prácticas Aplicadas

El proyecto implementa varias buenas prácticas de ingeniería:

-   separación de responsabilidades
-   programación funcional
-   manejo seguro de datos
-   configuración externa
-   código documentado
-   arquitectura clara

------------------------------------------------------------------------

# Posibles Mejoras Futuras

El proyecto puede evolucionar hacia:

-   API REST con Spring Boot
-   interfaz web con React
-   almacenamiento en base de datos
-   ranking de episodios
-   recomendador de series

------------------------------------------------------------------------

# Autor

Henry García Ospina

Proyecto académico desarrollado para practicar:

-   Java moderno
-   Streams
-   Spring Boot
-   consumo de APIs
