package es.ucab.entrenos.modulos.gamificacion.servicios;

import es.ucab.entrenos.modulos.gamificacion.modelos.Logro;
import es.ucab.entrenos.modulos.gamificacion.modelos.LogroDesbloqueado;
import es.ucab.entrenos.modulos.gamificacion.modelos.TipoCriterioLogro;
import es.ucab.entrenos.modulos.gamificacion.repositorios.IRepositorioLogro;
import es.ucab.entrenos.modulos.gamificacion.repositorios.IRepositorioLogroDesbloqueado;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.servicios.ServicioUsuario;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.publicacion.modelos.EstadoTransaccion;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import es.ucab.entrenos.modulos.publicacion.repositorios.IRepositorioTransaccion;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicioGamificacion {

    private final IRepositorioLogro repositorioLogro;
    private final IRepositorioLogroDesbloqueado repositorioLogroDesbloqueado;
    private final IRepositorioTransaccion repositorioTransaccion;
    private final ServicioUsuario servicioUsuario;
    private final ServicioNotificacion servicioNotificacion;

    public ServicioGamificacion(IRepositorioLogro repositorioLogro,
                                IRepositorioLogroDesbloqueado repositorioLogroDesbloqueado,
                                IRepositorioTransaccion repositorioTransaccion,
                                ServicioUsuario servicioUsuario,
                                ServicioNotificacion servicioNotificacion) {
        this.repositorioLogro = repositorioLogro;
        this.repositorioLogroDesbloqueado = repositorioLogroDesbloqueado;
        this.repositorioTransaccion = repositorioTransaccion;
        this.servicioUsuario = servicioUsuario;
        this.servicioNotificacion = servicioNotificacion;
    }

    public List<LogroDesbloqueado> evaluarLogros(String idUsuario) {
        List<Logro> todosLosLogros = repositorioLogro.obtenerTodos().stream()
                .filter(Logro::isActivo)
                .collect(Collectors.toList());
        List<LogroDesbloqueado> yaDesbloqueados = repositorioLogroDesbloqueado.obtenerPorUsuario(idUsuario);
        Set<String> idsDesbloqueados = yaDesbloqueados.stream()
                .map(LogroDesbloqueado::getIdLogro)
                .collect(Collectors.toSet());

        List<LogroDesbloqueado> nuevos = new ArrayList<>();

        for (Logro logro : todosLosLogros) {
            if (idsDesbloqueados.contains(logro.getIdLogro())) continue;

            boolean cumple = cumpleCriterio(idUsuario, logro.getTipoCriterio());
            if (cumple) {
                LogroDesbloqueado nuevo = desbloquearYRecompensar(idUsuario, logro);
                nuevos.add(nuevo);
            }
        }

        return nuevos;
    }

    private boolean cumpleCriterio(String idUsuario, String tipoCriterio) {
        switch (tipoCriterio) {
            case TipoCriterioLogro.PRIMERA_TRANSACCION:
                return cumplePrimeraTransaccion(idUsuario);
            case TipoCriterioLogro.MAESTRO_CONFIANZA:
                return cumpleMaestroConfianza(idUsuario);
            case TipoCriterioLogro.POLIMATA:
                return cumplePolimata(idUsuario);
            default:
                return false;
        }
    }

    private boolean cumplePrimeraTransaccion(String idUsuario) {
        return repositorioTransaccion.obtenerTodas().stream()
                .anyMatch(t -> t.getEstado() == EstadoTransaccion.FINALIZADA
                        && (t.getIdOfertante().equals(idUsuario) || t.getIdDemandante().equals(idUsuario)));
    }

    private boolean cumpleMaestroConfianza(String idUsuario) {
        Set<String> usuariosDistintos = repositorioTransaccion.obtenerTodas().stream()
                .filter(t -> t.getEstado() == EstadoTransaccion.FINALIZADA)
                .filter(t -> t.getIdOfertante().equals(idUsuario))
                .filter(t -> t.getCalificacion() != null && t.getCalificacion() == 5)
                .map(Transaccion::getIdDemandante)
                .collect(Collectors.toSet());
        return usuariosDistintos.size() >= 5;
    }

    private boolean cumplePolimata(String idUsuario) {
        Usuario usuario = servicioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));
        long categoriasDistintas = usuario.getHabilidadesOfrecidas().stream()
                .map(h -> h.getHabilidadBase().getCategoria())
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .distinct()
                .count();
        return categoriasDistintas >= 5;
    }

    private LogroDesbloqueado desbloquearYRecompensar(String idUsuario, Logro logro) {
        LogroDesbloqueado ld = new LogroDesbloqueado(idUsuario, logro.getIdLogro());
        repositorioLogroDesbloqueado.guardar(ld);

        Usuario usuario = servicioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + idUsuario));
        usuario.getMonedero().acreditarLogro(logro.getBonoCreditos());
        usuario.incrementarVersion();
        servicioUsuario.guardar(usuario);

        servicioNotificacion.enviarNotificacion("SISTEMA", idUsuario,
                "¡Felicidades! Has desbloqueado la medalla \"" + logro.getNombre()
                        + "\": " + logro.getDescripcion()
                        + ". Has recibido " + logro.getBonoCreditos() + " créditos como bono por este logro.",
                TipoNotificacion.LOGRO_DESBLOQUEADO);

        return ld;
    }

    public List<Logro> obtenerTodosLosLogros() {
        return repositorioLogro.obtenerTodos().stream()
                .filter(Logro::isActivo)
                .collect(Collectors.toList());
    }

    public List<LogroDesbloqueado> obtenerLogrosDesbloqueados(String idUsuario) {
        return repositorioLogroDesbloqueado.obtenerPorUsuario(idUsuario);
    }
}
