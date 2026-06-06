package es.ucab.entrenos.modulos.publicacion.modelos;

/**
 * Estado de una transacción de servicio entre usuarios (HU2/HU3).
 */
public enum EstadoTransaccion {
    /** Solicitud enviada, pendiente de aceptación (5 días para aceptar/rechazar) */
    PENDIENTE,
    /** Solicitud aceptada — créditos retenidos en escrow */
    INICIADA,
    /** Ambas partes marcaron como completado — créditos liberados */
    FINALIZADA,
    /** Rechazada por cualquiera de las partes o por expiración */
    RECHAZADA,
    /** Bajo revisión por incidencia/fraude (HU8) */
    EN_DISPUTA
}
