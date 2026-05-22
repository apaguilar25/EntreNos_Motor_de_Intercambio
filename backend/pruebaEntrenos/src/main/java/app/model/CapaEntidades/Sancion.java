package app.model.CapaEntidades;

import java.time.LocalDate;

public class Sancion {

    private boolean sancionActiva;
    private LocalDate fechaSancion;

    public Sancion(boolean sancionActiva, LocalDate fechaSancion) {
        this.sancionActiva = sancionActiva;
        this.fechaSancion = fechaSancion;
    }

    // Getters y Setters
    public boolean isSancionActiva() {
        return sancionActiva;
    }

    public void setSancionActiva(boolean sancionActiva) {
        this.sancionActiva = sancionActiva;
    }

    public LocalDate getFechaSancion() {
        return fechaSancion;
    }

    public void setFechaSancion(LocalDate fechaSancion) {
        this.fechaSancion = fechaSancion;
    }

}
