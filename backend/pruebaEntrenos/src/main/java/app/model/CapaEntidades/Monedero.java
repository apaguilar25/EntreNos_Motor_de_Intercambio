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


    }


    public Monedero() {
        this.creditosDisponibles = 0;
        this.creditosComprometidos = 0;
        this.creditosRetenidos = 0;
    }

    public int getCreditosDisponibles() {
        return creditosDisponibles;
    }

    public int getCreditosComprometidos() {
        return creditosComprometidos;
    }

    public void setCreditosDisponibles(int creditosDisponibles) {
        this.creditosDisponibles = creditosDisponibles;
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
