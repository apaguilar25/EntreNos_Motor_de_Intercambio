package es.ucab.entrenos.modulos.identidad.dtos;

import es.ucab.entrenos.modulos.subasta.dtos.SubastaResumenDTO;
import es.ucab.entrenos.modulos.gamificacion.dtos.LogroDesbloqueadoResponseDTO;
import java.util.List;

public class PerfilPublicoCompletoDTO {

    private PerfilPublicoDTO datosPersonales;
    private List<SubastaResumenDTO> subastas;
    private List<LogroDesbloqueadoResponseDTO> logros;
    private boolean enPodioSemanal;
    private boolean esAdministrador; // Bandera para el frontend

    private List<HabilidadOfrecidaResumenDTO> habilidadesOfrecidas;
    private List<NecesidadResumenDTO> habilidadesNecesitadas;

    public PerfilPublicoCompletoDTO(
            PerfilPublicoDTO datosPersonales,
            boolean esAdministrador,
            List<SubastaResumenDTO> subastas,
            List<LogroDesbloqueadoResponseDTO> logros,
            boolean enPodioSemanal,
            List<HabilidadOfrecidaResumenDTO> habilidadesOfrecidas,
            List<NecesidadResumenDTO> habilidadesNecesitadas) {

        this.esAdministrador = esAdministrador;
        this.datosPersonales = datosPersonales;
        this.subastas = subastas;
        this.logros = logros;
        this.enPodioSemanal = enPodioSemanal;
        this.habilidadesOfrecidas = habilidadesOfrecidas;
        this.habilidadesNecesitadas = habilidadesNecesitadas;
    }

    // Getters
    public PerfilPublicoDTO getDatosPersonales() { return datosPersonales; }
    public List<SubastaResumenDTO> getSubastas() { return subastas; }
    public List<LogroDesbloqueadoResponseDTO> getLogros() { return logros; }
    public boolean isEnPodioSemanal() { return enPodioSemanal; }
    public boolean isAdministrador() {return esAdministrador;}
    public List<HabilidadOfrecidaResumenDTO> getHabilidadesOfrecidas() {return habilidadesOfrecidas;}
    public List<NecesidadResumenDTO> getHabilidadesNecesitadas() {return habilidadesNecesitadas;}

    // Setters
    public void setDatosPersonales(PerfilPublicoDTO datosPersonales) { this.datosPersonales = datosPersonales; }
    public void setSubastas(List<SubastaResumenDTO> subastas) { this.subastas = subastas; }
    public void setLogros(List<LogroDesbloqueadoResponseDTO> logros) { this.logros = logros; }
    public void setEnPodioSemanal(boolean enPodioSemanal) { this.enPodioSemanal = enPodioSemanal; }



}


