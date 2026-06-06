package es.ucab.entrenos.modulos.identidad.dtos;

public class RegistroUsuarioRequestDTO {
    private String nombre;
    private String correoElectronico;
    private String telefono;
    private String descripcionPersonal;
    private String contrasena; // La contraseña plana que escribió el usuario en el front

    // Constructores, Getters y Setters
    public RegistroUsuarioRequestDTO() {}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDescripcionPersonal() { return descripcionPersonal; }
    public void setDescripcionPersonal(String descripcionPersonal) { this.descripcionPersonal = descripcionPersonal; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }
}