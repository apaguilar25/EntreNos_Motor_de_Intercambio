package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.Objects;
import java.util.UUID;

public class HabilidadOfrecida {

    private final String idInstancia; // El identificador único e inmutable
    private Habilidad habilidadBase;
    private int precioCreditos;
    private String descripcionServicio;

    // Constructor vacio requerido por librerias
    public HabilidadOfrecida() {
        this.idInstancia = UUID.randomUUID().toString();
    }

    public HabilidadOfrecida(Habilidad habilidadBase, int precioCreditos, String descripcionServicio) {
        if (habilidadBase == null) throw new IllegalArgumentException("La habilidad no puede ser nula.");

        this.idInstancia = UUID.randomUUID().toString(); // Nace con su ID único
        this.habilidadBase = habilidadBase;
        setPrecioCreditos(precioCreditos); // Aprovechamos la validación del setter
        setDescripcionServicio(descripcionServicio); // Aprovechamos la validación del setter
    }

    public String getIdInstancia() {
        return idInstancia;
    }

    public Habilidad getHabilidadBase() {
        return habilidadBase;
    }

    public int getPrecioCreditos() {
        return precioCreditos;
    }

    public void setPrecioCreditos(int precioCreditos) {
        if (precioCreditos <= 0) {
            throw new IllegalArgumentException("El valor en créditos debe ser un número entero positivo.");
        }
        this.precioCreditos = precioCreditos;
    }

    public String getDescripcionServicio() {
        return descripcionServicio;
    }

    public void setDescripcionServicio(String descripcionServicio) {
        if (descripcionServicio == null || descripcionServicio.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        this.descripcionServicio = descripcionServicio;
    }


}