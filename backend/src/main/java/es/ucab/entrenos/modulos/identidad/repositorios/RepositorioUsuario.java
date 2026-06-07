package es.ucab.entrenos.modulos.identidad.repositorios;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
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
                archivo.getParentFile().mkdirs(); // Crea la carpeta 'data' si no existe
            }
            if (!archivo.exists()) {
                archivo.createNewFile();
                // Inicializamos con un arreglo vacío [] para que GSON no explote al leerlo por primera vez
                try (FileWriter writer = new FileWriter(archivo, StandardCharsets.UTF_8)) {
                    writer.write("[]");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error crítico: No se pudo inicializar el almacenamiento JSON de usuarios.", e);
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
    public synchronized void guardar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("No se puede guardar un usuario nulo.");
        }

        // 1. Cargar la lista actual de usuarios en memoria
        ArrayList<Usuario> usuariosActivos = listarUsuarios();

        // 2. Buscar si el usuario ya existe en el archivo usando su ID
        int indiceExistente = -1;
        for (int i = 0; i < usuariosActivos.size(); i++) {
            if (usuariosActivos.get(i).getId().equals(usuario.getId())) {
                indiceExistente = i;
                break;
            }
        }

        // 3. Si ya existía (es una edición), lo reemplazamos. Si no, lo añadimos (es nuevo)
        if (indiceExistente != -1) {
            usuariosActivos.set(indiceExistente, usuario);
        } else {
            usuariosActivos.add(usuario);
        }

        // 4. Reescribir completamente el archivo JSON con la lista actualizada
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(RUTA_ARCHIVO), StandardCharsets.UTF_8)) {
            gson.toJson(usuariosActivos, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error crítico al escribir en el almacenamiento local JSON.", e);
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
