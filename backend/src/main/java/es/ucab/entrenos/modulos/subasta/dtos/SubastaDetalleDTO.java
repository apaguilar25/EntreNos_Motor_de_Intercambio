package es.ucab.entrenos.modulos.subasta.dtos;

import java.time.LocalDateTime;
import java.util.List;

public class SubastaDetalleDTO {
    private String id;
    private String nombreActivo;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaCierre;
    private List<PropuestaDetalleDTO> propuestas;
    private int cantidadParticipantes;

    public SubastaDetalleDTO(String id,
                             String nombreActivo,
                             String descripcion,
                             String estado,
                             LocalDateTime fechaCierre,
                             List<PropuestaDetalleDTO> propuestas) {
        this.id = id;
        this.nombreActivo = nombreActivo;
        this.descripcion = descripcion;
        this.estado = estado;
        this.fechaCierre = fechaCierre;
        this.propuestas = propuestas;
        this.cantidadParticipantes = propuestas.size();
    }

    // Getters
    public String getId() { return id; }
    public String getNombreActivo() { return nombreActivo; }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public List<PropuestaDetalleDTO> getPropuestas() { return propuestas; }
    public int getCantidadParticipantes() { return cantidadParticipantes; }
}