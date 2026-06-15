package es.ucab.entrenos.modulos.publicacion.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.identidad.excepciones.ConcurrenciaException;
import es.ucab.entrenos.modulos.publicacion.modelos.Solicitud;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RepositorioSolicitud implements IRepositorioSolicitud {

    private static final String RUTA_ARCHIVO = "data/solicitudes.json";
    private final Gson gson;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioSolicitud() {
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
            throw new RuntimeException("Error al inicializar solicitudes.json", e);
        }
    }

    @Override
    public List<Solicitud> obtenerTodas() {
        lock.readLock().lock();
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Solicitud>>() {}.getType();
            List<Solicitud> solicitudes = gson.fromJson(reader, tipoLista);
            return solicitudes != null ? solicitudes : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer solicitudes.json", e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Solicitud> obtenerPorId(String idSolicitud) {
        return obtenerTodas().stream()
                .filter(s -> s.getIdSolicitud().equalsIgnoreCase(idSolicitud))
                .findFirst();
    }

    @Override
    public void guardar(Solicitud solicitud) {
        lock.writeLock().lock();
        try {
            List<Solicitud> todas = obtenerTodas();
            boolean existe = false;
            for (int i = 0; i < todas.size(); i++) {
                Solicitud sBD = todas.get(i);
                if (sBD.getIdSolicitud().equalsIgnoreCase(solicitud.getIdSolicitud())) {
                    if (sBD.getVersion() != solicitud.getVersion()) {
                        throw new ConcurrenciaException(
                                "Error de concurrencia: La solicitud " + solicitud.getIdSolicitud() +
                                        " fue modificada por otra transacción. Por favor, recargue e intente de nuevo."
                        );
                    }
                    solicitud.setVersion(solicitud.getVersion() + 1);
                    todas.set(i, solicitud);
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                solicitud.setVersion(0);
                todas.add(solicitud);
            }
            escribirArchivo(todas);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void guardarTodas(List<Solicitud> solicitudes) {
        lock.writeLock().lock();
        try {
            escribirArchivo(solicitudes);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void escribirArchivo(List<Solicitud> solicitudes) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(solicitudes, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en solicitudes.json", e);
        }
    }
}
