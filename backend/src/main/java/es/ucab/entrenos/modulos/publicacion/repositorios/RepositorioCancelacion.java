package es.ucab.entrenos.modulos.publicacion.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.identidad.excepciones.ConcurrenciaException;
import es.ucab.entrenos.modulos.publicacion.modelos.Cancelacion;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RepositorioCancelacion implements IRepositorioCancelacion {

    private static final String RUTA_ARCHIVO = "data/cancelaciones.json";
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioCancelacion() {
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
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
                    writer.write("[]");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar cancelaciones.json", e);
        }
    }

    @Override
    public List<Cancelacion> obtenerTodas() {
        lock.readLock().lock();
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Cancelacion>>() {}.getType();
            List<Cancelacion> cancelaciones = gson.fromJson(reader, tipoLista);
            return cancelaciones != null ? cancelaciones : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer cancelaciones.json", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Cancelacion> obtenerPorId(String idCancelacion) {
        return obtenerTodas().stream()
                .filter(c -> c.getIdCancelacion().equalsIgnoreCase(idCancelacion))
                .findFirst();
    }

    @Override
    public void guardar(Cancelacion cancelacion) {
        lock.writeLock().lock();
        try {
            List<Cancelacion> todas = obtenerTodas();
            boolean existe = false;
            for (int i = 0; i < todas.size(); i++) {
                Cancelacion cBD = todas.get(i);
                if (cBD.getIdCancelacion().equalsIgnoreCase(cancelacion.getIdCancelacion())) {
                    if (cBD.getVersion() != cancelacion.getVersion()) {
                        throw new ConcurrenciaException(
                                "Error de concurrencia: La cancelacion " + cancelacion.getIdCancelacion() +
                                        " fue modificada por otra transacción. Por favor, recargue e intente de nuevo."
                        );
                    }
                    cancelacion.setVersion(cancelacion.getVersion() + 1);
                    todas.set(i, cancelacion);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                cancelacion.setVersion(0);
                todas.add(cancelacion);
            }
            escribirArchivo(todas);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void guardarTodas(List<Cancelacion> cancelaciones) {
        lock.writeLock().lock();
        try {
            escribirArchivo(cancelaciones);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void escribirArchivo(List<Cancelacion> cancelaciones) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(cancelaciones, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en cancelaciones.json", e);
        }
    }
}
