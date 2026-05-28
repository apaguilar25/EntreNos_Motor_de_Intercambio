package es.ucab.entrenos.modulos.publicacion.repositorios;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;
import org.springframework.stereotype.Repository;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Repository
public class RepositorioTransaccion implements IRepositorioTransaccion {
    private final File archivo = new File("data/transacciones.json");
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public List<Transaccion> obtenerTodas() {
        if (!archivo.exists() || archivo.length() == 0) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(archivo, new TypeReference<List<Transaccion>>() {});
        } catch (IOException e) {
            System.err.println("Error al leer transacciones.json: " + e.getMessage());
            return new ArrayList<>();
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
        List<Transaccion> todas = obtenerTodas();
        todas.removeIf(t -> t.getIdTransaccion().equalsIgnoreCase(transaccion.getIdTransaccion()));
        todas.add(transaccion);
        guardarTodas(todas);
    }
    @Override
    public void guardarTodas(List<Transaccion> transacciones) {
        try {
            archivo.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(archivo, transacciones);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir en transacciones.json", e);
        }
    }
}
