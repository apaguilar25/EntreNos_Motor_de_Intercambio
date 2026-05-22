package app.controller;

import app.model.CapaEntidades.Subasta;
import app.model.CapaEntidades.Oferta;
import app.model.CapaGestion.GestionSubasta;
import app.model.CapaPersistencia.PersistenciaSubasta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import app.model.CapaEntidades.EstadoActivoFisico;

@RestController
@RequestMapping("/api/subastas")
@CrossOrigin(origins = "*")
public class SubastaController {

    @Autowired
    private PersistenciaSubasta persistenciaSubasta;

    @Autowired
    private GestionSubasta gestionSubasta;

    // GET global para ver el catálogo de subastas
    @GetMapping
    public List<Subasta> listarTodas() {
        return persistenciaSubasta.cargar();
    }

    // GET por ID de subasta
    @GetMapping("/{id}")
    public Subasta obtenerPorId(@PathVariable String id) {
        return gestionSubasta.buscarSubasta(id);
    }

    // Endpoint para crear una subasta nueva
    @PostMapping("/crear")
    public ResponseEntity<String> crearSubasta(@RequestBody Map<String, Object> body) {
        try {
            String idSubastador = (String) body.get("idSubastador");
            String descripcion = (String) body.get("descripcion");
            String nombreActivo = (String) body.get("nombreActivo");
            String estadoFisicoStr = (String) body.get("estadoFisico");
            List<String> rutasImagenes = (List<String>) body.get("rutasImagenes");

            EstadoActivoFisico estadoFisico = EstadoActivoFisico.valueOf(estadoFisicoStr.toUpperCase());
            
            gestionSubasta.registrarSubasta(idSubastador, descripcion, nombreActivo, estadoFisico, rutasImagenes);
            return ResponseEntity.ok("{\"mensaje\": \"Subasta creada correctamente.\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // HU4: Adjudicar la oferta ganadora
    @PostMapping("/adjudicar")
    public ResponseEntity<String> adjudicarGanador(@RequestBody Oferta ofertaGanadora) {
        try {
            gestionSubasta.adjudicarGanador(ofertaGanadora);
            return ResponseEntity.ok("Ganador adjudicado con éxito. La subasta ha concluido.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // HU5: Participar enviando una oferta de bienes de consumo
    // URL: POST http://localhost:8080/api/subastas/SUB-101/ofertar
    @PostMapping("/{id}/ofertar")
    public ResponseEntity<String> enviarOferta(@PathVariable String id, @RequestBody Oferta nuevaOferta) {
        try {
            gestionSubasta.registrarOfertaEnSubasta(id, nuevaOferta);
            return ResponseEntity.ok("Tu oferta de bienes ha sido registrada en la subasta.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al procesar la oferta: " + e.getMessage());
        }
    }
}