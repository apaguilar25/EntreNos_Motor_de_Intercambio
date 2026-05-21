package org.example.CapaEntidades;
import java.time.LocalDate;
import java.util.ArrayList;
import subastas.Oferta;
import subastas.Historial;
import subastas.LineaBienConsumo;
import subastas.Subasta;

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

    // Constructor
    public Usuario(String idUsuario, String nombre, String correoElectronico, String descripcionPersonal) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
        this.descripcionPersonal = descripcionPersonal;
        this.reputacionHistorica = 0.0; // Se inicializa vacio al crear usuario nuevo

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



}


