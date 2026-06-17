package es.ucab.entrenos.modulos.publicacion.modelos;

import java.util.UUID;

public class Solicitud {

    private String idSolicitud;
    private String idPublicacion;
    private String idSolicitante;
    private EstadoSolicitud estado;
    private long fechaCreacion;
    private long fechaLimiteRespuesta;
    private int version;

    private static final long PLAZO_RESPUESTA_MS = 5L * 24 * 60 * 60 * 1000;

    public Solicitud() {}

    public Solicitud(String idPublicacion, String idSolicitante) {
        this.idSolicitud = "SOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.idPublicacion = idPublicacion;
        this.idSolicitante = idSolicitante;
        this.estado = EstadoSolicitud.PENDIENTE;
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaLimiteRespuesta = System.currentTimeMillis() + PLAZO_RESPUESTA_MS;
    }

    public void aceptar() {
        if (haExpirado()) {
            this.estado = EstadoSolicitud.EXPIRADA;
            throw new IllegalStateException("El plazo de 5 días para responder ha expirado.");
        }
        if (this.estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("No hay una solicitud pendiente.");
        }
        this.estado = EstadoSolicitud.ACEPTADA;
    }

    public void rechazar() {
        if (haExpirado()) {
            this.estado = EstadoSolicitud.EXPIRADA;
            throw new IllegalStateException("El plazo de 5 días para responder ha expirado.");
        }
        if (this.estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("No hay una solicitud pendiente.");
        }
        this.estado = EstadoSolicitud.RECHAZADA;
    }

    public void cancelar() {
        if (this.estado != EstadoSolicitud.PENDIENTE) {
            throw new IllegalStateException("Solo se puede cancelar una solicitud en estado PENDIENTE.");
        }
        this.estado = EstadoSolicitud.RECHAZADA;
    }

    public void expirar() {
        if (this.estado == EstadoSolicitud.PENDIENTE) {
            this.estado = EstadoSolicitud.EXPIRADA;
        }
    }

    public boolean haExpirado() {
        return this.estado == EstadoSolicitud.PENDIENTE
                && System.currentTimeMillis() > this.fechaLimiteRespuesta;
    }

    public String getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(String idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getIdPublicacion() { return idPublicacion; }
    public void setIdPublicacion(String idPublicacion) { this.idPublicacion = idPublicacion; }

    public String getIdSolicitante() { return idSolicitante; }
    public void setIdSolicitante(String idSolicitante) { this.idSolicitante = idSolicitante; }

    public EstadoSolicitud getEstado() { return estado; }
    public void setEstado(EstadoSolicitud estado) { this.estado = estado; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getFechaLimiteRespuesta() { return fechaLimiteRespuesta; }
    public void setFechaLimiteRespuesta(long fechaLimiteRespuesta) { this.fechaLimiteRespuesta = fechaLimiteRespuesta; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
