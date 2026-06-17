package es.ucab.entrenos.modulos.subasta.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.EstadoCuenta;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.subasta.dtos.AdjudicacionResponseDTO;
import es.ucab.entrenos.modulos.subasta.dtos.ContactoUsuarioDTO;
import es.ucab.entrenos.modulos.subasta.modelos.EstadoFisico;
import es.ucab.entrenos.modulos.subasta.modelos.EstadoSubasta;
import es.ucab.entrenos.modulos.subasta.modelos.Propuesta;
import es.ucab.entrenos.modulos.subasta.modelos.Subasta;
import es.ucab.entrenos.modulos.subasta.repositorios.IRepositorioSubasta;
import es.ucab.entrenos.modulos.subasta.utilidades.ManejadorCandados;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Service
public class ServicioSubasta {

    private final IRepositorioSubasta repositorioSubasta;
    private final IRepositorioUsuario repositorioUsuario;
    private final ServicioNotificacion servicioNotificacion;

    public ServicioSubasta(IRepositorioSubasta repositorioSubasta, IRepositorioUsuario repositorioUsuario, ServicioNotificacion servicioNotificacion) {
        this.repositorioSubasta = repositorioSubasta;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioNotificacion = servicioNotificacion;
    }

    private void asegurarUsuarioHabilitado(String idUsuario) {
        Usuario u = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (u.getEstado() != EstadoCuenta.ACTIVO) {
            throw new IllegalStateException("Tu cuenta no está activa. No puedes operar en subastas.");
        }
    }

    public Subasta crearSubasta(String idPropietario, String nombreActivo, String descripcion, EstadoFisico estadoFisico, List<String> imagenesUrls, LocalDateTime fechaCierre) {
        asegurarUsuarioHabilitado(idPropietario);
        Subasta subasta = new Subasta(idPropietario, nombreActivo, descripcion, estadoFisico, imagenesUrls, fechaCierre);
        repositorioSubasta.guardar(subasta);
        return subasta;
    }

    public Subasta hacerOferta(String idSubasta, Propuesta propuesta) {
        Lock candadoEscritura = ManejadorCandados.obtenerCandado(idSubasta).writeLock();
        candadoEscritura.lock();
        try {
            Subasta subasta = repositorioSubasta.buscarPorId(idSubasta)
                    .orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada."));

            asegurarUsuarioHabilitado(propuesta.getIdPostor());
            
            subasta.registrarPropuesta(propuesta);
            subasta.incrementarVersion();
            repositorioSubasta.guardar(subasta);

            Usuario postor = repositorioUsuario.buscarPorId(propuesta.getIdPostor()).orElse(null);
            String nombrePostor = postor != null ? postor.getNombre() : "Un usuario";

            servicioNotificacion.enviarNotificacion(
                    propuesta.getIdPostor(),
                    subasta.getIdPropietario(),
                    nombrePostor + " ha hecho una oferta en tu subasta: " + subasta.getNombreActivo(),
                    TipoNotificacion.NUEVA_OFERTA_SUBASTA,
                    subasta.getId(),
                    propuesta.getIdPropuesta()
            );

            return subasta;
        } finally {
            candadoEscritura.unlock();
        }
    }

    public List<Subasta> listarSubastasActivas() {
        return repositorioSubasta.listarTodas().stream()
                .filter(s -> s.getEstado() == EstadoSubasta.ACTIVA)
                .collect(Collectors.toList());
    }

    public List<Subasta> listarSubastasPorPropietario(String idPropietario) {
        return repositorioSubasta.listarTodas().stream()
                .filter(s -> s.getIdPropietario().equals(idPropietario))
                .collect(Collectors.toList());
    }

    public AdjudicacionResponseDTO adjudicarGanador(String idPropietario, String idSubasta, String idPropuesta) {
        // PEDIMOS EL CANDADO
        Lock candadoEscritura = ManejadorCandados.obtenerCandado(idSubasta).writeLock();
        candadoEscritura.lock();
        try {
            Subasta subasta = repositorioSubasta.buscarPorId(idSubasta)
                    .orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada."));

            if (!subasta.getIdPropietario().equals(idPropietario)) {
                throw new SecurityException("Solo el creador de la subasta puede elegir al ganador.");
            }

            subasta.adjudicarGanador(idPropuesta);
            subasta.incrementarVersion();
            repositorioSubasta.guardar(subasta);

            Propuesta ganadora = subasta.getPropuestas().stream()
                    .filter(p -> p.getIdPropuesta().equals(idPropuesta)).findFirst().get();

            servicioNotificacion.enviarNotificacion(subasta.getIdPropietario(), ganadora.getIdPostor(), "¡Felicidades! Tu propuesta ha sido elegida como ganadora.", TipoNotificacion.SUBASTA_GANADA, subasta.getId(), null);

            for (Propuesta p : subasta.getPropuestas()) {
                if (!p.getIdPropuesta().equals(idPropuesta)) {
                    servicioNotificacion.enviarNotificacion(subasta.getIdPropietario(), p.getIdPostor(), "Tu propuesta no ha sido elegida. La subasta ha finalizado.", TipoNotificacion.SUBASTA_CANCELADA, subasta.getId(), null);
                }
            }
            
            servicioNotificacion.eliminarNotificacionesPorReferencia(idPropietario, idSubasta);

            Usuario usuarioGanador = repositorioUsuario.buscarPorId(ganadora.getIdPostor()).get();
            ContactoUsuarioDTO contacto = new ContactoUsuarioDTO(usuarioGanador.getNombre(), usuarioGanador.getCorreoElectronico(), usuarioGanador.getTelefono());

            return new AdjudicacionResponseDTO("¡Subasta adjudicada! Comunícate con el ganador.", subasta, contacto);
        } finally {
            candadoEscritura.unlock(); // SOLTAMOS EL CANDADO
        }
    }


