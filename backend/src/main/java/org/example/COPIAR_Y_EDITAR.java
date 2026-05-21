package entidades;

import subastas.Oferta;
import subastas.Historial;
import subastas.LineaBienConsumo;
import subastas.Subasta;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class Usuario {
    private String idUsuario;
    private String nombre;
    private String correoElectronico;
    private String telefono;
    private String descripcionPersonal;
    private double reputacionHistorica;
    private boolean sancionActiva;
    private Date fechaSancion;

    // Composiciones y Asociaciones
    private Monedero monedero;
    private List<Habilidad> habilidades;
    private List<Necesidad> necesidades;
    private Imagen imagen;
    private List<Notificacion> notificaciones;
    private Historial historial;
    private List<Oferta> ofertas;
    private List<Subasta> subastasComoSubastador;

    public Usuario(String nombre, String correo, String telefono, Imagen imagen) {
        this.nombre = nombre;
        this.correoElectronico = correo;
        this.telefono = telefono;
        this.imagen = imagen;
        this.monedero = new Monedero();
        this.habilidades = new ArrayList<>();
        this.necesidades = new ArrayList<>();
        this.notificaciones = new ArrayList<>();
        this.ofertas = new ArrayList<>();
        this.subastasComoSubastador = new ArrayList<>();
    }

    public void recibirNotificacion(Notificacion nuevaNotificacion) {
        this.notificaciones.add(nuevaNotificacion);
    }

    public void calcularReputacionHistorica() {
        // Lógica para recalcular reputación
    }

    public double calcularReputacionPorServicio(intercambios.Servicio servicio) {
        // Lógica de cálculo
        return 0.0;
    }

    public Oferta crearOferta(Oferta oferta, List<LineaBienConsumo> lineas, List<Imagen> imagenes) {
        // Lógica para crear oferta
        this.ofertas.add(oferta);
        return oferta;
    }

    public Historial obtenerHistorial() {
        return this.historial;
    }

    // Getters y Setters omitidos por brevedad...
}

public class Monedero {
    private int creditosDisponibles;
    private int creditosComprometidos;

    public Monedero() {
        this.creditosDisponibles = 0;
        this.creditosComprometidos = 0;
    }

    public boolean tieneSaldoSuficiente(int monto) {
        return (this.creditosDisponibles - this.creditosComprometidos) >= monto;
    }

    public void retenerCreditosComprometidos(int monto) {
        if (tieneSaldoSuficiente(monto)) {
            this.creditosComprometidos += monto;
        }
    }
    // Getters y Setters...
}

public class Habilidad {
    private String nombre;
    private int precioCreditos;
    private String descripcionHabilidad;

    public Habilidad(String nombre, int precioCreditos, String descripcion) {
        this.nombre = nombre;
        this.precioCreditos = precioCreditos;
        this.descripcionHabilidad = descripcion;
    }
    // Getters y Setters...
}

public class Necesidad {
    private String nombre;
    private String descripcionNecesidad;

    public Necesidad(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcionNecesidad = descripcion;
    }
    // Getters y Setters...
}

public class Imagen {
    private String ruta;
    private Date fechaSubida;

    public Imagen() {
        this.fechaSubida = new Date();
    }
    // Getters y Setters...
}

public class Notificacion {
    private String idNotificacion;
    private String mensaje;
    private String tipo;
    private boolean estadoLectura;

    public void marcarNotificacionLeida() {
        this.estadoLectura = true;
    }
    // Getters y Setters...
}

public class SessionActual {
    private String idUsuario;
    // Getters y Setters...
}