package es.ucab.entrenos.modulos.gamificacion.dtos;

public class LogroDesbloqueadoResponseDTO {

    private String idLogro;
    private String nombre;
    private String descripcion;
    private int bonoCreditos;
    private String icono;
    private long fechaDesbloqueo;

    public LogroDesbloqueadoResponseDTO() {}

    public LogroDesbloqueadoResponseDTO(String idLogro, String nombre, String descripcion,
                                        int bonoCreditos, String icono, long fechaDesbloqueo) {
        this.idLogro = idLogro;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.bonoCreditos = bonoCreditos;
        this.icono = icono;
        this.fechaDesbloqueo = fechaDesbloqueo;
    }

    public String getIdLogro() { return idLogro; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public int getBonoCreditos() { return bonoCreditos; }
    public String getIcono() { return icono; }
    public long getFechaDesbloqueo() { return fechaDesbloqueo; }
}
