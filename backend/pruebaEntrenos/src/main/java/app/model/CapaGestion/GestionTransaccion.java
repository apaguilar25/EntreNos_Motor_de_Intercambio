package app.model.CapaGestion;

import app.model.CapaEntidades.EstadoTransaccion;
import app.model.CapaEntidades.Transaccion;
import app.model.CapaEntidades.TipoNotificacion;
import app.model.CapaEntidades.Notificacion;
import app.model.CapaPersistencia.PersistenciaTransacciones;
import app.model.CapaPersistencia.PersistenciaNotificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestionTransaccion {

    @Autowired
    private PersistenciaTransacciones persistenciaTransacciones;

    @Autowired
    private PersistenciaNotificacion persistenciaNotificacion;

    @Autowired
    private GestionUsuario gestionUsuario; // Para conectar con la reputación y monederos

    // 1. Registrar una nueva transacción (Ocurre justo después de que se ACEPTA una Solicitud en la HU2)
    public Transaccion registrarTransaccion(String idOfertante, String idDemandante, String nombreServicio, String descripcion, int creditosRetenidos) {
        List<Transaccion> transacciones = persistenciaTransacciones.cargar();

        Transaccion nueva = new Transaccion(idOfertante, idDemandante, nombreServicio, descripcion, creditosRetenidos);
        transacciones.add(nueva);
        persistenciaTransacciones.guardar(transacciones);

        enviarNotificacion(idOfertante, idDemandante, "El intercambio por '" + nombreServicio + "' ha iniciado.", TipoNotificacion.TRANSACCION_ACTUALIZADA);

        return nueva;
    }

    // 2. Buscar transacción por ID
    public Transaccion buscarTransaccion(String idTransaccion) {
        return persistenciaTransacciones.cargar().stream()
                .filter(t -> t.getIdTransaccion().equals(idTransaccion))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada."));
    }

    // 3. Confirmar parte del intercambio (Reemplaza actualizarEstado general del diagrama para ser más seguro)
    public void confirmarTransaccion(String idTransaccion, String idUsuarioQueConfirma) {
        List<Transaccion> transacciones = persistenciaTransacciones.cargar();
        boolean encontrada = false;

        for (Transaccion t : transacciones) {
            if (t.getIdTransaccion().equals(idTransaccion)) {
                encontrada = true;

                // Marcar la confirmación dependiendo de quién presionó el botón
                if (t.getIdOfertante().equals(idUsuarioQueConfirma)) {
                    t.setConfirmacionOfertante(true);
                } else if (t.getIdDemandante().equals(idUsuarioQueConfirma)) {
                    t.setConfirmacionDemandante(true);
                }

                // Si ambos confirmaron, la transacción finaliza
                if (t.isConfirmacionOfertante() && t.isConfirmacionDemandante()) {
                    t.setEstado(EstadoTransaccion.FINALIZADA);
                    liberarCreditos(t); // Trasladamos los créditos oficialmente
                    enviarNotificacion("SISTEMA", t.getIdOfertante(), "El intercambio ha finalizado con éxito. ¡No olvides calificar!", TipoNotificacion.TRANSACCION_ACTUALIZADA);
                    enviarNotificacion("SISTEMA", t.getIdDemandante(), "El intercambio ha finalizado con éxito. ¡No olvides calificar!", TipoNotificacion.TRANSACCION_ACTUALIZADA);
                }
                break;
            }
        }

        if (!encontrada) throw new IllegalArgumentException("Error al confirmar: Transacción no existe.");
        persistenciaTransacciones.guardar(transacciones);
    }

    // 4. Liberar Créditos (Transferir del comprometido del demandante al disponible del ofertante)
    public void liberarCreditos(Transaccion transaccion) {
        // En una implementación real más compleja, aquí llamarías a GestionUsuario
        // para descontar de un Monedero y sumar en el otro.
        System.out.println("[ECONOMÍA] Liberando " + transaccion.getCreditosRetenidos() + " créditos al usuario " + transaccion.getIdOfertante());
    }

    // 5. Calificar Servicio
    public void calificarServicio(String idPrestador, String nombreServicio, int puntuacion) {
        // Conectamos con el método de HU2 que dejamos preparado
        gestionUsuario.calcularReputacionServicio(idPrestador, nombreServicio, puntuacion);
        gestionUsuario.calcularReputacionHistorica(idPrestador);
    }

    // 6. Enviar Notificación
    public void enviarNotificacion(String idRemitente, String idDestinatario, String mensaje, TipoNotificacion tipo) {
        List<Notificacion> buzon = persistenciaNotificacion.cargar();
        buzon.add(new Notificacion(idRemitente, idDestinatario, mensaje, tipo));
        persistenciaNotificacion.guardar(buzon);
    }
}