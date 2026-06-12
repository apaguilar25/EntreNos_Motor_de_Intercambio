package es.ucab.entrenos.modulos.identidad.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.identidad.modelos.RolUsuario;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class RepositorioUsuario implements IRepositorioUsuario {

    // Ruta del archivo local en la raíz del proyecto o una carpeta de datos
    private static final String RUTA_ARCHIVO = "data/usuarios.json";
    private final Gson gson;

    public RepositorioUsuario() {
        // GsonBuilder con setPrettyPrinting hace que el archivo JSON sea legible para humanos
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        inicializarArchivo();
    }

    //  Asegura que la carpeta y el archivo JSON existan antes de cualquier operación.
    private void inicializarArchivo() {
        try {
            File archivo = new File(RUTA_ARCHIVO);
            if (archivo.getParentFile() != null && !archivo.getParentFile().exists()) {
                archivo.getParentFile().mkdirs();
            }
            if (!archivo.exists()) {
                archivo.createNewFile();

                // --- INYECCIÓN DEL SÚPER ADMINISTRADOR (USANDO CONSTRUCTOR) ---
                ArrayList<Usuario> seed = new ArrayList<>();

                // El objeto nace en un estado 100% válido y con su monedero/listas inicializadas
                Usuario admin = new Usuario(
                        java.util.UUID.randomUUID().toString(),
                        "Super Administrador",
                        "admin@alameda.com",
                        "0000000000",
                        "Administrador general de la comunidad",
                        new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("admin123")
                );

                // Le asignamos su rol de altos privilegios
                admin.setRol(es.ucab.entrenos.modulos.identidad.modelos.RolUsuario.ADMINISTRADOR);

                seed.add(admin);
                escribirArchivo(seed); // Lo guardamos en el JSON
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al crear el archivo de base de datos de usuarios.", e);
        }
    }




    // Lee tod.o el archivo JSON y lo convierte en una lista de usuarios en RAM.
    @Override
    public synchronized ArrayList<Usuario> listarUsuarios() {
        try (Reader reader = new InputStreamReader(new FileInputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            Type tipoLista = new TypeToken<ArrayList<Usuario>>() {}.getType();
            ArrayList<Usuario> usuarios = gson.fromJson(reader, tipoLista);

            // Si el archivo está vacío por alguna razón, retornamos una lista vacía en lugar de null
            return usuarios != null ? usuarios : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error al leer el catálogo de usuarios desde el almacenamiento.", e);
        }
    }

    //  Guarda o actualiza un usuario en el archivo JSON.
    //  Si el ID ya existe, sobreescribe los datos (Modificación). Si no, lo añade (Registro).
    @Override
    public synchronized void guardar(Usuario usuarioGuardar) {
        // 1. Salvaguardas y validaciones defensivas
        if (usuarioGuardar == null) {
            throw new IllegalArgumentException("El usuario a guardar no puede ser nulo.");
        }
        if (usuarioGuardar.getId() == null || usuarioGuardar.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("No se puede guardar un usuario sin un identificador (ID) válido.");
        }
        if (usuarioGuardar.getMonedero() == null) {
            throw new IllegalArgumentException("Error de consistencia: El usuario debe poseer un Monedero inicializado.");
        }

        ArrayList<Usuario> usuarios = listarUsuarios();
        boolean existe = false;

        for (int i = 0; i < usuarios.size(); i++) {
            Usuario uBD = usuarios.get(i);
            if (uBD.getId().equals(usuarioGuardar.getId())) {

                // LÓGICA DE BLOQUEO OPTIMISTA
                if (uBD.getVersion() != usuarioGuardar.getVersion()) {
                    throw new es.ucab.entrenos.modulos.identidad.excepciones.ConcurrenciaException(
                            "Error de concurrencia: El usuario " + usuarioGuardar.getCorreoElectronico() +
                                    " fue modificado por otra transacción. Por favor, recargue e intente de nuevo."
                    );
                }

                // Si la versión coincide, incrementamos el sello de versión
                usuarioGuardar.setVersion(usuarioGuardar.getVersion() + 1);
                usuarios.set(i, usuarioGuardar);
                existe = true;
                break;
            }
        }

        if (!existe) {
            // Registro nuevo: Nace con versión cero
            usuarioGuardar.setVersion(0);
            usuarios.add(usuarioGuardar);
        }

        // Persistencia física en el archivo JSON
        escribirArchivo(usuarios);
    }

    /**
     * Persiste de forma segura la lista completa de usuarios en el archivo físico JSON.
     * Utiliza exclusión mutua (synchronized) para evitar colisiones de escritura a nivel de hilos.
     * @param usuariosActivos Lista de usuarios actualizados a guardar.
     */
    private synchronized void escribirArchivo(ArrayList<Usuario> usuariosActivos) {
        if (usuariosActivos == null) {
            return;
        }

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {

            // Serializa la lista con formato estético (Pretty Printing)
            this.gson.toJson(usuariosActivos, writer);

        } catch (IOException e) {
            // Se lanza una excepción en tiempo de ejecución para notificar el fallo crítico de infraestructura
            throw new RuntimeException("Error crítico de infraestructura: No se pudo escribir en el almacenamiento local JSON (" + RUTA_ARCHIVO + ").", e);
        }
    }

    @Override
    public synchronized Optional<Usuario> buscarPorId(String id) {
        if (id == null || id.trim().isEmpty()) return Optional.empty();

        ArrayList<Usuario> usuarios = listarUsuarios();
        return usuarios.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    public synchronized Optional<Usuario> buscarPorTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) return Optional.empty();

        ArrayList<Usuario> usuarios = listarUsuarios();

        return usuarios.stream()
                .filter(u -> u.getTelefono().equals(telefono.trim()))
                .findFirst();
    }


    @Override
    public synchronized Optional<Usuario> buscarPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) return Optional.empty();

        ArrayList<Usuario> usuarios = listarUsuarios();

        // Buscamos ignorando mayúsculas/minúsculas para evitar problemas de login habituales
        return usuarios.stream()
                .filter(u -> u.getCorreoElectronico().equalsIgnoreCase(correo.trim()))
                .findFirst();
    }

}