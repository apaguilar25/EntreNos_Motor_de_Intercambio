package app.model.CapaPersistencia;

import app.model.CapaEntidades.Usuario;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository // Esto soluciona tu error de @Autowired
public class PersistenciaUsuario {

    // Ruta basada en tu árbol de directorios de la primera captura
// Agregamos la ruta del módulo backend antes del src
    private final String RUTA_ARCHIVO = "backend/pruebaEntrenos/src/main/java/app/model/data/usuarios.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void guardar(List<Usuario> usuarios) {
        try {
            File archivo = new File(RUTA_ARCHIVO);

            System.out.println("[DEBUG] Guardando datos en: " + archivo.getAbsolutePath());


            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, usuarios);
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios: " + e.getMessage());
        }
    }

    // Cambiamos List<Object> por List<Usuario> para solucionar el error del Muro
    public List<Usuario> cargar() {
        File archivo = new File(RUTA_ARCHIVO);


        System.out.println("[DEBUG] Buscando archivo JSON en: " + archivo.getAbsolutePath());


        // Si el archivo no existe o está vacío, devolvemos una lista vacía para evitar NullPointerException
        if (!archivo.exists() || archivo.length() == 0) {
            return new ArrayList<>();
        }

        try {
            // Jackson lee el JSON y lo transforma automáticamente en objetos Usuario
            return objectMapper.readValue(archivo, new TypeReference<List<Usuario>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer usuarios.json: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}