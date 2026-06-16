package es.ucab.entrenos.modulos.publicacion.dtos;

public class SolicitarCancelacionRequestDTO {
    private String idUsuario;
    private String idMotivoCancelacion;

    public SolicitarCancelacionRequestDTO() {}

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getIdMotivoCancelacion() { return idMotivoCancelacion; }
    public void setIdMotivoCancelacion(String idMotivoCancelacion) { this.idMotivoCancelacion = idMotivoCancelacion; }
}
