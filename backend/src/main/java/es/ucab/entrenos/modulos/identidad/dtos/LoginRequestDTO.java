package es.ucab.entrenos.modulos.identidad.dtos;

public class LoginRequestDTO {
    private String correoElectronico;
    private String contrasena;

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}