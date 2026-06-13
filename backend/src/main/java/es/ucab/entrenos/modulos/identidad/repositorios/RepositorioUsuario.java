package es.ucab.entrenos.modulos.identidad.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.identidad.excepciones.ConcurrenciaException;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class RepositorioUsuario implements IRepositorioUsuario {

    // Ruta del archivo local en la raíz del proyecto o una carpeta de datos
    private static final String RUTA_ARCHIVO = "data/usuarios.json";
    private final Gson gson;

    // --- MAGIA ARQUITECTÓNICA: Caché y Candados Concurrentes ---
    private final ConcurrentHashMap<String, Usuario> cacheUsuarios = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioUsuario() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        inicializarArchivoYCache();
    }

    /**
     * Asegura que el archivo JSON exista. Si no existe, crea al Super Admin.
     * Luego, carga todo el archivo a la Memoria RAM (Caché) para búsquedas ultrarrápidas.
     */
    private void inicializarArchivoYCache() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
                archivo.getParentFile().mkdirs();
            }
            if (!archivo.exists()) {
                archivo.createNewFile();

                // INYECCIÓN DEL SÚPER ADMINISTRADOR
                ArrayList<Usuario> seed = new ArrayList<>();
                Usuario admin = new Usuario(
                        java.util.UUID.randomUUID().toString(),
                        "Super Administrador",
                        "admin@alameda.com",
                        "0000000000",
                        "Administrador general de la comunidad",
                        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin123")
                );

                admin.setRol(es.ucab.entrenos.modulos.identidad.modelos.RolUsuario.ADMINISTRADOR);
                admin.setVersion(0);
                seed.add(admin);

                // Guardamos directamente en el disco duro porque el caché aún no se ha levantado
                try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
                    this.gson.toJson(seed, writer);
                }
            }

            // CARGA A LA CACHÉ (Indexación en Memoria)
            cargarCacheDesdeArchivo();

        } catch (IOException e) {
            throw new RuntimeException("Error crítico al inicializar la persistencia JSON de Usuarios.", e);
        }
    }

    /**
     * Lee el disco duro UNA SOLA VEZ y vuelca los usuarios en el ConcurrentHashMap.
     */
    private void cargarCacheDesdeArchivo() {
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Usuario>>() {}.getType();
            ArrayList<Usuario> usuarios = gson.fromJson(reader, tipoLista);

            if (usuarios != null) {
                for (Usuario u : usuarios) {
                    cacheUsuarios.put(u.getId(), u);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el catálogo de usuarios hacia la caché.", e);
        }
    }

    /**
     * Lista todos los usuarios.
     * RÁPIDO: Lee directamente de la memoria RAM, no del disco.
     */
    @Override
    public ArrayList<Usuario> listarUsuarios() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(cacheUsuarios.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Guarda o actualiza un usuario aplicando Bloqueo Optimista, Exclusión Mutua y Prevención Check-Then-Act.
     */
    @Override
    public void guardar(Usuario usuarioGuardar) {
        if (usuarioGuardar == null || usuarioGuardar.getId() == null || usuarioGuardar.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Usuario inválido para guardar.");
        }

        // Bloqueo de escritura: Nadie más puede leer ni escribir mientras actualizamos la caché y el disco
        lock.writeLock().lock();
        try {
            Usuario uBD = cacheUsuarios.get(usuarioGuardar.getId());

            if (uBD != null) {
                // LÓGICA DE BLOQUEO OPTIMISTA: Verificamos contra la memoria (O(1) de complejidad)
                if (usuarioGuardar.getVersion() <= uBD.getVersion()) {
                    throw new ConcurrenciaException(
                            "Error de concurrencia: El usuario " + usuarioGuardar.getCorreoElectronico() +
                                    " fue modificado por otra transacción. Por favor, recargue e intente de nuevo."
                    );
                }
            } else {
                // 1. Si es un usuario nuevo (Registro), forzamos su versión inicial a 0 por seguridad
                if (usuarioGuardar.getVersion() > 0) {
                    throw new IllegalStateException("Un usuario nuevo no puede nacer con una versión superior a 0.");
                }

                // 2. LÓGICA CHECK-THEN-ACT: Validamos unicidad DENTRO del candado de escritura
                boolean correoYaTomado = cacheUsuarios.values().stream()
                        .anyMatch(u -> u.getCorreoElectronico().equalsIgnoreCase(usuarioGuardar.getCorreoElectronico()));

                boolean telefonoYaTomado = cacheUsuarios.values().stream()
                        .anyMatch(u -> u.getTelefono().equals(usuarioGuardar.getTelefono()));

                if (correoYaTomado) {
                    throw new IllegalStateException("Condición de carrera evitada: El correo electrónico ya fue registrado durante el procesamiento.");
                }
                if (telefonoYaTomado) {
                    throw new IllegalStateException("Condición de carrera evitada: El teléfono ya fue registrado durante el procesamiento.");
                }
            }

            // 3. Actualizamos la memoria ultrarrápida
            cacheUsuarios.put(usuarioGuardar.getId(), usuarioGuardar);

            // 4. Volcamos la memoria completa al disco duro de forma segura
            escribirArchivoFisico();

        } finally {
            // SIEMPRE liberar el candado, incluso si ocurre una excepción
            lock.writeLock().unlock();
        }
    }

    /**
     * Persistencia física: Convierte el mapa de caché a JSON y lo guarda.
     */
    private void escribirArchivoFisico() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            // Guardamos los valores del HashMap como una lista JSON
            this.gson.toJson(cacheUsuarios.values(), writer);
        } catch (IOException e) {
            throw new RuntimeException("Error crítico de infraestructura: No se pudo escribir en el JSON local.", e);
        }
    }

    /**
     * Búsquedas Ultrarrápidas: Complejidad O(1) leyendo directo de RAM.
     * Múltiples hilos pueden hacer esto al mismo tiempo gracias al readLock.
     */
    @Override
    public Optional<Usuario> buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();

        lock.readLock().lock();
        try {
            return Optional.ofNullable(cacheUsuarios.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Usuario> buscarPorTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) return Optional.empty();

        lock.readLock().lock();
        try {
            return cacheUsuarios.values().stream()
                    .filter(u -> u.getTelefono().equals(telefono.trim()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) return Optional.empty();

        lock.readLock().lock();
        try {
            return cacheUsuarios.values().stream()
                    .filter(u -> u.getCorreoElectronico().equalsIgnoreCase(correo.trim()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }
}