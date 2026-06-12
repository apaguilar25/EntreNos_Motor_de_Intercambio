package es.ucab.entrenos.modulos.publicacion.dto;

public class RespuestaSolicitudDTO {
    private String idUsuario;
    private boolean aceptar;

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public boolean isAceptar() { return aceptar; }
    public void setAceptar(boolean aceptar) { this.aceptar = aceptar; }
}
