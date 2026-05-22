package app.model.CapaEntidades;

import app.model.CapaDTO.PublicacionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.model.CapaPersistencia.PersistenciaUsuario;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MuroPublicaciones {

    @Autowired
    private PersistenciaUsuario PersistenciaUsuario;

    // Traduce obtenerPublicaciones() de tu diagrama
    public List<PublicacionDTO> obtenerPublicaciones() {
        List<Usuario> usuarios = PersistenciaUsuario.cargar();
        List<PublicacionDTO> muro = new ArrayList<>();

        for (Usuario u : usuarios) {
            if (u.getHabilidades() != null) {
                u.getHabilidades().forEach(h -> muro.add(new PublicacionDTO(
                        u.getIdUsuario(), u.getNombre(), u.getReputacionHistorica(),
                        "HABILIDAD", h.getNombre(), h.getDescripcionHabilidad(), h.getPrecioCreditos()
                )));
            }
            if (u.getNecesidades() != null) {
                u.getNecesidades().forEach(n -> muro.add(new PublicacionDTO(
                        u.getIdUsuario(), u.getNombre(), u.getReputacionHistorica(),
                        "NECESIDAD", n.getNombre(), n.getDescripcionNecesidad(), 0
                )));
            }
        }
        return muro;
    }

    // Traduce filtrarPublicaciones() de tu diagrama
    public List<PublicacionDTO> filtrarPublicaciones(String tipo, String servicio) {
        return obtenerPublicaciones().stream()
                .filter(p -> (tipo == null || tipo.isEmpty() || p.getTipoPublicacion().equalsIgnoreCase(tipo)))
                .filter(p -> (servicio == null || servicio.isEmpty() || p.getNombreServicio().toLowerCase().contains(servicio.toLowerCase())))
                .collect(Collectors.toList());
    }
}