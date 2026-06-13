package es.ucab.entrenos.modulos.subasta.modelos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Subasta {

    private String id;
    private String idPropietario;
    private String nombreActivo;
    private String descripcion;
    private EstadoFisico estadoFisico;
    private List<String> imagenesUrls;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacionLicitacion;
    private EstadoSubasta estado;
    private String idPropuestaGanadora;
    private List<Propuesta> propuestas;

    private boolean propietarioConfirmoEntrega;
    private boolean ganadorConfirmoEntrega;

    private int version;

    public Subasta() {
        this.imagenesUrls = new ArrayList<>();
        this.propuestas = new ArrayList<>();
        this.fechaInicio = LocalDateTime.now();
        this.estado = EstadoSubasta.ACTIVA;
        this.version = 0;
        this.propietarioConfirmoEntrega = false;
        this.ganadorConfirmoEntrega = false;
    }

    public Subasta(String idPropietario, String nombreActivo, String descripcion,
                   EstadoFisico estadoFisico, List<String> imagenesUrls,
                   LocalDateTime fechaFinalizacionLicitacion) {

        if (idPropietario == null || idPropietario.trim().isEmpty()) throw new IllegalArgumentException("La subasta debe pertenecer a un propietario válido.");
        if (nombreActivo == null || nombreActivo.trim().isEmpty()) throw new IllegalArgumentException("El nombre del activo es obligatorio.");
        if (estadoFisico == null) throw new IllegalArgumentException("El estado físico debe ser Nuevo, Usado o Reparado.");
        if (imagenesUrls == null || imagenesUrls.isEmpty()) throw new IllegalArgumentException("Es obligatorio cargar al menos una imagen del activo.");
        if (fechaFinalizacionLicitacion == null || fechaFinalizacionLicitacion.isBefore(LocalDateTime.now())) throw new IllegalArgumentException("La fecha de finalización debe ser futura.");
        if (descripcion == null || descripcion.trim().isEmpty()) throw new IllegalArgumentException("La descripción del activo es obligatoria.");

        this.id = "SUB-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.idPropietario = idPropietario;
        this.nombreActivo = nombreActivo;
        this.estadoFisico = estadoFisico;
        this.descripcion = descripcion;
        this.imagenesUrls = new ArrayList<>(imagenesUrls);
        this.fechaInicio = LocalDateTime.now();
        this.fechaFinalizacionLicitacion = fechaFinalizacionLicitacion;
        this.estado = EstadoSubasta.ACTIVA;
        this.propuestas = new ArrayList<>();
        this.version = 0;
        this.propietarioConfirmoEntrega = false;
        this.ganadorConfirmoEntrega = false;
    }

    public void registrarPropuesta(Propuesta nuevaPropuesta) {
        if (nuevaPropuesta == null) throw new IllegalArgumentException("La propuesta no puede ser nula.");
        if (this.estado != EstadoSubasta.ACTIVA) throw new IllegalStateException("La subasta no está activa para recibir propuestas.");
        if (this.idPropietario.equals(nuevaPropuesta.getIdPostor())) throw new IllegalArgumentException("No puedes pujar en tu propia subasta.");

        this.propuestas.add(nuevaPropuesta);
    }

    public void adjudicarGanador(String idPropuesta) {
        // REGLA ESTRICTA (Competencia Justa): Solo cuando el tiempo expira
        if (this.estado != EstadoSubasta.ESPERANDO_DECISION) {
            throw new IllegalStateException("Debes esperar a que termine el tiempo de la subasta para elegir un ganador.");
        }

        boolean existe = propuestas.stream().anyMatch(p -> p.getIdPropuesta().equals(idPropuesta));
        if (!existe) throw new IllegalArgumentException("La propuesta no existe en esta subasta.");

        this.idPropuestaGanadora = idPropuesta;
        this.estado = EstadoSubasta.ADJUDICADA_INTERCAMBIO_PENDIENTE;
    }

    public void cancelarSubasta() {
        if (this.estado == EstadoSubasta.ADJUDICADA_INTERCAMBIO_PENDIENTE || this.estado == EstadoSubasta.FINALIZADA_CON_EXITO) {
            throw new IllegalStateException("No se puede cancelar una subasta que ya ha sido resuelta.");
        }

        // REGLA DE PROTECCIÓN AL POSTOR
        if (this.estado == EstadoSubasta.ACTIVA && !this.propuestas.isEmpty()) {
            throw new IllegalStateException("No puedes cancelar una subasta activa que ya tiene participantes. Debes esperar a que finalice el tiempo.");
        }

        this.estado = EstadoSubasta.CANCELADA;
    }

    public void confirmarIntercambio(String idUsuarioConfirmando) {
        if (this.estado != EstadoSubasta.ADJUDICADA_INTERCAMBIO_PENDIENTE) {
            throw new IllegalStateException("La subasta no está en fase de intercambio.");
        }

        if (idUsuarioConfirmando.equals(this.idPropietario)) {
            this.propietarioConfirmoEntrega = true;
        } else {
            Propuesta propuestaGanadora = this.propuestas.stream()
                    .filter(p -> p.getIdPropuesta().equals(this.idPropuestaGanadora))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Propuesta ganadora no encontrada."));

            if (idUsuarioConfirmando.equals(propuestaGanadora.getIdPostor())) {
                this.ganadorConfirmoEntrega = true;
            } else {
                throw new SecurityException("No tienes permisos para confirmar esta entrega.");
            }
        }

        if (this.propietarioConfirmoEntrega && this.ganadorConfirmoEntrega) {
            this.estado = EstadoSubasta.FINALIZADA_CON_EXITO;
        }
    }

    public void cerrarFaseLicitacion() {
        if (this.estado == EstadoSubasta.ACTIVA) {
            this.estado = EstadoSubasta.ESPERANDO_DECISION;
        }
    }

    public void cancelarSubastaDesierta() {
        if (this.estado == EstadoSubasta.ACTIVA) {
            this.estado = EstadoSubasta.CANCELADA;
        }
    }

    public void incrementarVersion() {
        this.version++;
    }

    // REGLA ERS: "permitirá editar mientras la subasta esté abierta"
    public void editarPropuesta(String idPropuesta, String idPostor,
                                List<BienOfrecido> nuevosBienes,
                                List<String> nuevasImagenes) {
        if (this.estado != EstadoSubasta.ACTIVA) {
            throw new IllegalStateException("El tiempo ha finalizado. Ya no puedes editar tu propuesta.");
        }

        Propuesta p = this.propuestas.stream()
                .filter(prop -> prop.getIdPropuesta().equals(idPropuesta))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La propuesta no existe."));

        if (!p.getIdPostor().equals(idPostor)) {
            throw new SecurityException("No puedes editar una propuesta que no te pertenece.");
        }

        p.setBienesOfrecidos(nuevosBienes);
        p.setImagenesUrls(nuevasImagenes);
    }

    // REGLA ERS: "podrá anular su propuesta siempre que el periodo esté activo"
    public void retirarPropuesta(String idPropuesta, String idPostor) {
        if (this.estado != EstadoSubasta.ACTIVA) {
            throw new IllegalStateException("La subasta ha cerrado. " +
                    "Tu oferta ya es vinculante y no puede ser retirada.");
        }

        boolean removida = this.propuestas.removeIf(p -> p.getIdPropuesta().equals(idPropuesta) && p.getIdPostor().equals(idPostor));
        if (!removida) {
            throw new IllegalArgumentException("Propuesta no encontrada o no tienes permisos.");
        }
    }


    // Getters y Setters...
    public String getId() { return id; }
    public String getIdPropietario() { return idPropietario; }
    public String getNombreActivo() { return nombreActivo; }
    public EstadoFisico getEstadoFisico() { return estadoFisico; }
    public String getDescripcion() { return descripcion; }
    public List<String> getImagenesUrls() { return new ArrayList<>(imagenesUrls); }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFinalizacionLicitacion() { return fechaFinalizacionLicitacion; }
    public EstadoSubasta getEstado() { return estado; }
    public String getIdPropuestaGanadora() { return idPropuestaGanadora; }
    public List<Propuesta> getPropuestas() { return new ArrayList<>(propuestas); }

    public boolean isPropietarioConfirmoEntrega() { return propietarioConfirmoEntrega; }
    public boolean isGanadorConfirmoEntrega() { return ganadorConfirmoEntrega; }

    public int getVersion() { return version; }
    public void setVersion(int version) { this.version = version; }
}