package es.ucab.entrenos.modulos.gamificacion.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.gamificacion.modelos.LogroDesbloqueado;
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
public class RepositorioLogroDesbloqueado implements IRepositorioLogroDesbloqueado {

    private static final String RUTA_ARCHIVO = "data/logros_desbloqueados.json";
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioLogroDesbloqueado() {
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
            throw new RuntimeException("Error al inicializar logros_desbloqueados.json", e);
        }
    }

    @Override
    public List<LogroDesbloqueado> obtenerTodos() {
        lock.readLock().lock();
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<LogroDesbloqueado>>() {}.getType();
            List<LogroDesbloqueado> items = gson.fromJson(reader, tipoLista);
            return items != null ? items : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer logros_desbloqueados.json", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<LogroDesbloqueado> obtenerPorId(String id) {
        return obtenerTodos().stream()
                .filter(ld -> ld.getId().equalsIgnoreCase(id))
                .findFirst();
    }

    @Override
    public List<LogroDesbloqueado> obtenerPorUsuario(String idUsuario) {
        return obtenerTodos().stream()
                .filter(ld -> ld.getIdUsuario().equals(idUsuario))
                .collect(Collectors.toList());
    }

    @Override
    public void guardar(LogroDesbloqueado logroDesbloqueado) {
        lock.writeLock().lock();
        try {
            List<LogroDesbloqueado> todos = obtenerTodos();
            todos.add(logroDesbloqueado);
            escribirArchivo(todos);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void guardarTodas(List<LogroDesbloqueado> logrosDesbloqueados) {
        lock.writeLock().lock();
        try {
            escribirArchivo(logrosDesbloqueados);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void escribirArchivo(List<LogroDesbloqueado> items) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(items, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en logros_desbloqueados.json", e);
        }
    }
}
