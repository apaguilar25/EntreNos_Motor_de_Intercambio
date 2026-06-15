package es.ucab.entrenos.modulos.publicacion.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.publicacion.modelos.Incidencia;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RepositorioIncidencia implements IRepositorioIncidencia {

    private static final String RUTA_ARCHIVO = "data/incidencias.json";
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioIncidencia() {
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
            throw new RuntimeException("Error al inicializar incidencias.json", e);
        }
    }

    @Override
    public List<Incidencia> obtenerTodas() {
        lock.readLock().lock();
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Incidencia>>() {}.getType();
            List<Incidencia> incidencias = gson.fromJson(reader, tipoLista);
            return incidencias != null ? incidencias : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer incidencias.json", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Incidencia> obtenerPorId(String idIncidencia) {
        return obtenerTodas().stream()
                .filter(i -> i.getIdIncidencia().equalsIgnoreCase(idIncidencia))
                .findFirst();
    }

    @Override
    public void guardar(Incidencia incidencia) {
        lock.writeLock().lock();
        try {
            List<Incidencia> todas = obtenerTodas();
            boolean existe = false;
            for (int i = 0; i < todas.size(); i++) {
                if (todas.get(i).getIdIncidencia().equalsIgnoreCase(incidencia.getIdIncidencia())) {
                    todas.set(i, incidencia);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                todas.add(incidencia);
            }
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
                gson.toJson(todas, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en incidencias.json", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
