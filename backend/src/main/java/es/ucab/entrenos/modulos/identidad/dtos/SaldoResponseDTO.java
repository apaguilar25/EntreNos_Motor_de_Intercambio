package es.ucab.entrenos.modulos.identidad.dtos;

public class SaldoResponseDTO {
    private float creditosDisponibles;
    private float creditosRetenidos;
    private float saldoDisponible;

    public SaldoResponseDTO() {}

    public SaldoResponseDTO(float creditosDisponibles, float creditosRetenidos) {
        this.creditosDisponibles = creditosDisponibles;
        this.creditosRetenidos = creditosRetenidos;
        this.saldoDisponible = creditosDisponibles - creditosRetenidos;
    }

    public float getCreditosDisponibles() { return creditosDisponibles; }
    public float getCreditosRetenidos() { return creditosRetenidos; }
    public float getSaldoDisponible() { return saldoDisponible; }
}
