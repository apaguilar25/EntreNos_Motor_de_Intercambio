package es.ucab.entrenos.modulos.reputacion.repositorios;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ucab.entrenos.modulos.reputacion.modelos.Resena;
import org.springframework.stereotype.Repository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RepositorioReputacion implements IRepositorioReputacion {
    private final File archivo = new File("data/resenas.json");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Resena> obtenerTodas() {
        if (!archivo.exists() || archivo.length() == 0) return new ArrayList<>();
        try {
            return objectMapper.readValue(archivo, new TypeReference<List<Resena>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer resenas.json: " + e.getMessage());
            return new ArrayList<>();
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
        List<Resena> todas = obtenerTodas();
        todas.removeIf(r -> r.getIdResena().equalsIgnoreCase(resena.getIdResena()));
        todas.add(resena);
        guardarTodas(todas);
    }

    @Override
    public void guardarTodas(List<Resena> resenas) {
        try {
            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, resenas);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en resenas.json", e);
        }
    }
}
