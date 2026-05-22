package app.model.CapaEntidades;

public class LineaBienConsumo {
    private int cantidad;
    private BienConsumo bienConsumo; // Relación 1

    public LineaBienConsumo() {}

    public LineaBienConsumo(int cantidad) {
        this.cantidad = cantidad;
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public BienConsumo getBienConsumo() { return bienConsumo; }
    public void setBienConsumo(BienConsumo bienConsumo) { this.bienConsumo = bienConsumo; }
}