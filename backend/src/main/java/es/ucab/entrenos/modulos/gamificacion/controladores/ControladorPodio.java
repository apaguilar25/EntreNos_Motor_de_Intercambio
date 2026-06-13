package es.ucab.entrenos.modulos.gamificacion.controladores;

import es.ucab.entrenos.modulos.gamificacion.dtos.PodioResponseDTO;
import es.ucab.entrenos.modulos.gamificacion.modelos.PodioSemanal;
import es.ucab.entrenos.modulos.gamificacion.servicios.ServicioPodio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/podio")
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorPodio {

    private final ServicioPodio servicioPodio;

    public ControladorPodio(ServicioPodio servicioPodio) {
        this.servicioPodio = servicioPodio;
    }

    @GetMapping
    public ResponseEntity<PodioResponseDTO> obtenerPodio() {
        Optional<PodioSemanal> podioOpt = servicioPodio.obtenerPodioActual();
        if (podioOpt.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(toDTO(podioOpt.get()));
    }

    @PostMapping("/calcular")
    public ResponseEntity<PodioResponseDTO> calcularPodio() {
        PodioSemanal podio = servicioPodio.calcularPodioSemanal();
        return ResponseEntity.ok(toDTO(podio));
    }

    private PodioResponseDTO toDTO(PodioSemanal p) {
        PodioResponseDTO dto = new PodioResponseDTO();
        dto.setIdPodio(p.getIdPodio());
        dto.setFechaInicioSemana(p.getFechaInicioSemana());
        dto.setFechaFinSemana(p.getFechaFinSemana());
        dto.setFechaCalculo(p.getFechaCalculo());
        if (p.getProveedorEliteId() != null) {
            dto.setProveedorElite(new PodioResponseDTO.EntradaPodio(
                    p.getProveedorEliteId(), p.getProveedorEliteNombre(), p.getProveedorEliteServicios()));
        }
        if (p.getMotorEconomiaId() != null) {
            dto.setMotorEconomia(new PodioResponseDTO.EntradaPodio(
                    p.getMotorEconomiaId(), p.getMotorEconomiaNombre(), p.getMotorEconomiaTransacciones()));
        }
        if (p.getEmbajadorCalidadId() != null) {
            dto.setEmbajadorCalidad(new PodioResponseDTO.EntradaPodio(
                    p.getEmbajadorCalidadId(), p.getEmbajadorCalidadNombre(), p.getEmbajadorCalidadPromedio()));
        }
        return dto;
    }
}
