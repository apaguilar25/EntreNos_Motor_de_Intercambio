package es.ucab.entrenos.modulos.subasta.dtos;

import es.ucab.entrenos.modulos.subasta.modelos.BienOfrecido;
import es.ucab.entrenos.modulos.subasta.modelos.EstadoFisico;
import java.util.List;

public class PropuestaDetalleDTO {

    // 1. Datos de Identidad (Esenciales para saber QUIÉN puja)
    private String idPropuesta;
    private String idPostor;
    private String nombrePostor;

    // 2. Datos de la Puja/Oferta (Lo que ofrecen a cambio)
    private List<BienOfrecido> bienesOfrecidos;
    private String descripcion;
    private EstadoFisico estadoFisico;
    private List<String> imagenesUrls;

    public PropuestaDetalleDTO(String idPropuesta, String idPostor, String nombrePostor,
                               List<BienOfrecido> bienesOfrecidos, String descripcion,
                               EstadoFisico estadoFisico, List<String> imagenesUrls) {
        this.idPropuesta = idPropuesta;
        this.idPostor = idPostor;
        this.nombrePostor = nombrePostor;
        this.bienesOfrecidos = bienesOfrecidos;
        this.descripcion = descripcion;
        this.estadoFisico = estadoFisico;
        this.imagenesUrls = imagenesUrls;
    }

    // Getters
    public String getIdPropuesta() {
        return idPropuesta;
    }

    public String getIdPostor() {
        return idPostor;
    }

    public String getNombrePostor() {
        return nombrePostor;
    }

    public List<BienOfrecido> getBienesOfrecidos() {
        return bienesOfrecidos;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public EstadoFisico getEstadoFisico() {
        return estadoFisico;
    }

    public List<String> getImagenesUrls() {
        return imagenesUrls;
    }

 
}