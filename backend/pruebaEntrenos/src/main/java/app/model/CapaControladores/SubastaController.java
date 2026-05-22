package app.controller;

import app.model.CapaEntidades.Subasta;
import app.model.CapaEntidades.Oferta;
import app.model.CapaGestion.GestionSubasta;
import app.model.CapaPersistencia.PersistenciaSubasta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subastas")
@CrossOrigin(origins = "*")
public class SubastaController {

    @Autowired
    private PersistenciaSubasta persistenciaSubasta;

    @Autowired
    private GestionSubasta gestionSubasta;

    @GetMapping
    public List<Subasta> listarTodas() {
        return persistenciaSubasta.cargar();
    }

    @GetMapping("/{id}")
    public Subasta obtenerPorId(@PathVariable String id) {
        return gestionSubasta.buscarSubasta(id);
    }

    // Endpoint para elegir un ganador desde el frontend
    @PostMapping("/adjudicar")
    public ResponseEntity<String> adjudicarGanador(@RequestBody Oferta ofertaGanadora) {
        try {
            gestionSubasta.adjudicarGanador(ofertaGanadora);
            return ResponseEntity.ok("Ganador adjudicado y subasta finalizada.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}