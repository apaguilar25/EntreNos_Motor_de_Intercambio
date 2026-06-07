package es.ucab.entrenos.modulos.reputacion.modelos;

public class Resena {
    private String idResena;
    private String idTransaccion;
    private String idEmisor;
    private String idReceptor;
    private int calificacion;
    private String comentario;
    private long fechaCreacion;

    public Resena() {}

    public Resena(String idResena, String idTransaccion, String idEmisor, String idReceptor, int calificacion, String comentario) {
        this.idResena = idResena;
        this.idTransaccion = idTransaccion;
        this.idEmisor = idEmisor;
        this.idReceptor = idReceptor;
        setCalificacion(calificacion);
        this.comentario = comentario;
        this.fechaCreacion = System.currentTimeMillis();
    }

    public String getIdResena() { return idResena; }
    public String getIdTransaccion() { return idTransaccion; }
    public String getIdEmisor() { return idEmisor; }
    public String getIdReceptor() { return idReceptor; }
    public int getCalificacion() { return calificacion; }
    public String getComentario() { return comentario; }
    public long getFechaCreacion() { return fechaCreacion; }

    public void setIdResena(String idResena) { this.idResena = idResena; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }
    public void setIdEmisor(String idEmisor) { this.idEmisor = idEmisor; }
    public void setIdReceptor(String idReceptor) { this.idReceptor = idReceptor; }
    public void setCalificacion(int calificacion) {
        if (calificacion < 1 || calificacion > 5)
            throw new IllegalArgumentException("La calificacion debe ser un entero entre 1 y 5.");
        this.calificacion = calificacion;
    }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
