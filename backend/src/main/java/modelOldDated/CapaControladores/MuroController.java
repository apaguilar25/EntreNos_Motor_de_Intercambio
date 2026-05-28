package modelOldDated.CapaControladores;

import modelOldDated.CapaDTO.PublicacionDTO;
import modelOldDated.CapaEntidades.MuroPublicaciones;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/muro")
@CrossOrigin(origins = "http://localhost:5173") // Permite que React (Vite en puerto 5173) se conecte sin errores de CORS
public class MuroController {

    @Autowired
    private MuroPublicaciones muroPublicaciones;

    // Endpoint para obtener todo el muro o filtrarlo
    // Ejemplo en React: http://localhost:8080/api/muro?tipo=HABILIDAD&servicio=Guitarra
    @GetMapping
    public List<PublicacionDTO> obtenerMuro(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String servicio) {

        if ((tipo != null && !tipo.isEmpty()) || (servicio != null && !servicio.isEmpty())) {
            return muroPublicaciones.filtrarPublicaciones(tipo, servicio);
        }
        return muroPublicaciones.obtenerPublicaciones();
    }
}