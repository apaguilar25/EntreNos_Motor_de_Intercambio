package es.ucab.entrenos.modulos.publicacion.servicios;

import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioPublicacion;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServicioExpiracion {

    private final IRepositorioPublicacion repositorioPublicacion;
    private final ServicioNotificacion servicioNotificacion;

    public ServicioExpiracion(IRepositorioPublicacion repositorioPublicacion,
                              ServicioNotificacion servicioNotificacion) {
        this.repositorioPublicacion = repositorioPublicacion;
        this.servicioNotificacion = servicioNotificacion;
    }

    @Scheduled(fixedRate = 60000)
    public void expirarSolicitudesVencidas() {
        List<Publicacion> publicaciones = repositorioPublicacion.obtenerTodas();
        boolean huboCambios = false;
        for (Publicacion pub : publicaciones) {
            if (pub.haExpiradoPlazoRespuesta()) {
                String solicitanteId = pub.getIdSolicitante();
                String solicitanteNombre = pub.getNombreSolicitante();
                pub.setEstadoSolicitud(Publicacion.ESTADO_SOLICITUD_EXPIRADA);
                pub.limpiarSolicitud();
                servicioNotificacion.enviarNotificacion("SISTEMA", pub.getIdUsuario(),
                        "La solicitud de " + (solicitanteNombre != null ? solicitanteNombre : "un usuario")
                                + " para " + pub.getNombreServicio() + " ha expirado automáticamente.",
                        TipoNotificacion.ALERTA_SISTEMA);
                if (solicitanteId != null) {
                    servicioNotificacion.enviarNotificacion("SISTEMA", solicitanteId,
                            "Tu solicitud para " + pub.getNombreServicio() + " ha expirado (sin respuesta).",
                            TipoNotificacion.ALERTA_SISTEMA);
                }
                huboCambios = true;
            }
        }
        if (huboCambios) {
            repositorioPublicacion.guardarTodas(publicaciones);
        }
    }
}
