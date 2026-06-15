package es.ucab.entrenos.modulos.publicacion.modelos;

public class MotivoCancelacion {
    private String id;
    private String descripcion;
    private boolean activo;

    public MotivoCancelacion() {}

    public MotivoCancelacion(String id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
        this.activo = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
