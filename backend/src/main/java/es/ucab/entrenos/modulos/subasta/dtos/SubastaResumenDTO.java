package es.ucab.entrenos.modulos.subasta.dtos;

import java.time.LocalDateTime;

public class SubastaResumenDTO {

    private String id;
    private String nombreActivo;
    private String estado;
    private LocalDateTime fechaInicio; // o fechaCreacion
    private LocalDateTime fechaCierre; // o fechaFinalizacionLicitacion

    public SubastaResumenDTO() {}

    public SubastaResumenDTO(String id, String nombreActivo, String estado, LocalDateTime fechaInicio, LocalDateTime fechaCierre) {
        this.id = id;
        this.nombreActivo = nombreActivo;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaCierre = fechaCierre;
    }

    // Getters
    public String getId() { return id; }
    public String getNombreActivo() { return nombreActivo; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombreActivo(String nombreActivo) { this.nombreActivo = nombreActivo; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
}