    public void modificarSubasta(String idPropietario, String idSubasta, String nuevaDescripcion) {
        Lock candadoEscritura = ManejadorCandados.obtenerCandado(idSubasta).writeLock();
        candadoEscritura.lock();
        try {
            Subasta subasta = repositorioSubasta.buscarPorId(idSubasta).orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada."));
            if (!subasta.getIdPropietario().equals(idPropietario)) throw new SecurityException("Solo el creador puede modificar la subasta.");
            if (subasta.getEstado() != EstadoSubasta.ACTIVA) throw new IllegalStateException("Solo puedes modificar una subasta que est ACTIVA.");
            
            subasta.setDescripcion(nuevaDescripcion);
            subasta.incrementarVersion();
            repositorioSubasta.guardar(subasta);
        } finally {
            candadoEscritura.unlock();
        }
    }

    public void cancelarSubastaManual(String idPropietario, String idSubasta) {
        Lock candadoEscritura = ManejadorCandados.obtenerCandado(idSubasta).writeLock();
        candadoEscritura.lock();
        try {
            Subasta subasta = repositorioSubasta.buscarPorId(idSubasta).orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada."));
            if (!subasta.getIdPropietario().equals(idPropietario)) throw new SecurityException("Solo el creador puede cancelar la subasta.");

            subasta.cancelarSubasta();
            subasta.incrementarVersion();
            repositorioSubasta.guardar(subasta);

            for (Propuesta p : subasta.getPropuestas()) {
                servicioNotificacion.enviarNotificacion("SISTEMA", p.getIdPostor(), "La subasta en la que participabas ha sido cancelada y no hubo ganador.", TipoNotificacion.SUBASTA_CANCELADA);
            }
            servicioNotificacion.eliminarNotificacionesPorReferencia(idPropietario, idSubasta);
        } finally {
            candadoEscritura.unlock();
        }
    }


    // --- CRON JOBS ---
    @Scheduled(fixedRate = 60000)
    public void automatizarCierreDeLicitaciones() {
        List<Subasta> subastasVencidas = repositorioSubasta.listarTodas().stream()
                .filter(s -> s.getEstado() == EstadoSubasta.ACTIVA && LocalDateTime.now().isAfter(s.getFechaFinalizacionLicitacion()))
                .toList();

        for (Subasta subasta : subastasVencidas) {
            // El Cron Job también respeta la fila del candado
            Lock candadoEscritura = ManejadorCandados.obtenerCandado(subasta.getId()).writeLock();
            candadoEscritura.lock();
            try {
                // Volvemos a buscar la subasta fresca por si algún postor la modificó mientras esperábamos en la fila
                Subasta subastaFresca = repositorioSubasta.buscarPorId(subasta.getId()).orElse(null);
                if (subastaFresca == null || subastaFresca.getEstado() != EstadoSubasta.ACTIVA) continue;

                if (subastaFresca.getPropuestas().isEmpty()) {
                    subastaFresca.cancelarSubastaDesierta();
                } else {
                    subastaFresca.cerrarFaseLicitacion();
                    servicioNotificacion.enviarNotificacion(subastaFresca.getIdPropietario(),
                            "Tu subasta terminó. Tienes propuestas pendientes.", TipoNotificacion.SUBASTA_POR_REVISAR);
                }
                subastaFresca.incrementarVersion();
                repositorioSubasta.guardar(subastaFresca);
            } finally {
                candadoEscritura.unlock();
            }
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void sancionarPropietariosInactivos() {
        List<Subasta> subastasAbandonadas = repositorioSubasta.listarTodas().stream()
                .filter(s -> s.getEstado() == EstadoSubasta.ESPERANDO_DECISION && LocalDateTime.now().isAfter(s.getFechaFinalizacionLicitacion().plusDays(5)))
                .toList();

        for (Subasta subasta : subastasAbandonadas) {
            subasta.cancelarSubasta();
            subasta.incrementarVersion();
            repositorioSubasta.guardar(subasta);

            Usuario propietario = repositorioUsuario.buscarPorId(subasta.getIdPropietario()).orElse(null);
            if (propietario != null) {
                propietario.aplicarSancionPorInactividadSubasta();
                repositorioUsuario.guardar(propietario);
                servicioNotificacion.enviarNotificacion("SISTEMA", propietario.getId(), "Cuenta suspendida por abandonar subasta (5 días inactivo).", TipoNotificacion.SANCION_APLICADA);
            }
            for (Propuesta p : subasta.getPropuestas()) {
                servicioNotificacion.enviarNotificacion("SISTEMA", p.getIdPostor(), "Subasta cancelada por inactividad del dueño. No hubo ganador.", TipoNotificacion.SUBASTA_CANCELADA);
            }
            servicioNotificacion.eliminarNotificacionesPorReferencia(subasta.getIdPropietario(), subasta.getId());
        }
    }
}