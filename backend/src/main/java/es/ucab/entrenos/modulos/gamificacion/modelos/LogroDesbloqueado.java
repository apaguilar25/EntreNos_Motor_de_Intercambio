package es.ucab.entrenos.modulos.gamificacion.modelos;

import java.util.UUID;

public class LogroDesbloqueado {

    private String id;
    private String idUsuario;
    private String idLogro;
    private long fechaDesbloqueo;

    public LogroDesbloqueado() {}

    public LogroDesbloqueado(String idUsuario, String idLogro) {
        this.id = UUID.randomUUID().toString();
        this.idUsuario = idUsuario;
        this.idLogro = idLogro;
        this.fechaDesbloqueo = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getIdLogro() { return idLogro; }
    public void setIdLogro(String idLogro) { this.idLogro = idLogro; }

    public long getFechaDesbloqueo() { return fechaDesbloqueo; }
    public void setFechaDesbloqueo(long fechaDesbloqueo) { this.fechaDesbloqueo = fechaDesbloqueo; }
}
