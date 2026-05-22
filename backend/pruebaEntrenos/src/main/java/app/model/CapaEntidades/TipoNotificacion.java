package app.model.CapaEntidades;

public enum TipoNotificacion {
    ALERTA_SISTEMA,     // Avisos generales del sistema (ej. "Tu cuenta ha sido suspendida por 24h")


    // Avisos de la HU2 (Muro y Solicitudes)
    NUEVA_SOLICITUD_ENTRANTE,   // "Jose quiere intercambiar contigo."
    ESTADO_SOLICITUD_CAMBIADO,  // "Tu solicitud fue rechazada" o "Expiró tras 5 días"

    // Avisos de la HU3 (Transacciones y Créditos)
    TRANSACCION_ACTUALIZADA,    // "El servicio finalizó, califica a tu contraparte"
    MOVIMIENTO_MONEDERO,        // "Se te han liberado 5 créditos"

    // Avisos del Módulo de Subastas
    ACTUALIZACION_SUBASTA       // "Alguien hizo una oferta mayor que la tuya" o "Ganaste"
}
