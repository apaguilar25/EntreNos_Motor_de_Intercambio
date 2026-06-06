package es.ucab.entrenos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableScheduling // Activa tareas automáticas: Validaciones, desbloqueos, etc.
public class EntreNosApplication {

    public static void main(String[] args) {
        SpringApplication.run(EntreNosApplication.class, args);
        System.out.println("=====================================================");
        System.out.println("🚀 ¡Servidor Backend 'EntreNos' iniciado con éxito! 🚀");
        System.out.println("=====================================================");
    }

    /**
     * Configuración Global de CORS
     * Permite que la aplicación React (Frontend) se comunique con esta API (Backend)
     * sin que el navegador bloquee las peticiones por motivos de seguridad.
     */
    @Bean
    public WebMvcConfigurer configuracionCors() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Aplica a todos nuestros Endpoints
                        .allowedOrigins("http://localhost:3000") // Puerto comun de React. Para Vite es "http://localhost:5173"
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos HTTP permitidos
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}