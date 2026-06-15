package es.ucab.entrenos.modulos.publicacion.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.identidad.excepciones.ConcurrenciaException;
import es.ucab.entrenos.modulos.publicacion.modelos.Publicacion;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RepositorioPublicacion implements IRepositorioPublicacion {

    private static final String RUTA_ARCHIVO = "data/publicaciones.json";
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioPublicacion() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
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
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO),
                        StandardCharsets.UTF_8)) {
                    writer.write("[]");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar publicaciones.json", e);
        }
    }

    @Override
    public List<Publicacion> obtenerTodas() {
        lock.readLock().lock();
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Publicacion>>() {}.getType();
            List<Publicacion> publicaciones = gson.fromJson(reader, tipoLista);
            return publicaciones != null ? publicaciones : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer publicaciones.json", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Publicacion> obtenerPorId(String idPublicacion) {
        return obtenerTodas().stream()
                .filter(p -> p.getIdPublicacion().equalsIgnoreCase(idPublicacion))
                .findFirst();
    }

    @Override
    public void guardar(Publicacion publicacion) {
        lock.writeLock().lock();
        try {
            List<Publicacion> todas = obtenerTodas();
            boolean existe = false;
            for (int i = 0; i < todas.size(); i++) {
                Publicacion pBD = todas.get(i);
                if (pBD.getIdPublicacion().equalsIgnoreCase(publicacion.getIdPublicacion())) {
                    if (pBD.getVersion() != publicacion.getVersion()) {
                        throw new ConcurrenciaException(
                                "Error de concurrencia: La publicación " + publicacion.getIdPublicacion() +
                                        " fue modificada por otra transacción. Por favor, recargue e intente de nuevo."
                        );
                    }
                    publicacion.setVersion(publicacion.getVersion() + 1);
                    todas.set(i, publicacion);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                publicacion.setVersion(0);
                todas.add(publicacion);
            }
            escribirArchivo(todas);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void guardarTodas(List<Publicacion> publicaciones) {
        lock.writeLock().lock();
        try {
            escribirArchivo(publicaciones);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void escribirArchivo(List<Publicacion> publicaciones) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(publicaciones, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en publicaciones.json", e);
        }
    }
}

