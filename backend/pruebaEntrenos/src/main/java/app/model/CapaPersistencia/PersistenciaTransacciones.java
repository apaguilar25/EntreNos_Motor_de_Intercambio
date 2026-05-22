package app.model.CapaPersistencia;

import app.model.CapaEntidades.Transaccion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PersistenciaTransacciones {

    // Misma ruta corregida de tu entorno
    private final String RUTA_ARCHIVO = "backend/pruebaEntrenos/src/main/java/app/model/data/transacciones.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void guardar(List<Transaccion> transacciones) {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, transacciones);
        } catch (IOException e) {
            System.err.println("Error al guardar transacciones: " + e.getMessage());
        }
    }

    public List<Transaccion> cargar() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists() || archivo.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(archivo, new TypeReference<List<Transaccion>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer transacciones.json: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}