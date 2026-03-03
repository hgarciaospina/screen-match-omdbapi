package com.alura.screenmatch;

import com.alura.screenmatch.model.DatosSerie;
import com.alura.screenmatch.service.ConsumoAPI;
import com.alura.screenmatch.service.ConvierteDatos;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

		var url = urlBase + "?t=game+of+thrones&apikey=" + apiKey;

		var json1 = consumoApi.obtenerDatos(url);
		System.out.println(json1);
		ConvierteDatos convierteDatos = new ConvierteDatos();
		var datos = convierteDatos.obtenerDatos(json1, DatosSerie.class);
		System.out.println(datos);

		// var json2 = consumoApi.obtenerDatos("https://coffee.alexflipnote.dev/random.json");
		// System.out.println(json2);


	}
}
