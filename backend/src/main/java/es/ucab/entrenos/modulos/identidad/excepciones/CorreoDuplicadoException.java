package es.ucab.entrenos.modulos.identidad.excepciones;

public class CorreoDuplicadoException extends RuntimeException {
    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}