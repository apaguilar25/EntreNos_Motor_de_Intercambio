package app.model.CapaEntidades;
import java.CapaPersistencia.PersistenciaUsuario;

import java.util.ArrayList;

public class MuroPublicaciones {

    public ArrayList<PublicacionDTO> obtenerTodasLasPublicaciones() {
        ArrayList<Usuario> usuarios = PersistenciaUsuario.cargarUsuarios();
        ArrayList<PublicacionDTO> muro = new ArrayList<>();

        for (Usuario u : usuarios) {
            // Mapear habilidades (Ofrece)
            if (u.getHabilidades() != null) {
                u.getHabilidades().forEach(h -> muro.add(new PublicacionDTO(
                        u.getIdUsuario(), u.getNombre(), u.getReputacionHistorica(),
                        "HABILIDAD", h.getNombre(), h.getDescripcionHabilidad(), h.getPrecioCreditos()
                )));
            }
            // Mapear necesidades (Demanda)
            if (u.getNecesidades() != null) {
                u.getNecesidades().forEach(n -> muro.add(new PublicacionDTO(
                        u.getIdUsuario(), u.getNombre(), u.getReputacionHistorica(),
                        "NECESIDAD", n.getNombre(), n.getDescripcionNecesidad(), 0
                )));
            }
        }
        return muro;
    }

    public void filtrarPublicaciones(){

    }

}
