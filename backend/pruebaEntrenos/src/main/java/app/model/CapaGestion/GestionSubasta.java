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

    // 1. registrarSubasta()
    public void registrarSubasta(String idSubastador, String descripcion, String nombreActivo, EstadoActivoFisico estadoFisico, List<String> rutasImagenes) {
        List<Subasta> subastas = persistenciaSubasta.cargar();

        // Armar el Activo y sus imágenes
        ActivoFisico activo = new ActivoFisico();
        activo.setNombreActivo(nombreActivo);
        activo.setEstadoFisico(estadoFisico);

        List<Imagen> imagenes = rutasImagenes.stream().map(Imagen::new).collect(Collectors.toList());
        activo.setImagenes(imagenes);

        // Armar la Subasta
        Subasta nuevaSubasta = new Subasta(idSubastador, descripcion, activo);
        nuevaSubasta.setIdSubasta("SUB-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase());
        nuevaSubasta.setFechaCreacion(new Date());
        // Por defecto, finaliza en 7 días
        nuevaSubasta.setFechaFinalizacion(new Date(System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000)));
        nuevaSubasta.setEstado(EstadoSubasta.ACTIVA);

        subastas.add(nuevaSubasta);
        persistenciaSubasta.guardar(subastas);

        System.out.println("[SISTEMA] Subasta registrada: " + nuevaSubasta.getIdSubasta());
    }

    // 2. buscarSubasta()
    public Subasta buscarSubasta(String idSubasta) {
        return persistenciaSubasta.cargar().stream()
                .filter(s -> s.getIdSubasta().equals(idSubasta))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Subasta no encontrada: " + idSubasta));
    }

    // 3. actualizarEstado()
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

    // 4. adjudicarGanador()
    public void adjudicarGanador(Oferta ofertaGanadora) {
        List<Subasta> subastas = persistenciaSubasta.cargar();

        for (Subasta subasta : subastas) {
            if (subasta.getIdSubasta().equals(ofertaGanadora.getIdSubasta())) {
                for (Oferta oferta : subasta.getOfertas()) {
                    if (oferta.getIdOfertante().equals(ofertaGanadora.getIdOfertante())) {
                        oferta.setEsGanadora(true);
                    } else {
                        oferta.setEsGanadora(false); // Las demás pierden
                    }
                }
                subasta.setEstado(EstadoSubasta.FINALIZADA);

                enviarNotificacion(ofertaGanadora.getIdOfertante(), "¡Felicidades! Tu oferta ha ganado en la subasta " + subasta.getIdSubasta(), TipoNotificacion.ACTUALIZACION_SUBASTA);
                break;
            }
        }
        persistenciaSubasta.guardar(subastas);
    }

    // 5. enviarNotificacion()
    public void enviarNotificacion(String destinatario, String mensaje, TipoNotificacion tipo) {
        List<Notificacion> buzon = persistenciaNotificacion.cargar();
        buzon.add(new Notificacion("SISTEMA", destinatario, mensaje, tipo));
        persistenciaNotificacion.guardar(buzon);
    }
}