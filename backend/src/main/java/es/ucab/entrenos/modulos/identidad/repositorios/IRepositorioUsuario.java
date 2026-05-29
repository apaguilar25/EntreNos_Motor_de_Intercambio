package es.ucab.entrenos.modulos.identidad.repositorios;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import java.util.ArrayList;
import java.util.Optional;


public interface IRepositorioUsuario {

    Optional<Usuario> buscarPorCorreo(String correo);
    Optional<Usuario> buscarPorId(String id);

    void guardar(Usuario usuario);
    ArrayList<Usuario> listarUsuarios();

}
