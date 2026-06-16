package es.ucab.entrenos.modulos.publicacion.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import es.ucab.entrenos.modulos.publicacion.modelos.Solicitud;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioSolicitud;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServicioExpiracion {

    private final IRepositorioSolicitud repositorioSolicitud;
    private final ServicioPublicacion servicioPublicacion;
    private final ServicioUsuario servicioUsuario;
    private final ServicioNotificacion servicioNotificacion;

    public ServicioExpiracion(IRepositorioSolicitud repositorioSolicitud,
                              ServicioPublicacion servicioPublicacion,
                              ServicioUsuario servicioUsuario,
                              ServicioNotificacion servicioNotificacion) {
        this.repositorioSolicitud = repositorioSolicitud;
        this.servicioPublicacion = servicioPublicacion;
        this.servicioUsuario = servicioUsuario;
        this.servicioNotificacion = servicioNotificacion;
    }

    @Scheduled(fixedRate = 60000)
    public void expirarSolicitudesVencidas() {
        List<Solicitud> solicitudes = repositorioSolicitud.obtenerTodas();
        boolean huboCambios = false;
        for (Solicitud sol : solicitudes) {
            if (sol.haExpirado()) {
                sol.expirar();
                servicioPublicacion.obtenerPublicacionPorId(sol.getIdPublicacion())
                        .ifPresent(pub -> {
                            String nombreSolicitante = servicioUsuario.buscarPorId(sol.getIdSolicitante())
                                    .map(Usuario::getNombre).orElse("Un usuario");
                            servicioNotificacion.enviarNotificacion("SISTEMA", pub.getIdUsuario(),
                                    "La solicitud de " + nombreSolicitante + " para " + pub.getNombreServicio()
                                            + " ha expirado automáticamente.",
                                    TipoNotificacion.ALERTA_SISTEMA);
                            servicioNotificacion.enviarNotificacion("SISTEMA", sol.getIdSolicitante(),
                                    "Tu solicitud para " + pub.getNombreServicio() + " ha expirado (sin respuesta).",
                                    TipoNotificacion.ALERTA_SISTEMA);
                        });
                huboCambios = true;
            }
        }
        if (huboCambios) {
            repositorioSolicitud.guardarTodas(solicitudes);
        }
    }
}
