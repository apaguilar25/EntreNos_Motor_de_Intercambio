package es.ucab.entrenos.modulos.publicacion.dto;

import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;

public class RecomendacionDTO {
    private Publicacion publicacion;
    private boolean estaEnMiCatalogo;
    private String tipoCoincidencia; // "HABILIDAD" (publicacion.ofrece = miNecesidad) o "NECESIDAD" (publicacion.solicita = miHabilidad)

    public RecomendacionDTO() {}

    public RecomendacionDTO(Publicacion publicacion, boolean estaEnMiCatalogo, String tipoCoincidencia) {
        this.publicacion = publicacion;
        this.estaEnMiCatalogo = estaEnMiCatalogo;
        this.tipoCoincidencia = tipoCoincidencia;
    }

    public Publicacion getPublicacion() { return publicacion; }
    public boolean isEstaEnMiCatalogo() { return estaEnMiCatalogo; }
    public String getTipoCoincidencia() { return tipoCoincidencia; }
}
