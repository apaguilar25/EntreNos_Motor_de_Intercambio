package es.ucab.entrenos.modulos.gamificacion.dtos;

public class PodioResponseDTO {

    private String idPodio;
    private long fechaInicioSemana;
    private long fechaFinSemana;
    private long fechaCalculo;

    private EntradaPodio proveedorElite;
    private EntradaPodio motorEconomia;
    private EntradaPodio embajadorCalidad;

    public PodioResponseDTO() {}

    public static class EntradaPodio {
        private String idUsuario;
        private String nombreUsuario;
        private double valor;

        public EntradaPodio() {}

        public EntradaPodio(String idUsuario, String nombreUsuario, double valor) {
            this.idUsuario = idUsuario;
            this.nombreUsuario = nombreUsuario;
            this.valor = valor;
        }

        public String getIdUsuario() { return idUsuario; }
        public String getNombreUsuario() { return nombreUsuario; }
        public double getValor() { return valor; }

        public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }
        public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
        public void setValor(double valor) { this.valor = valor; }
    }

    public String getIdPodio() { return idPodio; }
    public long getFechaInicioSemana() { return fechaInicioSemana; }
    public long getFechaFinSemana() { return fechaFinSemana; }
    public long getFechaCalculo() { return fechaCalculo; }
    public EntradaPodio getProveedorElite() { return proveedorElite; }
    public EntradaPodio getMotorEconomia() { return motorEconomia; }
    public EntradaPodio getEmbajadorCalidad() { return embajadorCalidad; }

    public void setIdPodio(String idPodio) { this.idPodio = idPodio; }
    public void setFechaInicioSemana(long fechaInicioSemana) { this.fechaInicioSemana = fechaInicioSemana; }
    public void setFechaFinSemana(long fechaFinSemana) { this.fechaFinSemana = fechaFinSemana; }
    public void setFechaCalculo(long fechaCalculo) { this.fechaCalculo = fechaCalculo; }
    public void setProveedorElite(EntradaPodio proveedorElite) { this.proveedorElite = proveedorElite; }
    public void setMotorEconomia(EntradaPodio motorEconomia) { this.motorEconomia = motorEconomia; }
    public void setEmbajadorCalidad(EntradaPodio embajadorCalidad) { this.embajadorCalidad = embajadorCalidad; }
}
