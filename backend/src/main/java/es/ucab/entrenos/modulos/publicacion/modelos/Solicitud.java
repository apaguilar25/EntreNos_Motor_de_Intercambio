package es.ucab.entrenos.modulos.publicacion.modelos;

import java.util.UUID;

public class Solicitud {

    private String idSolicitud;
    private String idPublicacion;
    private String idSolicitante;
    private String estado;
    private long fechaCreacion;
    private long fechaLimiteRespuesta;
    private int version;

    private static final long PLAZO_RESPUESTA_MS = 5L * 24 * 60 * 60 * 1000;

    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_ACEPTADA = "ACEPTADA";
    public static final String ESTADO_RECHAZADA = "RECHAZADA";
    public static final String ESTADO_EXPIRADA = "EXPIRADA";

    public Solicitud() {}

    public Solicitud(String idPublicacion, String idSolicitante) {
        this.idSolicitud = "SOL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.idPublicacion = idPublicacion;
        this.idSolicitante = idSolicitante;
        this.estado = ESTADO_PENDIENTE;
        this.fechaCreacion = System.currentTimeMillis();
        this.fechaLimiteRespuesta = System.currentTimeMillis() + PLAZO_RESPUESTA_MS;
    }

    public void aceptar() {
        if (haExpirado()) {
            this.estado = ESTADO_EXPIRADA;
            throw new IllegalStateException("El plazo de 5 días para responder ha expirado.");
        }
        if (!ESTADO_PENDIENTE.equals(this.estado)) {
            throw new IllegalStateException("No hay una solicitud pendiente.");
        }
        this.estado = ESTADO_ACEPTADA;
    }

    public void rechazar() {
        if (haExpirado()) {
            this.estado = ESTADO_EXPIRADA;
            throw new IllegalStateException("El plazo de 5 días para responder ha expirado.");
        }
        if (!ESTADO_PENDIENTE.equals(this.estado)) {
            throw new IllegalStateException("No hay una solicitud pendiente.");
        }
        this.estado = ESTADO_RECHAZADA;
    }

    public void expirar() {
        if (ESTADO_PENDIENTE.equals(this.estado)) {
            this.estado = ESTADO_EXPIRADA;
        }
    }

    public boolean haExpirado() {
        return ESTADO_PENDIENTE.equals(this.estado)
                && System.currentTimeMillis() > this.fechaLimiteRespuesta;
    }

    public String getIdSolicitud() { return idSolicitud; }
    public void setIdSolicitud(String idSolicitud) { this.idSolicitud = idSolicitud; }

    public String getIdPublicacion() { return idPublicacion; }
    public void setIdPublicacion(String idPublicacion) { this.idPublicacion = idPublicacion; }

    public String getIdSolicitante() { return idSolicitante; }
    public void setIdSolicitante(String idSolicitante) { this.idSolicitante = idSolicitante; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public long getFechaLimiteRespuesta() { return fechaLimiteRespuesta; }
    public void setFechaLimiteRespuesta(long fechaLimiteRespuesta) { this.fechaLimiteRespuesta = fechaLimiteRespuesta; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}
