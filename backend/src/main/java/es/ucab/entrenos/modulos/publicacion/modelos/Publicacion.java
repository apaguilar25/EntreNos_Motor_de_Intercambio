package es.ucab.entrenos.modulos.publicacion.modelos;

public class Publicacion {

    private String idPublicacion;
    private String idUsuario;
    private String nombreUsuario;
    private double reputacionUsuario;
    private String tipoPublicacion;
    private String nombreServicio;
    private String descripcion;
    private int precioCreditos;
    private boolean disponible;
    private long fechaCreacion;
    private boolean esVecinoDestacado;
    private String idInstanciaCatalogo;
    private int version;

    public Publicacion() {}

    public Publicacion(String idUsuario, String tipoPublicacion, String nombreServicio, String descripcion, int precioCreditos) {
        this.idUsuario = idUsuario;
        this.tipoPublicacion = tipoPublicacion;
        this.nombreServicio = nombreServicio;
        this.descripcion = descripcion;
        this.precioCreditos = precioCreditos;
        this.disponible = true;
        this.fechaCreacion = System.currentTimeMillis();
    }

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
    public String getIdInstanciaCatalogo() { return idInstanciaCatalogo; }
    public int getVersion() { return version; }

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
    public void setIdInstanciaCatalogo(String idInstanciaCatalogo) { this.idInstanciaCatalogo = idInstanciaCatalogo; }
    public void setVersion(int version) { this.version = version; }
}
