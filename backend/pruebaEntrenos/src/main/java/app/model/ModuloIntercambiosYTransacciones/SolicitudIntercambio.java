package org.example.ModuloIntercambiosYTransacciones;

import java.util.Date;

public class SolicitudIntercambio {

    private String idSolicitudIntercambio;
    private String idUsuarioDemandante;
    private String idUsuarioOfertante;
    private Date fechaEmision;
    private String nombreServicio;
    private int precioCreditos;
    private String descripcionServicio;
    private String estadoSolicitud; // Pendiente, Aceptada, Rechazada

    private boolean verificarExpiracion(Date fechaActual){
        return false;
    };

    public SolicitudIntercambio(String idSolicitudIntercambio, String idUsuarioDemandante, String idUsuarioOfertante, Date fechaEmision, String nombreServicio, int precioCreditos, String descripcionServicio, String estadoSolicitud) {
        this.idSolicitudIntercambio = idSolicitudIntercambio;
        this.idUsuarioDemandante = idUsuarioDemandante;
        this.idUsuarioOfertante = idUsuarioOfertante;
        this.fechaEmision = fechaEmision;
        this.nombreServicio = nombreServicio;
        this.precioCreditos = precioCreditos;
        this.descripcionServicio = descripcionServicio;
        this.estadoSolicitud = estadoSolicitud;
    }
}
