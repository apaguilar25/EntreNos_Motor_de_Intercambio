package app.model.CapaPersistencia;

import app.model.CapaEntidades.SolicitudIntercambio;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PersistenciaSolicitud {

    private final String RUTA_ARCHIVO = "src/main/java/app/model/data/transacciones.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<SolicitudIntercambio> cargar() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists() || archivo.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(archivo, new TypeReference<List<SolicitudIntercambio>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error cargando solicitudes", e);
        }
    }

    public void guardar(List<SolicitudIntercambio> datos) {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, datos);
        } catch (IOException e) {
            throw new RuntimeException("Error guardando solicitudes", e);
        }
    }
}