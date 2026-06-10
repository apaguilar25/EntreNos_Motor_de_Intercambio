package es.ucab.entrenos.modulos.subasta.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.subasta.modelos.Subasta;
import es.ucab.entrenos.modulos.utility.LocalDateTimeAdapter; // <-- Importamos la nueva utilidad
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RepositorioSubasta implements IRepositorioSubasta {

    private final String RUTA_ARCHIVO = "src/main/resources/subastas.json";
    private final Gson gson;

    public RepositorioSubasta() {
        // Configuramos GSON usando el adaptador externo
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        inicializarArchivo();
    }

    @Override
    public synchronized void guardar(Subasta subasta) {
        List<Subasta> subastas = listarTodas();

        subastas.removeIf(s -> s.getId().equals(subasta.getId()));
        subastas.add(subasta);

        escribirArchivo(subastas);
    }

    @Override
    public Optional<Subasta> buscarPorId(String id) {
        return listarTodas().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Subasta> listarTodas() {
        File archivo = new File(RUTA_ARCHIVO);
        if (!archivo.exists() || archivo.length() == 0) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(archivo)) {
            Type tipoLista = new TypeToken<ArrayList<Subasta>>(){}.getType();
            List<Subasta> subastas = gson.fromJson(reader, tipoLista);
            return subastas != null ? subastas : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Error al leer las subastas desde GSON: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void escribirArchivo(List<Subasta> subastas) {
        try (FileWriter writer = new FileWriter(RUTA_ARCHIVO)) {
            gson.toJson(subastas, writer);
        } catch (IOException e) {
            System.err.println("Error al escribir en el archivo JSON con GSON: " + e.getMessage());
        }
    }

    private void inicializarArchivo() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            File carpeta = archivo.getParentFile();

            if (carpeta != null && !carpeta.exists()) {
                carpeta.mkdirs();
            }
            if (!archivo.exists()) {
                archivo.createNewFile();
                escribirArchivo(new ArrayList<>());
            }
        } catch (IOException e) {
            System.err.println("Error al inicializar el archivo: " + e.getMessage());
        }
    }
}