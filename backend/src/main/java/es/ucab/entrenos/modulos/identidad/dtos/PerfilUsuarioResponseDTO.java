package es.ucab.entrenos.modulos.identidad.dtos;

import es.ucab.entrenos.modulos.identidad.modelos.HabilidadOfrecida;
import es.ucab.entrenos.modulos.identidad.modelos.NecesidadRegistrada;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.subasta.dtos.SubastaResumenDTO;
import java.util.List;
import java.util.ArrayList;

public class PerfilUsuarioResponseDTO {
    private String id;
    private String nombre;
    private String correoElectronico;
    private String telefono;
    private String descripcionPersonal;
    private String urlFotoPerfil;
    private float creditosDisponibles;
    private float saldoDisponible;
    private float creditosComprometidos;
    private boolean catalogoCompletado;
    private float promedioCalificacion;
    private int cantidadCalificaciones;
    private String rol;

    private List<SubastaResumenDTO> subastasComoDueno;
    private List<SubastaResumenDTO> subastasComoParticipante;

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
        this.saldoDisponible = usuario.getMonedero().getSaldoDisponible();
        this.creditosComprometidos = usuario.getMonedero().getCreditosComprometidos();
        this.catalogoCompletado = usuario.isCatalogoCompletado();
        this.promedioCalificacion = usuario.getPromedioCalificacion();
        this.cantidadCalificaciones = usuario.getCantidadCalificaciones();
        this.rol = usuario.getRol() != null ? usuario.getRol().name() : "USUARIO_REGULAR";
        this.ofertas = usuario.getHabilidadesOfrecidas();
        this.necesidades = usuario.getNecesidadesRegistradas();

        // NUEVO: Inicializar para evitar nulos
        this.subastasComoDueno = new ArrayList<>();
        this.subastasComoParticipante = new ArrayList<>();

    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getTelefono() { return telefono; }
    public String getDescripcionPersonal() { return descripcionPersonal; }
    public String getUrlFotoPerfil() { return urlFotoPerfil; }
    public float getCreditosDisponibles() { return creditosDisponibles; }
    public float getSaldoDisponible() { return saldoDisponible; }
    public float getCreditosComprometidos() { return creditosComprometidos; }
    public boolean isCatalogoCompletado() { return catalogoCompletado; }
    public float getPromedioCalificacion() { return promedioCalificacion; }
    public int getCantidadCalificaciones() { return cantidadCalificaciones; }
    public String getRol() { return rol; }
    public List<HabilidadOfrecida> getOfertas() { return ofertas; }
    public List<NecesidadRegistrada> getNecesidades() { return necesidades; }

    public List<SubastaResumenDTO> getSubastasComoDueno()
        { return subastasComoDueno; }

    public void setSubastasComoDueno(List<SubastaResumenDTO> subastasComoDueno)
        { this.subastasComoDueno = subastasComoDueno; }

    public List<SubastaResumenDTO> getSubastasComoParticipante()
        { return subastasComoParticipante; }

    public void setSubastasComoParticipante(List<SubastaResumenDTO> subastasComoParticipante)
        { this.subastasComoParticipante = subastasComoParticipante; }
}
