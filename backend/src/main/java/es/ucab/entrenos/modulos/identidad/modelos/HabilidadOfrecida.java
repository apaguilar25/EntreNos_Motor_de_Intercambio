package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.Objects;

public class HabilidadOfrecida {
    private Habilidad habilidadBase;
    private int precioCreditos;
    private String descripcionServicio;

    public HabilidadOfrecida() {}

    public HabilidadOfrecida(Habilidad habilidadBase, int precioCreditos, String descripcionServicio) {
        if (habilidadBase == null) throw new IllegalArgumentException("La habilidad no puede ser nula.");
        this.habilidadBase = habilidadBase;
        setPrecioCreditos(precioCreditos); // Se usa el setter para aprovechar la validación (precio >= 0)
        setDescripcionServicio(descripcionServicio);
    }

    public Habilidad getHabilidadBase() { return habilidadBase; }

    public int getPrecioCreditos() { return precioCreditos; }

    // Requisito: Permite editar el valor en cualquier momento y valida que sea positivo
    public void setPrecioCreditos(int precioCreditos) {
        if (precioCreditos <= 0) {
            throw new IllegalArgumentException("El valor en créditos debe ser un número entero positivo.");
        }
        this.precioCreditos = precioCreditos;
    }

    public String getDescripcionServicio() { return descripcionServicio; }

    // Requisito: Permite editar la descripción en cualquier momento
    public void setDescripcionServicio(String descripcionServicio) {
        if (descripcionServicio == null || descripcionServicio.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        this.descripcionServicio = descripcionServicio;
    }

    // Se permite varias habilidades de la misma categoria mientras tengan descripciones distintas
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabilidadOfrecida that = (HabilidadOfrecida) o;

        // Compara habilidad base y descripción (ignorando mayúsculas y espacios extra para evitar trampas)
        return Objects.equals(habilidadBase, that.habilidadBase) &&
                this.descripcionServicio.trim().equalsIgnoreCase(that.descripcionServicio.trim());
    }

    @Override
    public int hashCode() {
        return Objects.hash(habilidadBase, descripcionServicio.trim().toLowerCase());
    }
}