package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioHabilidad;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/habilidades")
public class ControladorHabilidad {

    private final ServicioHabilidad servicioHabilidad;

    public ControladorHabilidad(ServicioHabilidad servicioHabilidad) {
        this.servicioHabilidad = servicioHabilidad;
    }

    /**
     * Endpoint: GET http://localhost:8080/api/habilidades
     * Propósito: Devuelve la lista de las 5 habilidades oficiales del sistema.
     */
    @GetMapping
    public ResponseEntity<List<Habilidad>> listarHabilidadesSoportadas() {
        List<Habilidad> catalogo = servicioHabilidad.obtenerTodas();
        return ResponseEntity.ok(catalogo);
    }
}