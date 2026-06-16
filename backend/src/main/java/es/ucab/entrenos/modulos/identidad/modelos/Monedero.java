package es.ucab.entrenos.modulos.identidad.modelos;

public class Monedero {

    private float creditosDisponibles;
    private float creditosComprometidos;
    private float creditosPorLogros;

    public Monedero() {
    }

    public Monedero(float creditosDisponibles) {
        this.creditosDisponibles = creditosDisponibles;
    }

    public void acreditar(float montoCreditos) {
        if (montoCreditos > 0) {
            this.creditosDisponibles += montoCreditos;
        }
    }
    public void acreditarLogro(float montoCreditos){
        if(montoCreditos > 0){
            this.creditosDisponibles += montoCreditos;
            this.creditosPorLogros += montoCreditos;
        }
    }
    public void descontar(float montoCreditos) {
        if (montoCreditos <= 0) {
            throw new IllegalArgumentException("El monto a descontar debe ser positivo.");
        }
        if (getSaldoDisponible() < montoCreditos) {
            throw new IllegalStateException("Saldo disponible insuficiente. Disponible: "
                    + getSaldoDisponible() + ", solicitado: " + montoCreditos);
        }
        this.creditosDisponibles -= montoCreditos;
    }

    public void comprometer(float montoCreditos) {
        if (montoCreditos <= 0) {
            throw new IllegalArgumentException("El monto a comprometer debe ser positivo.");
        }
        if (getSaldoDisponible() < montoCreditos) {
            throw new IllegalStateException("Saldo disponible insuficiente para comprometer. Disponible: "
                    + getSaldoDisponible() + ", solicitado: " + montoCreditos);
        }
        this.creditosComprometidos += montoCreditos;
    }

    public void liberarCompromiso() {
        this.creditosDisponibles -= this.creditosComprometidos;
        this.creditosComprometidos = 0;
    }

    public void devolverCompromiso() {
        this.creditosComprometidos = 0;
    }

    public float getSaldoDisponible() {
        return this.creditosDisponibles - this.creditosComprometidos;
    }

    public float getCreditosDisponibles() {
        return creditosDisponibles;
    }

    public float getCreditosComprometidos() {
        return creditosComprometidos;
    }
    public float getCreditosPorLogros() {
        return creditosPorLogros;
    }
}
