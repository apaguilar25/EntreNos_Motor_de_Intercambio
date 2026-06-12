package es.ucab.entrenos.modulos.identidad.excepciones;

/**
 * Lanzada cuando dos hilos intentan modificar el mismo recurso en archivo físico
 * al mismo tiempo y ocurre una colisión de versiones.
 */
public class ConcurrenciaException extends RuntimeException {
    public ConcurrenciaException(String mensaje) {
        super(mensaje);
    }
}