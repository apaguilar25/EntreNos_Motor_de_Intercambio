package es.ucab.entrenos.modulos.publicacion.modelos;

/**
 * Publicación en el muro de servicios (HU1/HU2).
 * Las publicaciones son generadas desde el catálogo dinámico del usuario:
 *  - Cada HabilidadOfrecida del usuario puede generar una publicación de tipo HABILIDAD.
 *  - Cada NecesidadRegistrada del usuario puede generar una publicación de tipo NECESIDAD.
 */
public class Publicacion {

    private String idPublicacion;
    private String idUsuario;
    private String nombreUsuario;
    private double reputacionUsuario;
    private String tipoPublicacion; // "HABILIDAD" o "NECESIDAD"
    private String nombreServicio;  // nombre de la categoría (Habilidad.categoria)
    private String descripcion;
    private int precioCreditos;     // 0 para publicaciones de tipo NECESIDAD
    private boolean disponible;
    private long fechaCreacion;

    // HU10: Indica si el usuario está en el podio semanal (Vecino Destacado)
    private boolean esVecinoDestacado;

    public Publicacion() {}

    public Publicacion(String idUsuario, String nombreUsuario, double reputacionUsuario,
                       String tipoPublicacion, String nombreServicio, String descripcion, int precioCreditos) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.reputacionUsuario = reputacionUsuario;
        this.tipoPublicacion = tipoPublicacion;
        this.nombreServicio = nombreServicio;
        this.descripcion = descripcion;
        this.precioCreditos = precioCreditos;
        this.disponible = true;
        this.fechaCreacion = System.currentTimeMillis();
        this.esVecinoDestacado = false;
    }

    // --- Getters ---
    public String getIdPublicacion() { return idPublicacion; }
    public String getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public double getReputacionUsuario() { return reputacionUsuario; }
    public String getTipoPublicacion() { return tipoPublicacion; }
    public String getNombreServicio() { return nombreServicio; }
    public String getDescripcion() { return descripcion; }
    public int getPrecioCreditos() { return precioCreditos; }
    public boolean isDisponible() { return disponible; }
    public long getFechaCreacion() { return fechaCreacion; }
    public boolean isEsVecinoDestacado() { return esVecinoDestacado; }

    // --- Setters ---
    public void setIdPublicacion(String idPublicacion) { this.idPublicacion = idPublicacion; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
    public void setReputacionUsuario(double reputacionUsuario) { this.reputacionUsuario = reputacionUsuario; }
    public void setTipoPublicacion(String tipoPublicacion) { this.tipoPublicacion = tipoPublicacion; }
    public void setNombreServicio(String nombreServicio) { this.nombreServicio = nombreServicio; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setPrecioCreditos(int precioCreditos) { this.precioCreditos = precioCreditos; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public void setFechaCreacion(long fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setEsVecinoDestacado(boolean esVecinoDestacado) { this.esVecinoDestacado = esVecinoDestacado; }
}
