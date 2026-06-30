package es.ucab.entrenos.modulos.notificacion.servicios;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ucab.entrenos.modulos.notificacion.modelos.Notificacion;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.repositorios.IRepositorioNotificacion;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServicioNotificacion {
    private final IRepositorioNotificacion repositorioNotificacion;
    private final SseService sseService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ServicioNotificacion(IRepositorioNotificacion repositorioNotificacion, SseService sseService) {
        this.repositorioNotificacion = repositorioNotificacion;
        this.sseService = sseService;
    }

    public void enviarNotificacion(String idRemitente, String idDestinatario, String mensaje, TipoNotificacion tipo) {
        Notificacion notificacion = new Notificacion(idRemitente, idDestinatario, mensaje, tipo);
        repositorioNotificacion.guardar(notificacion);
        enviarPush(idDestinatario, notificacion);
    }

    public void enviarNotificacion(String idRemitente, String idDestinatario, String mensaje, TipoNotificacion tipo, String idReferencia, String idReferenciaAuxiliar) {
        Notificacion notificacion = new Notificacion(idRemitente, idDestinatario, mensaje, tipo, idReferencia, idReferenciaAuxiliar);
        repositorioNotificacion.guardar(notificacion);
        enviarPush(idDestinatario, notificacion);
    }

    /**
     * SOBRECARGA PARA NOTIFICACIONES DEL SISTEMA:
     * Este método se usa cuando el propio backend (Cron Jobs o Reglas de Negocio)
     * necesita notificar a un usuario sin que haya un remitente humano.
     */
    public void enviarNotificacion(String idDestinatario, String mensaje, TipoNotificacion tipo) {
        // Reutilizamos la lógica del método original, pero inyectamos "SISTEMA" como remitente
        this.enviarNotificacion("SISTEMA", idDestinatario, mensaje, tipo);
    }

    private void enviarPush(String idDestinatario, Notificacion notificacion) {
        try {
            String json = objectMapper.writeValueAsString(notificacion);
            sseService.enviar(idDestinatario, "nueva-notificacion", json);
        } catch (JsonProcessingException e) {
            // Si falla la serialización, no bloqueamos el flujo
        }
    }

    public List<Notificacion> obtenerNotificaciones(String idDestinatario) {
        return repositorioNotificacion.obtenerPorDestinatario(idDestinatario);
    }

    public List<Notificacion> obtenerNoLeidas(String idDestinatario) {
        return repositorioNotificacion.obtenerNoLeidas(idDestinatario);
    }

    public int contarNoLeidas(String idDestinatario) {
        return repositorioNotificacion.obtenerNoLeidas(idDestinatario).size();
    }

    public void marcarComoLeida(String idNotificacion) {
        repositorioNotificacion.marcarComoLeida(idNotificacion);
    }

    public void marcarTodasComoLeidas(String idDestinatario) {
        List<Notificacion> noLeidas = repositorioNotificacion.obtenerNoLeidas(idDestinatario);
        for (Notificacion n : noLeidas) {
            repositorioNotificacion.marcarComoLeida(n.getIdNotificacion());
        }
    }

    public void eliminarNotificacion(String idNotificacion) {
        repositorioNotificacion.eliminar(idNotificacion);
    }

    public void eliminarNotificacionesPorReferencia(String idDestinatario, String idReferencia) {
        repositorioNotificacion.eliminarPorReferencia(idDestinatario, idReferencia);
    }
}
