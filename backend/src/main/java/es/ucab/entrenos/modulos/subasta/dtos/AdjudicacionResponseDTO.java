package es.ucab.entrenos.modulos.subasta.dtos;

import es.ucab.entrenos.modulos.subasta.modelos.Subasta;

public class AdjudicacionResponseDTO {
    private String mensaje;
    private Subasta subasta;
    private ContactoUsuarioDTO contactoGanador;
    private ContactoUsuarioDTO contactoPropietario;

    public AdjudicacionResponseDTO(String mensaje, Subasta subasta, ContactoUsuarioDTO contactoGanador, ContactoUsuarioDTO contactoPropietario) {
        this.mensaje = mensaje;
        this.subasta = subasta;
        this.contactoGanador = contactoGanador;
        this.contactoPropietario = contactoPropietario;
    }

    // Getters
    public String getMensaje() { return mensaje; }
    public Subasta getSubasta() { return subasta; }
    public ContactoUsuarioDTO getContactoGanador() { return contactoGanador; }
    public ContactoUsuarioDTO getContactoPropietario() { return contactoPropietario; }
}