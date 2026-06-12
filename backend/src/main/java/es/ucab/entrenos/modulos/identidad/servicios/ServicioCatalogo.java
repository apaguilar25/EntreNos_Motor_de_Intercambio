package es.ucab.entrenos.modulos.identidad.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.*;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioUsuario;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ServicioCatalogo {

    private final IRepositorioUsuario repositorioUsuario;
    private static final float CAPITAL_SEMILLA_INICIAL = 50.0f;

    public ServicioCatalogo(IRepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    public void configurarCatalogoInicial(String idUsuario, List<HabilidadOfrecida> ofertas, List<NecesidadRegistrada> necesidades) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        // Interactuamos ÚNICAMENTE con la raíz del agregado (Usuario)
        for (HabilidadOfrecida oferta : ofertas) {
            usuario.agregarHabilidadOfrecida(oferta);
        }
        for (NecesidadRegistrada necesidad : necesidades) {
            usuario.agregarNecesidad(necesidad);
        }

        usuario.finalizarConfiguracionCatalogo(CAPITAL_SEMILLA_INICIAL);
        repositorioUsuario.guardar(usuario);
    }

    public void agregarHabilidadIndividual(String idUsuario, Habilidad habilidadBase, int precioCreditos, String descripcionServicio) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        // Cero código repetido de validación de Enums aquí. El objeto Usuario se defiende solo.
        usuario.agregarHabilidadOfrecida(new HabilidadOfrecida(habilidadBase, precioCreditos, descripcionServicio));
        repositorioUsuario.guardar(usuario);
    }

    public void editarHabilidadOfrecida(String idUsuario, String idInstancia, int precioCreditos, String descripcionServicio) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        usuario.actualizarHabilidadOfrecida(idInstancia, precioCreditos, descripcionServicio);
        repositorioUsuario.guardar(usuario);
    }

    public void eliminarHabilidadOfrecida(String idUsuario, String idInstancia) {
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        usuario.eliminarHabilidadOfrecida(idInstancia);
        repositorioUsuario.guardar(usuario);
    }
}