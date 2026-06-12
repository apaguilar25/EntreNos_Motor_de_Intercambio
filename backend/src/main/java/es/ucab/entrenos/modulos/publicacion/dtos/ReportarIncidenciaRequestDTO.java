package es.ucab.entrenos.modulos.publicacion.dtos;

public class ReportarIncidenciaRequestDTO {
    private String descripcion;
    private String urlEvidencia; // URL de la foto cargada

    public ReportarIncidenciaRequestDTO() {}

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUrlEvidencia() { return urlEvidencia; }
    public void setUrlEvidencia(String urlEvidencia) { this.urlEvidencia = urlEvidencia; }
}