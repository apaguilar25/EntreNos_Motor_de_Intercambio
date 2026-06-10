package es.ucab.entrenos.modulos.subasta.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import es.ucab.entrenos.modulos.subasta.modelos.EstadoFisico;
import es.ucab.entrenos.modulos.subasta.modelos.EstadoSubasta;
import es.ucab.entrenos.modulos.subasta.modelos.Propuesta;
import es.ucab.entrenos.modulos.subasta.modelos.Subasta;
import es.ucab.entrenos.modulos.subasta.repositorios.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ServicioSubasta {

    private final IRepositorioSubasta repositorioSubasta;
    private final IRepositorioUsuario repositorioUsuario;

    public ServicioSubasta(IRepositorioSubasta repositorioSubasta, IRepositorioUsuario repositorioUsuario) {
        this.repositorioSubasta = repositorioSubasta;
        this.repositorioUsuario = repositorioUsuario;
    }

    // --- 1. MÉTODOS ACCIONADOS POR EL USUARIO ---

    public Subasta publicarSubasta(Usuario propietario, String nombreActivo, String descripcion,
                                   EstadoFisico estadoFisico, List<String> imagenes,
                                   LocalDateTime fechaCierre) {

        String nuevoId = "SUB-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // El constructor de Subasta validará las reglas de negocio (sanciones, fechas, etc.)
        Subasta nuevaSubasta = new Subasta(nuevoId, propietario, nombreActivo, descripcion, estadoFisico, imagenes, fechaCierre);

        repositorioSubasta.guardar(nuevaSubasta);
        return nuevaSubasta;
    }

    public Propuesta enviarPuja(String idSubasta, Usuario postor, String nombreActivoOfrecido,
                                String descripcion, EstadoFisico estadoFisico, List<String> imagenes) {

        // 1. Buscamos la subasta en nuestro repositorio JSON
        Subasta subasta = repositorioSubasta.buscarPorId(idSubasta)
                .orElseThrow(() -> new IllegalArgumentException("❌ Error: No se encontró ninguna subasta con el ID proporcionado."));

        // 2. Instanciamos la Propuesta (Su constructor validará que no falten datos ni fotos)
        Propuesta nuevaPropuesta = new Propuesta(postor, idSubasta, nombreActivoOfrecido, descripcion, estadoFisico, imagenes);

        // 3. Intentamos registrar la propuesta en la subasta.
        // ¡Magia del DDD! La propia clase Subasta decidirá si la acepta o si lanza una excepción
        // (por ejemplo, si la subasta está cerrada o si el usuario es el dueño).
        subasta.registrarPropuesta(nuevaPropuesta);

        // 4. Si la subasta aceptó la propuesta sin lanzar errores, guardamos la subasta actualizada en el JSON
        repositorioSubasta.guardar(subasta);

        return nuevaPropuesta;
    }

    // --- 2. MÉTODOS ACCIONADOS AUTOMÁTICAMENTE POR EL SISTEMA (CRON JOBS) ---

    /**
     * Tarea 1: Cierra las licitaciones cuando se alcanza la fecha límite.
     * Se ejecuta automáticamente cada 5 minutos.
     */
    @Scheduled(fixedRate = 300000) // 300,000 milisegundos = 5 minutos
    public void automatizarCierreDeLicitaciones() {
        List<Subasta> subastasActivas = repositorioSubasta.listarTodas().stream()
                .filter(s -> s.getEstado() == EstadoSubasta.ACTIVA)
                .toList();

        for (Subasta subasta : subastasActivas) {
            if (LocalDateTime.now().isAfter(subasta.getFechaFinalizacionLicitacion())) {
                // El tiempo expiró, cambiamos el estado
                subasta.setEstado(EstadoSubasta.ESPERANDO_DECISION);
                repositorioSubasta.guardar(subasta);
                System.out.println("⏰ Sistema: La subasta " + subasta.getId() + " ha cerrado su fase de licitación.");
            }
        }
    }

    /**
     * Tarea 2: Sanciona a los usuarios que dejan vencer los 5 días de gracia para elegir un ganador.
     * Se ejecuta automáticamente cada hora.
     */
    @Scheduled(fixedRate = 3600000) // 3,600,000 milisegundos = 1 hora
    public void automatizarSancionesPorInactividad() {
        List<Subasta> subastasEsperando = repositorioSubasta.listarTodas().stream()
                .filter(s -> s.getEstado() == EstadoSubasta.ESPERANDO_DECISION)
                .toList();

        for (Subasta subasta : subastasEsperando) {
            // Usamos el método inteligente que creaste en el modelo
            if (subasta.haExpiradoPlazoDeResolucion()) {

                // 1. Cancelamos la subasta
                subasta.setEstado(EstadoSubasta.CERRADA_POR_INACTIVIDAD);
                repositorioSubasta.guardar(subasta);

                // 2. Buscamos al propietario y le aplicamos la sanción (reutilizando métodos de Identidad)
                Usuario propietarioInfractor = subasta.getPropietario();
                propietarioInfractor.aplicarSancionPorInactividadSubasta();
                repositorioUsuario.guardar(propietarioInfractor);

                System.out.println("🚨 Sistema: Subasta " + subasta.getId() + " cerrada por inactividad. Usuario "
                        + propietarioInfractor.getCorreoElectronico() + " sancionado por 72 horas.");
            }
        }
    }
}