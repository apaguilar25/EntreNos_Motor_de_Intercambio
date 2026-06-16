package es.ucab.entrenos.modulos.reputacion.controladores;

import es.ucab.entrenos.modulos.reputacion.modelos.Resena;
import es.ucab.entrenos.modulos.reputacion.servicios.ServicioReputacion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reputacion")
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorReputacion {
    private final ServicioReputacion servicioReputacion;

    public ControladorReputacion(ServicioReputacion servicioReputacion) {
        this.servicioReputacion = servicioReputacion;
    }

    @PostMapping
    public ResponseEntity<?> crearResena(@RequestBody Resena resena) {
        try {
            Resena nueva = servicioReputacion.crearResena(
                    resena.getIdTransaccion(),
                    resena.getIdEmisor(),
                    resena.getIdReceptor(),
                    resena.getCalificacion()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Resena>> obtenerPorUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(servicioReputacion.obtenerResenasPorUsuario(idUsuario));
    }

    @GetMapping("/transaccion/{idTransaccion}")
    public ResponseEntity<List<Resena>> obtenerPorTransaccion(@PathVariable String idTransaccion) {
        return ResponseEntity.ok(servicioReputacion.obtenerResenasPorTransaccion(idTransaccion));
    }

    @GetMapping
    public ResponseEntity<List<Resena>> listarTodas() {
        return ResponseEntity.ok(servicioReputacion.listarTodas());
    }
}

