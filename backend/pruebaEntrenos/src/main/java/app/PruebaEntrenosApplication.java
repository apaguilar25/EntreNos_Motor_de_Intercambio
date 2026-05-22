package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling // Activa tareas automaticas: Validaciones
public class PruebaEntrenosApplication {
	public static void main(String[] args) {
		SpringApplication.run(PruebaEntrenosApplication.class, args);
        System.out.print("Hello and welcome!");
	}
}
