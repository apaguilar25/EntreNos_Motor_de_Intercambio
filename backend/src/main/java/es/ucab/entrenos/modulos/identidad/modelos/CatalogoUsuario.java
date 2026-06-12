package es.ucab.entrenos.modulos.identidad.modelos;

import java.util.ArrayList;

public class CatalogoUsuario {

    private boolean completado;
    private ArrayList<HabilidadOfrecida> habilidadesOfrecidas;
    private ArrayList<NecesidadRegistrada> necesidadesRegistradas;

    public CatalogoUsuario() {
        this.completado = false;
        this.habilidadesOfrecidas = new ArrayList<>();
        this.necesidadesRegistradas = new ArrayList<>();
    }

    // --- MÉTODOS DE NEGOCIO ENCAPSULADOS ---

    public void marcarComoCompletado() {
        if (this.completado) {
            throw new IllegalStateException("El catálogo ya fue completado anteriormente.");
        }
        this.completado = true;
    }

    public void agregarHabilidadOfrecida(HabilidadOfrecida nuevaHabilidad) {
        if (nuevaHabilidad == null) throw new IllegalArgumentException("La habilidad no puede ser nula.");

        boolean duplicada = this.habilidadesOfrecidas.stream()
                .anyMatch(h -> h.getHabilidadBase().getId().equals(nuevaHabilidad.getHabilidadBase().getId())
                        && h.getDescripcionServicio().equalsIgnoreCase(nuevaHabilidad.getDescripcionServicio()));

        if (duplicada) {
            throw new IllegalStateException("Ya tienes una oferta para esta habilidad con la misma descripción.");
        }
        this.habilidadesOfrecidas.add(nuevaHabilidad);
    }

    public void actualizarHabilidadOfrecida(String idInstancia, int nuevoPrecio, String nuevaDescripcion) {
        HabilidadOfrecida existente = this.habilidadesOfrecidas.stream()
                .filter(h -> h.getIdInstancia().equals(idInstancia))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la oferta específica en el catálogo."));

        if (nuevaDescripcion != null && !nuevaDescripcion.trim().isEmpty() && !nuevaDescripcion.equals(existente.getDescripcionServicio())) {
            boolean conflicto = this.habilidadesOfrecidas.stream()
                    .anyMatch(h -> !h.getIdInstancia().equals(idInstancia)
                            && h.getHabilidadBase().getId().equals(existente.getHabilidadBase().getId())
                            && h.getDescripcionServicio().equalsIgnoreCase(nuevaDescripcion.trim()));

            if (conflicto) {
                throw new IllegalStateException("Ya tienes otra oferta de esta misma categoría con esa descripción.");
            }
            existente.setDescripcionServicio(nuevaDescripcion.trim());
        }

        if (nuevoPrecio > 0) {
            existente.setPrecioCreditos(nuevoPrecio);
        }
    }

    public void agregarNecesidad(NecesidadRegistrada nuevaNecesidad) {
        if (nuevaNecesidad == null) throw new IllegalArgumentException("La necesidad no puede estar vacía.");

        boolean duplicada = this.necesidadesRegistradas.stream()
                .anyMatch(n -> n.getNecesidadBase().getId().equals(nuevaNecesidad.getNecesidadBase().getId())
                        && n.getDescripcionCondiciones().equalsIgnoreCase(nuevaNecesidad.getDescripcionCondiciones()));

        if (duplicada) {
            throw new IllegalStateException("Error: Ya tienes esta necesidad registrada con esas mismas condiciones.");
        }
        this.necesidadesRegistradas.add(nuevaNecesidad);
    }

    public void actualizarNecesidadRegistrada(String idInstancia, String nuevaDescripcion) {
        NecesidadRegistrada existente = this.necesidadesRegistradas.stream()
                .filter(n -> n.getIdInstancia().equals(idInstancia))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la necesidad específica en el catálogo."));

        if (nuevaDescripcion != null && !nuevaDescripcion.trim().isEmpty() && !nuevaDescripcion.equals(existente.getDescripcionCondiciones())) {
            boolean conflicto = this.necesidadesRegistradas.stream()
                    .anyMatch(n -> !n.getIdInstancia().equals(idInstancia)
                            && n.getNecesidadBase().getId().equals(existente.getNecesidadBase().getId())
                            && n.getDescripcionCondiciones().equalsIgnoreCase(nuevaDescripcion.trim()));

            if (conflicto) {
                throw new IllegalStateException("Ya tienes otra necesidad de esta misma categoría con esas condiciones exactas.");
            }
            existente.setDescripcionCondiciones(nuevaDescripcion.trim());
        }
    }

    public void eliminarHabilidadOfrecida(String idInstancia) {
        boolean eliminado = this.habilidadesOfrecidas.removeIf(h -> h.getIdInstancia().equals(idInstancia));
        if (!eliminado) throw new IllegalArgumentException("No se encontró la oferta específica para eliminar.");
    }

    public void eliminarNecesidadRegistrada(String idInstancia) {
        boolean eliminado = this.necesidadesRegistradas.removeIf(n -> n.getIdInstancia().equals(idInstancia));
        if (!eliminado) throw new IllegalArgumentException("No se encontró la necesidad específica para eliminar.");
    }

    // Getters Inmutables
    public boolean isCompletado() { return completado; }
    public ArrayList<HabilidadOfrecida> getHabilidadesOfrecidas() { return new ArrayList<>(this.habilidadesOfrecidas); }
    public ArrayList<NecesidadRegistrada> getNecesidadesRegistradas() { return new ArrayList<>(this.necesidadesRegistradas); }
}