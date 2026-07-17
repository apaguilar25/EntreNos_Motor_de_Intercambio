package es.ucab.entrenos.modulos.identidad.dtos;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;

public class PerfilPublicoDTO {
    private String id;
    private String nombre;
    private String urlFotoPerfil;
    private float promedioCalificacion;
    private String descripcionPersonal;

    public PerfilPublicoDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nombre = usuario.getNombre();
        this.urlFotoPerfil = usuario.getUrlFotoPerfil();
        this.promedioCalificacion = usuario.getPromedioCalificacion();
        this.descripcionPersonal = usuario.getDescripcionPersonal();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrlFotoPerfil() {
        return urlFotoPerfil;
    }

    public void setUrlFotoPerfil(String urlFotoPerfil) {
        this.urlFotoPerfil = urlFotoPerfil;
    }

    public float getPromedioCalificacion() {
        return promedioCalificacion;
    }

    public void setPromedioCalificacion(float promedioCalificacion) {
        this.promedioCalificacion = promedioCalificacion;
    }

    public String getDescripcionPersonal() {
        return descripcionPersonal;
    }

    public void setDescripcionPersonal(String descripcionPersonal) {
        this.descripcionPersonal = descripcionPersonal;
    }
}