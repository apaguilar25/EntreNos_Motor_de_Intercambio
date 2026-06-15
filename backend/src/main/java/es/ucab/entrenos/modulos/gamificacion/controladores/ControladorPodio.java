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
        PodioResponseDTO dto = servicioPodio.obtenerTop3();
        if (dto.getProveedorElite() == null || dto.getProveedorElite().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/calcular")
    public ResponseEntity<PodioResponseDTO> calcularPodio() {
        servicioPodio.calcularPodioSemanal();
        PodioResponseDTO dto = servicioPodio.obtenerTop3();
        return ResponseEntity.ok(dto);
    }
}
