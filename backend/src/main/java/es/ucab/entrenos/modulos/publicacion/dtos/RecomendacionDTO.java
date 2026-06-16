package es.ucab.entrenos.modulos.publicacion.dto;

import es.ucab.entrenos.modulos.publicacion.dtos.PublicacionResponseDTO;

public class RecomendacionDTO {
    private PublicacionResponseDTO publicacion;
    private boolean estaEnMiCatalogo;
    private String tipoCoincidencia; // "HABILIDAD" (publicacion.ofrece = miNecesidad) o "NECESIDAD" (publicacion.solicita = miHabilidad)

    public RecomendacionDTO() {}

    public RecomendacionDTO(PublicacionResponseDTO publicacion, boolean estaEnMiCatalogo, String tipoCoincidencia) {
        this.publicacion = publicacion;
        this.estaEnMiCatalogo = estaEnMiCatalogo;
        this.tipoCoincidencia = tipoCoincidencia;
    }

    public PublicacionResponseDTO getPublicacion() { return publicacion; }
    public boolean isEstaEnMiCatalogo() { return estaEnMiCatalogo; }
    public String getTipoCoincidencia() { return tipoCoincidencia; }
}
