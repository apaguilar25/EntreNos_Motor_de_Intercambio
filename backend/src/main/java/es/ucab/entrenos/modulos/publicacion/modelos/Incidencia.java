package es.ucab.entrenos.modulos.publicacion.modelos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Incidencia {

    private String idIncidencia;
    private String idTransaccion;
    private String idUsuarioReportante;
    private String descripcion;
    private String urlEvidencia;
    private List<String> fotosEvidencia;
    private String idUsuarioDefensor;
    private String descripcionDefensa;
    private String urlEvidenciaDefensa;
    private List<String> fotosEvidenciaDefensa;
    private String estado;
    private long fechaCreacion;

    public Incidencia() {
        this.fotosEvidencia = new ArrayList<>();
        this.fotosEvidenciaDefensa = new ArrayList<>();
    }

    public Incidencia(String idTransaccion, String idUsuarioReportante, String descripcion, String urlEvidencia, List<String> fotosEvidencia) {
        this.idIncidencia = "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.idTransaccion = idTransaccion;
        this.idUsuarioReportante = idUsuarioReportante;
        this.descripcion = descripcion;
        this.urlEvidencia = urlEvidencia;
        this.fotosEvidencia = fotosEvidencia != null ? fotosEvidencia : new ArrayList<>();
        this.fotosEvidenciaDefensa = new ArrayList<>();
        this.estado = "ABIERTA";
        this.fechaCreacion = System.currentTimeMillis();
    }

    public String getIdIncidencia() { return idIncidencia; }
    public String getIdTransaccion() { return idTransaccion; }
    public String getIdUsuarioReportante() { return idUsuarioReportante; }
    public String getDescripcion() { return descripcion; }
    public String getUrlEvidencia() { return urlEvidencia; }
    public List<String> getFotosEvidencia() { return fotosEvidencia; }
    public String getIdUsuarioDefensor() { return idUsuarioDefensor; }
    public String getDescripcionDefensa() { return descripcionDefensa; }
    public String getUrlEvidenciaDefensa() { return urlEvidenciaDefensa; }
    public List<String> getFotosEvidenciaDefensa() { return fotosEvidenciaDefensa; }
    public String getEstado() { return estado; }
    public long getFechaCreacion() { return fechaCreacion; }

    public void setIdIncidencia(String idIncidencia) { this.idIncidencia = idIncidencia; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }
    public void setIdUsuarioReportante(String idUsuarioReportante) { this.idUsuarioReportante = idUsuarioReportante; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setUrlEvidencia(String urlEvidencia) { this.urlEvidencia = urlEvidencia; }
    public void setFotosEvidencia(List<String> fotosEvidencia) { this.fotosEvidencia = fotosEvidencia; }
    public void setIdUsuarioDefensor(String idUsuarioDefensor) { this.idUsuarioDefensor = idUsuarioDefensor; }
    public void setDescripcionDefensa(String descripcionDefensa) { this.descripcionDefensa = descripcionDefensa; }
    public void setUrlEvidenciaDefensa(String urlEvidenciaDefensa) { this.urlEvidenciaDefensa = urlEvidenciaDefensa; }
    public void setFotosEvidenciaDefensa(List<String> fotosEvidenciaDefensa) { this.fotosEvidenciaDefensa = fotosEvidenciaDefensa; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
