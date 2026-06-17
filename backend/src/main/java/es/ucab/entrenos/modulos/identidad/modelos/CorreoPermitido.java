package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.UUID;

public class CorreoPermitido {
    private String id;
    private String correo;
    private long fechaCreacion;

    public CorreoPermitido() {}

    public CorreoPermitido(String correo) {
        this.id = UUID.randomUUID().toString();
        this.correo = correo;
        this.fechaCreacion = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
