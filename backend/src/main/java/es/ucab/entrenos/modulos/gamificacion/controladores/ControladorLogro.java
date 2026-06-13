package es.ucab.entrenos.modulos.gamificacion.controladores;

import es.ucab.entrenos.modulos.gamificacion.dtos.LogroDesbloqueadoResponseDTO;
import es.ucab.entrenos.modulos.gamificacion.dtos.LogroEstadoDTO;
import es.ucab.entrenos.modulos.gamificacion.modelos.Logro;
import es.ucab.entrenos.modulos.gamificacion.modelos.LogroDesbloqueado;
import es.ucab.entrenos.modulos.gamificacion.servicios.ServicioGamificacion;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logros")
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorLogro {

    private final ServicioGamificacion servicioGamificacion;

    public ControladorLogro(ServicioGamificacion servicioGamificacion) {
        this.servicioGamificacion = servicioGamificacion;
    }

    @GetMapping
    public ResponseEntity<List<LogroEstadoDTO>> listarTodos() {
        List<Logro> logros = servicioGamificacion.obtenerTodosLosLogros();
        List<LogroEstadoDTO> dtos = logros.stream()
                .map(l -> new LogroEstadoDTO(l, false, null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<LogroEstadoDTO>> listarPorUsuario(@PathVariable String idUsuario) {
        List<Logro> todos = servicioGamificacion.obtenerTodosLosLogros();
        List<LogroDesbloqueado> desbloqueados = servicioGamificacion.obtenerLogrosDesbloqueados(idUsuario);
        Map<String, Long> fechaPorLogro = desbloqueados.stream()
                .collect(Collectors.toMap(LogroDesbloqueado::getIdLogro, LogroDesbloqueado::getFechaDesbloqueo));
        List<LogroEstadoDTO> dtos = todos.stream()
                .map(l -> new LogroEstadoDTO(l, fechaPorLogro.containsKey(l.getIdLogro()), fechaPorLogro.get(l.getIdLogro())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/usuario/{idUsuario}/desbloqueados")
    public ResponseEntity<List<LogroDesbloqueadoResponseDTO>> listarDesbloqueados(@PathVariable String idUsuario) {
        List<LogroDesbloqueado> desbloqueados = servicioGamificacion.obtenerLogrosDesbloqueados(idUsuario);
        Map<String, Logro> logrosMap = servicioGamificacion.obtenerTodosLosLogros().stream()
                .collect(Collectors.toMap(Logro::getIdLogro, l -> l));
        List<LogroDesbloqueadoResponseDTO> dtos = desbloqueados.stream()
                .map(ld -> {
                    Logro logro = logrosMap.get(ld.getIdLogro());
                    if (logro == null) return null;
                    return new LogroDesbloqueadoResponseDTO(
                            logro.getIdLogro(), logro.getNombre(), logro.getDescripcion(),
                            logro.getBonoCreditos(), logro.getIcono(), ld.getFechaDesbloqueo());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
