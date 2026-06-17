package es.ucab.entrenos.modulos.publicacion.dtos;

public class SolicitarCancelacionRequestDTO {
    private String idUsuario;
    private String motivo;

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
