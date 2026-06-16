package es.ucab.entrenos.modulos.publicacion.dto;

public class CalificarRequestDTO {
    private String idUsuario;
    private int calificacion;

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public int getCalificacion() { return calificacion; }
    public void setCalificacion(int calificacion) { this.calificacion = calificacion; }
}
