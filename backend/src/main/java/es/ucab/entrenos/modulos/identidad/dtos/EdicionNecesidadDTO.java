package es.ucab.entrenos.modulos.identidad.dtos;

public class EdicionNecesidadDTO {
    private String idInstancia;
    private String descripcionCondiciones;

    // --- GETTERS Y SETTERS OBLIGATORIOS ---

    public String getIdInstancia() {
        return idInstancia;
    }

    public void setIdInstancia(String idInstancia) {
        this.idInstancia = idInstancia;
    }

    public String getDescripcionCondiciones() {
        return descripcionCondiciones;
    }

    public void setDescripcionCondiciones(String descripcionCondiciones) {
        this.descripcionCondiciones = descripcionCondiciones;
    }
}