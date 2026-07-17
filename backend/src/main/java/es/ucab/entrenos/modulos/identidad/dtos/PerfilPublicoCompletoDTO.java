package es.ucab.entrenos.modulos.identidad.dtos;

import es.ucab.entrenos.modulos.subasta.dtos.SubastaResumenDTO;
import es.ucab.entrenos.modulos.gamificacion.dtos.LogroDesbloqueadoResponseDTO;
import java.util.List;

public class PerfilPublicoCompletoDTO {

    private PerfilPublicoDTO datosPersonales;
    private List<SubastaResumenDTO> subastas;
    private List<LogroDesbloqueadoResponseDTO> logros;
    private boolean enPodioSemanal;

    public PerfilPublicoCompletoDTO(
            PerfilPublicoDTO datosPersonales,
            List<SubastaResumenDTO> subastas,
            List<LogroDesbloqueadoResponseDTO> logros,
            boolean enPodioSemanal) {

        this.datosPersonales = datosPersonales;
        this.subastas = subastas;
        this.logros = logros;
        this.enPodioSemanal = enPodioSemanal;
    }

    // Getters
    public PerfilPublicoDTO getDatosPersonales() { return datosPersonales; }
    public List<SubastaResumenDTO> getSubastas() { return subastas; }
    public List<LogroDesbloqueadoResponseDTO> getLogros() { return logros; }
    public boolean isEnPodioSemanal() { return enPodioSemanal; }

    // Setters
    public void setDatosPersonales(PerfilPublicoDTO datosPersonales) { this.datosPersonales = datosPersonales; }
    public void setSubastas(List<SubastaResumenDTO> subastas) { this.subastas = subastas; }
    public void setLogros(List<LogroDesbloqueadoResponseDTO> logros) { this.logros = logros; }
    public void setEnPodioSemanal(boolean enPodioSemanal) { this.enPodioSemanal = enPodioSemanal; }
}