package org.example.CapaEntidades;

public class Necesidad {

    private String nombre;
    private String descripcionNecesidad;

    public Necesidad(String nombre, String descripcionNecesidad) {
        this.nombre = nombre;
        this.descripcionNecesidad = descripcionNecesidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcionNecesidad() {
        return descripcionNecesidad;
    }

    public void setDescripcionNecesidad(String descripcionNecesidad) {
        this.descripcionNecesidad = descripcionNecesidad;
    }
}
