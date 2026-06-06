package es.ucab.entrenos.modulos.publicacion.modelos;

import java.util.UUID;

/**
 * Transacción de intercambio de servicio por créditos comunitarios (HU2/HU3/HU8).
 */
public class Transaccion {

    private String idTransaccion;
    private String idPublicacion;     // Publicación que originó esta transacción
    private String idOfertante;
    private String idDemandante;
    private EstadoTransaccion estado;
    private int creditosRetenidos;
    private boolean confirmacionOfertante;
    private boolean confirmacionDemandante;
    private String nombreServicio;
    private String descripcion;
    private long fechaCreacion;

    // HU2: Fecha límite de 5 días para aceptar/rechazar
    private long fechaLimiteAceptacion;

    // HU8: Reporte de incidencia/fraude
    private boolean tieneIncidencia;
    private String descripcionIncidencia;
    private String urlEvidenciaIncidencia;

    // HU3: Calificación del servicio (1–5 estrellas)
    private Integer calificacionDada; // null hasta que se califique

    public Transaccion() {}

    public Transaccion(String idPublicacion, String idOfertante, String idDemandante,
                       String nombreServicio, String descripcion, int creditosRetenidos) {
        this.idTransaccion = "TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.idPublicacion = idPublicacion;
        this.idOfertante = idOfertante;
        this.idDemandante = idDemandante;
        this.nombreServicio = nombreServicio;
        this.descripcion = descripcion;
        this.creditosRetenidos = creditosRetenidos;
        this.estado = EstadoTransaccion.PENDIENTE;
        this.confirmacionOfertante = false;
        this.confirmacionDemandante = false;
        this.fechaCreacion = System.currentTimeMillis();
        // HU2: 5 días para aceptar o rechazar
        this.fechaLimiteAceptacion = System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000);
        this.tieneIncidencia = false;
    }

    // HU3: Confirmar entrega por el ofertante
    public void confirmarEntregaOfertante() {
        if (this.estado != EstadoTransaccion.INICIADA) {
            throw new IllegalStateException("Solo se puede confirmar una transacción en estado INICIADA.");
        }
        if (this.tieneIncidencia) {
            throw new IllegalStateException("No se puede confirmar mientras exista una incidencia activa.");
        }
        this.confirmacionOfertante = true;
        verificarCompletitud();
    }

    // HU3: Confirmar recepción por el demandante
    public void confirmarRecepcionDemandante() {
        if (this.estado != EstadoTransaccion.INICIADA) {
            throw new IllegalStateException("Solo se puede confirmar una transacción en estado INICIADA.");
        }
        if (this.tieneIncidencia) {
            throw new IllegalStateException("No se puede confirmar mientras exista una incidencia activa.");
        }
        this.confirmacionDemandante = true;
        verificarCompletitud();
    }

    // Si ambas partes confirmaron → FINALIZADA
    private void verificarCompletitud() {
        if (this.confirmacionOfertante && this.confirmacionDemandante) {
            this.estado = EstadoTransaccion.FINALIZADA;
        }
    }

    // HU2: Verificar si expiró el plazo de 5 días para aceptar
    public boolean haExpiradoPlazoAceptacion() {
        return this.estado == EstadoTransaccion.PENDIENTE
                && System.currentTimeMillis() > this.fechaLimiteAceptacion;
    }

    // HU8: Reportar incidencia/fraude
    public void reportarIncidencia(String descripcion, String urlEvidencia) {
        if (this.estado != EstadoTransaccion.INICIADA) {
            throw new IllegalStateException("Solo se puede reportar una incidencia en transacciones INICIADAS.");
        }
        if (descripcion == null || descripcion.trim().length() < 20) {
            throw new IllegalArgumentException("La descripción del incidente debe tener al menos 20 caracteres.");
        }
        this.tieneIncidencia = true;
        this.descripcionIncidencia = descripcion;
        this.urlEvidenciaIncidencia = urlEvidencia;
        this.estado = EstadoTransaccion.EN_DISPUTA;
    }

    // --- Getters ---
    public String getIdTransaccion() { return idTransaccion; }
    public String getIdPublicacion() { return idPublicacion; }
    public String getIdOfertante() { return idOfertante; }
    public String getIdDemandante() { return idDemandante; }
    public EstadoTransaccion getEstado() { return estado; }
    public int getCreditosRetenidos() { return creditosRetenidos; }
    public boolean isConfirmacionOfertante() { return confirmacionOfertante; }
    public boolean isConfirmacionDemandante() { return confirmacionDemandante; }
    public String getNombreServicio() { return nombreServicio; }
    public String getDescripcion() { return descripcion; }
    public long getFechaCreacion() { return fechaCreacion; }
    public long getFechaLimiteAceptacion() { return fechaLimiteAceptacion; }
    public boolean isTieneIncidencia() { return tieneIncidencia; }
    public String getDescripcionIncidencia() { return descripcionIncidencia; }
    public String getUrlEvidenciaIncidencia() { return urlEvidenciaIncidencia; }
    public Integer getCalificacionDada() { return calificacionDada; }

    // --- Setters ---
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }
    public void setIdPublicacion(String idPublicacion) { this.idPublicacion = idPublicacion; }
    public void setIdOfertante(String idOfertante) { this.idOfertante = idOfertante; }
    public void setIdDemandante(String idDemandante) { this.idDemandante = idDemandante; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }
    public void setCreditosRetenidos(int creditosRetenidos) { this.creditosRetenidos = creditosRetenidos; }
    public void setConfirmacionOfertante(boolean confirmacionOfertante) { this.confirmacionOfertante = confirmacionOfertante; }
    public void setConfirmacionDemandante(boolean confirmacionDemandante) { this.confirmacionDemandante = confirmacionDemandante; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setFechaLimiteAceptacion(long fechaLimiteAceptacion) { this.fechaLimiteAceptacion = fechaLimiteAceptacion; }
    public void setTieneIncidencia(boolean tieneIncidencia) { this.tieneIncidencia = tieneIncidencia; }
    public void setDescripcionIncidencia(String descripcionIncidencia) { this.descripcionIncidencia = descripcionIncidencia; }
    public void setUrlEvidenciaIncidencia(String urlEvidenciaIncidencia) { this.urlEvidenciaIncidencia = urlEvidenciaIncidencia; }
    public void setCalificacionDada(Integer calificacionDada) { this.calificacionDada = calificacionDada; }
}

