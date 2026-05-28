package modelOldDated.CapaEntidades;

import java.util.UUID;

public class Notificacion {
    private String idNotificacion;
    private String idRemitente;
    private String idDestinatario;
    private String mensaje;
    private TipoNotificacion tipo;
    private boolean estadoLectura; // false = No leída, true = Leída

    // Constructor vacío necesario para Jackson (Lectura de JSON)
    public Notificacion() {}

    public Notificacion(String idRemitente, String idDestinatario, String mensaje, TipoNotificacion tipo) {
        this.idNotificacion = UUID.randomUUID().toString(); // Genera un ID único automático
        this.idRemitente = idRemitente;
        this.idDestinatario = idDestinatario;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.estadoLectura = false; // Por defecto nace sin leer
    }



    // Getters y Setters
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
}