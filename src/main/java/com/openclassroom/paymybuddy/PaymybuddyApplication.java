package com.openclassroom.paymybuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;


/**
 * Classe principale pour démarrer l'application Pay My Buddy.
 */
@SpringBootApplication
public class PaymybuddyApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().load();
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));

		SpringApplication.run(PaymybuddyApplication.class, args);
		System.out.println("L'application Pay My Buddy est démarrée et prête à être testée via Postman !");
	}
}