package es.ucab.entrenos.modulos.gamificacion.modelos;

public class Logro {

    private String idLogro;
    private String nombre;
    private String descripcion;
    private String tipoCriterio;
    private int bonoCreditos;
    private String icono;
    private boolean activo;

    public Logro() {}

    public Logro(String idLogro, String nombre, String descripcion, String tipoCriterio,
                 int bonoCreditos, String icono) {
        this.idLogro = idLogro;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipoCriterio = tipoCriterio;
        this.bonoCreditos = bonoCreditos;
        this.icono = icono;
        this.activo = true;
    }

    public String getIdLogro() { return idLogro; }
    public void setIdLogro(String idLogro) { this.idLogro = idLogro; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getTipoCriterio() { return tipoCriterio; }
    public void setTipoCriterio(String tipoCriterio) { this.tipoCriterio = tipoCriterio; }

    public int getBonoCreditos() { return bonoCreditos; }
    public void setBonoCreditos(int bonoCreditos) { this.bonoCreditos = bonoCreditos; }

    public String getIcono() { return icono; }
    public void setIcono(String icono) { this.icono = icono; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
