package app.model.CapaPersistencia;

import app.model.CapaEntidades.SolicitudIntercambio;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PersistenciaSolicitud {

    private final String RUTA_ARCHIVO = "backend/pruebaEntrenos/src/main/java/app/model/data/solicitudes.json";

    // Configuración clave para ignorar campos intrusos
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public void guardar(List<SolicitudIntercambio> solicitudes) {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, solicitudes);
        } catch (IOException e) {
            System.err.println("Error al guardar solicitudes: " + e.getMessage());
        }
    }

    public List<SolicitudIntercambio> cargar() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists() || archivo.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(archivo, new TypeReference<List<SolicitudIntercambio>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error cargando solicitudes", e);
        }
    }
}