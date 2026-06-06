package es.ucab.entrenos.modulos.identidad.modelos;

public class Monedero {

    private float creditosDisponibles;

    public Monedero() {
    }

    public Monedero(float creditosDisponibles) {
        this.creditosDisponibles = creditosDisponibles;
    }

    public void acreditar(float montoCreditos){
        if (montoCreditos > 0) {
            this.creditosDisponibles += montoCreditos;
        }    }

    // TODO aplicar descuentos por compra de servicios, etc. y validar que no se pueda descontar más de lo disponible
    public void descontar(float montoCreditos){
        return;
    }

    public float getCreditosDisponibles() {
        return creditosDisponibles;
    }

}
