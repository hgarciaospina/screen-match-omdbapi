package com.alura.screenmatch;

import com.alura.screenmatch.model.DatosEpisodio;
import com.alura.screenmatch.model.DatosSerie;
import com.alura.screenmatch.model.DatosTemporada;
import com.alura.screenmatch.service.ConsumoAPI;
import com.alura.screenmatch.service.ConvierteDatos;
import com.sun.jdi.IntegerValue;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {
	private final String urlBase;
	private final String apiKey;

	public ScreenmatchApplication(String omdbUrl, String omdbApiKey) {
		this.urlBase = omdbUrl;
		this.apiKey = omdbApiKey;
	}


	public static void main(String[] args) {

		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		var consumoApi = new ConsumoAPI();

		//Datos series
		var url1 = urlBase
				+ "?t=game+of+thrones&apikey="
				+ apiKey;
		var json1 = consumoApi.obtenerDatos(url1);

		System.out.println(json1);

		ConvierteDatos convierteDatos = new ConvierteDatos();
		var datosSeries = convierteDatos.obtenerDatos(json1, DatosSerie.class);
		System.out.println(datosSeries+"\n");

		//Datos episodios: https://www.omdbapi.com/?t=game+of+thrones&Season=1&episode=1&apikey=xxxxxxxx
		var url2 = urlBase
				+ "?t=game+of+thrones"
				+ "&Season=1"
				+ "&Episode=1"
				+ "&apikey=" + apiKey;
		var json2 = consumoApi.obtenerDatos(url2);

		System.out.println(json2);

		var datosEpisodios = convierteDatos.obtenerDatos(json2, DatosEpisodio.class);
		System.out.println(datosEpisodios+"\n");

		//Datos temporadas: https://www.omdbapi.com/?t=game+of+thrones&Season=1&apikey=xxxxxxxx
		List<DatosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i <= Integer.parseInt(datosSeries.totalTemporadas()); i++) {
			var url3 = urlBase
					+ "?t=game+of+thrones"
					+ "&Season="+i
					+ "&apikey=" + apiKey;
			var json3 = consumoApi.obtenerDatos(url3);
			var datosTemporadas = convierteDatos.obtenerDatos(json3, DatosTemporada.class);
			temporadas.add(datosTemporadas);
		}

		temporadas.forEach(System.out::println);

		// var json4= consumoApi.obtenerDatos("https://coffee.alexflipnote.dev/random.json");
		// System.out.println(json4);
	}
}
