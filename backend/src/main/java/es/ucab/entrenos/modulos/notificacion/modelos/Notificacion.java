package es.ucab.entrenos.modulos.notificacion.modelos;

import java.util.UUID;

public class Notificacion {
    private String idNotificacion;
    private String idRemitente;
    private String idDestinatario;
    private String mensaje;
    private TipoNotificacion tipo;
    private boolean estadoLectura;
    private long fechaCreacion;

    public Notificacion() {}

    public Notificacion(String idRemitente, String idDestinatario, String mensaje, TipoNotificacion tipo) {
        this.idNotificacion = UUID.randomUUID().toString();
        this.idRemitente = idRemitente;
        this.idDestinatario = idDestinatario;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.estadoLectura = false;
        this.fechaCreacion = System.currentTimeMillis();
    }

    public String getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(String idNotificacion) { this.idNotificacion = idNotificacion; }

    public String getIdRemitente() { return idRemitente; }
    public void setIdRemitente(String idRemitente) { this.idRemitente = idRemitente; }

    public String getIdDestinatario() { return idDestinatario; }
    public void setIdDestinatario(String idDestinatario) { this.idDestinatario = idDestinatario; }

    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }

    public TipoNotificacion getTipo() { return tipo; }
    public void setTipo(TipoNotificacion tipo) { this.tipo = tipo; }

    public boolean isEstadoLectura() { return estadoLectura; }
    public void setEstadoLectura(boolean estadoLectura) { this.estadoLectura = estadoLectura; }

    public long getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
