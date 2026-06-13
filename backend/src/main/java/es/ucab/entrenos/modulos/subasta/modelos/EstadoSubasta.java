package es.ucab.entrenos.modulos.subasta.modelos;

public enum EstadoSubasta {
    ACTIVA,             // Período de licitación abierto (recibiendo ofertas)
    ESPERANDO_DECISION, // Licitación cerrada, el propietario tiene 5 días para decidir
    ADJUDICADA_INTERCAMBIO_PENDIENTE,         // Finalizada con un ganador seleccionado
    FINALIZADA_CON_EXITO,
    CANCELADA,          // Declarada desierta o cancelada manualmente por el dueño
    CERRADA_POR_INACTIVIDAD // Cancelada por el sistema tras vencer los 5 días de gracia
}