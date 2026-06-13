package es.ucab.entrenos.modulos.notificacion.repositorios;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ucab.entrenos.modulos.notificacion.modelos.Notificacion;
import org.springframework.stereotype.Repository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

@Repository
public class RepositorioNotificacion implements IRepositorioNotificacion {
    private final File archivo = new File("data/notificaciones.json");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public List<Notificacion> obtenerTodas() {
        lock.readLock().lock();
        try {
            if (!archivo.exists() || archivo.length() == 0) return new ArrayList<>();
            return objectMapper.readValue(archivo, new TypeReference<List<Notificacion>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer notificaciones.json: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Notificacion> obtenerPorId(String idNotificacion) {
        return obtenerTodas().stream()
                .filter(n -> n.getIdNotificacion().equals(idNotificacion))
                .findFirst();
    }

    @Override
    public List<Notificacion> obtenerPorDestinatario(String idDestinatario) {
        return obtenerTodas().stream()
                .filter(n -> n.getIdDestinatario().equals(idDestinatario))
                .collect(Collectors.toList());
    }

    @Override
    public List<Notificacion> obtenerNoLeidas(String idDestinatario) {
        return obtenerTodas().stream()
                .filter(n -> n.getIdDestinatario().equals(idDestinatario) && !n.isEstadoLectura())
                .collect(Collectors.toList());
    }

    @Override
    public void guardar(Notificacion notificacion) {
        lock.writeLock().lock();
        try {
            List<Notificacion> todas = leerArchivo();
            todas.add(notificacion);
            guardarTodas(todas);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void guardarTodas(List<Notificacion> notificaciones) {
        try {
            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, notificaciones);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en notificaciones.json", e);
        }
    }

    @Override
    public void marcarComoLeida(String idNotificacion) {
        lock.writeLock().lock();
        try {
            List<Notificacion> todas = leerArchivo();
            for (Notificacion n : todas) {
                if (n.getIdNotificacion().equals(idNotificacion)) {
                    n.setEstadoLectura(true);
                    break;
                }
            }
            guardarTodas(todas);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private List<Notificacion> leerArchivo() {
        if (!archivo.exists() || archivo.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(archivo, new TypeReference<List<Notificacion>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer notificaciones.json: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
