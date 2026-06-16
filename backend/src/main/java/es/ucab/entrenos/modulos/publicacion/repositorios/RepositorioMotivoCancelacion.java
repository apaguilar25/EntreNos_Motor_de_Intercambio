package es.ucab.entrenos.modulos.publicacion.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.publicacion.modelos.MotivoCancelacion;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RepositorioMotivoCancelacion implements IRepositorioMotivoCancelacion {

    private static final String RUTA_ARCHIVO = "data/motivos_cancelacion.json";
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioMotivoCancelacion() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        inicializarArchivo();
    }

    private void inicializarArchivo() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
                archivo.getParentFile().mkdirs();
            }
            if (!archivo.exists()) {
                archivo.createNewFile();
                List<MotivoCancelacion> semilla = new ArrayList<>();
                semilla.add(new MotivoCancelacion("MC-001", "Me equivoqué al enviar la solicitud / oferta."));
                semilla.add(new MotivoCancelacion("MC-002", "Ya no necesito este servicio / Ya resolví mi necesidad."));
                semilla.add(new MotivoCancelacion("MC-003", "Ya no tengo la habilidad, el tiempo o los materiales disponibles."));
                semilla.add(new MotivoCancelacion("MC-004", "La otra persona no responde a mis mensajes."));
                semilla.add(new MotivoCancelacion("MC-005", "No pudimos llegar a un acuerdo en el horario o los detalles."));
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
                    gson.toJson(semilla, writer);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar motivos_cancelacion.json", e);
        }
    }

    @Override
    public List<MotivoCancelacion> obtenerTodos() {
        lock.readLock().lock();
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<MotivoCancelacion>>() {}.getType();
            List<MotivoCancelacion> motivos = gson.fromJson(reader, tipoLista);
            return motivos != null ? motivos : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer motivos_cancelacion.json", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<MotivoCancelacion> obtenerPorId(String id) {
        return obtenerTodos().stream()
                .filter(m -> m.getId().equalsIgnoreCase(id))
                .findFirst();
    }
}
