package es.ucab.entrenos.modulos.reputacion.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.identidad.excepciones.ConcurrenciaException;
import es.ucab.entrenos.modulos.reputacion.modelos.Resena;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Repository
public class RepositorioReputacion implements IRepositorioReputacion {

    private static final String RUTA_ARCHIVO = "data/resenas.json";
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioReputacion() {
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
            throw new RuntimeException("Error al inicializar resenas.json", e);
        }
    }

    @Override
    public List<Resena> obtenerTodas() {
        lock.readLock().lock();
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Resena>>() {}.getType();
            List<Resena> resenas = gson.fromJson(reader, tipoLista);
            return resenas != null ? resenas : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer resenas.json", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Resena> obtenerPorId(String idResena) {
        return obtenerTodas().stream()
                .filter(r -> r.getIdResena().equalsIgnoreCase(idResena))
                .findFirst();
    }

    @Override
    public List<Resena> obtenerPorReceptor(String idUsuario) {
        return obtenerTodas().stream()
                .filter(r -> r.getIdReceptor().equalsIgnoreCase(idUsuario))
                .collect(Collectors.toList());
    }

    @Override
    public List<Resena> obtenerPorEmisor(String idUsuario) {
        return obtenerTodas().stream()
                .filter(r -> r.getIdEmisor().equalsIgnoreCase(idUsuario))
                .collect(Collectors.toList());
    }

    @Override
    public List<Resena> obtenerPorTransaccion(String idTransaccion) {
        return obtenerTodas().stream()
                .filter(r -> r.getIdTransaccion().equalsIgnoreCase(idTransaccion))
                .collect(Collectors.toList());
    }

    @Override
    public void guardar(Resena resena) {
        lock.writeLock().lock();
        try {
            List<Resena> todas = obtenerTodas();
            boolean existe = false;
            for (int i = 0; i < todas.size(); i++) {
                Resena rBD = todas.get(i);
                if (rBD.getIdResena().equalsIgnoreCase(resena.getIdResena())) {
                    if (rBD.getVersion() != resena.getVersion()) {
                        throw new ConcurrenciaException(
                                "Error de concurrencia: La reseña " + resena.getIdResena() +
                                        " fue modificada por otra transacción. Por favor, recargue e intente de nuevo."
                        );
                    }
                    resena.setVersion(resena.getVersion() + 1);
                    todas.set(i, resena);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                resena.setVersion(0);
                todas.add(resena);
            }
            guardarTodas(todas);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void guardarTodas(List<Resena> resenas) {
        lock.writeLock().lock();
        try {
            escribirArchivo(resenas);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void escribirArchivo(List<Resena> resenas) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(resenas, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en resenas.json", e);
        }
    }
}
