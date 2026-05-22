package app.controller;

import app.model.CapaEntidades.Transaccion;
import app.model.CapaGestion.GestionTransaccion;
import app.model.CapaPersistencia.PersistenciaTransacciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = "*") // Evita bloqueos de CORS con tu Frontend de Vite
public class TransaccionController {

    @Autowired
    private PersistenciaTransacciones persistenciaTransacciones;

    @Autowired
    private GestionTransaccion gestionTransaccion;

    // Listar todas (GET) -> http://localhost:8080/api/transacciones
    @GetMapping
    public List<Transaccion> obtenerTodasLasTransacciones() {
        return persistenciaTransacciones.cargar();
    }

    // Buscar por ID (GET) -> http://localhost:8080/api/transacciones/TX-001
    @GetMapping("/{id}")
    public Transaccion obtenerPorId(@PathVariable String id) {
        return persistenciaTransacciones.cargar().stream()
                .filter(t -> t.getIdTransaccion().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con el ID: " + id));
    }

    // 1. Endpoint para el botón del Ofertante (PUT)
    // URL de prueba: http://localhost:8080/api/transacciones/TX-001/confirmar-entrega
    @PutMapping("/{id}/confirmar-entrega")
    public ResponseEntity<String> confirmarEntrega(@PathVariable String id) {
        try {
            gestionTransaccion.confirmarEntrega(id);
            return ResponseEntity.ok("Entrega del servicio registrada con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Endpoint para el botón del Demandante (PUT)
    // URL de prueba: http://localhost:8080/api/transacciones/TX-001/confirmar-recepcion
    @PutMapping("/{id}/confirmar-recepcion")
    public ResponseEntity<String> confirmarRecepcion(@PathVariable String id) {
        try {
            gestionTransaccion.confirmarRecepcion(id);
            return ResponseEntity.ok("Recepción del servicio registrada con éxito.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}