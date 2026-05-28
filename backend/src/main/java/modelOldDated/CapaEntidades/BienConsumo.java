package modelOldDated.CapaEntidades;

public class BienConsumo {
    private String nombreBienConsumo;

    public BienConsumo() {}

    public BienConsumo(String nombreBienConsumo) {
        this.nombreBienConsumo = nombreBienConsumo;
    }

    public String getNombreBienConsumo() { return nombreBienConsumo; }
    public void setNombreBienConsumo(String nombreBienConsumo) { this.nombreBienConsumo = nombreBienConsumo; }
}