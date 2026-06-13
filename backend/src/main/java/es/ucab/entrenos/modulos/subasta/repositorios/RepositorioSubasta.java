package es.ucab.entrenos.modulos.subasta.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.identidad.excepciones.ConcurrenciaException;
import es.ucab.entrenos.modulos.subasta.modelos.Subasta;
import es.ucab.entrenos.modulos.utility.LocalDateTimeAdapter;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RepositorioSubasta implements IRepositorioSubasta {

    // Ruta del archivo local
    private static final String RUTA_ARCHIVO = "data/subastas.json";
    private final Gson gson;

    // --- MAGIA ARQUITECTÓNICA: Caché y Candados Concurrentes ---
    private final ConcurrentHashMap<String, Subasta> cacheSubastas = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioSubasta() {
        // Configuramos Gson inyectando el adaptador de fechas que ya tenías creado
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    /**
     * BUENA PRÁCTICA (Spring Boot Lifecycle):
     * Se ejecuta DESPUÉS de que Spring instancia la clase, garantizando que todo el entorno es seguro.
     */
    @PostConstruct
    private void inicializarArchivoYCache() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
                archivo.getParentFile().mkdirs();
            }
            if (!archivo.exists()) {
                archivo.createNewFile();
                // Si el archivo es nuevo, lo iniciamos como un array vacío "[]"
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
                    writer.write("[]");
                }
            }

            // Cargar el disco duro a la Memoria RAM (Caché)
            cargarCacheDesdeArchivo();

        } catch (IOException e) {
            throw new RuntimeException("Error crítico al inicializar la persistencia JSON de Subastas.", e);
        }
    }

    private void cargarCacheDesdeArchivo() {
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Subasta>>() {}.getType();
            ArrayList<Subasta> subastas = gson.fromJson(reader, tipoLista);

            if (subastas != null) {
                for (Subasta s : subastas) {
                    cacheSubastas.put(s.getId(), s);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el catálogo de subastas hacia la caché.", e);
        }
    }

    /**
     * CLONACIÓN PROFUNDA: Simula una Base de Datos real devolviendo copias.
     * Esto evita modificar la referencia original en la memoria RAM accidentalmente (La Trampa de Memoria).
     */
    private Subasta clonar(Subasta s) {
        if (s == null) return null;
        return this.gson.fromJson(this.gson.toJson(s), Subasta.class);
    }

    @Override
    public List<Subasta> listarTodas() {
        lock.readLock().lock();
        try {
            List<Subasta> copiaSegura = new ArrayList<>();
            for (Subasta s : cacheSubastas.values()) {
                copiaSegura.add(clonar(s)); // Entregamos solo clones
            }
            return copiaSegura;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Subasta> buscarPorId(String idSubasta) {
        if (idSubasta == null || idSubasta.trim().isEmpty()) return Optional.empty();

        lock.readLock().lock();
        try {
            return Optional.ofNullable(clonar(cacheSubastas.get(idSubasta)));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void guardar(Subasta subastaGuardar) {
        if (subastaGuardar == null || subastaGuardar.getId() == null || subastaGuardar.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Subasta inválida para guardar.");
        }

        // Bloqueo de escritura: Máxima exclusión mutua
        lock.writeLock().lock();
        try {
            Subasta subastaBD = cacheSubastas.get(subastaGuardar.getId());

            if (subastaBD != null) {
                // LÓGICA DE BLOQUEO OPTIMISTA (Contra Condiciones de Carrera)
                if (subastaGuardar.getVersion() <= subastaBD.getVersion()) {
                    throw new ConcurrenciaException(
                            "Error de concurrencia: La subasta '" + subastaGuardar.getNombreActivo() +
                                    "' acaba de recibir una puja o modificacion. Por favor, recargue e intente de nuevo."
                    );
                }
            } else {
                // Si la subasta es totalmente nueva, forzamos su versión a 0
                if (subastaGuardar.getVersion() > 0) {
                    throw new IllegalStateException("Una subasta nueva no puede nacer con una versión superior a 0.");
                }
            }

            // Actualizamos la caché protegiéndola con un CLON profundo
            cacheSubastas.put(subastaGuardar.getId(), clonar(subastaGuardar));

            // Volcamos la memoria RAM al archivo físico (.json)
            escribirArchivoFisico();

        } finally {
            // SIEMPRE soltamos el candado
            lock.writeLock().unlock();
        }
    }

    private void escribirArchivoFisico() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            this.gson.toJson(cacheSubastas.values(), writer);
        } catch (IOException e) {
            throw new RuntimeException("Error crítico de infraestructura: No se pudo escribir en el JSON local de subastas.", e);
        }
    }
}