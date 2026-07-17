package es.ucab.entrenos.modulos.identidad.dtos;

public class HabilidadOfrecidaResumenDTO {
    private String idInstancia;
    private String categoria;
    private int precioCreditos;
    private String descripcionServicio;

    public HabilidadOfrecidaResumenDTO(String idInstancia, String categoria, int precioCreditos, String descripcionServicio) {
        this.idInstancia = idInstancia;
        this.categoria = categoria;
        this.precioCreditos = precioCreditos;
        this.descripcionServicio = descripcionServicio;
    }

    public String getIdInstancia() { return idInstancia; }
    public String getCategoria() { return categoria; }
    public int getPrecioCreditos() { return precioCreditos; }
    public String getDescripcionServicio() { return descripcionServicio; }
}