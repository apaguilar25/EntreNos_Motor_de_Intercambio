package org.example.CapaEntidades;

public class Monedero {

    private int creditosDisponibles;
    private int creditosComprometidos;

    public boolean tieneFondosSuficientes(int costo){
        int restante = creditosDisponibles - costo;

        if (restante < 0) {
            return false;
        }
        return true;
    }

    public void calcularCreditosComprometidos(int monto){


    }


    public Monedero(int creditosDisponibles, int creditosComprometidos) {
        this.creditosDisponibles = creditosDisponibles;
        this.creditosComprometidos = creditosComprometidos;
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
}
