package es.ucab.entrenos.modulos.gamificacion.dtos;

import es.ucab.entrenos.modulos.gamificacion.modelos.Logro;

public class LogroEstadoDTO {

    private String idLogro;
    private String nombre;
    private String descripcion;
    private int bonoCreditos;
    private boolean desbloqueado;
    private Long fechaDesbloqueo;

    public LogroEstadoDTO() {}

    public LogroEstadoDTO(Logro logro, boolean desbloqueado, Long fechaDesbloqueo) {
        this.idLogro = logro.getIdLogro();
        this.nombre = logro.getNombre();
        this.descripcion = logro.getDescripcion();
        this.bonoCreditos = logro.getBonoCreditos();
        this.desbloqueado = desbloqueado;
        this.fechaDesbloqueo = fechaDesbloqueo;
    }

    public String getIdLogro() { return idLogro; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getBonoCreditos() { return bonoCreditos; }
    public boolean isDesbloqueado() { return desbloqueado; }
    public Long getFechaDesbloqueo() { return fechaDesbloqueo; }
}
