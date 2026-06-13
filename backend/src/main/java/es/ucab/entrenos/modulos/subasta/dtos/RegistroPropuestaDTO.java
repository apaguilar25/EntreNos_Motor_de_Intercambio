package es.ucab.entrenos.modulos.subasta.dtos;

import es.ucab.entrenos.modulos.subasta.modelos.BienOfrecido;
import es.ucab.entrenos.modulos.subasta.modelos.EstadoFisico;
import java.util.List;

public class RegistroPropuestaDTO {
    private List<BienOfrecido> bienesOfrecidos;
    private String descripcion;
    private EstadoFisico estadoFisico;
    private List<String> imagenesUrls;

    // Getters y Setters
    public List<BienOfrecido> getBienesOfrecidos() { return bienesOfrecidos; }
    public void setBienesOfrecidos(List<BienOfrecido> bienesOfrecidos) { this.bienesOfrecidos = bienesOfrecidos; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public EstadoFisico getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(EstadoFisico estadoFisico) { this.estadoFisico = estadoFisico; }

    public List<String> getImagenesUrls() { return imagenesUrls; }
    public void setImagenesUrls(List<String> imagenesUrls) { this.imagenesUrls = imagenesUrls; }
}