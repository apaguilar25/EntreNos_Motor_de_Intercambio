package app.model.CapaEntidades;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;
import org.example.ModuloSubastas.Oferta;

public class Usuario {
    private String idUsuario;
    private String nombre;
    private String correoElectronico;
    private String telefono;
    private String descripcionPersonal;
    private Double reputacionHistorica;
    private boolean sancionActiva;
    private LocalDate fechaSancion;

    // Asociaciones y Agregaciones
    private Monedero monedero;
    private ArrayList<Habilidad> listaHabilidades;
    private ArrayList<Necesidad> listaNecesidades;
    private ArrayList<Oferta> listaOfertasRealizadas;
    private ArrayList<Notificacion> notificaciones;
    private Imagen fotoPerfil;

    // Constructor
    public Usuario(String nombre, String correoElectronico, String telefono, Imagen fotoPerfil) {
        this.idUsuario = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.telefono = telefono;
        this.fotoPerfil = fotoPerfil;
        this.monedero = new Monedero();
        this.listaHabilidades = new ArrayList<>();
        this.listaNecesidades = new ArrayList<>();
        this.notificaciones = new ArrayList<>();
        this.sancionActiva = false;
        this.reputacionHistorica = 5.0; // Inicia con reputación perfecta
    }


    // Getters y Setters
    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getDescripcionPersonal() {
        return descripcionPersonal;
    }

    public void setDescripcionPersonal(String descripcionPersonal) {
        this.descripcionPersonal = descripcionPersonal;
    }

    public Double getReputacionHistorica() {
        return reputacionHistorica;
    }

    public void setReputacionHistorica(Double reputacionHistorica) {
        this.reputacionHistorica = reputacionHistorica;
    }

    public ArrayList<Habilidad> getHabilidades() {
        return listaHabilidades;
    }

    public void setListaHabilidades(ArrayList<Habilidad> listaHabilidades) {
        this.listaHabilidades = listaHabilidades;
    }
}


