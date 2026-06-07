package es.ucab.entrenos.modulos.identidad.controladores;

import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioHabilidad;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habilidades")
public class ControladorHabilidad {

    private final ServicioHabilidad servicioHabilidad;

    public ControladorHabilidad(ServicioHabilidad servicioHabilidad) {
        this.servicioHabilidad = servicioHabilidad;
    }

    @GetMapping
    public ResponseEntity<List<Habilidad>> listarHabilidades() {
        return ResponseEntity.ok(servicioHabilidad.obtenerTodas());
    }

    @PostMapping
    public ResponseEntity<?> crearHabilidad(@RequestBody java.util.Map<String, String> request) {
        try {
            String categoria = request.get("categoria");
            Habilidad nueva = servicioHabilidad.crearHabilidad(categoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarHabilidad(
            @PathVariable String id,
            @RequestBody java.util.Map<String, String> request) {
        try {
            String nuevaCategoria = request.get("categoria");
            servicioHabilidad.editarHabilidad(id, nuevaCategoria);
            return ResponseEntity.ok().body("Habilidad maestra actualizada con éxito.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict si hay duplicados
        }
    }
}