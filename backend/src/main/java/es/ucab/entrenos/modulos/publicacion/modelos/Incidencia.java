package es.ucab.entrenos.modulos.publicacion.modelos;

import java.util.UUID;

public class Incidencia {

    private String idIncidencia;
    private String idTransaccion;
    private String idUsuarioReportante;
    private String descripcion;
    private String urlEvidencia;
    private String idUsuarioDefensor;
    private String descripcionDefensa;
    private String urlEvidenciaDefensa;
    private String estado;
    private long fechaCreacion;

    public Incidencia() {}

    public Incidencia(String idTransaccion, String idUsuarioReportante, String descripcion, String urlEvidencia) {
        this.idIncidencia = "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.idTransaccion = idTransaccion;
        this.idUsuarioReportante = idUsuarioReportante;
        this.descripcion = descripcion;
        this.urlEvidencia = urlEvidencia;
        this.estado = "ABIERTA";
        this.fechaCreacion = System.currentTimeMillis();
    }

    public String getIdIncidencia() { return idIncidencia; }
    public String getIdTransaccion() { return idTransaccion; }
    public String getIdUsuarioReportante() { return idUsuarioReportante; }
    public String getDescripcion() { return descripcion; }
    public String getUrlEvidencia() { return urlEvidencia; }
    public String getIdUsuarioDefensor() { return idUsuarioDefensor; }
    public String getDescripcionDefensa() { return descripcionDefensa; }
    public String getUrlEvidenciaDefensa() { return urlEvidenciaDefensa; }
    public String getEstado() { return estado; }
    public long getFechaCreacion() { return fechaCreacion; }

    public void setIdIncidencia(String idIncidencia) { this.idIncidencia = idIncidencia; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }
    public void setIdUsuarioReportante(String idUsuarioReportante) { this.idUsuarioReportante = idUsuarioReportante; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setUrlEvidencia(String urlEvidencia) { this.urlEvidencia = urlEvidencia; }
    public void setIdUsuarioDefensor(String idUsuarioDefensor) { this.idUsuarioDefensor = idUsuarioDefensor; }
    public void setDescripcionDefensa(String descripcionDefensa) { this.descripcionDefensa = descripcionDefensa; }
    public void setUrlEvidenciaDefensa(String urlEvidenciaDefensa) { this.urlEvidenciaDefensa = urlEvidenciaDefensa; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
