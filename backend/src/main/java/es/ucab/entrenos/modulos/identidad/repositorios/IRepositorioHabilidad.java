package es.ucab.entrenos.modulos.identidad.repositorios;

import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.modelos.Usuario;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public interface IRepositorioHabilidad {

    public List<Habilidad> listarTodas();
}
