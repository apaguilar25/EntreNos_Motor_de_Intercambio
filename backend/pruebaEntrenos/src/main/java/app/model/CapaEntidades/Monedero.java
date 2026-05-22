package app.model.CapaEntidades;

public class Monedero {

    private int creditosDisponibles;
    private int creditosComprometidos;
    private int creditosRetenidos;

    public boolean tieneFondosSuficientes(int costo){
        int restante = creditosDisponibles - costo;

        if (restante < 0) {
            return false;
        }
        return true;
    }

    public void calcularCreditosComprometidos(int monto){
        if (tieneFondosSuficientes(monto)) {
            this.creditosDisponibles -= monto;
            this.creditosComprometidos += monto;
        } else {
            throw new IllegalStateException("Fondos insuficientes para comprometer.");
        }
    }

    public void revertirCreditosComprometidos(int monto){
        if (this.creditosComprometidos >= monto) {
            this.creditosComprometidos -= monto;
            this.creditosDisponibles += monto;
        } else {
            throw new IllegalStateException("El monto a revertir es mayor a los créditos comprometidos.");
        }
    }

    public Monedero() {
        this.creditosDisponibles = 0;
        this.creditosComprometidos = 0;
        this.creditosRetenidos = 0;
    }

    public Monedero(int creditosDisponibles, int creditosComprometidos) {
        this.creditosDisponibles = creditosDisponibles;
        this.creditosComprometidos = creditosComprometidos;
    }

    // Verifica si el usuario tiene suficientes créditos reales para gastar
    public boolean tieneFondosSuficientes(int costo){
        // Simplificado para devolver la evaluación lógica directamente
        return this.creditosDisponibles >= costo;
    }

    // Congela los créditos cuando se envía una solicitud de intercambio
    public void calcularCreditosComprometidos(int monto){
        if (!tieneFondosSuficientes(monto)) {
            throw new IllegalStateException("Operación rechazada: No tienes suficientes créditos disponibles.");
        }

        // Se resta de disponibles y pasan a estar comprometidos
        this.creditosDisponibles -= monto;
        this.creditosComprometidos += monto;
    }

    // Getters y Setters
    public int getCreditosDisponibles() {
        return creditosDisponibles;
    }

    public void setCreditosDisponibles(int creditosDisponibles) {
        this.creditosDisponibles = creditosDisponibles;
    }

    public int getCreditosComprometidos() {
        return creditosComprometidos;
    }

    public void setCreditosComprometidos(int creditosComprometidos) {
        this.creditosComprometidos = creditosComprometidos;
    }

    public int getCreditosRetenidos() {
        return creditosRetenidos;
    }

    public void setCreditosRetenidos(int creditosRetenidos) {
        this.creditosRetenidos = creditosRetenidos;
    }
}