package app.model.CapaPersistencia;

import app.model.CapaEntidades.Subasta;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PersistenciaSubasta {

    private final String rutaArchivo = "backend/pruebaEntrenos/src/main/java/app/model/data/subasta.json";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<Subasta> cargar() {
        File archivo = new File(rutaArchivo);
        if (!archivo.exists() || archivo.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(archivo, new TypeReference<List<Subasta>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Error cargando subastas", e);
        }
    }

    public void guardar(List<Subasta> datos) {
        try {
            File archivo = new File(rutaArchivo);
            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, datos);
        } catch (IOException e) {
            System.err.println("Error al guardar subastas: " + e.getMessage());
        }
    }
}