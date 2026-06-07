package es.ucab.entrenos.modulos.identidad.dtos;

public class NuevaOfertaIndividualDTO {
    private String idHabilidadCategoria;
    private int precioCreditos;
    private String descripcionServicio;

    public String getIdHabilidadCategoria() { return idHabilidadCategoria; }
    public void setIdHabilidadCategoria(String idHabilidadCategoria) { this.idHabilidadCategoria = idHabilidadCategoria; }

    public int getPrecioCreditos() { return precioCreditos; }
    public void setPrecioCreditos(int precioCreditos) { this.precioCreditos = precioCreditos; }

    public String getDescripcionServicio() { return descripcionServicio; }
    public void setDescripcionServicio(String descripcionServicio) { this.descripcionServicio = descripcionServicio; }
}