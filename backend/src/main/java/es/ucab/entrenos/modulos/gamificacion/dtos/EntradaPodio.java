package es.ucab.entrenos.modulos.gamificacion.dtos;

public class EntradaPodio {
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
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
}
