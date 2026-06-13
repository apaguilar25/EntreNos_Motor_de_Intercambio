package es.ucab.entrenos.modulos.subasta.controladores;

import es.ucab.entrenos.modulos.subasta.dtos.RegistroPropuestaDTO;
import es.ucab.entrenos.modulos.subasta.modelos.Propuesta;
import es.ucab.entrenos.modulos.subasta.servicios.ServicioPropuesta;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ControladorPropuesta {

    private final ServicioPropuesta servicioPropuesta;

    public ControladorPropuesta(ServicioPropuesta servicioPropuesta) {
        this.servicioPropuesta = servicioPropuesta;
    }

    // Registrar nueva propuesta (HU5)
    @PostMapping("/subastas/{idSubasta}/propuestas")
    public ResponseEntity<Propuesta> registrarPropuesta(@AuthenticationPrincipal String idPostor, @PathVariable String idSubasta, @RequestBody RegistroPropuestaDTO dto) {
        Propuesta nueva = servicioPropuesta.registrarPropuesta(idSubasta, idPostor, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // Editar propuesta existente (HU5)
    @PutMapping("/subastas/{idSubasta}/propuestas/{idPropuesta}")
    public ResponseEntity<Propuesta> editarPropuesta(@AuthenticationPrincipal String idPostor, @PathVariable String idSubasta, @PathVariable String idPropuesta, @RequestBody RegistroPropuestaDTO dto) {
        Propuesta editada = servicioPropuesta.editarPropuesta(idSubasta, idPostor, idPropuesta, dto);
        return ResponseEntity.ok(editada);
    }

    // Retirar propuesta (HU5)
    @DeleteMapping("/subastas/{idSubasta}/propuestas/{idPropuesta}")
    public ResponseEntity<?> retirarPropuesta(@AuthenticationPrincipal String idPostor, @PathVariable String idSubasta, @PathVariable String idPropuesta) {
        servicioPropuesta.retirarPropuesta(idSubasta, idPostor, idPropuesta);
        return ResponseEntity.ok(Map.of("mensaje", "Tu propuesta ha sido retirada exitosamente."));
    }

    // Historial personal del Postor (HU5)
    @GetMapping("/propuestas/historial")
    public ResponseEntity<List<Map<String, Object>>> obtenerHistorial(@AuthenticationPrincipal String idPostor) {
        return ResponseEntity.ok(servicioPropuesta.obtenerHistorialPropuestasDeUsuario(idPostor));
    }

    // Visibilidad de competencia anónima (HU5)
    @GetMapping("/subastas/{idSubasta}/competencia")
    public ResponseEntity<Map<String, Object>> verCompetencia(@PathVariable String idSubasta) {
        return ResponseEntity.ok(servicioPropuesta.obtenerSubastaConCompetenciaAnonima(idSubasta));
    }
}