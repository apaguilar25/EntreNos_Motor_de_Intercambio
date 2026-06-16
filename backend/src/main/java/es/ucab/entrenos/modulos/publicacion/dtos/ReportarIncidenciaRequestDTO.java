package es.ucab.entrenos.modulos.publicacion.dtos;

public class ReportarIncidenciaRequestDTO {
    private String idUsuario;
    private String descripcion;
    private String urlEvidencia;

    public ReportarIncidenciaRequestDTO() {}

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUrlEvidencia() { return urlEvidencia; }
    public void setUrlEvidencia(String urlEvidencia) { this.urlEvidencia = urlEvidencia; }
}