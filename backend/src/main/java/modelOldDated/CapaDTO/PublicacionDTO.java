package modelOldDated.CapaDTO;

public class PublicacionDTO {
    private String idUsuario;
    private String nombreUsuario;
    private double reputacionUsuario;
    private String tipoPublicacion; // "HABILIDAD" o "NECESIDAD"
    private String nombreServicio;
    private String descripcion;
    private int precioCreditos;

    public PublicacionDTO(String idUsuario, String nombreUsuario, double reputacionUsuario,
                          String tipo, String nombreServicio, String descripcion, int precioCreditos) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.reputacionUsuario = reputacionUsuario;
        this.tipoPublicacion = tipo;
        this.nombreServicio = nombreServicio;
        this.descripcion = descripcion;
        this.precioCreditos = precioCreditos;
    }

    // Getters
    public String getIdUsuario() { return idUsuario; }
    public String getNombreUsuario() { return nombreUsuario; }
    public double getReputacionUsuario() { return reputacionUsuario; }
    public String getTipoPublicacion() { return tipoPublicacion; }
    public String getNombreServicio() { return nombreServicio; }
    public String getDescripcion() { return descripcion; }
    public int getPrecioCreditos() { return precioCreditos; }
}