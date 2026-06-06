package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.Objects;

public class NecesidadRegistrada {
    private Habilidad necesidadBase;
    private String descripcionCondiciones;

    public NecesidadRegistrada() {}

    public NecesidadRegistrada(Habilidad necesidadBase, String descripcionCondiciones) {
        if (necesidadBase == null) throw new IllegalArgumentException("La necesidad no puede ser nula.");
        this.necesidadBase = necesidadBase;
        setDescripcionCondiciones(descripcionCondiciones);
    }

    public Habilidad getNecesidadBase() { return necesidadBase; }

    public String getDescripcionCondiciones() { return descripcionCondiciones; }

    // Requisito: Permite editar la descripción en cualquier momento
    public void setDescripcionCondiciones(String descripcionCondiciones) {
        if (descripcionCondiciones == null || descripcionCondiciones.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción de condiciones es obligatoria.");
        }
        this.descripcionCondiciones = descripcionCondiciones;
    }

    // Se permiten varias habilidades de la misma categoria mientras tengan descripciones distintas
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NecesidadRegistrada that = (NecesidadRegistrada) o;

        // Compara habilidad base y descripción (ignorando mayúsculas y espacios extra para evitar trampas)
        return Objects.equals(necesidadBase, that.necesidadBase) &&
                this.descripcionCondiciones.trim().equalsIgnoreCase(that.descripcionCondiciones.trim());
    }

    @Override
    public int hashCode() {
        return Objects.hash(necesidadBase, descripcionCondiciones.trim().toLowerCase());
    }
}