package es.ucab.entrenos.modulos.publicacion.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.identidad.excepciones.ConcurrenciaException;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RepositorioTransaccion implements IRepositorioTransaccion {

    private static final String RUTA_ARCHIVO = "data/transacciones.json";
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioTransaccion() {
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
            throw new RuntimeException("Error al inicializar transacciones.json", e);
        }
    }

    @Override
    public List<Transaccion> obtenerTodas() {
        lock.readLock().lock();
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Transaccion>>() {}.getType();
            List<Transaccion> transacciones = gson.fromJson(reader, tipoLista);
            return transacciones != null ? transacciones : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer transacciones.json", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Transaccion> obtenerPorId(String idTransaccion) {
        return obtenerTodas().stream()
                .filter(t -> t.getIdTransaccion().equalsIgnoreCase(idTransaccion))
                .findFirst();
    }

    @Override
    public void guardar(Transaccion transaccion) {
        lock.writeLock().lock();
        try {
            List<Transaccion> todas = obtenerTodas();
            boolean existe = false;
            for (int i = 0; i < todas.size(); i++) {
                Transaccion tBD = todas.get(i);
                if (tBD.getIdTransaccion().equalsIgnoreCase(transaccion.getIdTransaccion())) {
                    if (tBD.getVersion() != transaccion.getVersion()) {
                        throw new ConcurrenciaException(
                                "Error de concurrencia: La transacción " + transaccion.getIdTransaccion() +
                                        " fue modificada por otra transacción. Por favor, recargue e intente de nuevo."
                        );
                    }
                    transaccion.setVersion(transaccion.getVersion() + 1);
                    todas.set(i, transaccion);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                transaccion.setVersion(0);
                todas.add(transaccion);
            }
            escribirArchivo(todas);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void guardarTodas(List<Transaccion> transacciones) {
        lock.writeLock().lock();
        try {
            escribirArchivo(transacciones);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void escribirArchivo(List<Transaccion> transacciones) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(transacciones, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en transacciones.json", e);
        }
    }
}
