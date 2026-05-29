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
        setPrecioCreditos(precioCreditos); // Usamos el setter para aprovechar la validación
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

    // Sobreescribimos equals para que el .contains() de Usuario detecte duplicados
    // basándose en la habilidad base (Ej: no puedes ofrecer "Plomería" dos veces)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HabilidadOfrecida that = (HabilidadOfrecida) o;
        return Objects.equals(habilidadBase, that.habilidadBase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(habilidadBase);
    }
}