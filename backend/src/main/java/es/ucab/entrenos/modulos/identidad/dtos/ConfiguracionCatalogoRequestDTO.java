package es.ucab.entrenos.modulos.identidad.dtos;

import java.util.List;

public class ConfiguracionCatalogoRequestDTO {

    // Clases internas para simplificar los DTOs del catálogo
    public static class OfertaDTO {
        private String idHabilidadCategoria;
        private int precioCreditos;
        private String descripcionServicio;

        // Getters y Setters...
        public String getIdHabilidadCategoria() { return idHabilidadCategoria; }
        public void setIdHabilidadCategoria(String id) { this.idHabilidadCategoria = id; }
        public int getPrecioCreditos() { return precioCreditos; }
        public void setPrecioCreditos(int precio) { this.precioCreditos = precio; }
        public String getDescripcionServicio() { return descripcionServicio; }
        public void setDescripcionServicio(String desc) { this.descripcionServicio = desc; }
    }

    public static class NecesidadDTO {
        private String idHabilidadCategoria;
        private String descripcionCondiciones;

        // Getters y Setters...
        public String getIdHabilidadCategoria() { return idHabilidadCategoria; }
        public void setIdHabilidadCategoria(String id) { this.idHabilidadCategoria = id; }
        public String getDescripcionCondiciones() { return descripcionCondiciones; }
        public void setDescripcionCondiciones(String desc) { this.descripcionCondiciones = desc; }
    }

    private List<OfertaDTO> ofertas;
    private List<NecesidadDTO> necesidades;

    public List<OfertaDTO> getOfertas() { return ofertas; }
    public void setOfertas(List<OfertaDTO> ofertas) { this.ofertas = ofertas; }

    public List<NecesidadDTO> getNecesidades() { return necesidades; }
    public void setNecesidades(List<NecesidadDTO> necesidades) { this.necesidades = necesidades; }
}