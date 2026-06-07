package es.ucab.entrenos.modulos.identidad.servicios;

import es.ucab.entrenos.modulos.identidad.modelos.Habilidad;
import es.ucab.entrenos.modulos.identidad.repositorios.IRepositorioHabilidad;
import es.ucab.entrenos.modulos.utility.UtilidadTexto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServicioHabilidad {

    private final IRepositorioHabilidad repositorioHabilidad;

    public ServicioHabilidad(IRepositorioHabilidad repositorioHabilidad) {
        this.repositorioHabilidad = repositorioHabilidad;
    }

    public List<Habilidad> obtenerTodas() {
        return repositorioHabilidad.listarTodas();
    }

    public Optional<Habilidad> buscarPorId(String id) {
        return repositorioHabilidad.buscarPorId(id);
    }

    public Habilidad crearHabilidad(String categoria) {
        if (categoria == null || categoria.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }

        // Limpiamos de espacios dobles ("Limpieza   Hogar" -> "Limpieza Hogar")
        String categoriaLimpia = UtilidadTexto.limpiarEspacios(categoria);

        // Usamos nuestra súper utilidad para la comparación
        boolean existe = repositorioHabilidad.listarTodas().stream()
                .filter(h -> h != null && h.getCategoria() != null) // <-- ¡El escudo protector!
                .anyMatch(h -> UtilidadTexto.sonIgualesEstrictos(h.getCategoria(), categoriaLimpia));
        if (existe) {
            throw new IllegalStateException("Error: Ya existe una habilidad en el catálogo maestro con la categoría '" + categoriaLimpia + "'.");
        }

        String nuevoId = "HAB-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        Habilidad nuevaHabilidad = new Habilidad(nuevoId, categoriaLimpia);
        repositorioHabilidad.guardar(nuevaHabilidad);
        return nuevaHabilidad;
    }

    public void editarHabilidad(String id, String nuevaCategoria) {
        if (nuevaCategoria == null || nuevaCategoria.trim().isEmpty()) {
            throw new IllegalArgumentException("El nuevo nombre de la categoría es obligatorio.");
        }

        String categoriaLimpia = UtilidadTexto.limpiarEspacios(nuevaCategoria);

        Habilidad habilidadActual = repositorioHabilidad.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("La habilidad que intentas editar no existe."));

        // Usamos la utilidad también aquí
        boolean conflicto = repositorioHabilidad.listarTodas().stream()
                .filter(h -> h != null && h.getCategoria() != null) // <-- ¡El escudo protector!
                .anyMatch(h -> !h.getId().equals(id)
                        && UtilidadTexto.sonIgualesEstrictos(h.getCategoria(), categoriaLimpia));
        if (conflicto) {
            throw new IllegalStateException("Error: No puedes renombrar esta habilidad porque ya existe otra con ese nombre.");
        }

        habilidadActual.setCategoria(categoriaLimpia);
        repositorioHabilidad.guardar(habilidadActual);
    }
}