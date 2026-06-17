package es.ucab.entrenos.modulos.gamificacion.modelos;

import es.ucab.entrenos.modulos.gamificacion.dtos.EntradaPodio;
import java.util.List;
import java.util.UUID;

public class PodioSemanal {

    private String idPodio;
    private long fechaInicioSemana;
    private long fechaFinSemana;
    private long fechaCalculo;

    private List<EntradaPodio> proveedorElite;
    private List<EntradaPodio> motorEconomia;
    private List<EntradaPodio> embajadorCalidad;

    public PodioSemanal() {}

    public PodioSemanal(long fechaInicioSemana, long fechaFinSemana) {
        this.idPodio = "PD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.fechaInicioSemana = fechaInicioSemana;
        this.fechaFinSemana = fechaFinSemana;
        this.fechaCalculo = System.currentTimeMillis();
    }

    public String getIdPodio() { return idPodio; }
    public void setIdPodio(String idPodio) { this.idPodio = idPodio; }

    public long getFechaInicioSemana() { return fechaInicioSemana; }
    public void setFechaInicioSemana(long fechaInicioSemana) { this.fechaInicioSemana = fechaInicioSemana; }

    public long getFechaFinSemana() { return fechaFinSemana; }
    public void setFechaFinSemana(long fechaFinSemana) { this.fechaFinSemana = fechaFinSemana; }

    public long getFechaCalculo() { return fechaCalculo; }
    public void setFechaCalculo(long fechaCalculo) { this.fechaCalculo = fechaCalculo; }

    public List<EntradaPodio> getProveedorElite() { return proveedorElite; }
    public void setProveedorElite(List<EntradaPodio> proveedorElite) { this.proveedorElite = proveedorElite; }

    public List<EntradaPodio> getMotorEconomia() { return motorEconomia; }
    public void setMotorEconomia(List<EntradaPodio> motorEconomia) { this.motorEconomia = motorEconomia; }

    public List<EntradaPodio> getEmbajadorCalidad() { return embajadorCalidad; }
    public void setEmbajadorCalidad(List<EntradaPodio> embajadorCalidad) { this.embajadorCalidad = embajadorCalidad; }
}
