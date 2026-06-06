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
    private float creditosDisponibles; // Sacado del Monedero
    private boolean catalogoCompletado;

    // Podemos devolver las listas tal cual porque las clases envoltorio no tienen datos sensibles
    private List<HabilidadOfrecida> ofertas;
    private List<NecesidadRegistrada> necesidades;

    public PerfilUsuarioResponseDTO() {}

    public PerfilUsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.correoElectronico = usuario.getCorreoElectronico();
        this.telefono = usuario.getTelefono();
        this.descripcionPersonal = usuario.getDescripcionPersonal();
        this.urlFotoPerfil = usuario.getUrlFotoPerfil(); // "default.png" inicialmente
        this.creditosDisponibles = usuario.getMonedero().getCreditosDisponibles(); // 0 o 50 (Capital Semilla)
        this.catalogoCompletado = usuario.isCatalogoCompletado();
        this.ofertas = usuario.getHabilidadesOfrecidas();
        this.necesidades = usuario.getNecesidadesRegistradas();
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDescripcionPersonal() {
        return descripcionPersonal;
    }

    public String getUrlFotoPerfil() {
        return urlFotoPerfil;
    }

    public float getCreditosDisponibles() {
        return creditosDisponibles;
    }

    public boolean isCatalogoCompletado() {
        return catalogoCompletado;
    }

    public List<HabilidadOfrecida> getOfertas() {
        return ofertas;
    }

    public List<NecesidadRegistrada> getNecesidades() {
        return necesidades;
    }
}