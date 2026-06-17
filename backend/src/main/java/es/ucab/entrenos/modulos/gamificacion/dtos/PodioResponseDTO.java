package es.ucab.entrenos.modulos.gamificacion.dtos;

import es.ucab.entrenos.modulos.gamificacion.dtos.EntradaPodio;

import java.util.List;

public class PodioResponseDTO {

    private String idPodio;
    private long fechaInicioSemana;
    private long fechaFinSemana;
    private long fechaCalculo;

    private List<EntradaPodio> proveedorElite;
    private List<EntradaPodio> motorEconomia;
    private List<EntradaPodio> embajadorCalidad;

    public PodioResponseDTO() {}

    public String getIdPodio() { return idPodio; }
    public long getFechaInicioSemana() { return fechaInicioSemana; }
    public long getFechaFinSemana() { return fechaFinSemana; }
    public long getFechaCalculo() { return fechaCalculo; }
    public List<EntradaPodio> getProveedorElite() { return proveedorElite; }
    public List<EntradaPodio> getMotorEconomia() { return motorEconomia; }
    public List<EntradaPodio> getEmbajadorCalidad() { return embajadorCalidad; }

    public void setIdPodio(String idPodio) { this.idPodio = idPodio; }
    public void setFechaInicioSemana(long fechaInicioSemana) { this.fechaInicioSemana = fechaInicioSemana; }
    public void setFechaFinSemana(long fechaFinSemana) { this.fechaFinSemana = fechaFinSemana; }
    public void setFechaCalculo(long fechaCalculo) { this.fechaCalculo = fechaCalculo; }
    public void setProveedorElite(List<EntradaPodio> proveedorElite) { this.proveedorElite = proveedorElite; }
    public void setMotorEconomia(List<EntradaPodio> motorEconomia) { this.motorEconomia = motorEconomia; }
    public void setEmbajadorCalidad(List<EntradaPodio> embajadorCalidad) { this.embajadorCalidad = embajadorCalidad; }
}
