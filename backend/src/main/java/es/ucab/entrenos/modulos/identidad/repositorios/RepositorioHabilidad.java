package es.ucab.entrenos.modulos.identidad.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioHabilidad {

    private static final String RUTA_ARCHIVO = "data/habilidades.json";
    private final Gson gson;

    public RepositorioHabilidad() {
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
                // Si no existe, creamos la lista por defecto y la guardamos
                List<Habilidad> iniciales = new ArrayList<>();
                iniciales.add(new Habilidad("HAB-001", "Plomería"));
                iniciales.add(new Habilidad("HAB-002", "Electricidad"));
                iniciales.add(new Habilidad("HAB-003", "Carpintería"));
                iniciales.add(new Habilidad("HAB-004", "Limpieza del Hogar"));
                iniciales.add(new Habilidad("HAB-005", "Soporte Técnico / Computación"));

                guardarTodas(iniciales);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al inicializar el catálogo de habilidades JSON.", e);
        }
    }

    public synchronized List<Habilidad> listarTodas() {
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Habilidad>>() {}.getType();
            List<Habilidad> habilidades = gson.fromJson(reader, tipoLista);
            return habilidades != null ? habilidades : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el catálogo de habilidades.", e);
        }
    }

    private synchronized void guardarTodas(List<Habilidad> habilidades) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(habilidades, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir el catálogo de habilidades.", e);
        }
    }
}