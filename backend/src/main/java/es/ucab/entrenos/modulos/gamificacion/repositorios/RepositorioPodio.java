package es.ucab.entrenos.modulos.gamificacion.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import es.ucab.entrenos.modulos.gamificacion.modelos.PodioSemanal;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RepositorioPodio implements IRepositorioPodio {

    private static final String RUTA_ARCHIVO = "data/podio_semanal.json";
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioPodio() {
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
                    writer.write("null");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar podio_semanal.json", e);
        }
    }

    @Override
    public Optional<PodioSemanal> obtenerActual() {
        lock.readLock().lock();
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            PodioSemanal podio = gson.fromJson(reader, PodioSemanal.class);
            return Optional.ofNullable(podio);
        } catch (IOException e) {
            throw new RuntimeException("Error al leer podio_semanal.json", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void guardar(PodioSemanal podio) {
        lock.writeLock().lock();
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(podio, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en podio_semanal.json", e);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
