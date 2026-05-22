package app.model.CapaGestion;

import app.model.CapaEntidades.*;
import app.model.CapaPersistencia.PersistenciaSubasta;
import app.model.CapaPersistencia.PersistenciaNotificacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GestionSubasta {

    @Autowired
    private PersistenciaSubasta persistenciaSubasta;

    @Autowired
    private PersistenciaNotificacion persistenciaNotificacion;

    public GestionSubasta() {
    }

    // ========================================================================
    // MÉTODOS ESTRICTOS DEL DIAGRAMA UML (HU4 / HU5)
    // ========================================================================

    public void registrarSubasta(String idSubastador, String descripcion, String nombreActivo, EstadoActivoFisico estadoFisico, List<String> rutasImagenes) {
        List<Subasta> subastas = persistenciaSubasta.cargar();

        ActivoFisico activo = new ActivoFisico();
        activo.setNombreActivo(nombreActivo);
        activo.setEstadoFisico(estadoFisico);

        List<Imagen> imagenes = rutasImagenes.stream().map(Imagen::new).collect(Collectors.toList());
        activo.setImagenes(imagenes);

        Subasta nuevaSubasta = new Subasta(idSubastador, descripcion, activo);
        nuevaSubasta.setIdSubasta("SUB-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        nuevaSubasta.setFechaCreacion(new Date());
        nuevaSubasta.setFechaFinalizacion(new Date(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)));
        nuevaSubasta.setEstado(EstadoSubasta.ACTIVA);

        subastas.add(nuevaSubasta);
        persistenciaSubasta.guardar(subastas);
    }

    public Subasta buscarSubasta(String idSubasta) {
        return persistenciaSubasta.cargar().stream()
                .filter(s -> s.getIdSubasta().equals(idSubasta))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada: " + idSubasta));
    }

    public void actualizarEstado(String idSubasta, EstadoSubasta estado) {
        List<Subasta> subastas = persistenciaSubasta.cargar();
        boolean encontrada = false;

        for (Subasta s : subastas) {
            if (s.getIdSubasta().equals(idSubasta)) {
                s.setEstado(estado);
                encontrada = true;
                break;
            }
        }

        if (!encontrada) throw new IllegalArgumentException("Subasta no encontrada.");
        persistenciaSubasta.guardar(subastas);
    }

    public void adjudicarGanador(Oferta ofertaGanadora) {
        List<Subasta> subastas = persistenciaSubasta.cargar();

        for (Subasta subasta : subastas) {
            if (subasta.getIdSubasta().equals(ofertaGanadora.getIdSubasta())) {
                for (Oferta oferta : subasta.getOfertas()) {
                    if (oferta.getIdOfertante().equals(ofertaGanadora.getIdOfertante())) {
                        oferta.setEsGanadora(true);
                    } else {
                        oferta.setEsGanadora(false);
                    }
                }
                subasta.setEstado(EstadoSubasta.FINALIZADA);
                enviarNotificacion(ofertaGanadora.getIdOfertante(), "¡Tu oferta ha ganado la subasta " + subasta.getIdSubasta() + "!", TipoNotificacion.ACTUALIZACION_SUBASTA);
                break;
            }
        }
        persistenciaSubasta.guardar(subastas);
    }

    public void enviarNotificacion(String idUsuario, String mensaje, TipoNotificacion tipo) {
        List<Notificacion> buzon = persistenciaNotificacion.cargar();
        buzon.add(new Notificacion("SISTEMA", idUsuario, mensaje, tipo));
        persistenciaNotificacion.guardar(buzon);
    }

    // ========================================================================
    // LÓGICA OPERATIVA ADICIONAL PARA HU5: PARTICIPACIÓN (OFRECER)
    // ========================================================================

    public void registrarOfertaEnSubasta(String idSubasta, Oferta nuevaOferta) {
        List<Subasta> subastas = persistenciaSubasta.cargar();

        Subasta subastaSeleccionada = subastas.stream()
                .filter(s -> s.getIdSubasta().equals(idSubasta))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La subasta especificada no existe."));

        // Regla de Negocio 1: Validar estado activo
        if (subastaSeleccionada.getEstado() != EstadoSubasta.ACTIVA) {
            throw new IllegalStateException("No se pueden enviar ofertas. La subasta está: " + subastaSeleccionada.getEstado());
        }

        // Regla de Negocio 2: Uso del método del UML para verificar expiración por tiempo
        if (subastaSeleccionada.verificarCierreSubasta(new Date())) {
            subastaSeleccionada.setEstado(EstadoSubasta.EN_REVISION);
            persistenciaSubasta.guardar(subastas);
            throw new IllegalStateException("La subasta ha alcanzado su fecha límite y está bajo revisión.");
        }

        // Configuración automatizada de la oferta entrante
        nuevaOferta.setIdSubasta(idSubasta);
        nuevaOferta.setFechaDePublicacion(new Date());
        nuevaOferta.setEsGanadora(false);

        // Añadir la oferta a la colección de la subasta correspondiente
        subastaSeleccionada.getOfertas().add(nuevaOferta);
        persistenciaSubasta.guardar(subastas);

        // Notificar automáticamente al creador de la subasta sobre la nueva puja recibida
        enviarNotificacion(subastaSeleccionada.getIdSubastador(),
                "Has recibido una nueva oferta de bienes de consumo en tu subasta " + idSubasta,
                TipoNotificacion.ACTUALIZACION_SUBASTA);

        System.out.println("[HU5] Oferta registrada con éxito de " + nuevaOferta.getIdOfertante() + " para " + idSubasta);
    }
}