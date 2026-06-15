package es.ucab.entrenos.modulos.publicacion.dtos;

public class ResponderCancelacionRequestDTO {
    private String idUsuario;
    private boolean aceptar;

    public ResponderCancelacionRequestDTO() {}

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public boolean isAceptar() { return aceptar; }
    public void setAceptar(boolean aceptar) { this.aceptar = aceptar; }
}
