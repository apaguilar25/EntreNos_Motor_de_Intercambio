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
import java.util.Optional;

@Repository
public class RepositorioHabilidad implements IRepositorioHabilidad {

    private static final String RUTA_ARCHIVO = "data/habilidades.json";
    private final Gson gson;

    public RepositorioHabilidad() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        inicializarArchivo(); // <-- ¡Aquí ocurre la magia automáticamente al encender la app!
    }

    private void inicializarArchivo() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            // Creamos la carpeta "data" si no existe
            if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
                archivo.getParentFile().mkdirs();
            }
            // Si el archivo JSON no existe, lo creamos y lo llenamos con el catálogo base
            if (!archivo.exists()) {
                archivo.createNewFile();

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

    // --- NUEVOS MÉTODOS DE LA INTERFAZ ---

    @Override
    public synchronized List<Habilidad> listarTodas() {
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Habilidad>>() {}.getType();
            List<Habilidad> habilidades = gson.fromJson(reader, tipoLista);
            return habilidades != null ? habilidades : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el catálogo de habilidades.", e);
        }
    }

    @Override
    public Optional<Habilidad> buscarPorId(String id) {
        // Usamos Streams para buscar rápidamente la habilidad en la lista
        return listarTodas().stream()
                .filter(h -> h.getId().equals(id))
                .findFirst();
    }

    @Override
    public synchronized void guardar(Habilidad habilidadGuardar) {
        List<Habilidad> habilidades = listarTodas();
        boolean existe = false;

        // Buscamos si la habilidad ya existe para actualizarla
        for (int i = 0; i < habilidades.size(); i++) {
            if (habilidades.get(i).getId().equals(habilidadGuardar.getId())) {
                habilidades.set(i, habilidadGuardar); // Reemplazamos la vieja por la nueva
                existe = true;
                break;
            }
        }

        // Si no existía en el JSON, significa que es una creación nueva
        if (!existe) {
            habilidades.add(habilidadGuardar);
        }

        // Guardamos la lista completa de nuevo en el archivo
        guardarTodas(habilidades);
    }

    // --- MÉTODO PRIVADO DE ESCRITURA ---

    private synchronized void guardarTodas(List<Habilidad> habilidades) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(habilidades, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error al escribir el catálogo de habilidades.", e);
        }
    }
}