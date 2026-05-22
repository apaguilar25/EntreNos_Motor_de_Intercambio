package app.model.CapaEntidades;

import java.util.UUID;

public class Notificacion {

    private String idNotificacion;
    private String idUsuarioDestinatario;
    private String mensaje;
    private TipoNotificacion tipo;
    private boolean leida;

    public Notificacion(TipoNotificacion tipo, String mensaje, String idUsuarioDestinatario) {
        this.idNotificacion = UUID.randomUUID().toString(); // TODO REVISAR Genera un ID único automático
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.idUsuarioDestinatario = idUsuarioDestinatario;
        this.leida = false;
    }

    public void marcarNotificacionLeida(){
        this.leida = true;
    }

    public String getIdNotificacion() {
        return idNotificacion;
    }

    public String getIdUsuarioDestinatario() {
        return idUsuarioDestinatario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setIdNotificacion(String idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public void setIdUsuarioDestinatario(String idUsuarioDestinatario) {
        this.idUsuarioDestinatario = idUsuarioDestinatario;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public void setTipo(TipoNotificacion tipo) {
        this.tipo = tipo;
    }
}

