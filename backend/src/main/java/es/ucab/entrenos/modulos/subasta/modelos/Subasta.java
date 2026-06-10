package es.ucab.entrenos.modulos.subasta.modelos;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Subasta {

    private String id;
    private Usuario propietario; // El miembro de la comunidad que subasta
    private String nombreActivo;
    private String descripcion;
    private EstadoFisico estadoFisico;
    private List<String> imagenesUrls; // Lista de rutas/URLs de las fotos
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacionLicitacion; // Cuándo cierra la recepción de Propuestas
    private EstadoSubasta estado;
    private String idPropuestaGanadora; // Guardará el ID de la Propuesta elegida (se usará en la HU5)
    private List<Propuesta> propuestas;

    public Subasta() {
        this.imagenesUrls = new ArrayList<>();
        this.propuestas = new ArrayList<>();
        this.fechaInicio = LocalDateTime.now();
        this.estado = EstadoSubasta.ACTIVA;
    }

    public Subasta(String id, Usuario propietario, String nombreActivo,
                   String descripcion, EstadoFisico estadoFisico,
                   List<String> imagenesUrls,
                   LocalDateTime fechaFinalizacionLicitacion) {

        // REGLA DE NEGOCIO: Validaciones obligatorias del ERS para poder publicar
        if (propietario == null) {
            throw new IllegalArgumentException("Toda subasta debe pertenecer a un propietario.");
        }
        if (nombreActivo == null || nombreActivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del activo es obligatorio.");
        }
        if (estadoFisico == null) {
            throw new IllegalArgumentException("El estado físico debe ser Nuevo, Usado o Reparado.");
        }
        if (imagenesUrls == null || imagenesUrls.isEmpty()) {
            throw new IllegalArgumentException("Es obligatorio cargar al menos una imagen del activo.");
        }
        if (fechaFinalizacionLicitacion == null || fechaFinalizacionLicitacion.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha de finalización debe ser una fecha futura.");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción del activo es obligatoria.");
        }

        // Si el usuario está sancionado, no puede crear subastas
        if (propietario.tieneSancionActiva()) {
            throw new IllegalStateException("El usuario tiene una sanción activa y no puede crear nuevas subastas.");
        }

        this.id = id;
        this.propietario = propietario;
        this.nombreActivo = nombreActivo;
        this.estadoFisico = estadoFisico;
        this.descripcion = descripcion;
        this.imagenesUrls = new ArrayList<>(imagenesUrls);
        this.fechaInicio = LocalDateTime.now();
        this.fechaFinalizacionLicitacion = fechaFinalizacionLicitacion;
        this.estado = EstadoSubasta.ACTIVA;
    }



    // ❌ REGLA DE NEGOCIO: Adjudicación única e irreversible
    public void adjudicarGanador(String idPropuesta) {
        if (this.estado != EstadoSubasta.ESPERANDO_DECISION && this.estado != EstadoSubasta.ACTIVA) {
            throw new IllegalStateException("Solo se pueden adjudicar subastas que estén cerradas o activas.");
        }
        if (idPropuesta == null || idPropuesta.trim().isEmpty()) {
            throw new IllegalArgumentException("Debe especificar una Propuesta ganadora válida.");
        }
        this.idPropuestaGanadora = idPropuesta;
        this.estado = EstadoSubasta.ADJUDICADA;
    }

    public void registrarPropuesta(Propuesta nuevaPropuesta) {
        if (nuevaPropuesta == null) {
            throw new IllegalArgumentException("La propuesta no puede ser nula.");
        }
    }

    // ❌ REGLA DE NEGOCIO: Cancelación manual por el subastador
    public void cancelarSubasta() {
        if (this.estado == EstadoSubasta.ADJUDICADA || this.estado == EstadoSubasta.CERRADA_POR_INACTIVIDAD) {
            throw new IllegalStateException("No se puede cancelar una subasta que ya ha sido resuelta.");
        }
        this.estado = EstadoSubasta.CANCELADA;
    }

    // Verifica si ya pasaron los 5 días reglamentarios desde que cerró el periodo de subasta
    public boolean haExpiradoPlazoDeResolucion() {
        return this.estado == EstadoSubasta.ESPERANDO_DECISION &&
                LocalDateTime.now().isAfter(this.fechaFinalizacionLicitacion.plusDays(5));
    }

    // Getters y Setters estándar...
    public String getId() { return id; }
    public Usuario getPropietario() { return propietario; }
    public String getNombreActivo() { return nombreActivo; }
    public EstadoFisico getEstadoFisico() { return estadoFisico; }
    public String getDescripcion() { return descripcion; }
    public List<String> getImagenesUrls() { return imagenesUrls; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFinalizacionLicitacion() { return fechaFinalizacionLicitacion; }
    public EstadoSubasta getEstado() { return estado; }
    public void setEstado(EstadoSubasta estado) { this.estado = estado; }
    public String getIdPropuestaGanadora() { return idPropuestaGanadora; }
}