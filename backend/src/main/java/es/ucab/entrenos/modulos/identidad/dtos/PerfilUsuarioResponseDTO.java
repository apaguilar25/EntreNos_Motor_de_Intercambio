package es.ucab.entrenos.modulos.identidad.dtos;

import es.ucab.entrenos.modulos.identidad.modelos.HabilidadOfrecida;
import es.ucab.entrenos.modulos.identidad.modelos.NecesidadRegistrada;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import java.util.List;

public class PerfilUsuarioResponseDTO {
    private String id;
    private String nombre;
    private String correoElectronico;
    private String telefono;
    private String descripcionPersonal;
    private String urlFotoPerfil;
    private float creditosDisponibles;
    private boolean catalogoCompletado;
    private float promedioCalificacion;
    private int cantidadCalificaciones;

    private List<HabilidadOfrecida> ofertas;
    private List<NecesidadRegistrada> necesidades;

    public PerfilUsuarioResponseDTO() {}

    public PerfilUsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.correoElectronico = usuario.getCorreoElectronico();
        this.telefono = usuario.getTelefono();
        this.descripcionPersonal = usuario.getDescripcionPersonal();
        this.urlFotoPerfil = usuario.getUrlFotoPerfil();
        this.creditosDisponibles = usuario.getMonedero().getCreditosDisponibles();
        this.catalogoCompletado = usuario.isCatalogoCompletado();
        this.promedioCalificacion = usuario.getPromedioCalificacion();
        this.cantidadCalificaciones = usuario.getCantidadCalificaciones();
        this.ofertas = usuario.getHabilidadesOfrecidas();
        this.necesidades = usuario.getNecesidadesRegistradas();
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getTelefono() { return telefono; }
    public String getDescripcionPersonal() { return descripcionPersonal; }
    public String getUrlFotoPerfil() { return urlFotoPerfil; }
    public float getCreditosDisponibles() { return creditosDisponibles; }
    public boolean isCatalogoCompletado() { return catalogoCompletado; }
    public float getPromedioCalificacion() { return promedioCalificacion; }
    public int getCantidadCalificaciones() { return cantidadCalificaciones; }
    public List<HabilidadOfrecida> getOfertas() { return ofertas; }
    public List<NecesidadRegistrada> getNecesidades() { return necesidades; }
}
