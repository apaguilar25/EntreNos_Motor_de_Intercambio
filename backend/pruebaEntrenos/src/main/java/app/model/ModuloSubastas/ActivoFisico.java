package app.model.ModuloSubastas;

public class ActivoFisico {

    String nombre;
    EstadoActivoFisico estadoFisico;

    public ActivoFisico(String nombre, EstadoActivoFisico estadoFisico) {
        this.nombre = nombre;
        this.estadoFisico = estadoFisico;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public EstadoActivoFisico getEstadoFisico() {
        return estadoFisico;
    }

    public void setEstadoFisico(EstadoActivoFisico estadoFisico) {
        this.estadoFisico = estadoFisico;
    }
}
