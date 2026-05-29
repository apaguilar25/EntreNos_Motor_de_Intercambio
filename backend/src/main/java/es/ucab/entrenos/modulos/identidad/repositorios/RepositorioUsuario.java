package es.ucab.entrenos.modulos.identidad.repositorios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

@Repository // ¡ESTO ES CLAVE PARA SPRING BOOT!
public class RepositorioUsuario implements IRepositorioUsuario {

    // TODO abrir usuarios.json con GSON

    @Override
    public void guardar(Usuario usuario) {
        // Lógica de GSON para leer, añadir/modificar el usuario en la lista y reescribir el JSON
    }

    @Override
    public ArrayList<Usuario> listarUsuarios() {
        return null;
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        // Lógica para leer la lista de GSON y buscar el correo.
        // Si lo encuentra: return Optional.of(usuarioEncontrado);
        // Si no existe: return Optional.empty();
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> buscarPorId(String id) {
        return Optional.empty();
    }

    // ... implementación del resto de métodos ...
}