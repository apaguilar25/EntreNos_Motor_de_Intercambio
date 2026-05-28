package modelOldDated.CapaEntidades;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Subasta {
    private String idSubasta;
    private String idSubastador;
    private String descripcion;
    private Date fechaCreacion;
    private Date fechaFinalizacion;
    private EstadoSubasta estado;

    private ActivoFisico activoFisico; // Relación 1
    private List<Oferta> ofertas = new ArrayList<>(); // Relación *

    public Subasta() {}

    public Subasta(String idSubastador, String descripcion, ActivoFisico activoFisico) {
        this.idSubastador = idSubastador;
        this.descripcion = descripcion;
        this.activoFisico = activoFisico;
    }

    // Métodos del UML
    public Boolean verificarCierreSubasta(Date fecha) {
        if (fechaFinalizacion == null) return false;
        return fecha.after(fechaFinalizacion);
    }

    public Boolean verificarExpiracionResolucion(Date fecha) {
        // Lógica de dominio: por ejemplo, 48h después de finalizar
        return false;
    }

    // Getters y Setters
    public String getIdSubasta() { return idSubasta; }
    public void setIdSubasta(String idSubasta) { this.idSubasta = idSubasta; }

    public String getIdSubastador() { return idSubastador; }
    public void setIdSubastador(String idSubastador) { this.idSubastador = idSubastador; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Date getFechaFinalizacion() { return fechaFinalizacion; }
    public void setFechaFinalizacion(Date fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; }

    public EstadoSubasta getEstado() { return estado; }
    public void setEstado(EstadoSubasta estado) { this.estado = estado; }

    public ActivoFisico getActivoFisico() { return activoFisico; }
    public void setActivoFisico(ActivoFisico activoFisico) { this.activoFisico = activoFisico; }

    public List<Oferta> getOfertas() { return ofertas; }
    public void setOfertas(List<Oferta> ofertas) { this.ofertas = ofertas; }
}