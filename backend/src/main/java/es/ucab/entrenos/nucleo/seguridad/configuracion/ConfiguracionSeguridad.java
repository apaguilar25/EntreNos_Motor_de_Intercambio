package es.ucab.entrenos.nucleo.seguridad.configuracion;

import es.ucab.entrenos.nucleo.seguridad.jwt.FiltroAutenticacionJwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    private final FiltroAutenticacionJwt filtroJwt;

    public ConfiguracionSeguridad(FiltroAutenticacionJwt filtroJwt) {
        this.filtroJwt = filtroJwt;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(org.springframework.security.config.Customizer.withDefaults()) // ¡VITAL! Permite que Spring Security respete los CORS de EntreNosApplication
                .csrf(AbstractHttpConfigurer::disable) // Desactivamos CSRF porque somos una API REST sin estado
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No usamos Cookies ni Sesiones de Tomcat
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas que no requieren token
                        .requestMatchers("/api/auth/login", "/api/auth/registro").permitAll()
                        .requestMatchers("/api/usuarios/registro").permitAll()
                        .requestMatchers("/api/habilidades").permitAll() // Ver catálogo global es público
                        .requestMatchers("/api/**").permitAll() // Temporalmente permitido para el MOCK de frontend
                        // Todas las demás rutas exigen token válido
                        .anyRequest().authenticated()
                )
                // Agregamos nuestro filtro antes del filtro por defecto de Spring
                .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}