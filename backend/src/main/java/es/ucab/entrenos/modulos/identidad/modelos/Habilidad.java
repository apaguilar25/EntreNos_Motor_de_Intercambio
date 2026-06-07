package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.Objects;

public class Habilidad {
    private String id;
    private String categoria;

    public Habilidad() {
    }

    public Habilidad(String id, String categoria) {
        this.id = id;
        setCategoria(categoria);
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
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría no puede estar vacío.");
        }
        this.categoria = categoria.trim();
    }

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
}