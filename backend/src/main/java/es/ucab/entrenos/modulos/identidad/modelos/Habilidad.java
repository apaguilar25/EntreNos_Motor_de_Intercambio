package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.Objects;

public class Habilidad {

    // Precio y Descripcion van en Publicacion
    private String id;
    private String categoria;

    // Constructor vacio
    public Habilidad() {
    }

    public Habilidad(String id, String categoria) {
        this.id = id;
        this.categoria = categoria;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    /**
     * Se sobreescribe el met.odo equals para que Java sepa que dos habilidades
     * son la misma si tienen el mismo ID, evitando así duplicados en el perfil del Usuario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Habilidad habilidad = (Habilidad) o;
        return Objects.equals(id, habilidad.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return categoria + " (" + id + ")";
    }

}
