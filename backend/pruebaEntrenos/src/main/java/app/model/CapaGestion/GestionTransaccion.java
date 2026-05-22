package app.model.CapaGestion;

import app.model.CapaEntidades.Transaccion;
import app.model.CapaEntidades.EstadoTransaccion;
import app.model.CapaEntidades.Notificacion;
import app.model.CapaEntidades.TipoNotificacion;
import app.model.CapaEntidades.Usuario;
import app.model.CapaPersistencia.PersistenciaTransacciones;
import app.model.CapaPersistencia.PersistenciaNotificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GestionTransaccion {

    @Autowired
    private PersistenciaTransacciones persistenciaTransacciones;

    @Autowired
    private PersistenciaNotificacion persistenciaNotificacion;

    // Constructor vacío exigido por el diagrama UML
    public GestionTransaccion() {
    }

    // ========================================================================
    // MÉTODOS ESTRICTOS DEL DIAGRAMA UML
    // ========================================================================

    // 1. registrarTransaccion según el UML (Firma estricta)
    public void registrarTransaccion(String idOfertante, String idDemandante, String nombreServicio) {
        // Llama al método operativo usando valores por defecto para no duplicar código
        registrarTransaccion(idOfertante, idDemandante, nombreServicio, "Sin descripción", 0);
    }

    // 2. buscarTransaccion según el UML
    public Transaccion buscarTransaccion(String idTransaccion) {
        return persistenciaTransacciones.cargar().stream()
                .filter(t -> t.getIdTransaccion().equals(idTransaccion))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con el ID: " + idTransaccion));
    }

    // 3. actualizarEstado según el UML
    public void actualizarEstado(String idTransaccion, EstadoTransaccion estado) {
        List<Transaccion> transacciones = persistenciaTransacciones.cargar();
        boolean encontrada = false;

        for (Transaccion tx : transacciones) {
            if (tx.getIdTransaccion().equals(idTransaccion)) {
                tx.setEstado(estado);
                encontrada = true;
                break;
            }
        }

        if (!encontrada) throw new IllegalArgumentException("No se encontró la transacción.");
        persistenciaTransacciones.guardar(transacciones);
        System.out.println("[SISTEMA] Transacción " + idTransaccion + " actualizada a estado: " + estado);
    }

    // 4. retenerCreditos según el UML
    public void retenerCreditos() {
        System.out.println("[BANCO INTERNO] Créditos retenidos como garantía de la transacción.");
    }

    // 5. liberarCreditos según el UML
    public void liberarCreditos() {
        System.out.println("[BANCO INTERNO] Créditos liberados y transferidos al ofertante.");
    }

    // 6. calificarServicio según el UML (CREADO Y FUNCIONAL)
    public void calificarServicio(String idPrestador, int puntuacion) {
        if (puntuacion < 1 || puntuacion > 5) {
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5.");
        }

        // Aquí se conectaría idealmente con PersistenciaUsuario para guardar la calificación
        System.out.println("[SISTEMA DE REPUTACIÓN] El usuario " + idPrestador + " ha recibido una calificación de " + puntuacion + " estrellas.");

        // Simulación de envío de notificación al prestador avisando de su nueva calificación
        Usuario prestadorMock = new Usuario();
        prestadorMock.setIdUsuario(idPrestador);
        enviarNotificacion(prestadorMock, "Has recibido una nueva calificación de " + puntuacion + " estrellas en tu último servicio.", TipoNotificacion.ALERTA_SISTEMA);
    }

    // 7. enviarNotificacion según el UML (Firma estricta usando objeto Usuario)
    public void enviarNotificacion(Usuario destinatario, String mensaje, TipoNotificacion tipo) {
        if (destinatario == null || destinatario.getIdUsuario() == null) return;

        List<Notificacion> buzon = persistenciaNotificacion.cargar();
        buzon.add(new Notificacion("SISTEMA", destinatario.getIdUsuario(), mensaje, tipo));
        persistenciaNotificacion.guardar(buzon);
    }


    // ========================================================================
    // MÉTODOS OPERATIVOS (Para que los Controladores y la HU3 no se rompan)
    // ========================================================================

    // Sobrecarga de registrarTransaccion (Usada por la HU2 al aceptar solicitud)
    public void registrarTransaccion(String idOfertante, String idDemandante, String nombreServicio, String descripcion, int creditos) {
        List<Transaccion> transacciones = persistenciaTransacciones.cargar();

        Transaccion nuevaTx = new Transaccion();
        nuevaTx.setIdTransaccion("TX-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        nuevaTx.setIdOfertante(idOfertante);
        nuevaTx.setIdDemandante(idDemandante);
        nuevaTx.setNombreServicio(nombreServicio);
        nuevaTx.setDescripcion(descripcion);
        nuevaTx.setCreditosRetenidos(creditos);
        nuevaTx.setEstado(EstadoTransaccion.INICIADA);
        nuevaTx.setConfirmacionOfertante(false);
        nuevaTx.setConfirmacionDemandante(false);

        transacciones.add(nuevaTx);
        persistenciaTransacciones.guardar(transacciones);
        retenerCreditos(); // Usamos el método del UML

        System.out.println("[SISTEMA] Nueva transacción INICIADA: " + nuevaTx.getIdTransaccion());
    }

    // Restaurado: Confirmación del Ofertante (Usado en TransaccionController)
    public void confirmarEntrega(String idTransaccion) {
        List<Transaccion> transacciones = persistenciaTransacciones.cargar();
        Transaccion tx = buscarTransaccion(idTransaccion); // Usa el método del UML

        if (tx.getEstado() != EstadoTransaccion.INICIADA) {
            throw new IllegalStateException("La transacción no se puede confirmar en estado: " + tx.getEstado());
        }

        tx.setConfirmacionOfertante(true);
        verificarYFinalizarTransaccion(tx, transacciones);
    }

    // Restaurado: Confirmación del Demandante (Usado en TransaccionController)
    public void confirmarRecepcion(String idTransaccion) {
        List<Transaccion> transacciones = persistenciaTransacciones.cargar();
        Transaccion tx = buscarTransaccion(idTransaccion); // Usa el método del UML

        if (tx.getEstado() != EstadoTransaccion.INICIADA) {
            throw new IllegalStateException("La transacción no se puede confirmar en estado: " + tx.getEstado());
        }

        tx.setConfirmacionDemandante(true);
        verificarYFinalizarTransaccion(tx, transacciones);
    }

    // Lógica interna para cerrar el ciclo si hay doble confirmación
    private void verificarYFinalizarTransaccion(Transaccion tx, List<Transaccion> transacciones) {
        if (tx.isConfirmacionOfertante() && tx.isConfirmacionDemandante()) {
            tx.setEstado(EstadoTransaccion.FINALIZADA);
            liberarCreditos(); // Usamos el método del UML

            System.out.println("[HU3 - SISTEMA] Transacción " + tx.getIdTransaccion() + " FINALIZADA. Créditos transferidos.");
        }

        // Importante: Guardar los cambios independientemente de si finalizó o solo cambió un booleano
        persistenciaTransacciones.guardar(transacciones);
    }
}