package es.ucab.entrenos.modulos.subasta.modelos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Propuesta {

    private String idPropuesta;
    private String idSubasta;

    // Bajo acoplamiento. Solo guardamos el ID, no el objeto Usuario completo.
    private String idPostor;

    // HU5: Reemplazamos el activo único por una lista para permitir múltiples bienes con cantidades
    private List<BienOfrecido> bienesOfrecidos;

    private String descripcion;
    private EstadoPropuesta estadoPropuesta;
    private EstadoFisico estadoFisico;
    private List<String> imagenesUrls;

    private LocalDateTime fechaPropuesta;

    // Constructor vacío necesario para que Jackson pueda deserializar desde el JSON
    public Propuesta() {
        this.fechaPropuesta = LocalDateTime.now();
        this.imagenesUrls = new ArrayList<>();
        this.bienesOfrecidos = new ArrayList<>();
    }

    public Propuesta(String idPostor, String idSubasta, List<BienOfrecido> bienesOfrecidos,
                     String descripcion, EstadoFisico estadoFisico, List<String> imagenesUrls) {

        if (idPostor == null || idPostor.trim().isEmpty())
            throw new IllegalArgumentException("La propuesta debe tener un id de postor válido.");
        if (idSubasta == null || idSubasta.trim().isEmpty())
            throw new IllegalArgumentException("La propuesta debe estar referenciada a una subasta.");
        if (bienesOfrecidos == null || bienesOfrecidos.isEmpty())
            throw new IllegalArgumentException("Debes incluir al menos un bien ofrecido en tu propuesta.");
        if (descripcion == null || descripcion.trim().isEmpty())
            throw new IllegalArgumentException("La descripción de la propuesta es obligatoria.");
        if (estadoFisico == null)
            throw new IllegalArgumentException("El estado físico general es obligatorio.");
        if (imagenesUrls == null || imagenesUrls.isEmpty())
            throw new IllegalArgumentException("Debes incluir al menos una foto como evidencia visual.");

        // CAMBIO: El prefijo ahora es PROP- reflejando la terminología correcta
        this.idPropuesta = "PROP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.idSubasta = idSubasta;
        this.idPostor = idPostor;
        this.bienesOfrecidos = new ArrayList<>(bienesOfrecidos);
        this.descripcion = descripcion;
        this.estadoFisico = estadoFisico;
        this.imagenesUrls = new ArrayList<>(imagenesUrls);
        this.fechaPropuesta = LocalDateTime.now();

        // Inicializamos el estado por defecto
        this.estadoPropuesta = EstadoPropuesta.EN_EVALUACION;
    }

    // --- MÉTODOS DE COMPORTAMIENTO ---

    public void marcarComoAceptada() {
        this.estadoPropuesta = EstadoPropuesta.ACEPTADA;
    }

    public void marcarComoRechazada() {
        this.estadoPropuesta = EstadoPropuesta.RECHAZADA;
    }

    // --- SETTERS PARA EDICIÓN (Requisito de la HU5) ---
    // Permiten editar la propuesta mientras la subasta siga activa

    public void setBienesOfrecidos(List<BienOfrecido> bienesOfrecidos) {
        if (bienesOfrecidos == null || bienesOfrecidos.isEmpty()) {
            throw new IllegalArgumentException("Debes ofrecer al menos un bien.");
        }
        this.bienesOfrecidos = new ArrayList<>(bienesOfrecidos);
    }

    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria.");
        }
        this.descripcion = descripcion;
    }

    public void setEstadoFisico(EstadoFisico estadoFisico) {
        if (estadoFisico == null) {
            throw new IllegalArgumentException("El estado físico es obligatorio.");
        }
        this.estadoFisico = estadoFisico;
    }

    public void setImagenesUrls(List<String> imagenesUrls) {
        if (imagenesUrls == null || imagenesUrls.isEmpty()) {
            throw new IllegalArgumentException("La evidencia visual es obligatoria.");
        }
        this.imagenesUrls = new ArrayList<>(imagenesUrls);
    }

    // --- GETTERS ---

    public String getIdPropuesta() { return idPropuesta; }
    public String getIdSubasta() { return idSubasta; }
    public String getIdPostor() { return idPostor; }
    public List<BienOfrecido> getBienesOfrecidos() { return new ArrayList<>(bienesOfrecidos); }
    public String getDescripcion() { return descripcion; }
    public EstadoFisico getEstadoFisico() { return estadoFisico; }
    public List<String> getImagenesUrls() { return new ArrayList<>(imagenesUrls); }
    public LocalDateTime getFechaPropuesta() { return fechaPropuesta; }
    public EstadoPropuesta getEstadoPropuesta() { return estadoPropuesta; }
}