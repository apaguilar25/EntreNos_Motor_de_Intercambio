package es.ucab.entrenos.modulos.subasta.dtos;

public class ContactoUsuarioDTO {
    private String nombre;
    private String correoElectronico;
    private String telefono;

    public ContactoUsuarioDTO(String nombre, String correoElectronico, String telefono) {
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
    }

    // Getters
    public String getNombre() { return nombre; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getTelefono() { return telefono; }
}