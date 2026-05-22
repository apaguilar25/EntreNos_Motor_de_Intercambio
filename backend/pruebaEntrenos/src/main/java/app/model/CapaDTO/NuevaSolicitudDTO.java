package app.model.CapaDTO;

public class NuevaSolicitudDTO {
    private String idOfertante;
    private String idDemandante;
    private String nombreServicio;
    private int precioCreditos;
    private String descripcionServicio;

    // Getters y Setters
    public String getIdOfertante() { return idOfertante; }
    public void setIdOfertante(String idOfertante) { this.idOfertante = idOfertante; }
    public String getIdDemandante() { return idDemandante; }
    public void setIdDemandante(String idDemandante) { this.idDemandante = idDemandante; }
    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }
    public int getPrecioCreditos() { return precioCreditos; }
    public void setPrecioCreditos(int precioCreditos) { this.precioCreditos = precioCreditos; }
    public String getDescripcionServicio() { return descripcionServicio; }
    public void setDescripcionServicio(String descripcionServicio) { this.descripcionServicio = descripcionServicio; }
}