package es.ucab.entrenos.modulos.identidad.modelos;

public enum EstadoCuenta {
    ACTIVO,
    BLOQUEADO_SEGURIDAD,  // Temporal (24h por fuerza bruta en Login)
    SUSPENDIDO_FRAUDE,    // Permanente (Por 2 reportes de fraude validados)
    SUSPENDIDO_SUBASTA    // Temporal (72h por inactividad/abandono en una subasta)
}