package es.ucab.entrenos.modulos.gamificacion.modelos;

import java.util.UUID;

public class PodioSemanal {

    private String idPodio;
    private long fechaInicioSemana;
    private long fechaFinSemana;
    private long fechaCalculo;

    private String proveedorEliteId;
    private String proveedorEliteNombre;
    private int proveedorEliteServicios;

    private String motorEconomiaId;
    private String motorEconomiaNombre;
    private int motorEconomiaTransacciones;

    private String embajadorCalidadId;
    private String embajadorCalidadNombre;
    private double embajadorCalidadPromedio;

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

    public String getProveedorEliteId() { return proveedorEliteId; }
    public void setProveedorEliteId(String proveedorEliteId) { this.proveedorEliteId = proveedorEliteId; }

    public String getProveedorEliteNombre() { return proveedorEliteNombre; }
    public void setProveedorEliteNombre(String proveedorEliteNombre) { this.proveedorEliteNombre = proveedorEliteNombre; }

    public int getProveedorEliteServicios() { return proveedorEliteServicios; }
    public void setProveedorEliteServicios(int proveedorEliteServicios) { this.proveedorEliteServicios = proveedorEliteServicios; }

    public String getMotorEconomiaId() { return motorEconomiaId; }
    public void setMotorEconomiaId(String motorEconomiaId) { this.motorEconomiaId = motorEconomiaId; }

    public String getMotorEconomiaNombre() { return motorEconomiaNombre; }
    public void setMotorEconomiaNombre(String motorEconomiaNombre) { this.motorEconomiaNombre = motorEconomiaNombre; }

    public int getMotorEconomiaTransacciones() { return motorEconomiaTransacciones; }
    public void setMotorEconomiaTransacciones(int motorEconomiaTransacciones) { this.motorEconomiaTransacciones = motorEconomiaTransacciones; }

    public String getEmbajadorCalidadId() { return embajadorCalidadId; }
    public void setEmbajadorCalidadId(String embajadorCalidadId) { this.embajadorCalidadId = embajadorCalidadId; }

    public String getEmbajadorCalidadNombre() { return embajadorCalidadNombre; }
    public void setEmbajadorCalidadNombre(String embajadorCalidadNombre) { this.embajadorCalidadNombre = embajadorCalidadNombre; }

    public double getEmbajadorCalidadPromedio() { return embajadorCalidadPromedio; }
    public void setEmbajadorCalidadPromedio(double embajadorCalidadPromedio) { this.embajadorCalidadPromedio = embajadorCalidadPromedio; }
}
