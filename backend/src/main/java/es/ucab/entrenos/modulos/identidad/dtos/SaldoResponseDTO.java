package es.ucab.entrenos.modulos.identidad.dtos;

public class SaldoResponseDTO {
    private float creditosDisponibles;
    private float creditosComprometidos;
    private float saldoDisponible;

    public SaldoResponseDTO() {}

    public SaldoResponseDTO(float creditosDisponibles, float creditosComprometidos) {
        this.creditosDisponibles = creditosDisponibles;
        this.creditosComprometidos = creditosComprometidos;
        this.saldoDisponible = creditosDisponibles - creditosComprometidos;
    }

    public float getCreditosDisponibles() { return creditosDisponibles; }
    public float getCreditosComprometidos() { return creditosComprometidos; }
    public float getSaldoDisponible() { return saldoDisponible; }
}
