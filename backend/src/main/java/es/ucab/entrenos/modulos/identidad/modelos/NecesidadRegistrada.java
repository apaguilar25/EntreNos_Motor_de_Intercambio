package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.Objects;
import java.util.UUID;

public class NecesidadRegistrada {
    private final String idInstancia; // El identificador único e inmutable
    private Habilidad necesidadBase;
    private String descripcionCondiciones;

    // Constructor vacio requerido por librerias
    public NecesidadRegistrada() {
        this.idInstancia = UUID.randomUUID().toString();
    }

    public NecesidadRegistrada(Habilidad necesidadBase, String descripcionCondiciones) {
        if (necesidadBase == null) throw new IllegalArgumentException("La necesidad no puede ser nula.");

        this.idInstancia = UUID.randomUUID().toString(); // Nace con su ID único
        this.necesidadBase = necesidadBase;
        setDescripcionCondiciones(descripcionCondiciones);
    }

    public Habilidad getNecesidadBase() { return necesidadBase; }

    public String getDescripcionCondiciones() { return descripcionCondiciones; }

    public String getIdInstancia() {
        return idInstancia;
    }

    // Requisito: Permite editar la descripción en cualquier momento
    public void setDescripcionCondiciones(String descripcionCondiciones) {
        if (descripcionCondiciones == null || descripcionCondiciones.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción de condiciones es obligatoria.");
        }
        this.descripcionCondiciones = descripcionCondiciones;
    }

}