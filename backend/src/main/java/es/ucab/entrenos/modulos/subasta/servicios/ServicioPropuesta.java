package es.ucab.entrenos.modulos.subasta.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.EstadoCuenta;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import es.ucab.entrenos.modulos.notificacion.modelos.TipoNotificacion;
import es.ucab.entrenos.modulos.notificacion.servicios.ServicioNotificacion;
import es.ucab.entrenos.modulos.subasta.dtos.RegistroPropuestaDTO;
import es.ucab.entrenos.modulos.subasta.modelos.EstadoSubasta;
import es.ucab.entrenos.modulos.subasta.modelos.Propuesta;
import es.ucab.entrenos.modulos.subasta.modelos.Subasta;
import es.ucab.entrenos.modulos.subasta.repositorios.IRepositorioSubasta;
import es.ucab.entrenos.modulos.subasta.utilidades.ManejadorCandados; // Importamos el manejador de candados
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
public class ServicioPropuesta {

    private final IRepositorioSubasta repositorioSubasta;
    private final IRepositorioUsuario repositorioUsuario;
    private final ServicioNotificacion servicioNotificacion;

    public ServicioPropuesta(IRepositorioSubasta repositorioSubasta, IRepositorioUsuario repositorioUsuario, ServicioNotificacion servicioNotificacion) {
        this.repositorioSubasta = repositorioSubasta;
        this.repositorioUsuario = repositorioUsuario;
        this.servicioNotificacion = servicioNotificacion;
    }

    private void asegurarUsuarioHabilitado(String idUsuario) {
        Usuario u = repositorioUsuario.buscarPorId(idUsuario).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (u.getEstado() != EstadoCuenta.ACTIVO) throw new IllegalStateException("Tu cuenta no está activa.");
    }

    // --- ESCRITURA: Requiere Bloqueo Pesimista (WriteLock) y Optimista (Version) ---

    public Propuesta registrarPropuesta(String idSubasta, String idPostor, RegistroPropuestaDTO dto) {
        Lock candadoEscritura = ManejadorCandados.obtenerCandado(idSubasta).writeLock();
        candadoEscritura.lock(); // BLOQUEO PESIMISTA ON
        try {
            asegurarUsuarioHabilitado(idPostor);
            Subasta subasta = repositorioSubasta.buscarPorId(idSubasta)
                    .orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada."));

            boolean yaParticipa = subasta.getPropuestas().stream().anyMatch(p -> p.getIdPostor().equals(idPostor));
            if (yaParticipa) throw new IllegalStateException("Ya has enviado una propuesta. Utiliza la opción de editar.");

            Propuesta propuesta = new Propuesta(idPostor, idSubasta, dto.getBienesOfrecidos(), dto.getDescripcion(), dto.getEstadoFisico(), dto.getImagenesUrls());

            subasta.registrarPropuesta(propuesta);

            subasta.incrementarVersion(); // BLOQUEO OPTIMISTA (Avisa al repositorio de cambios)
            repositorioSubasta.guardar(subasta);

            servicioNotificacion.enviarNotificacion(subasta.getIdPropietario(), "¡Alguien ha ofertado en tu subasta de " + subasta.getNombreActivo() + "!", TipoNotificacion.NUEVA_OFERTA_SUBASTA);
            return propuesta;
        } finally {
            candadoEscritura.unlock(); // BLOQUEO PESIMISTA OFF (Asegurado incluso si hay excepciones)
        }
    }

    public Propuesta editarPropuesta(String idSubasta, String idPostor, String idPropuesta, RegistroPropuestaDTO dto) {
        Lock candadoEscritura = ManejadorCandados.obtenerCandado(idSubasta).writeLock();
        candadoEscritura.lock();
        try {
            Subasta subasta = repositorioSubasta.buscarPorId(idSubasta)
                    .orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada."));

            subasta.editarPropuesta(idPropuesta, idPostor, dto.getBienesOfrecidos(), dto.getImagenesUrls());

            Propuesta p = subasta.getPropuestas().stream().filter(prop -> prop.getIdPropuesta().equals(idPropuesta)).findFirst().get();
            p.setDescripcion(dto.getDescripcion());
            p.setEstadoFisico(dto.getEstadoFisico());

            subasta.incrementarVersion(); // BLOQUEO OPTIMISTA
            repositorioSubasta.guardar(subasta);
            return p;
        } finally {
            candadoEscritura.unlock();
        }
    }

    public void retirarPropuesta(String idSubasta, String idPostor, String idPropuesta) {
        Lock candadoEscritura = ManejadorCandados.obtenerCandado(idSubasta).writeLock();
        candadoEscritura.lock();
        try {
            Subasta subasta = repositorioSubasta.buscarPorId(idSubasta)
                    .orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada."));

            subasta.retirarPropuesta(idPropuesta, idPostor);

            subasta.incrementarVersion(); // BLOQUEO OPTIMISTA
            repositorioSubasta.guardar(subasta);
        } finally {
            candadoEscritura.unlock();
        }
    }

    // --- LECTURA: Requiere Bloqueo Pesimista (ReadLock) para ver datos consistentes ---

    public Map<String, Object> obtenerSubastaConCompetenciaAnonima(String idSubasta) {
        Lock candadoLectura = ManejadorCandados.obtenerCandado(idSubasta).readLock();
        candadoLectura.lock(); // Permite múltiples lectores a la vez, pero bloquea si alguien está escribiendo
        try {
            Subasta subasta = repositorioSubasta.buscarPorId(idSubasta)
                    .orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada."));

            List<Map<String, Object>> propuestasAnonimas = new ArrayList<>();
            int contador = 1;

            for (Propuesta p : subasta.getPropuestas()) {
                propuestasAnonimas.add(Map.of("alias", "Postor Oculto #" + contador++, "bienesOfrecidos", p.getBienesOfrecidos(), "imagenesEvidencia", p.getImagenesUrls()));
            }
            return Map.of("informacionSubasta", subasta, "propuestasCompetencia", propuestasAnonimas);
        } finally {
            candadoLectura.unlock();
        }
    }

    public List<Map<String, Object>> obtenerHistorialPropuestasDeUsuario(String idPostor) {
        // Al ser un historial global que lee múltiples subastas, usamos la foto en memoria que provee el repositorio
        // para no crear un cuello de botella bloqueando todas las subastas del sistema.
        return repositorioSubasta.listarTodas().stream()
                .filter(sub -> sub.getPropuestas().stream().anyMatch(p -> p.getIdPostor().equals(idPostor)))
                .map(sub -> {
                    Propuesta miPropuesta = sub.getPropuestas().stream().filter(p -> p.getIdPostor().equals(idPostor)).findFirst().get();
                    String resultado = "EN_COMPETENCIA";
                    if (sub.getEstado() == EstadoSubasta.CANCELADA) resultado = "SUBASTA_CANCELADA";
                    else if (sub.getEstado() == EstadoSubasta.ESPERANDO_DECISION) resultado = "ESPERANDO_DECISION_DEL_PROPIETARIO";
                    else if (sub.getIdPropuestaGanadora() != null) {
                        resultado = sub.getIdPropuestaGanadora().equals(miPropuesta.getIdPropuesta()) ? "¡GANASTE!" : "PERDISTE";
                    }
                    return Map.<String, Object>of("idSubasta", sub.getId(), "activoSubastado", sub.getNombreActivo(), "estadoSubasta", sub.getEstado(), "miPropuesta", miPropuesta, "resultado", resultado);
                }).collect(Collectors.toList());
    }
}