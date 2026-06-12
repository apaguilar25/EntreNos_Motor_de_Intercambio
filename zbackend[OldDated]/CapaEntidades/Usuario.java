//package modelOldDated.CapaEntidades;
//import modelOldDated.ModuloSubastas.Oferta;
//
//import java.util.ArrayList;
//import java.util.UUID;
//
//public class Usuario {
//    private String idUsuario;
//    private String nombre;
//    private String correoElectronico;
//    private String telefono;
//    private String descripcionPersonal;
//    private Double reputacionHistorica;
//    private Sancion sancion;
//
//    // Asociaciones y Agregaciones
//    private Monedero monedero;
//    private ArrayList<Habilidad> habilidades;
//    private ArrayList<Necesidad> necesidades;
//    private ArrayList<Oferta> listaOfertasRealizadas;
//    private ArrayList<Notificacion> notificaciones;
//    private Imagen fotoPerfil;
//
//    // Constructor
//
//    public Usuario() {
//        // Constructor vacío necesario para Jackson (Lectura de JSON)
//    }
//
//    public Usuario(String nombre, String correoElectronico, String telefono, Imagen fotoPerfil) {
//        this.idUsuario = UUID.randomUUID().toString();
//        this.nombre = nombre;
//        this.correoElectronico = correoElectronico;
//        this.telefono = telefono;
//        this.fotoPerfil = fotoPerfil;
//        this.monedero = new Monedero();
//        this.habilidades = new ArrayList<>();
//        this.necesidades = new ArrayList<>();
//        this.notificaciones = new ArrayList<>();
//        this.sancion = null; // No tiene sanción al inicio
//        this.reputacionHistorica = 5.0; // Inicia con reputación perfecta
//    }
//
//
//    // Getters y Setters
//    public String getIdUsuario() {
//        return idUsuario;
//    }
//
//    public void setIdUsuario(String idUsuario) {
//        this.idUsuario = idUsuario;
//    }
//
//    public String getNombre() {
//        return nombre;
//    }
//
//    public void setNombre(String nombre) {
//        this.nombre = nombre;
//    }
//
//    public String getCorreoElectronico() {
//        return correoElectronico;
//    }
//
//    public void setCorreoElectronico(String correoElectronico) {
//        this.correoElectronico = correoElectronico;
//    }
//
//    public String getDescripcionPersonal() {
//        return descripcionPersonal;
//    }
//
//    public void setDescripcionPersonal(String descripcionPersonal) {
//        this.descripcionPersonal = descripcionPersonal;
//    }
//
//    public Double getReputacionHistorica() {
//        return reputacionHistorica;
//    }
//
//    public void setReputacionHistorica(Double reputacionHistorica) {
//        this.reputacionHistorica = reputacionHistorica;
//    }
//
//    public ArrayList<Habilidad> getHabilidades() {
//        return habilidades;
//    }
//
//    public void setHabilidades(ArrayList<Habilidad> listaHabilidades) {
//        this.habilidades = listaHabilidades;
//    }
//
//    public ArrayList<Necesidad> getNecesidades() {
//        return necesidades;
//    }
//
//    public void setNecesidades(ArrayList<Necesidad> necesidades) {
//        this.necesidades = necesidades;
//    }
//
//    public String getTelefono() {
//        return telefono;
//    }
//
//    public void setTelefono(String telefono) {
//        this.telefono = telefono;
//    }
//
//    public Imagen getFotoPerfil() {
//        return fotoPerfil;
//    }
//
//    public void setFotoPerfil(Imagen fotoPerfil) {
//        this.fotoPerfil = fotoPerfil;
//    }
//
//    public Monedero getMonedero() {
//        return monedero;
//    }
//
//    public void setMonedero(Monedero monedero) {
//        this.monedero = monedero;
//    }
//
//    public ArrayList<Oferta> getListaOfertasRealizadas() {
//        return listaOfertasRealizadas;
//    }
//
//    public void setListaOfertasRealizadas(ArrayList<Oferta> listaOfertasRealizadas) {
//        this.listaOfertasRealizadas = listaOfertasRealizadas;
//    }
//
//    public ArrayList<Notificacion> getNotificaciones() {
//        return notificaciones;
//    }
//
//    public void setNotificaciones(ArrayList<Notificacion> notificaciones) {
//        this.notificaciones = notificaciones;
//    }
//
//    public Sancion getSancion() {
//        return sancion;
//    }
//
//    public void setSancion(Sancion sancion) {
//        this.sancion = sancion;
//    }
//}
//
//
