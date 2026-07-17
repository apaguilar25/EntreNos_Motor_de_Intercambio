package es.ucab.entrenos.modulos.identidad.dtos;

public class EdicionOfertaDTO {
    private String idInstancia;
    private int precioCreditos;
    private String descripcionServicio;

    public String getIdInstancia() {
        return idInstancia;
    }

    public void setIdInstancia(String idInstancia) {
        this.idInstancia = idInstancia;
    }

    public int getPrecioCreditos() {
        return precioCreditos;
    }

    public void setPrecioCreditos(int precioCreditos) {
        this.precioCreditos = precioCreditos;
    }

    public String getDescripcionServicio() {
        return descripcionServicio;
    }

    public void setDescripcionServicio(String descripcionServicio) {
        this.descripcionServicio = descripcionServicio;
    }
}