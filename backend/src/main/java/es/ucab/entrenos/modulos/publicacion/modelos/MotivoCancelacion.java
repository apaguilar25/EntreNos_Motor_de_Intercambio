package es.ucab.entrenos.modulos.publicacion.modelos;

public enum MotivoCancelacion {
    EQUIVOCACION("Me equivoqué al enviar la solicitud / oferta."),
    YA_NO_NECESITO("Ya no necesito este servicio / Ya resolví mi necesidad."),
    SIN_ACUERDO("No pudimos llegar a un acuerdo en el horario o los detalles.");

    private final String descripcion;

    MotivoCancelacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
