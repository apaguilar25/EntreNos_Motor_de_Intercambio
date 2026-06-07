package es.ucab.entrenos.modulos.identidad.excepciones;

public class HabilidadDuplicadaException extends RuntimeException {
    public HabilidadDuplicadaException(String message) {
        super(message);
    }
}