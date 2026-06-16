package es.ucab.entrenos.modulos.reputacion.modelos;

public class Resena {
    private String idResena;
    private String idTransaccion;
    private String idEmisor;
    private String idReceptor;
    private int calificacion;
    private long fechaCreacion;
    private int version;

    public Resena() {}

    public Resena(String idResena, String idTransaccion, String idEmisor, String idReceptor,
                  int calificacion) {
        this.idResena = idResena;
        this.idTransaccion = idTransaccion;
        this.idEmisor = idEmisor;
        this.idReceptor = idReceptor;
        setCalificacion(calificacion);
        this.fechaCreacion = System.currentTimeMillis();
    }

    public String getIdResena() { return idResena; }
    public String getIdTransaccion() { return idTransaccion; }
    public String getIdEmisor() { return idEmisor; }
    public String getIdReceptor() { return idReceptor; }
    public int getCalificacion() { return calificacion; }
    public long getFechaCreacion() { return fechaCreacion; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }

    public void setIdResena(String idResena) { this.idResena = idResena; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }
    public void setIdEmisor(String idEmisor) { this.idEmisor = idEmisor; }
    public void setIdReceptor(String idReceptor) { this.idReceptor = idReceptor; }
    public void setCalificacion(int calificacion) {
        if (calificacion < 1 || calificacion > 5)
            throw new IllegalArgumentException("La calificacion debe ser un entero entre 1 y 5.");
        this.calificacion = calificacion;
    }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}

