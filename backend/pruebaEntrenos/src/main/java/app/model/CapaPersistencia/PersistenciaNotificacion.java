package app.model.CapaPersistencia;

import app.model.CapaEntidades.Notificacion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PersistenciaNotificacion {

    private final String RUTA_ARCHIVO = "src/main/java/app/model/data/notificaciones.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void guardar(List<Notificacion> notificaciones) {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, notificaciones);
        } catch (IOException e) {
            System.err.println("Error al guardar notificaciones: " + e.getMessage());
        }
    }

    public List<Notificacion> cargar() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists() || archivo.length() == 0) return new ArrayList<>();

        try {
            return objectMapper.readValue(archivo, new TypeReference<List<Notificacion>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer notificaciones: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}