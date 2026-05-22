package app.model.CapaEntidades;

public class Habilidad {

    private String nombre;
    private int precioCreditos;
    private String descripcionHabilidad;

    public Habilidad() {
    }

    public Habilidad(String nombre, int precioCreditos, String descripcionHabilidad) {
        this.nombre = nombre;
        this.precioCreditos = precioCreditos;
        this.descripcionHabilidad = descripcionHabilidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getPrecioCreditos() {
        return precioCreditos;
    }

    public void setPrecioCreditos(int precioCreditos) {
        this.precioCreditos = precioCreditos;
    }

    public String getDescripcionHabilidad() {
        return descripcionHabilidad;
    }

    public void setDescripcionHabilidad(String descripcionHabilidad) {
        this.descripcionHabilidad = descripcionHabilidad;
    }
}
