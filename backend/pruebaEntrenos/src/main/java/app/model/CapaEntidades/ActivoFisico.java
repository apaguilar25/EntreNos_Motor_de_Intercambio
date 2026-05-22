package app.model.CapaEntidades;

import java.util.ArrayList;
import java.util.List;

public class ActivoFisico {
    private String nombreActivo;
    private EstadoActivoFisico estadoFisico;
    private List<Imagen> imagenes = new ArrayList<>(); // Composición 1..*

    public ActivoFisico() {}

    public String getNombreActivo() { return nombreActivo; }
    public void setNombreActivo(String nombreActivo) { this.nombreActivo = nombreActivo; }

    public EstadoActivoFisico getEstadoFisico() { return estadoFisico; }
    public void setEstadoFisico(EstadoActivoFisico estadoFisico) { this.estadoFisico = estadoFisico; }

    public List<Imagen> getImagenes() { return imagenes; }
    public void setImagenes(List<Imagen> imagenes) { this.imagenes = imagenes; }
}