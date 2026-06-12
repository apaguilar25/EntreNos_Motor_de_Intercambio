package modelOldDated.CapaEntidades;

import java.util.UUID;

public class Transaccion {
    private String idTransaccion;
    private String idOfertante;
    private String idDemandante;
    private EstadoTransaccion estado;
    private int creditosRetenidos;
    private boolean confirmacionOfertante;
    private boolean confirmacionDemandante;

    // Campos extraídos de tu constructor en el diagrama UML
    private String nombreServicio;
    private String descripcion;

    // Constructor vacío obligatorio para Jackson
    public Transaccion() {
    }

    // Constructor basado en tu UML
    public Transaccion(String idOfertante, String idDemandante, String nombreServicio, String descripcion, int creditosRetenidos) {
        this.idTransaccion = UUID.randomUUID().toString();
        this.idOfertante = idOfertante;
        this.idDemandante = idDemandante;
        this.nombreServicio = nombreServicio;
        this.descripcion = descripcion;
        this.creditosRetenidos = creditosRetenidos;
        this.estado = EstadoTransaccion.INICIADA;
        this.confirmacionOfertante = false;
        this.confirmacionDemandante = false;
    }

    // Getters y Setters
    public String getIdTransaccion() { return idTransaccion; }
    public void setIdTransaccion(String idTransaccion) { this.idTransaccion = idTransaccion; }
    public String getIdOfertante() { return idOfertante; }
    public void setIdOfertante(String idOfertante) { this.idOfertante = idOfertante; }
    public String getIdDemandante() { return idDemandante; }
    public void setIdDemandante(String idDemandante) { this.idDemandante = idDemandante; }
    public EstadoTransaccion getEstado() { return estado; }
    public void setEstado(EstadoTransaccion estado) { this.estado = estado; }
    public int getCreditosRetenidos() { return creditosRetenidos; }
    public void setCreditosRetenidos(int creditosRetenidos) { this.creditosRetenidos = creditosRetenidos; }
    public boolean isConfirmacionOfertante() { return confirmacionOfertante; }
    public void setConfirmacionOfertante(boolean confirmacionOfertante) { this.confirmacionOfertante = confirmacionOfertante; }
    public boolean isConfirmacionDemandante() { return confirmacionDemandante; }
    public void setConfirmacionDemandante(boolean confirmacionDemandante) { this.confirmacionDemandante = confirmacionDemandante; }
    public String getNombreServicio() { return nombreServicio; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}