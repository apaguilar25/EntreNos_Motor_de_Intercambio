package es.ucab.entrenos.modulos.identidad.modelos;

public class Monedero {

    private float creditosDisponibles;
    private float creditosRetenidos;
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

    public void retener(float montoCreditos) {
        if (montoCreditos <= 0) {
            throw new IllegalArgumentException("El monto a retener debe ser positivo.");
        }
        if (getSaldoDisponible() < montoCreditos) {
            throw new IllegalStateException("Saldo disponible insuficiente para retener. Disponible: "
                    + getSaldoDisponible() + ", solicitado: " + montoCreditos);
        }
        this.creditosRetenidos += montoCreditos;
    }

    public void liberarRetencion() {
        this.creditosDisponibles -= this.creditosRetenidos;
        this.creditosRetenidos = 0;
    }

    public void devolverRetencion() {
        this.creditosRetenidos = 0;
    }

    public float getSaldoDisponible() {
        return this.creditosDisponibles - this.creditosRetenidos;
    }

    public float getCreditosDisponibles() {
        return creditosDisponibles;
    }

    public float getCreditosRetenidos() {
        return creditosRetenidos;
    }
    public float getCreditosPorLogros() {
        return creditosPorLogros;
    }
}
