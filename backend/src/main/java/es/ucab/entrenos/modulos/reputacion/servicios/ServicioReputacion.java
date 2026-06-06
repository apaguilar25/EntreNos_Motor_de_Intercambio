package es.ucab.entrenos.modulos.reputacion.servicios;

import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.reputacion.modelos.Resena;
import es.ucab.entrenos.modulos.reputacion.repositorios.IRepositorioReputacion;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServicioReputacion {
    private final IRepositorioReputacion repositorioReputacion;
    private final ServicioUsuario servicioUsuario;

    public ServicioReputacion(IRepositorioReputacion repositorioReputacion, ServicioUsuario servicioUsuario) {
        this.repositorioReputacion = repositorioReputacion;
        this.servicioUsuario = servicioUsuario;
    }

    public Resena crearResena(String idTransaccion, String idEmisor, String idReceptor, int calificacion, String comentario) {
        if (calificacion < 1 || calificacion > 5)
            throw new IllegalArgumentException("La calificacion debe ser un entero entre 1 y 5.");

        String idResena = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Resena resena = new Resena(idResena, idTransaccion, idEmisor, idReceptor, calificacion, comentario);
        repositorioReputacion.guardar(resena);

        servicioUsuario.actualizarReputacion(idReceptor, calificacion);
        return resena;
    }

    public List<Resena> obtenerResenasPorUsuario(String idUsuario) {
        return repositorioReputacion.obtenerPorReceptor(idUsuario);
    }

    public List<Resena> obtenerResenasPorEmisor(String idUsuario) {
        return repositorioReputacion.obtenerPorEmisor(idUsuario);
    }

    public List<Resena> obtenerResenasPorTransaccion(String idTransaccion) {
        return repositorioReputacion.obtenerPorTransaccion(idTransaccion);
    }

    public Optional<Resena> obtenerPorId(String idResena) {
        return repositorioReputacion.obtenerPorId(idResena);
    }

    public List<Resena> listarTodas() {
        return repositorioReputacion.obtenerTodas();
    }
}
