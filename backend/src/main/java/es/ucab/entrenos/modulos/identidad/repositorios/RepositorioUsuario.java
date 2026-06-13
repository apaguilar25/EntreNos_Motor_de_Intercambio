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

    private static final String RUTA_ARCHIVO = "data/usuarios.json";
    private final Gson gson;

    private final ConcurrentHashMap<String, Usuario> cacheUsuarios = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public RepositorioUsuario() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        inicializarArchivoYCache();
    }

    private void inicializarArchivoYCache() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
                archivo.getParentFile().mkdirs();
            }
            if (!archivo.exists()) {
                archivo.createNewFile();

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

                try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
                    this.gson.toJson(seed, writer);
                }
            }

            cargarCacheDesdeArchivo();

        } catch (IOException e) {
            throw new RuntimeException("Error crítico al inicializar la persistencia JSON de Usuarios.", e);
        }
    }

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
     * MÁS MAGIA ARQUITECTÓNICA: Clonación profunda.
     * Simula el comportamiento de una Base de Datos real devolviendo copias de los registros,
     * evitando que las referencias en RAM sean mutadas accidentalmente fuera de los candados.
     */
    private Usuario clonar(Usuario u) {
        if (u == null) return null;
        // Convertimos el objeto a JSON y lo volvemos a leer para crear una instancia totalmente nueva
        return this.gson.fromJson(this.gson.toJson(u), Usuario.class);
    }

    @Override
    public ArrayList<Usuario> listarUsuarios() {
        lock.readLock().lock();
        try {
            ArrayList<Usuario> copiaSegura = new ArrayList<>();
            for (Usuario u : cacheUsuarios.values()) {
                copiaSegura.add(clonar(u)); // Devolvemos clones
            }
            return copiaSegura;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void guardar(Usuario usuarioGuardar) {
        if (usuarioGuardar == null || usuarioGuardar.getId() == null || usuarioGuardar.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Usuario inválido para guardar.");
        }

        lock.writeLock().lock();
        try {
            Usuario uBD = cacheUsuarios.get(usuarioGuardar.getId());

            if (uBD != null) {
                // LÓGICA DE BLOQUEO OPTIMISTA
                if (usuarioGuardar.getVersion() <= uBD.getVersion()) {
                    throw new ConcurrenciaException(
                            "Error de concurrencia: El usuario " + usuarioGuardar.getCorreoElectronico() +
                                    " fue modificado por otra transacción. Por favor, recargue e intente de nuevo."
                    );
                }
            } else {
                if (usuarioGuardar.getVersion() > 0) {
                    throw new IllegalStateException("Un usuario nuevo no puede nacer con una versión superior a 0.");
                }

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

            // Guardamos un CLON en el caché para proteger la referencia original de futuras modificaciones externas
            cacheUsuarios.put(usuarioGuardar.getId(), clonar(usuarioGuardar));
            escribirArchivoFisico();

        } finally {
            lock.writeLock().unlock();
        }
    }

    private void escribirArchivoFisico() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            this.gson.toJson(cacheUsuarios.values(), writer);
        } catch (IOException e) {
            throw new RuntimeException("Error crítico de infraestructura: No se pudo escribir en el JSON local.", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();

        lock.readLock().lock();
        try {
            return Optional.ofNullable(clonar(cacheUsuarios.get(id)));
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
                    .map(this::clonar) // Entregamos un clon
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
                    .map(this::clonar) // Entregamos un clon
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }
}