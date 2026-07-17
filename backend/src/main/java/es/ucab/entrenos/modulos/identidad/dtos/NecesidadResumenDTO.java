package es.ucab.entrenos.modulos.identidad.dtos;

public class NecesidadResumenDTO {
    private String idInstancia;
    private String categoria;
    private String descripcionCondiciones;

    public NecesidadResumenDTO(String idInstancia, String categoria, String descripcionCondiciones) {
        this.idInstancia = idInstancia;
        this.categoria = categoria;
        this.descripcionCondiciones = descripcionCondiciones;
    }

    public String getIdInstancia() { return idInstancia; }
    public String getCategoria() { return categoria; }
    public String getDescripcionCondiciones() { return descripcionCondiciones; }
}