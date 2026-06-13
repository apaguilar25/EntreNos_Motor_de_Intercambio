package es.ucab.entrenos.modulos.notificacion.servicios;

import es.ucab.entrenos.modulos.notificacion.modelos.Notificacion;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.repositorios.IRepositorioNotificacion;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServicioNotificacion {
    private final IRepositorioNotificacion repositorioNotificacion;

    public ServicioNotificacion(IRepositorioNotificacion repositorioNotificacion) {
        this.repositorioNotificacion = repositorioNotificacion;
    }

    public void enviarNotificacion(String idRemitente, String idDestinatario, String mensaje, TipoNotificacion tipo) {
        Notificacion notificacion = new Notificacion(idRemitente, idDestinatario, mensaje, tipo);
        repositorioNotificacion.guardar(notificacion);
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
}
