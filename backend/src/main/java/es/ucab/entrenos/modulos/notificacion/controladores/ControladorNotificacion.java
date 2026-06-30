package es.ucab.entrenos.modulos.notificacion.controladores;

import es.ucab.entrenos.modulos.notificacion.modelos.Notificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.SseService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "http://localhost:5173")
public class ControladorNotificacion {
    private final ServicioNotificacion servicioNotificacion;
    private final SseService sseService;

    public ControladorNotificacion(ServicioNotificacion servicioNotificacion, SseService sseService) {
        this.servicioNotificacion = servicioNotificacion;
        this.sseService = sseService;
    }

    @GetMapping(value = "/stream/{idUsuario}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String idUsuario) {
        return sseService.suscribir(idUsuario);
    }

    @GetMapping("/{idDestinatario}")
    public ResponseEntity<List<Notificacion>> obtenerNotificaciones(@PathVariable String idDestinatario) {
        return ResponseEntity.ok(servicioNotificacion.obtenerNotificaciones(idDestinatario));
    }

    @GetMapping("/{idDestinatario}/no-leidas")
    public ResponseEntity<List<Notificacion>> obtenerNoLeidas(@PathVariable String idDestinatario) {
        return ResponseEntity.ok(servicioNotificacion.obtenerNoLeidas(idDestinatario));
    }

    @GetMapping("/{idDestinatario}/contar")
    public ResponseEntity<Map<String, Integer>> contarNoLeidas(@PathVariable String idDestinatario) {
        return ResponseEntity.ok(Map.of("count", servicioNotificacion.contarNoLeidas(idDestinatario)));
    }

    @PutMapping("/{idNotificacion}/leer")
    public ResponseEntity<Void> marcarComoLeida(@PathVariable String idNotificacion) {
        servicioNotificacion.marcarComoLeida(idNotificacion);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{idDestinatario}/leer-todas")
    public ResponseEntity<Void> marcarTodasComoLeidas(@PathVariable String idDestinatario) {
        servicioNotificacion.marcarTodasComoLeidas(idDestinatario);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{idNotificacion}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable String idNotificacion) {
        servicioNotificacion.eliminarNotificacion(idNotificacion);
        return ResponseEntity.noContent().build();
    }
}
