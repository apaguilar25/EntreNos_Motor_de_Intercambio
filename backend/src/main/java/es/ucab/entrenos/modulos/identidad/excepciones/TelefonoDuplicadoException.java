package es.ucab.entrenos.modulos.identidad.excepciones;

public class TelefonoDuplicadoException extends RuntimeException {
    public TelefonoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}