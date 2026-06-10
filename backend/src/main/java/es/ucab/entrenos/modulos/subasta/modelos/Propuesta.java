package es.ucab.entrenos.modulos.subasta.modelos;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Propuesta {

    private String id;
    private String idSubasta; // Referencia por si la necesitamos en reportes futuros
    private Usuario postor; // La persona que hace la oferta

    // Detalles del artículo que se está ofreciendo a cambio
    private String nombreActivoOfrecido;
    private String descripcion;
    private EstadoFisico estadoFisico;
    private List<String> imagenesUrls;

    private LocalDateTime fechaPuja;

    public Propuesta() {
        this.fechaPuja = LocalDateTime.now();
        this.imagenesUrls = new ArrayList<>();
    }

    public Propuesta(Usuario postor, String idSubasta, String nombreActivoOfrecido,
                     String descripcion, EstadoFisico estadoFisico, List<String> imagenesUrls) {

        if (postor == null) throw new IllegalArgumentException("La puja debe tener un postor.");
        if (nombreActivoOfrecido == null || nombreActivoOfrecido.trim().isEmpty()) throw new IllegalArgumentException("Debes especificar el nombre del activo ofrecido.");
        if (descripcion == null || descripcion.trim().isEmpty()) throw new IllegalArgumentException("La descripción del activo ofrecido es obligatoria.");
        if (estadoFisico == null) throw new IllegalArgumentException("El estado físico es obligatorio.");
        if (imagenesUrls == null || imagenesUrls.isEmpty()) throw new IllegalArgumentException("Debes incluir al menos una foto del activo ofrecido.");

        this.id = "PUJ-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.idSubasta = idSubasta;
        this.postor = postor;
        this.nombreActivoOfrecido = nombreActivoOfrecido;
        this.descripcion = descripcion;
        this.estadoFisico = estadoFisico;
        this.imagenesUrls = new ArrayList<>(imagenesUrls);
        this.fechaPuja = LocalDateTime.now();
    }



    // Getters
    public String getId() { return id; }
    public String getIdSubasta() { return idSubasta; }
    public Usuario getPostor() { return postor; }
    public String getNombreActivoOfrecido() { return nombreActivoOfrecido; }
    public String getDescripcion() { return descripcion; }
    public EstadoFisico getEstadoFisico() { return estadoFisico; }
    public List<String> getImagenesUrls() { return new ArrayList<>(imagenesUrls); }
    public LocalDateTime getFechaPuja() { return fechaPuja; }
}