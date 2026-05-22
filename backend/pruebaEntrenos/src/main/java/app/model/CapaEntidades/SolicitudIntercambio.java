package app.model.CapaEntidades;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SolicitudIntercambio {
    private String idSolicitudIntercambio;
    private String idOfertante;
    private String idDemandante;

    private Date fechaEmision;
    private String nombreServicio;
    private int precioCreditos;
    private String descripcionServicio;
    private EstadoSolicitudIntercambio estado; // PENDIENTE, ACEPTADA, RECHAZADA, EXPIRADA

    // Trazabilidad mediante llaves foráneas (IDs)
    private String idEmisor;
    private String idReceptor;

    public SolicitudIntercambio() {
    }

    public SolicitudIntercambio(String idEmisor, String idReceptor, String nombreServicio, int precioCreditos, String descripcion) {
        this.idSolicitudIntercambio = UUID.randomUUID().toString();
        this.fechaEmision = new Date();
        this.idEmisor = idEmisor;
        this.idReceptor = idReceptor;
        this.nombreServicio = nombreServicio;
        this.precioCreditos = precioCreditos;
        this.descripcionServicio = descripcion;
        this.estado = EstadoSolicitudIntercambio.PENDIENTE;
    }

    // Cambios de estado controlados por negocio interno (Reglas de HU2)
    public void marcarComoRechazada() {
        validarEstadoPendiente();
        this.estado = EstadoSolicitudIntercambio.RECHAZADA;
    }

    public void marcarComoAceptada() {
        validarEstadoPendiente();
        this.estado = EstadoSolicitudIntercambio.ACEPTADA;
    }

    public void marcarComoExpirada() {
        validarEstadoPendiente();
        this.estado = EstadoSolicitudIntercambio.RECHAZADA;
    }

    public boolean verificarExpiracion(Date fechaActual) {
        long diffInMillies = Math.abs(fechaActual.getTime() - this.fechaEmision.getTime());
        long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        return diffInDays >= 5;
    }

    private void validarEstadoPendiente() {
        if (!"PENDIENTE".equals(this.estado)) {
            throw new IllegalStateException("La solicitud ya no se encuentra en estado PENDIENTE.");
        }
    }

    // Getters y Setters

    public String getIdSolicitudIntercambio() {
        return idSolicitudIntercambio;
    }

    public void setIdSolicitudIntercambio(String idSolicitudIntercambio) {
        this.idSolicitudIntercambio = idSolicitudIntercambio;
    }

    public SolicitudIntercambio(String idSolicitudIntercambio, String idOfertante, String idDemandante, Date fechaEmision, String nombreServicio, int precioCreditos, String descripcionServicio, EstadoSolicitudIntercambio estado, String idEmisor, String idReceptor) {
        this.idSolicitudIntercambio = idSolicitudIntercambio;
        this.idOfertante = idOfertante;
        this.idDemandante = idDemandante;
        this.fechaEmision = fechaEmision;
        this.nombreServicio = nombreServicio;
        this.precioCreditos = precioCreditos;
        this.descripcionServicio = descripcionServicio;
        this.estado = estado;
        this.idEmisor = idEmisor;
        this.idReceptor = idReceptor;
    }

    public Date getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(Date fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public String getNombreServicio() {
        return nombreServicio;
    }

    public void setNombreServicio(String nombreServicio) {
        this.nombreServicio = nombreServicio;
    }

    public int getPrecioCreditos() {
        return precioCreditos;
    }

    public String getIdOfertante() {
        return idOfertante;
    }

    public String getIdDemandante() {
        return idDemandante;
    }

    public void setPrecioCreditos(int precioCreditos) {
        this.precioCreditos = precioCreditos;
    }

    public String getDescripcionServicio() {
        return descripcionServicio;
    }

    public void setDescripcionServicio(String descripcion) {
        this.descripcionServicio = descripcion;
    }

    public EstadoSolicitudIntercambio getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitudIntercambio estado) {
        this.estado = estado;
    }

    public String getIdEmisor() {
        return idEmisor;
    }

    public void setIdEmisor(String idEmisor) {
        this.idEmisor = idEmisor;
    }

    public String getIdReceptor() {
        return idReceptor;
    }

    public void setIdReceptor(String idReceptor) {
        this.idReceptor = idReceptor;
    }

    public void setIdOfertante(String idOfertante) {
    }

    public void setIdDemandante(String idDemandante) {
    }
}