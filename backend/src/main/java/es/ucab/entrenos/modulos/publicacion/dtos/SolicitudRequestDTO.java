package es.ucab.entrenos.modulos.publicacion.dto;

public class SolicitudRequestDTO {
    private String idUsuario;
    private String nombreUsuario;
    private Integer precioOfertado;

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public Integer getPrecioOfertado() { return precioOfertado; }
    public void setPrecioOfertado(Integer precioOfertado) { this.precioOfertado = precioOfertado; }
}
