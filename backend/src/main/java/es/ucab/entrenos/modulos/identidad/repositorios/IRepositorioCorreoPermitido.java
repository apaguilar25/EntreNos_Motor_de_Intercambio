package es.ucab.entrenos.modulos.identidad.repositorios;

import es.ucab.entrenos.modulos.identidad.modelos.CorreoPermitido;

import java.util.List;
import java.util.Optional;

public interface IRepositorioCorreoPermitido {
    List<CorreoPermitido> obtenerTodos();
    Optional<CorreoPermitido> obtenerPorCorreo(String correo);
    void guardar(CorreoPermitido correoPermitido);
    void eliminar(String correo);
}
