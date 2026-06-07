package es.ucab.entrenos.modulos.identidad.repositorios;

import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface IRepositorioHabilidad {

    List<Habilidad> listarTodas();
    Optional<Habilidad> buscarPorId(String id);
    void guardar(Habilidad habilidad);
}

