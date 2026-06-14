package es.ucab.entrenos.modulos.publicacion.dtos;

import es.ucab.entrenos.modulos.gamificacion.modelos.LogroDesbloqueado;
import es.ucab.entrenos.modulos.publicacion.modelos.Transaccion;

import java.util.List;

public class ConfirmacionTransaccionResponseDTO {
    private Transaccion transaccion;
    private List<LogroDesbloqueado> nuevosLogros;

    public ConfirmacionTransaccionResponseDTO() {}

    public ConfirmacionTransaccionResponseDTO(Transaccion transaccion, List<LogroDesbloqueado> nuevosLogros) {
        this.transaccion = transaccion;
        this.nuevosLogros = nuevosLogros;
    }

    public Transaccion getTransaccion() { return transaccion; }
    public List<LogroDesbloqueado> getNuevosLogros() { return nuevosLogros; }
}
