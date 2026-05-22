package app.model.CapaGestion;

import app.model.CapaEntidades.SolicitudIntercambio;
import app.model.CapaEntidades.EstadoSolicitudIntercambio;
import app.model.CapaEntidades.Notificacion;
import app.model.CapaEntidades.TipoNotificacion;
import app.model.CapaPersistencia.PersistenciaNotificacion;
import app.model.CapaPersistencia.PersistenciaSolicitud;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class GestionSolicitudIntercambio {

    @Autowired
    private PersistenciaSolicitud persistenciaSolicitud;

    @Autowired
    private PersistenciaNotificacion persistenciaNotificacion;

    @Autowired
    private GestionUsuario gestionUsuario;

    // 1. Registrar Solicitud adaptado a tus nuevos campos y constructor
    public void registrarSolicitud(String idEmisor, String idReceptor, String nombreServicio, int precioCreditos, String descripcionServicio) {
        // Antes de crear la solicitud, intentamos comprometer los créditos. 
        // Si no hay fondos, esto lanzará una excepción y detendrá el proceso.
        if (precioCreditos > 0) {
            gestionUsuario.comprometerCreditos(idEmisor, precioCreditos);
        }

        List<SolicitudIntercambio> solicitudes = persistenciaSolicitud.cargar();

        SolicitudIntercambio nueva = new SolicitudIntercambio(
                idEmisor, idReceptor, nombreServicio, precioCreditos, descripcionServicio
        );

        solicitudes.add(nueva);
        persistenciaSolicitud.guardar(solicitudes);

        String mensajeNotificacion = "Has recibido una propuesta de intercambio para el servicio: " + nombreServicio;
        enviarNotificacion(idEmisor, idReceptor, mensajeNotificacion, TipoNotificacion.NUEVA_SOLICITUD_ENTRANTE);
    }

    // 2. Proceso automático corregido (Usa SolicitudIntercambio en vez de Transaccion)
    @Scheduled(fixedRate = 86400000)
    public void procesarExpiracionesAutomaticas() {
        List<SolicitudIntercambio> solicitudes = persistenciaSolicitud.cargar(); // Corregido: Usa persistenciaSolicitud
        Date fechaActual = new Date();
        boolean huboCambios = false;

        for (SolicitudIntercambio solicitud : solicitudes) {
            if (solicitud.getEstado() == EstadoSolicitudIntercambio.PENDIENTE) {
                if (solicitud.verificarExpiracion(fechaActual)) {
                    solicitud.marcarComoExpirada();
                    huboCambios = true;

                    String mensajeExpiracion = "Tu solicitud de intercambio por '" + solicitud.getNombreServicio() + "' ha expirado.";
                    enviarNotificacion("SISTEMA", solicitud.getIdEmisor(), mensajeExpiracion, TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO);
                }
            }
        }

        if (huboCambios) {
            persistenciaSolicitud.guardar(solicitudes);
            System.out.println("[BACKGROUND PROCESS] Solicitudes expiradas actualizadas.");
        }
    }

    // 3. Sistema de Notificaciones
    public void enviarNotificacion(String idRemitente, String idDestinatario, String mensaje, TipoNotificacion tipo) {
        List<Notificacion> buzon = persistenciaNotificacion.cargar();
        buzon.add(new Notificacion(idRemitente, idDestinatario, mensaje, tipo));
        persistenciaNotificacion.guardar(buzon);
    }

    public SolicitudIntercambio buscarSolicitud(String idSolicitudIntercambio) {
        return persistenciaSolicitud.cargar().stream()
                .filter(s -> s.getIdSolicitudIntercambio().equals(idSolicitudIntercambio))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada."));
    }

<<<<<<< HEAD
    // 5. Actualizar estado y disparar Transacción si es ACEPTADA
=======
    // 5. Obtener Solicitudes Enviadas por un Usuario
    public List<SolicitudIntercambio> obtenerSolicitudesEnviadas(String idEmisor) {
        return persistenciaSolicitud.cargar().stream()
                .filter(s -> s.getIdEmisor().equals(idEmisor))
                .collect(java.util.stream.Collectors.toList());
    }



>>>>>>> db4991d (Conexion Back-Front HU1 y HU2 completo (creo))
    public void actualizarEstado(String idSolicitudIntercambio, EstadoSolicitudIntercambio estado) {
        List<SolicitudIntercambio> solicitudes = persistenciaSolicitud.cargar();
        boolean encontrado = false;

        for (SolicitudIntercambio solicitud : solicitudes) {
            if (solicitud.getIdSolicitudIntercambio().equals(idSolicitudIntercambio)) {
                encontrado = true;

                if (EstadoSolicitudIntercambio.ACEPTADA.equals(estado)) {
                    solicitud.marcarComoAceptada();

                    // Al aceptar la solicitud, se genera automáticamente la transacción en la HU3
                    gestionTransaccion.registrarTransaccion(
                            solicitud.getIdOfertante(),
                            solicitud.getIdDemandante(),
                            solicitud.getNombreServicio()
                    );

                    String mensaje = "¡Buenas noticias! Tu propuesta por '" + solicitud.getNombreServicio() + "' ha sido ACEPTADA.";
                    enviarNotificacion(solicitud.getIdReceptor(), solicitud.getIdEmisor(), mensaje, TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO);

                } else if (EstadoSolicitudIntercambio.RECHAZADA.equals(estado)) {
                    solicitud.marcarComoRechazada(); // O marcarComoRechazada() según tu entidad

                    String mensaje = "Tu propuesta de intercambio por '" + solicitud.getNombreServicio() + "' ha sido rechazada.";
                    enviarNotificacion(solicitud.getIdReceptor(), solicitud.getIdEmisor(), mensaje, TipoNotificacion.ESTADO_SOLICITUD_CAMBIADO);
                }
                break;
            }
        }

        if (!encontrado) throw new IllegalArgumentException("No se encontró la solicitud: " + idSolicitudIntercambio);

        persistenciaSolicitud.guardar(solicitudes);
    }
<<<<<<< HEAD
=======

    public void cancelarSolicitud(String idSolicitudIntercambio) {
        List<SolicitudIntercambio> solicitudes = persistenciaSolicitud.cargar();
        boolean encontrado = false;

        for (SolicitudIntercambio solicitud : solicitudes) {
            if (solicitud.getIdSolicitudIntercambio().equals(idSolicitudIntercambio)) {
                encontrado = true;
                solicitud.marcarComoCancelada();
                
                if (solicitud.getPrecioCreditos() > 0) {
                    gestionUsuario.revertirCreditosComprometidos(solicitud.getIdEmisor(), solicitud.getPrecioCreditos());
                }
                break;
            }
        }

        if (!encontrado) {
            throw new IllegalArgumentException("No se encontró ninguna solicitud con el ID proporcionado: " + idSolicitudIntercambio);
        }

        persistenciaSolicitud.guardar(solicitudes);
        System.out.println("[SISTEMA] Solicitud " + idSolicitudIntercambio + " ha sido CANCELADA y se han devuelto los créditos.");
    }


>>>>>>> db4991d (Conexion Back-Front HU1 y HU2 completo (creo))
}