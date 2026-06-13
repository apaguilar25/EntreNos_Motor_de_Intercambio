package es.ucab.entrenos.modulos.identidad.dtos;

public class LoginResponseDTO {
    private String token;
    private String rol;

    public LoginResponseDTO(String token, String rol) {
        this.token = token;
        this.rol = rol;
    }

    public String getToken() { return token; }
    public String getRol() { return rol; }
}