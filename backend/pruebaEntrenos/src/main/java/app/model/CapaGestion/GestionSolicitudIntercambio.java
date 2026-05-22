package app.model.CapaGestion;

import app.model.CapaEntidades.SolicitudIntercambio;
import app.model.CapaEntidades.EstadoSolicitudIntercambio;
import app.model.CapaEntidades.Notificacion;
import app.model.CapaEntidades.TipoNotificacion;
import app.model.CapaPersistencia.PersistenciaSolicitud;
import app.model.CapaPersistencia.PersistenciaNotificacion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled; // Necesario para automatizar tareas
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class GestionSolicitudIntercambio {

    @Autowired
    private PersistenciaSolicitud persistenciaSolicitud;

    @Autowired
    private PersistenciaNotificacion persistenciaNotificacion;

    // 1. Registrar Solicitud adaptado a tus nuevos campos y constructor
    public void registrarSolicitud(String idEmisor, String idReceptor, String nombreServicio, int precioCreditos, String descripcionServicio) {
        List<SolicitudIntercambio> solicitudes = persistenciaSolicitud.cargar();

        // Usamos tu nuevo constructor parametrizado
        SolicitudIntercambio nueva = new SolicitudIntercambio(
                idEmisor, idReceptor, nombreServicio, precioCreditos, descripcionServicio
        );

        solicitudes.add(nueva);
        persistenciaSolicitud.guardar(solicitudes);

        // Disparamos la notificación al receptor usando tus campos de trazabilidad
        String mensajeNotificacion = "Has recibido una propuesta de intercambio para el servicio: " + nombreServicio;
        enviarNotificacion(idEmisor, idReceptor, mensajeNotificacion, TipoNotificacion.NUEVA_SOLICITUD_ENTRANTE);
    }

    // 2. Traduce procesarExpiracionesAutomaticas() de tu diagrama UML
    // fixedRate = 86400000 significa que el méto.do se ejecuta automáticamente cada 24 horas
    @Scheduled(fixedRate = 86400000)
    public void procesarExpiracionesAutomaticas() {
        List<SolicitudIntercambio> solicitudes = persistenciaSolicitud.cargar();
        Date fechaActual = new Date();
        boolean huboCambios = false;

        for (SolicitudIntercambio solicitud : solicitudes) {
            // Se evaluan solo las pendientes
            if (solicitud.getEstado() == EstadoSolicitudIntercambio.PENDIENTE) {

                // Si >= 5 dias
                if (solicitud.verificarExpiracion(fechaActual)) {

                    // Invocamos tu méto.do encapsulado de negocio
                    solicitud.marcarComoExpirada();
                    huboCambios = true;

                    // Notificamos de forma automática al emisor que su propuesta caducó
                    String mensajeExpiracion = "Tu solicitud de intercambio por '" + solicitud.getNombreServicio() + "' ha expirado tras superar el límite de 5 días.";
                    enviarNotificacion("SISTEMA", solicitud.getIdEmisor(), mensajeExpiracion, TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO);
                }
            }
        }

        // Solo se reescribe el JSON si hubo algún cambio de estado
        if (huboCambios) {
            persistenciaSolicitud.guardar(solicitudes);
            System.out.println("[BACKGROUND PROCESS] Se procesaron y actualizaron las solicitudes expiradas en el JSON.");
        }
    }

    // 3. Sistema de Notificaciones
    public void enviarNotificacion(String idRemitente, String idDestinatario, String mensaje, TipoNotificacion tipo) {
        List<Notificacion> buzon = persistenciaNotificacion.cargar();
        Notificacion nuevaNotificacion = new Notificacion(idRemitente, idDestinatario, mensaje, tipo);
        buzon.add(nuevaNotificacion);
        persistenciaNotificacion.guardar(buzon);
    }

    // 4. Buscar Solicitud
    public SolicitudIntercambio buscarSolicitud(String idSolicitudIntercambio) {
        return persistenciaSolicitud.cargar().stream()
                .filter(s -> s.getIdSolicitudIntercambio().equals(idSolicitudIntercambio))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));
    }



    public void actualizarEstado(String idSolicitudIntercambio, EstadoSolicitudIntercambio estado) {
        // 1. Cargamos todas las solicitudes guardadas en el JSON
        List<SolicitudIntercambio> solicitudes = persistenciaSolicitud.cargar();
        boolean encontrado = false;

        // 2. Buscamos la solicitud específica por su ID
        for (SolicitudIntercambio solicitud : solicitudes) {
            if (solicitud.getIdSolicitudIntercambio().equals(idSolicitudIntercambio)) {
                encontrado = true;

                // 3. Evaluamos la acción del usuario usando tus métodos encapsulados de negocio
                if (EstadoSolicitudIntercambio.ACEPTADA.equals(estado)) {
                    solicitud.marcarComoAceptada();

                    // Notificamos al emisor que su propuesta fue aceptada
                    String mensaje = "¡Buenas noticias! Tu propuesta de intercambio por '" + solicitud.getNombreServicio() + "' ha sido ACEPTADA.";
                    enviarNotificacion(solicitud.getIdReceptor(), solicitud.getIdEmisor(), mensaje, TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO);

                } else if (EstadoSolicitudIntercambio.RECHAZADA.equals(estado)) {
                    solicitud.marcarComoRechazada();

                    // Notificamos al emisor que su propuesta fue rechazada
                    String mensaje = "Tu propuesta de intercambio por '" + solicitud.getNombreServicio() + "' ha sido rechazada.";
                    enviarNotificacion(solicitud.getIdReceptor(), solicitud.getIdEmisor(), mensaje, TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO);
                } else {
                    throw new IllegalArgumentException("Estado no permitido. Solo se puede actualizar a ACEPTADA o RECHAZADA.");
                }
                break; // Rompemos el ciclo ya que encontramos el registro
            }
        }

        if (!encontrado) {
            throw new IllegalArgumentException("No se encontró ninguna solicitud con el ID proporcionado: " + idSolicitudIntercambio);
        }

        // 4. Guardamos la lista con el estado ya actualizado en el archivo JSON
        persistenciaSolicitud.guardar(solicitudes);
        System.out.println("[SISTEMA] Solicitud " + idSolicitudIntercambio + " actualizada con éxito a: " + estado);
    }


}