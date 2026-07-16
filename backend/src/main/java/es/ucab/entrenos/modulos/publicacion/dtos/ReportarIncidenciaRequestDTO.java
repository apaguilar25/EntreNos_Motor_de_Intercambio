package es.ucab.entrenos.modulos.publicacion.dtos;

import java.util.List;

public class ReportarIncidenciaRequestDTO {
    private String idUsuario;
    private String descripcion;
    private String urlEvidencia;
    private List<String> fotosEvidencia;

    public ReportarIncidenciaRequestDTO() {}

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getUrlEvidencia() { return urlEvidencia; }
    public void setUrlEvidencia(String urlEvidencia) { this.urlEvidencia = urlEvidencia; }

    public List<String> getFotosEvidencia() { return fotosEvidencia; }
    public void setFotosEvidencia(List<String> fotosEvidencia) { this.fotosEvidencia = fotosEvidencia; }
}
